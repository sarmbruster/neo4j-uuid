package org.neo4j.extension.uuid;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.AutoIndexer;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

import java.util.Map;

/**
 * handle the setup of auto indexing for UUIDs and registers a {@link UUIDTransactionEventHandler}
 */
class UUIDLifeCycle extends LifecycleAdapter {

    private TransactionEventHandler transactionEventHandler;
    private GraphDatabaseService graphDatabaseService;
    private IndexManager indexManager;
    private Config config;

    UUIDLifeCycle(GraphDatabaseService graphDatabaseService, Config config) {
        this.graphDatabaseService = graphDatabaseService;
        this.indexManager = graphDatabaseService.index();
        this.config = config;
    }

    /**
     * since {@link org.neo4j.kernel.NodeAutoIndexerImpl#start()} is called *after* {@link org.neo4j.extension.uuid.UUIDLifeCycle#start()} it would apply config settings for auto indexing. To prevent this we change config here.
     * @throws Throwable
     */
    @Override
    public void init() throws Throwable {
        Map<String,String> params = config.getParams();
        params.put(GraphDatabaseSettings.node_auto_indexing.name(), "true");
        params.put(GraphDatabaseSettings.relationship_auto_indexing.name(), "true");
        config.applyChanges(params);
    }

    @Override
    public void start() throws Throwable {
        startUUIDIndexing(indexManager.getNodeAutoIndexer());
        startUUIDIndexing(indexManager.getRelationshipAutoIndexer());
        transactionEventHandler = new UUIDTransactionEventHandler();
        graphDatabaseService.registerTransactionEventHandler(transactionEventHandler);
    }

    @Override
    public void stop() throws Throwable {
        stopUUIDIndexing(indexManager.getNodeAutoIndexer());
        stopUUIDIndexing(indexManager.getRelationshipAutoIndexer());
        graphDatabaseService.unregisterTransactionEventHandler(transactionEventHandler);
    }

    void startUUIDIndexing(AutoIndexer<? extends PropertyContainer> autoIndexer) {
        autoIndexer.startAutoIndexingProperty(UUIDTransactionEventHandler.UUID_PROPERTY_NAME);
    }

    void stopUUIDIndexing(AutoIndexer<? extends PropertyContainer> autoIndexer) {
        autoIndexer.stopAutoIndexingProperty(UUIDTransactionEventHandler.UUID_PROPERTY_NAME);
    }
}
