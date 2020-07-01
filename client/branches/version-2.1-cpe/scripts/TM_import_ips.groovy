/**
 * Imports subnets and IP addresses from a file located on the server, for they can not be bulk-imported using the standard import module. This script does not support folders.
 * Line format (all fields are mandatory): OBJECT_TYPE:OBJECT_CLASS:OBJECT_NAME:PARENT_CLASS:PARENT_NAME
 * OBJECT_TYPE: Use "subnet" for subnets and "ip" for IP addresses
 * OBJECT_CLASS: The class of the object you want to create (e.g. IPAddress or SubnetIPv4)
 * OBJECT_NAME: The name of the object you want to create, typically an address in a CIDR format for subnets or (examples: 192.68.0.0/24, 192.168.0.1, 2001:0db8:85a3:0000:0000:8a2e:0370:7334)
 * PARENT_CLASS: The class name of the parent of the object you want to create. If the parent does not exist, the line will be ignored. Use "rootIPv4" for the root of IPv4 subnets and "rootIPv6" for the root of IPv6 subnets
 * PARENT_NAME: The name of the parent object. If there are multiple objects with the same name, The first found will be used. Use any value (even an empty string) if the parent is a root pool (see PARENT_CLASS for details).
 * Neotropic SAS - version 1.0
 * Parameters: -fileName: The name of the file used to import the objects
 */

import org.neo4j.graphdb.Direction;
import org.kuwaiba.services.persistence.impl.neo4j.RelTypes;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.services.persistence.util.Constants;

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
            def objectClassNode = classIndex.get(Constants.PROPERTY_NAME, tokens[1] /*OBJECT_CLASS*/).getSingle();
            if (objectClassNode == null)
                taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("No class was found with name %s in line %s", tokens[1])));
            else {

                switch (tokens[0]){ //The object type
                    case "subnet":
                        def parentNode;
                        if (tokens[3].equals ("rootIPv4"))
                            parentNode = Util.getRootIPv4(graphDb);
                        else {
                            if (tokens[3].equals ("rootIPv6"))
                                parentNode = Util.getRootIPv6(graphDb);
                            else {
                                def parentClassNode = classIndex.get(Constants.PROPERTY_NAME, tokens[3] /*PARENT_CLASS*/).getSingle();
                                if (parentClassNode == null)
                                    taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("No class was found with name %s in line %s", tokens[3], number)));
                                else
                                    parentNode = Util.getObject(parentClassNode, tokens[4] /*PARENT_NAME*/);
                            }
                        }
                         
                        if (parentNode == null)
                            taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Parent of class %s and name %s could not be found in line %s", tokens[3] , tokens[4], number)));
                        else {
                            //If everything is OK, create the subnet
                            def newObjectNode = graphDb.createNode(); 
                            newObjectNode.setProperty("name", tokens[2] /*OBJECT_NAME*/);
                            newObjectNode.createRelationshipTo(objectClassNode, RelTypes.INSTANCE_OF);

                            if (tokens[3].equals ("rootIPv4") || tokens[3].equals ("rootIPv6")) //Most subnets are children of another pool, but some will be located in one of the root subnet pools (of folders, whch are not supported in this script)
                                newObjectNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);                            
                            else
                                newObjectNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
                                        
                            objectIndex.putIfAbsent(newObjectNode, Constants.PROPERTY_ID, newObjectNode.getId());
                            taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("Line %s processed sucessfully", number)));
                        }
                        break;
                    case "ip":
                        def parentClassNode = classIndex.get(Constants.PROPERTY_NAME, tokens[3] /*PARENT_CLASS*/).getSingle();
                        if (parentClassNode == null)
                            taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("No class was found with name %s in line %s", tokens[3])));

                        def parentNode = Util.getObject(parentClassNode, tokens[4] /*PARENT_NAME*/);
                        if (parentNode == null)
                            taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Parent of class %s and name %s could not be found in line %s", tokens[3] , tokens[4], number)));
                        else {
                            //If everything is OK, create the IP address
                            def newObjectNode = graphDb.createNode(); 
                            newObjectNode.setProperty("name", tokens[2] /*OBJECT_NAME*/);
                            newObjectNode.createRelationshipTo(objectClassNode, RelTypes.INSTANCE_OF);
                            newObjectNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
                            objectIndex.putIfAbsent(newObjectNode, Constants.PROPERTY_ID, newObjectNode.getId());

                            taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("Line %s processed sucessfully", number)));
                        }
                        break;
                    default:
                        taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Unknown object type %s found in line %s", tokens[0], number)));
                }
            }
        }
    }
    //Returns the result
    return taskResult;
} catch (Exception e) {
    return TaskResult.createErrorResult(String.format("Unexpected error: %s", e.getMessage()));
}
/**
* Helper class with useful methods
**/
class Util {
    def static ipv4RootPool;
    def static ipv6RootPool;
    /**
    * Finds an object given a name
    **/
    def static getObject(classNode, objectName) {
        for (instanceRel in classNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF)) {
            def instanceNode = instanceRel.getStartNode();
            if (objectName.equals(instanceNode.getProperty("name")))
                return instanceNode;
        }
    }

    /**
    * Gets the root pool of IPv4 subnets   
    **/
    def static getRootIPv4(graphDb) {
        if (ipv4RootPool == null) 
            ipv4RootPool = getFirstRootPool(graphDb, Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
        
        return ipv4RootPool;
    }

    /**
    * Gets the root pool of IPv6 subnets   
    **/
    def static getRootIPv6(graphDb) {
        if (ipv6RootPool == null) 
            ipv6RootPool = getFirstRootPool(graphDb, Constants.CLASS_SUBNET_IPV6, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
 
        return ipv6RootPool;
    }
    
    /**
     * Retrieves the first root pool matching the type and class conditions
     */
    def static getFirstRootPool(graphDb, className, type){
        def poolsIndex = graphDb.index().forNodes(Constants.INDEX_POOLS);
        def poolNodes = poolsIndex.query(Constants.PROPERTY_ID, "*");
        for (poolNode in poolNodes) {
            if (!poolNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF_SPECIAL)) { //Root pools don't have parents
                if ((int)poolNode.getProperty(Constants.PROPERTY_TYPE) == type && className.equals(poolNode.getProperty(Constants.PROPERTY_CLASS_NAME)))
                    return poolNode;
            }
        }
    }
}
