package org.neo4j.extension.uuid

import org.neo4j.graphdb.DynamicRelationshipType
import org.neo4j.graphdb.TransactionFailureException
import org.neo4j.kernel.extension.KernelExtensionFactory
import org.neo4j.server.plugins.PluginLifecycle

class UUIDTransactionEventHandlerSpec extends NeoSpecification {

    static UUID_REGEX = /^[a-f0-9]{32}$/

    def "check ServiceLoader works for KernelExtensionFactory"() {
        when:
        def services = ServiceLoader.load(KernelExtensionFactory)

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
        def nai = graphDB.index().nodeAutoIndexer
        def hits = nai.autoIndex.get('uuid', node.getProperty('uuid'))

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

}
