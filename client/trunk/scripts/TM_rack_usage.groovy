/**
 * Finds the racks whose occupation level surpasses certain threholds
 * Neotropic SAS - version 1.0
 * Parameters: -thresholdHigh: The upper threshold (usage percentage). It will generate a red notification
 *             -thresholdMid: The lower threshold (usage percentage). It will generate a red notification
 */

//Creates the task result instance using reflection
def taskResult = TaskResult.newInstance();

//Check if the parameters exist and are set
if (scriptParameters.get("thresholdHigh") == null || scriptParameters.get("thresholdHigh").isEmpty()) 
	return TaskResult.createErrorResult("Parameter thresholdHigh not set");

//Check if the parameters exist and are set
if (scriptParameters.get("thresholdMid") == null || scriptParameters.get("thresholdMid").isEmpty()) 
	return TaskResult.createErrorResult("Parameter thresholdMid not set");

def thresholdHigh = Integer.valueOf(scriptParameters.get("thresholdHigh"));
def thresholdMid = Integer.valueOf(scriptParameters.get("thresholdMid"));

if (thresholdHigh <= thresholdMid)
	return TaskResult.createErrorResult("thresholdHigh must be greater than thresholdMid");

//Gets the rack node
def rackClassNode = classIndex.get(Constants.PROPERTY_NAME, "Rack").getSingle();

if (rackClassNode == null)
	taskResult = TaskResult.createErrorResult("Class Rack not found");
else {
	rackClassNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF).each {instanceRel ->
		def rackInstanceNode = instanceRel.getStartNode();
		if (!rackInstanceNode.hasProperty("rackUnits"))
			totalRackUnits = 0;
		else
			totalRackUnits = rackInstanceNode.getProperty("rackUnits");
		
		def occupiedRackUnits = 0;
		rackInstanceNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF).each { childRel ->
			def rackableInstanceNode = childRel.getStartNode();
			if (rackableInstanceNode.hasProperty("rackUnits"))
				occupiedRackUnits += rackableInstanceNode.getProperty("rackUnits");
		}
		
		def percentage;
		if (totalRackUnits == 0)
			percentage = 100;
		else
			percentage = 100 * occupiedRackUnits / totalRackUnits; 
		
		def serialNumber = rackInstanceNode.hasProperty("serialNumber") && !((String)rackInstanceNode.getProperty("serialNumber")).isEmpty() ? rackInstanceNode.getProperty("serialNumber") : "<NOT SET>";
		
		if (percentage > thresholdHigh)
			taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("The rack %s with serial number %s and id %s has a %s%% occupation", 
				rackInstanceNode.getProperty("name"), serialNumber, rackInstanceNode.getId(), percentage)));
		else if (percentage > thresholdMid)
			taskResult.getMessages().add(TaskResult.createWarningMessage(String.format("The rack %s with serial number %s and id %s has a %s%% occupation", 
				rackInstanceNode.getProperty("name"), serialNumber, rackInstanceNode.getId(), percentage)));
	}
}

//Returns the result
taskResult;