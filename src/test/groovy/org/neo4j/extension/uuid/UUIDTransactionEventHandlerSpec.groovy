package org.neo4j.extension.uuid

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.test.ImpermanentGraphDatabase
import spock.lang.Specification

class UUIDTransactionEventHandlerSpec extends Specification {

    def "check if a new node has a uuid"() {
        setup:
        GraphDatabaseService graphDB =  new ImpermanentGraphDatabase()
        graphDB.registerTransactionEventHandler(new UUIDTransactionEventHandler())

        when:
        def tx = graphDB.beginTx()
        def node = null
        try {
            node = graphDB.createNode()
            tx.success()
        } finally {
            tx.finish()
        }

        then:
        node.hasProperty('uuid')

    }
}
