/** 
 * Checks if a port has a physical connection attached to it. If so, make it render red, with a simple prefix.
 * Neotropic SAS - version 1.0
 * Applies to: GenericService or its subclasses.
 */
import java.util.Properties;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.Validator;
import org.kuwaiba.apis.persistence.application.ValidatorDefinition;

public class %s extends ValidatorDefinition {
    //Mandatory, boiler-plate constructor
    public %s (long id, String name, String description, String classToBeApplied, String script, boolean enabled) {
        super(id, name, description, classToBeApplied, script, enabled);
    }

    public Validator run(String objectClass, String objectId) {
        BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
        
        if (!bem.getSpecialAttribute(objectClass, objectId, "endpointA").isEmpty() || !bem.getSpecialAttribute(objectClass, objectId, "endpointB").isEmpty()) {
            Properties properties = new Properties();
            properties.setProperty("color", "FF0000");
            properties.setProperty("prefix", "[+]");
            return new Validator(getName(), properties);
        }
        
        //If the port is not connected, just pass along
        return null;
    }
}
