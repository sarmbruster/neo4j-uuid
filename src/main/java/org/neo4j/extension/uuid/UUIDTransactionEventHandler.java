package org.neo4j.extension.uuid;

import org.apache.commons.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.index.AutoIndexer;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.server.plugins.PluginLifecycle;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

//@Path("uuid")
public class UUIDTransactionEventHandler implements TransactionEventHandler, PluginLifecycle {

    public static final String UUID_PROPERTY_NAME = "uuid";

    @Override
    public Object beforeCommit(TransactionData data) throws Exception {

        checkForUuidChanges(data.removedNodeProperties(), "remove");
        checkForUuidChanges(data.assignedNodeProperties(), "assign");
        checkForUuidChanges(data.removedRelationshipProperties(), "remove");
        checkForUuidChanges(data.assignedRelationshipProperties(), "assign");

        populateUuidsFor(data.createdNodes());
        populateUuidsFor(data.createdRelationships());

        return null;
    }

    @Override
    public void afterCommit(TransactionData data, java.lang.Object state) {
    }

    @Override
    public void afterRollback(TransactionData data, java.lang.Object state) {
    }

    @Override
    public Collection<Injectable<?>> start(GraphDatabaseService graphDatabaseService, Configuration config) {
        IndexManager indexManager = graphDatabaseService.index();
        setupUUIDIndexing(indexManager.getNodeAutoIndexer());
        setupUUIDIndexing(indexManager.getRelationshipAutoIndexer());
        graphDatabaseService.registerTransactionEventHandler(this);
        return Collections.emptySet();
    }

    @Override
    public void stop() {
        // intentionally empty
    }

    /**
     * @param propertyContainers set UUID property for a iterable on nodes or relationships
     */
    private void populateUuidsFor(Iterable<? extends PropertyContainer> propertyContainers) {
        for (PropertyContainer propertyContainer : propertyContainers) {
            if (!propertyContainer.hasProperty(UUID_PROPERTY_NAME)) {
                propertyContainer.setProperty(UUID_PROPERTY_NAME, UUID.randomUUID().toString());
            }
        }
    }

    private void checkForUuidChanges(Iterable<? extends PropertyEntry<? extends PropertyContainer>> changeList, String action) {
        for (PropertyEntry<? extends PropertyContainer> removedProperty : changeList) {
            if (removedProperty.key().equals(UUID_PROPERTY_NAME)) {
                throw new IllegalStateException("you are not allowed to " + action + " " + UUID_PROPERTY_NAME + " properties");
            }
        }
    }

    private void setupUUIDIndexing(AutoIndexer<? extends PropertyContainer> autoIndexer) {
        autoIndexer.startAutoIndexingProperty(UUID_PROPERTY_NAME);
        autoIndexer.setEnabled(true);
    }

}
