/** 
 * Sample validator that checks if a port has a physical connection attached to it
 * Neotropic SAS - version 1.0
 * Applies to: GenericPort or its subclasses
 */
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.Validator;

//We get the Business Entity Manager instance
def bem = PersistenceService.getInstance().getBusinessEntityManager();
//Here we will put the color and additional text properties.
def properties = Properties.newInstance();

//It's not really necessary to check if the object is actually a port, just try to fetch the elements related using an "endpointA" or "endpointB" relationship.
if (!bem.getSpecialAttribute(objectClass, objectId, "endpointA").isEmpty() || !bem.getSpecialAttribute(objectClass, objectId, "endpointB").isEmpty()) {
    properties.setProperty("color", "FF0000");
    properties.setProperty("prefix", "[-]");
}

return new Validator(validatorDefinitionName, properties);
