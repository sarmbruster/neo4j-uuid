package org.neo4j.extension.uuid;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Simple REST webservice to find nodes/relationships by uuid
 */
@Path("/")
public class UUIDRestInterface {

    @Context
    GraphDatabaseService graphDatabaseService;

    @GET
    @Path("/node/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String findNodeByUUID(@PathParam("uuid") String uuid) {
        IndexHits<Node> hits = graphDatabaseService.index().forNodes(UUIDTransactionEventHandler.UUID_INDEX_NAME).get(UUIDTransactionEventHandler.UUID_PROPERTY_NAME, uuid);
        return Long.toString(hits.getSingle().getId());
    }

    @GET
    @Path("/relationship/{uuid}")
    public String findRelationshipByUUID(@PathParam("uuid") String uuid) {
        IndexHits<Relationship> hits = graphDatabaseService.index().forRelationships(UUIDTransactionEventHandler.UUID_INDEX_NAME).get(UUIDTransactionEventHandler.UUID_PROPERTY_NAME, uuid);
        return Long.toString(hits.getSingle().getId());
    }

}
