/**
 * Imports subnets and IP addresses from a file located on the server, for they can not be bulk-imported using the standard import module. This script does not support folders.
 * Line format (all fields are mandatory): OBJECT_TYPE:OBJECT_CLASS:OBJECT_NAME:PARENT_CLASS:PARENT_NAME
 * OBJECT_TYPE: Use "subnet" for subnets and "ip" for IP addresses
 * OBJECT_CLASS: The class of the object you want to create (e.g. IPAddress or SubnetIPv4)
 * OBJECT_NAME: The name of the object you want to create, typically an address in a CIDR format for subnets or (examples: 192.68.0.0/24, 192.168.0.1, 2001:0db8:85a3:0000:0000:8a2e:0370:7334)
 * PARENT_CLASS: The class name of the parent of the object you want to create. If the parent does not exist, the line will be ignored. Use "rootIPv4" for the root of IPv4 subnets and "rootIPv6" for the root of IPv6 subnets
 * PARENT_NAME: The name of the parent object. If there are multiple objects with the same name, a warning will be displayed and the line will be ignored. Use any value (even an empty string) if the parent is a root pool (see PARENT_CLASS for details).
 * Neotropic SAS - version 1.0
 * Parameters: -fileName: The name of the file used to import the objects
 */
import org.kuwaiba.apis.persistence.PersistenceService;

def bem = PersistenceService.getInstance().getBusinessEntityManager();
def fileName = scriptParameters.get("fileName");
def columnSeparator = ";";

// Check if the file name was provided
if (fileName == null || fileName.isEmpty()) 
	return TaskResult.createErrorResult("Parameter fileName not set");


// Check if the file exists and it's readable
def importFile = new File(fileName);

if (!importFile.exists())
    return TaskResult.createErrorResult(String.format("File %s does not exist", fileName));

if (!importFile.canRead())
    return TaskResult.createErrorResult(String.format("File %s exists, but it's not readable", fileName));

try {
    def taskResult = TaskResult.newInstance();

    //Parses and processes every line
    importFile.eachLine() { line, number ->
        def tokens = line.split(columnSeparator);
        if (tokens.length != 5) //Remember that all columns are mandatory
            taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Line %s does not have 5 columns as expected", number)));
        else {
            switch (tokens[0]){ //The object type
                case "subnet":
                    //if ()
                    //def parentObject = 
                    break;
                case "ip":
                    def possibleParents = bem.getObjectsWithFilterLight(tokens[3] /*PARENT_CLASS*/, "name", tokens[4] /*PARENT_NAME*/)
                    if (possibleParents.isEmpty())
                        taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Parent with name %s could not be found in line %s", tokens[4], number)));
                    else {
                        if (possibleParents.size() > 1)
                            taskResult.getMessages().add(TaskResult.createWarningMessage(String.format("Multiple parents with name %s were found in line %s", tokens[4], number)));
                        else {
                            def parentObject = possibleParents.get(0);
                            //If everything is OK, create the IP address
                            def attributes = new HashMap();
                            attributes.put("name", tokens[2] /*OBJECT_NAME*/);
                            bem.createSpecialObject(tokens[1] /*OBJECT_CLASS*/, parentObject.getClassName(), parentObject.getId(), attributes, -1 /*No Template*/);
                            taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("Line %s processed sucessfully", number)));
                        }
                    }
                    break;
                default:
                    taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Unknown object type %s found in line %s", tokens[0], number)));
            }
        }
    }
    //Returns the result
    return taskResult;
} catch (Exception e) {
    return TaskResult.createErrorResult(String.format("Unexpected error: %s", e.getMessage()));
}
