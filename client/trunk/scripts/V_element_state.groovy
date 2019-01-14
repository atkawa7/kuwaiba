/** 
 * Sample validator that checks the attribute "state" of an element and set a few properties that can be interpreted by the client to show it in a particular way in
 * say, trees, or search results.
 * Neotropic SAS - version 1.0
 * Applies to: Any object of a class with an attribute "state".
 */
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.Validator;

//We get the Business Entity Manager instance
def bem = PersistenceService.getInstance().getBusinessEntityManager();

//The parameters "objectClass" and "objectId" are injected into the script
//We first get the current value of the attribute "state". If the element does not have such attribute, 
//the validator definition will be ignored and the error logged at server side.
def operationalState = bem.getAttributeValueAsString(objectClass, objectId, "state")
//Here we will put the color and additional text properties.
def properties = Properties.newInstance();

//Change the value of the state depending on your own values
if (operationalState == null) {
    properties.setProperty("color", "0000FF");
    properties.setProperty("prefix", "[Null State]");
    properties.setProperty("suffix", "[Null State]");
} else {
    if (operationalState == "Spare") {
        properties.setProperty("color", "FF0000");
        properties.setProperty("prefix", "[Spare]");
    } else {
        if (operationalState == "Transit") {
            properties.setProperty("color", "00FF00");
            properties.setProperty("suffix", "[In Transit]");
        } else {
            properties.setProperty("color", "CCCCCC");
            properties.setProperty("prefix", "Other State");
            properties.setProperty("suffix", "Other State");
        }
    }
}

return new Validator(validatorDefinitionName, properties);
