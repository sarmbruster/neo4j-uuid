package org.neo4j.extension.uuid;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * handle the setup of auto indexing for UUIDs and registers a {@link UUIDTransactionEventHandler}
 */
class UUIDLifeCycle extends LifecycleAdapter {

    private TransactionEventHandler transactionEventHandler;
    private GraphDatabaseService graphDatabaseService;
    private IndexManager indexManager;
    private ScheduledFuture scheduledFuture;

    UUIDLifeCycle(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
        this.indexManager = graphDatabaseService.index();
    }

    @Override
    public void start() throws Throwable {
        transactionEventHandler = new UUIDTransactionEventHandler(graphDatabaseService);
        graphDatabaseService.registerTransactionEventHandler(transactionEventHandler);

        scheduledFuture = new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    indexManager.forNodes(UUIDTransactionEventHandler.UUID_INDEX_NAME);
                    indexManager.forRelationships(UUIDTransactionEventHandler.UUID_INDEX_NAME);
                    scheduledFuture.cancel(false);
                } catch (IllegalArgumentException e) {
                    // thrown if index creation fails while startup is still in progress
                }

            }
        }, 10, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() throws Throwable {
        graphDatabaseService.unregisterTransactionEventHandler(transactionEventHandler);
    }

}
