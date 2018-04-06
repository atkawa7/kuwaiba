import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.Relationship;
/**
* This utility allows you to remove a node from an index. As an option, you can delete the node itself  and its relationships. Parameters:
* 1. Database path
* 2. List of comma separated ids of the nodes to remove from the index
* 3. Name of the index
* 4. Optional. true or false, if the node should be deleted from te database. Default value is false
*/
public class RemoveFromIndex {
	public static void main(String args[]) {
		if (args.length < 3) {
			System.out.println("You must provide a database path, a comma-separated list of ids to remove index name. As an option, you can delete the node itself  and its relationships.");
			System.exit(0);
		}
		
	        String databasePath = args[0];
		String[] nodesToDelete = args[1].split(",");
		String indexName = args[2];
		boolean deleteSelf = false;

		if (args.length > 3) {
			try {
				deleteSelf = Boolean.valueOf(args[3]);
			} catch(NumberFormatException ex) {
				System.out.println("Wrong deleteSelf option. The nodes won't be deleted, only removed from the index.");
			}
		}

		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databasePath);
		try (Transaction tx = graphDb.beginTx()) {
			Index<Node> index = graphDb.index().forNodes(indexName);							

			for (String idToDelete : nodesToDelete) {
				System.out.print(String.format("Processing node %s... ", idToDelete));
				try {
					Node nodeToDelete = index.get("id", Long.valueOf(idToDelete)).getSingle();
					if (nodeToDelete == null)
						System.out.println(String.format("Node with id %s could not be found", idToDelete));
					else
						index.remove(nodeToDelete);

					if (deleteSelf) {
						for (Relationship relationship : nodeToDelete.getRelationships())
                    					relationship.delete();
						nodeToDelete.delete();
					}
					System.out.println("OK");
				} catch(Exception ex) {
					System.out.println(String.format("ERROR -> %s", ex));
				}
			}
			tx.success();
			System.out.println("Changes committed successfully");
		}
		graphDb.shutdown();
	}
}
