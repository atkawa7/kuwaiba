/**
 * Check for inconsistencies in list type attributes at database level. It checks if the related list types are correctly named according to the datamodel
 * and if they correspond to the right type.
 * Neotropic SAS - version 1.0
 * Parameters: none
 */

def taskResult = TaskResult.newInstance();
def cypherQuery = "MATCH (aListTypeClass)<-[:INSTANCE_OF]-(listTypeItem:listTypeItems)<-[r1:RELATED_TO]-(anObject)-[:INSTANCE_OF]->(anInventoryClass)-[:HAS_ATTRIBUTE]->(anAttribute) WHERE r1.name = anAttribute.name AND anAttribute.type <> aListTypeClass.name RETURN anObject.name AS theObject, anInventoryClass.name AS theClass, anAttribute.name AS theAttribute, anAttribute.type AS theRightType, listTypeItem.name AS theListType, aListTypeClass.name AS theWrongType";

def result = graphDb.execute(cypherQuery);

if (!result.hasNext())
    return TaskResult.createErrorResult("No list type inconsistencies found");

while (result.hasNext()) {
    def row = result.next();
    taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("The object %s (%s) has an attribute %s set to %s (%s), but should be %s", 
				row.get("theObject"), row.get("theClass"), row.get("theAttribute"), row.get("theListType"), row.get("theWrongType"), row.get("theRightType"))));
}

//Returns the result
taskResult;