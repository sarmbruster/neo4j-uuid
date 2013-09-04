package org.neo4j.extension.uuid;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import java.util.UUID;

/**
 * a {@see TransactionEventHandler} that
 * <ul>
 *     <li>generates UUID properties for each new node and relationship</li>
 *     <li>rejects any modification to pre-existing uuids</li>
 * </ul>
 */
public class UUIDTransactionEventHandler implements TransactionEventHandler {

    public static final String UUID_PROPERTY_NAME = "uuid";

    private final TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator();

    @Override
    public Object beforeCommit(TransactionData transactionData) throws Exception {

        checkForUuidChanges(transactionData.removedNodeProperties(), transactionData, "remove");
        checkForUuidChanges(transactionData.assignedNodeProperties(), transactionData, "assign");
        checkForUuidChanges(transactionData.removedRelationshipProperties(), transactionData, "remove");
        checkForUuidChanges(transactionData.assignedRelationshipProperties(), transactionData, "assign");

        populateUuidsFor(transactionData.createdNodes());
        populateUuidsFor(transactionData.createdRelationships());

        return null;
    }

    @Override
    public void afterCommit(TransactionData data, java.lang.Object state) {
    }

    @Override
    public void afterRollback(TransactionData data, java.lang.Object state) {
    }

    /**
     * @param propertyContainers set UUID property for a iterable on nodes or relationships
     */
    private void populateUuidsFor(Iterable<? extends PropertyContainer> propertyContainers) {
        for (PropertyContainer propertyContainer : propertyContainers) {
            if (!propertyContainer.hasProperty(UUID_PROPERTY_NAME)) {

                final UUID uuid = uuidGenerator.generate();
                final StringBuilder sb = new StringBuilder();
                sb.append(Long.toHexString(uuid.getMostSignificantBits())).append(Long.toHexString(uuid.getLeastSignificantBits()));

                propertyContainer.setProperty(UUID_PROPERTY_NAME, sb.toString());
            }
        }
    }

    private void checkForUuidChanges(Iterable<? extends PropertyEntry<? extends PropertyContainer>> changeList, TransactionData transactionData, String action) {
        for (PropertyEntry<? extends PropertyContainer> removedProperty : changeList) {
            if (removedProperty.key().equals(UUID_PROPERTY_NAME) && ( !isPropertyContainerDeleted(transactionData, removedProperty))) {
                throw new IllegalStateException("you are not allowed to " + action + " " + UUID_PROPERTY_NAME + " properties");
            }
        }
    }

    private boolean isPropertyContainerDeleted(TransactionData transactionData, PropertyEntry<? extends PropertyContainer> propertyEntry) {
        PropertyContainer entity = propertyEntry.entity();
        if (entity instanceof Node) {
            return transactionData.isDeleted((Node)entity);
        } else {
            return transactionData.isDeleted((Relationship)entity);
        }
    }

}
