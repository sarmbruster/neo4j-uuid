package org.neo4j.extension.uuid;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.lifecycle.Lifecycle;

/**
 * Neo4j kernel extension for initializing a {@link UUIDTransactionEventHandler} and setting up autoindexing appropriately.
 */
public class UUIDKernelExtensionFactory extends KernelExtensionFactory<UUIDKernelExtensionFactory.Dependencies> {

    public interface Dependencies
    {
        GraphDatabaseService getGraphDatabaseService();
        Config getConfig();
    }

    public UUIDKernelExtensionFactory() {
        super("uuid");
    }

    @Override
    public Lifecycle newKernelExtension(final Dependencies dependencies) throws Throwable {
        return new UUIDLifeCycle(dependencies.getGraphDatabaseService(), dependencies.getConfig());
    }

}
