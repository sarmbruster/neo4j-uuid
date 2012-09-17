package org.neo4j.extension.uuid

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Transaction
import org.neo4j.graphdb.Node
import spock.lang.Shared
import spock.lang.Specification
import org.neo4j.server.helpers.ServerBuilder
import org.neo4j.server.NeoServer
import org.neo4j.server.plugins.PluginLifecycle
import org.neo4j.graphdb.DynamicRelationshipType
import org.neo4j.graphdb.TransactionFailureException

class UUIDTransactionEventHandlerSpec extends Specification {

    static UUID_REGEX = /^[a-f0-9]{32}$/
    @Shared GraphDatabaseService graphDB
    @Shared NeoServer server
    Transaction tx

    def setupSpec() {
        server = ServerBuilder.server().withThirdPartyJaxRsPackage("org.neo4j.extension.uuid", "/uuid").build();
        server.start()
        graphDB = server.database.graph
    }

    def cleanupSpec() {
        server.stop()
    }

    def "check ServiceLoader works for PluginLifecycle"() {
        when:
        def services = ServiceLoader.load(PluginLifecycle)
        then:
        services.iterator().size() > 0
    }

    def "check if a new node has a uuid"() {
        setup:
        def node = withTransaction { graphDB.createNode() }

        when:
        def uuid = node.getProperty('uuid', null)

        then:
        uuid =~ UUID_REGEX
    }

    def "check if a new relationship has a uuid"() {
        setup:
        def rel = withTransaction {
            def startNode = graphDB.createNode()
            def endNode = graphDB.createNode()
            startNode.createRelationshipTo(endNode, DynamicRelationshipType.withName('RELATED'))
        }

        when:
        def uuid = rel.getProperty('uuid', null)

        then:
        uuid =~ UUID_REGEX
    }


    def "verify autoindexing on uuids"() {
        setup:
        def node = withTransaction { graphDB.createNode() }

        when:
        def hits = graphDB.index().nodeAutoIndexer.autoIndex.get('uuid', node.getProperty('uuid'))

        then:
        hits.size()==1
        hits.single == node
    }

    def "check if changes to uuid are rejected"() {
        setup:
        def startNode
        def rel
        withTransaction {
                    startNode = graphDB.createNode()
                    def endNode = graphDB.createNode()
                    rel = startNode.createRelationshipTo(endNode, DynamicRelationshipType.withName('RELATED'))
                }

        when: "trying to change uuid on node"
        withTransaction { startNode.setProperty("uuid", "123")}

        then:
        def e = thrown(TransactionFailureException)
        e.cause.cause.message =~ /you are not allowed to assign uuid properties/

        when: "trying to remove uuid on node"
        withTransaction { startNode.removeProperty("uuid")}

        then:
        e = thrown(TransactionFailureException)
        e.cause.cause.message =~ /you are not allowed to remove uuid properties/

        when: "trying to change uuid on relationship"
        withTransaction { rel.setProperty("uuid", "123")}

        then:
        e = thrown(TransactionFailureException)
        e.cause.cause.message =~ /you are not allowed to assign uuid properties/

        when: "trying to remove uuid on relationship"
        withTransaction { rel.removeProperty("uuid")}

        then:
        e = thrown(TransactionFailureException)
        e.cause.cause.message =~ /you are not allowed to remove uuid properties/

    }

    def withTransaction(Closure closure) {
        def tx = graphDB.beginTx()
        try {
            def result = closure.call()
            tx.success()
            return result
        } finally {
            tx.finish()
        }
    }

}
