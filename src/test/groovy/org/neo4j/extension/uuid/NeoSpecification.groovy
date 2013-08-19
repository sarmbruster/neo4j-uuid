package org.neo4j.extension.uuid

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.test.TestGraphDatabaseFactory
import spock.lang.Specification

/**
 * abstract base class for spock tests using a NeoServer
 */
abstract class NeoSpecification extends Specification {

    GraphDatabaseService graphDB

    def setup() {
        graphDB = new TestGraphDatabaseFactory().newImpermanentDatabase()
    }

    def cleanup() {
        graphDB.shutdown()
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
