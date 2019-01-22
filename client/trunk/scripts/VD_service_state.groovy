/** 
 * Checks if the attribute "state" of a service is set to "Ceased" and marks it to be displayed magenta. It also adds a prefix and a suffix to the object's display name.
 * Neotropic SAS - version 1.0
 * Applies to: GenericService or its subclasses.
 */
import java.util.Properties;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.Validator;
import org.kuwaiba.apis.persistence.application.ValidatorDefinition;

//Don't forget to use "%s" as class name, as it will be generated on-the-fly later
public class %s extends ValidatorDefinition {

    //Mandatory, boiler-plate constructor. Don't forget to use "%s" as class name, as it will be generated on-the-fly later
    public %s (long id, String name, String description, String classToBeApplied, String script, boolean enabled) {
        super(id, name, description, classToBeApplied, script, enabled);
    }

    public Validator run(String objectClass, long objectId) {
        BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
        String state = bem.getAttributeValueAsString(objectClass, objectId, "state");

        if (state == "Ceased") {
            //Here we will put the color and additional text properties.
            Properties properties = new Properties();
            properties.setProperty("color", "8632D9");
            properties.setProperty("prefix", "[CEASED]");
            properties.setProperty("suffix", "[XXX]");
            return new Validator(getName(), properties);
        }
        
        //If the service is not ceased, just pass along
        return null;
    }
}



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
            properties.setProperty("prefix", "[Other State]");
            properties.setProperty("suffix", "[Other State]");
        }
    }
}

return new Validator(validatorDefinitionName, properties);
