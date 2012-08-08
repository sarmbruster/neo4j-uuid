# neo4j-uuid #

## purpose ##
A lot of [Neo4j](http://www.neo4j.org) based projects I've seen need a unique identifier for nodes/relationship to reference contents in the graph database from some third party system. The most obvious thing namely using node.getId() is a bad choice since after deletion of a node it's id might be recycled. For details on why and how this works, see an excellect [blog post](http://digitalstain.blogspot.de/2010/10/neo4j-internals-file-storage.html).
A best practice for having unique identifiers is to assign each node/relationship a uuid property and let Neo4j perform autoindexing on the uuid property. Third party systems store only the UUID. A simple index lookup retrieves then the referenced node or relationship in the graph database.

## license ##
tbd

### implementation details ###
tbd
