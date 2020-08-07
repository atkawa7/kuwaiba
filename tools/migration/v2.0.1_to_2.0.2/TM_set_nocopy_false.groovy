/**
 * Sets all attribute metadata noCopy property to false. The noCopy property 
 * was ignored until version 2.0.1 and many classes in the default data model
 * had it set to true, causing object copy operations to not copy certain
 * attributes. After running it (with the flag commitOnExecute set to true in the Task Manager task), restart the server or rebuild the class metadata cache
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
