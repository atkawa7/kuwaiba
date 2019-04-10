/** 
 * Checks if the attribute "state" of a service is set to "Ceased" and marks it to be displayed magenta. It also adds a prefix and a suffix to the object's display name.
 * Neotropic SAS - version 1.1
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

    public Validator run(String objectClass, String objectId) {
        BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
        String state = bem.getAttributeValueAsString(objectClass, objectId, "state");

        if ("Ceased".equals(state)) {
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