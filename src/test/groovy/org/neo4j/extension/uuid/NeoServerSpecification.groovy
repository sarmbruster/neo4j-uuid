package org.neo4j.extension.uuid

import spock.lang.Specification
import spock.lang.Shared
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.server.NeoServer
import org.neo4j.server.helpers.ServerBuilder

/**
 * abstract base class for spock tests using a NeoServer
 */
abstract class NeoServerSpecification extends Specification {

    public static final String MOUNTPOINT = "/db/uuid"
    @Shared GraphDatabaseService graphDB
    @Shared NeoServer server

    def setupSpec() {
        server = ServerBuilder.server().onPort(37474).withThirdPartyJaxRsPackage("org.neo4j.extension.uuid", MOUNTPOINT).build();
        server.start()
        graphDB = server.database.graph
    }

    def cleanupSpec() {
        server.stop()
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
