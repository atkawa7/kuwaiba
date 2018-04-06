import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

/**
* This small programs allows you to check if all the nodes in an index have the right fields. It has three parameters, all mandatory
* 1. The path to the database.
* 2. A comma-separated list of fields to check against.
* 3. The index name.
*/
public class IndexIntegrityChecker {
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("You must provide a database path, a comma-separated list of fields to check and an index name.");
			System.exit(0);
		}
		
	        String databasePath = args[0];
		String[] requiredFields = args[1].split(",");
		String indexName = args[2];
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databasePath);
		try (Transaction tx = graphDb.beginTx()) {
			Index<Node> poolsIndex = graphDb.index().forNodes(indexName);
			System.out.println("Initiating index analysis...");
			for (Node poolNode : poolsIndex.query("id", "*")) {
                		System.out.print(String.format("Testing %s [%s]... ", 
					poolNode.hasProperty("name") ? poolNode.getProperty("name") : "***", poolNode.getId()));

				//Check the required fields
				boolean complies = true;
				for (String requiredField : requiredFields) {
					if(!poolNode.hasProperty(requiredField)) {
						System.out.print(String.format("(%s) ", requiredField));
						complies = false;
					}
				}
				if (complies)
					System.out.println("OK");
				else
					System.out.println("ERROR");
            		}
			System.out.println("Analysis finished");
		}
		graphDb.shutdown();
	}
}
