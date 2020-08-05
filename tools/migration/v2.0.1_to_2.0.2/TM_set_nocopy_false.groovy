/**
 * Set all attribute metadata noCopy property to false. This is a patch that should be used migrating from Kuwaiba v 2.0.1 to 2.0.2. After 
 * running it (with the flag commitOnExecute set to true in the Task Manager task), restart the server or rebuild the class metadata cache
 * by modifying the datamodel (i.e. creating and deleting a class).
 * Neotropic SAS - version 1.0
 * Parameters: None
 */

//Creates the task result instance using reflection
def taskResult = TaskResult.newInstance();

 try {
     def query = "MATCH (classMetadata)-[:HAS_ATTRIBUTE]->(attributeMetadata) SET attributeMetadata.noCopy = false";
     graphDb.execute(query);
     taskResult.getMessages().add(TaskResult.createInformationMessage("All attributes in the data model were patched correctly"));
 } catch(Exception e) {
     taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Unexpected error found while executing the script: %s", 
					e.getMessage())));
 }

 taskResult