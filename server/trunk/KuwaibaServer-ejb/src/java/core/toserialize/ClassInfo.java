package core.toserialize;

import entity.core.metamodel.AttributeMetadata;
import entity.core.metamodel.ClassMetadata;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Es una clase wrapper de la entidad ClassMetadata que contiene la información
 * requerida por los clientes para el despliegue de atributos y eventualmente reglas de negocio
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassInfo extends ClassInfoLight{
    
    private String [] attributeNames; //En algún momento pensé que el displayName
                                      //de los atributos se podía fijar de una vez en el
                                      //RemoteObject que se devolvía, pero lo deseché
                                      //debido a que igual el meta contiene la información
                                      //de cuáles atributos se muestran, cuáles son administrativos, etc
                                      //definitivamente lo más ordenado es dejar eso en el meta
    private String [] attributeTypes;
    private String [] attributeDisplayNames;
    private Boolean [] attributesIsVisible;
    private Boolean [] attributesIsAdministrative;
    private Boolean [] attributesIsMultiple; //Si el attributo es una relación o una enumeración
    private String [] attributesDescription;

    public ClassInfo(){}
    public ClassInfo(ClassMetadata myClass){
        super(myClass.getId(),myClass.getName(),myClass.getPackageName().getName());
        
        List<AttributeMetadata> ar = myClass.getAttributes();
        this.attributeNames = new String[ar.size()];
        this.attributeTypes = new String[this.attributeNames.length];
        this.attributeDisplayNames = new String[this.attributeNames.length];
        this.attributesIsVisible = new Boolean[this.attributeNames.length];
        this.attributesIsAdministrative = new Boolean[this.attributeNames.length];
        this.attributesIsMultiple = new Boolean[this.attributeNames.length];
        this.attributesDescription = new String[this.attributeNames.length];

        int i = 0;
        for (AttributeMetadata myAtt : ar){
            this.attributeNames[i] = myAtt.getName();
            this.attributeTypes[i] = myAtt.getType();
            this.attributeDisplayNames[i] = myAtt.getDisplayName() == null?
                "":myAtt.getDisplayName();
            this.attributesIsAdministrative[i] = myAtt.isAdministrative();
            this.attributesIsVisible[i] = myAtt.IsVisible();
            this.attributesIsMultiple[i] = myAtt.isMultiple();

            this.attributesDescription[i] = myAtt.getDescription()==null?
                "":myAtt.getDescription();
            i++;
        }
    }

    public String[] getAttributeDisplayNames() {
        return attributeDisplayNames;
    }

    public void setAttributeDisplayNames(String[] attributeDisplayNames) {
        this.attributeDisplayNames = attributeDisplayNames;
    }

    public String[] getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(String[] attributeNames) {
        this.attributeNames = attributeNames;
    }

    public String[] getAttributeTypes() {
        return attributeTypes;
    }

    public void setAttributeTypes(String[] attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    public String[] getAttributesDescription() {
        return attributesDescription;
    }

    public void setAttributesDescription(String[] attributesDescription) {
        this.attributesDescription = attributesDescription;
    }

    public Boolean[] getAttributesIsAdministrative() {
        return attributesIsAdministrative;
    }

    public void setAttributesIsAdministrative(Boolean[] attributesIsAdministrative) {
        this.attributesIsAdministrative = attributesIsAdministrative;
    }

    public Boolean[] getAttributesIsVisible() {
        return attributesIsVisible;
    }

    public void setAttributesIsVisible(Boolean[] attributesIsVisible) {
        this.attributesIsVisible = attributesIsVisible;
    }

    public Boolean[] getAttributesIsMultiple() {
        return attributesIsMultiple;
    }

    public void setAttributesIsMultiple(Boolean[] attributesIsMultiple) {
        this.attributesIsMultiple = attributesIsMultiple;
    }
}
