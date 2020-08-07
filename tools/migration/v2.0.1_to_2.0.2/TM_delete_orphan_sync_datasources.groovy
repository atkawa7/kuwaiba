/**
 * This scripts deletes the orphans SyncDatasourceConfigurations. Due to a bug in version 2.0.1, the data source configurations related to an object remained
 * after deleting such object.
 * Neotropic SAS - version 2.0
 * Parameters: None
 */
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.helpers.collection.Iterators;

def taskResult = TaskResult.newInstance();

try {
	def cypherQuery = "MATCH (n:syncGroups)-[BELONGS_TO_GROUP]-(d) WHERE NOT (d)-[:HAS_CONFIGURATION]-() DETACH DELETE d RETURN d";
	
	Result result = graphDb.execute(cypherQuery);
	ResourceIterator<Node> physicalNodeColumn = result.columnAs("n");
	List<Node> nodes = Iterators.asList(physicalNodeColumn);
	if(nodes.isEmpty()){
		nodes.each{node ->
			taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("The data source condiguration with id %s was removed", nodeId)));
		}
	}
	else
		taskResult.getMessages().add(TaskResult.createErrorMessage("No data source configurations were deleted"));
	return taskResult;

} catch (Exception e) {
	return TaskResult.createErrorResult(String.format("Unexpected error: %s", e.getMessage()));
}
