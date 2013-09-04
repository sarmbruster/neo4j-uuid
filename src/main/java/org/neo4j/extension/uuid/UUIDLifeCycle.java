package org.neo4j.extension.uuid;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

/**
 * handle the setup of auto indexing for UUIDs and registers a {@link UUIDTransactionEventHandler}
 */
class UUIDLifeCycle extends LifecycleAdapter {

    private TransactionEventHandler transactionEventHandler;
    private GraphDatabaseService graphDatabaseService;
    private IndexManager indexManager;

    UUIDLifeCycle(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
        this.indexManager = graphDatabaseService.index();
    }

    @Override
    public void start() throws Throwable {
        transactionEventHandler = new UUIDTransactionEventHandler(graphDatabaseService);
        graphDatabaseService.registerTransactionEventHandler(transactionEventHandler);
    }

    @Override
    public void stop() throws Throwable {
        graphDatabaseService.unregisterTransactionEventHandler(transactionEventHandler);
    }

}
