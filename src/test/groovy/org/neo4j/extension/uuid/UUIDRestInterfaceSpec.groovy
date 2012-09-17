package org.neo4j.extension.uuid

import org.neo4j.server.rest.RestRequest
import org.neo4j.server.NeoServer
import org.neo4j.server.rest.domain.GraphDbHelper
import com.sun.jersey.api.client.Client
import spock.lang.Shared

import static org.neo4j.server.helpers.FunctionalTestHelper.CLIENT
import org.neo4j.server.rest.JaxRsResponse
import javax.ws.rs.core.MediaType

import static org.junit.Assert.assertThat
import static org.junit.Assert.assertThat
import com.sun.jersey.api.client.filter.LoggingFilter
import org.neo4j.graphdb.DynamicRelationshipType

class UUIDRestInterfaceSpec extends NeoServerSpecification {

    @Shared Client client = Client.create()
    @Shared GraphDbHelper helper
    private RestRequest request

    def setupSpec() {
        assert graphDB
        this.helper = new GraphDbHelper( server.getDatabase() )
        //client.addFilter(new LoggingFilter())
    }

    def setup() {
        request = new RestRequest(server.baseUri().resolve(NeoServerSpecification.MOUNTPOINT+"/"), client)
    }


    def "test /relationship/{uuid}"() {
        setup:
        def node = withTransaction { graphDB.createNode()}

        when:
        def response = request.get("node/" + node.getProperty('uuid'))

        then:
        response.status==200

        and:
        response.getEntity() == node.id as String

    }

    def "test /node/{uuid}"() {
        setup:
        def rel = withTransaction {
            def startNode = graphDB.createNode()
            def endNode = graphDB.createNode()
            startNode.createRelationshipTo(endNode, DynamicRelationshipType.withName('RELATED'))
        }

        when:
        def response = request.get("relationship/" + rel.getProperty('uuid'))

        then:
        response.status==200

        and:
        response.getEntity() == rel.id as String

    }

}
