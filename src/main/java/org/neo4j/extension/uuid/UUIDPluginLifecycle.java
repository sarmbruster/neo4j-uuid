package org.neo4j.extension.uuid;

import org.apache.commons.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.AutoIndexer;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.server.plugins.PluginLifecycle;

import java.util.Collection;
import java.util.Collections;

/**
 * implementation of {@see PluginLifecycle} that sets up autoindexing for UUID property and registers the {@see UUIDTransactionEventHandler}.
 */
public class UUIDPluginLifecycle implements PluginLifecycle {
    public UUIDPluginLifecycle() {
    }

    @Override
    public Collection<Injectable<?>> start(GraphDatabaseService graphDatabaseService, Configuration config) {
        IndexManager indexManager = graphDatabaseService.index();
        setupUUIDIndexing(indexManager.getNodeAutoIndexer());
        setupUUIDIndexing(indexManager.getRelationshipAutoIndexer());
        graphDatabaseService.registerTransactionEventHandler(new UUIDTransactionEventHandler());
        return Collections.emptySet();
    }

    @Override
    public void stop() {
        // intentionally empty
    }

    void setupUUIDIndexing(AutoIndexer<? extends PropertyContainer> autoIndexer) {
        autoIndexer.startAutoIndexingProperty(UUIDTransactionEventHandler.UUID_PROPERTY_NAME);
        autoIndexer.setEnabled(true);
    }
}