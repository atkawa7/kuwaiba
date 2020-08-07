/**
 * This scripts inverts the relationship direction between the syncDatasourceConfigurations and its devices
 * also adds the label syncDatasourceConfiguration to all the SyncDatasourceNodes
 * the direction of this relationships was (SyncDsConf)-[HAS_CONFIGURATION]->(device)
 * the correct direction is (SyncDsConf)<-[HAS_CONFIGURATION]-(device)
 * Neotropic SAS - version 1.0
 * Parameters: None
 */
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.helpers.collection.Iterators;

def taskResult = TaskResult.newInstance();

try {
	def cypherQuery = "MATCH (n:inventoryObjects)<-[r:HAS_CONFIGURATION]-(m) CREATE (n)-[r2:HAS_CONFIGURATION]->(m) SET m:syncDatasourceConfiguration DELETE r";
	graphDb.execute(cypherQuery);

	cypherQuery = "MATCH (n:inventoryObjects)<-[r:HAS_CONFIGURATION]-(m) RETURN r";
	Result result = graphDb.execute(cypherQuery);
	ResourceIterator<Node> physicalNodeColumn = result.columnAs("r");
	List<Node> nodes = Iterators.asList(physicalNodeColumn);
    if(nodes.isEmpty())
		taskResult.getMessages().add(TaskResult.createInformationMessage("The relationship direction in all the SyncDatasourceConfigurations was updated"));
	else
		taskResult.getMessages().add(TaskResult.createErrorMessage("The relationship direction in some SyncDatasourceConfiguration has the wrong direction"));
	return taskResult;

} catch (Exception e) {
	return TaskResult.createErrorResult(String.format("Unexpected error: %s", e.getMessage()));
}
