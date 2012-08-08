package org.neo4j.extension.uuid;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import java.util.UUID;

class UUIDTransactionEventHandler implements TransactionEventHandler {

    public static final String UUID_PROPERTY_NAME = "uuid";

    @java.lang.Override
    public Object beforeCommit(TransactionData data) throws Exception {
        for (Node node : data.createdNodes()) {
            if (!node.hasProperty(UUID_PROPERTY_NAME)) {
                node.setProperty(UUID_PROPERTY_NAME, UUID.randomUUID().toString());
            }
        }
        return null;
    }

    @java.lang.Override
    public void afterCommit(TransactionData data, java.lang.Object state) {
    }

    @java.lang.Override
    public void afterRollback(TransactionData data, java.lang.Object state) {
    }
}
