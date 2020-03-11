/**
 * Name:            renameSpecialRelationships
 * Description:     Task to rename special relationships. In the hash map names can set the pair (oldSpecialRelationshipName, newSpecialRelationshipName)
 * commitOnExecute: true
 * Date:            2020/03/11
 * Author:          Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
import java.util.HashMap;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.TaskResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
/**
  * Special relationship names standard
  * The name must be camel case
  * The name cannot contain spaces
  * The name must be a verb
  * The name start with the module name
  */
HashMap<String, String> names = new HashMap();
//names.put("oldSpecialRelationshipName", "newSpecialRelationshipName");

TaskResult taskResult = new TaskResult();
taskResult.getMessages().add(TaskResult.createInformationMessage("Start rename special relationships"));
GraphDatabaseService gds = (GraphDatabaseService) PersistenceService.getInstance().getConnectionManager().getConnectionHandler();
try {
    for (String oldRelName : names.keySet()) {
        String newRelName = names.get(oldRelName);
        Transaction tx = gds.beginTx();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(String.format("MATCH ()-[r:RELATED_TO_SPECIAL {name:'%s'}]-()\n", oldRelName)); //NOI18N
        queryBuilder.append(String.format("SET r.name = '%s'\n", newRelName)); //NOI18N

        Result result = gds.execute(queryBuilder.toString());
        tx.success();
        taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("Rename the special relationship from %s to %s", oldRelName, newRelName)));
    }
} catch(Exception ex) {
    taskResult.getMessages().add(TaskResult.createErrorMessage(ex.getMessage()));
}
taskResult.getMessages().add(TaskResult.createInformationMessage("End rename special relationships"));
return taskResult;