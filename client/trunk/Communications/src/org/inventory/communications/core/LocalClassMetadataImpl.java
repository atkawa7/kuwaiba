package org.inventory.communications.core;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.webservice.ClassInfo;

/**
 * Representa la información meta asociada a una clase para ser usada por los componentes locales
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalClassMetadataImpl extends LocalClassMetadataLightImpl
        implements LocalClassMetadata{

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
    private Boolean [] attributesIsMultiple;
    private String [] attributesDescription;

    public LocalClassMetadataImpl(ClassInfo cm){
        super(cm.getId(),cm.getClassName(),cm.getPackage());
        this.attributeNames = cm.getAttributeNames().toArray(new String[0]);
        this.attributeTypes = cm.getAttributeTypes().toArray(new String[0]);
        this.attributeDisplayNames = cm.getAttributeDisplayNames().toArray(new String[0]);
        this.attributesIsVisible = cm.getAttributesIsVisible().toArray(new Boolean[0]);
        this.attributesIsAdministrative = cm.getAttributesIsAdministrative().toArray(new Boolean[0]);
        this.attributesIsMultiple = cm.getAttributesIsMultiple().toArray(new Boolean[0]);
        this.attributesDescription = cm.getAttributesDescription().toArray(new String[0]);
    }

    public Boolean[] getAttributesIsMultiple() {
        return attributesIsMultiple;
    }

    public void setAttributesIsMultiple(Boolean[] attributesIsMultiple) {
        this.attributesIsMultiple = attributesIsMultiple;
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

    public String getDisplayNameForAttribute(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributeDisplayNames[i].equals("")?att:this.attributeDisplayNames[i];
        return att;
    }

    public Boolean isAdministrative(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributesIsAdministrative[i];
        return false;
    }

    public Boolean isMultiple(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributesIsMultiple[i];
        return false;
    }
    
    public Boolean isVisible(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributesIsVisible[i];
        return false;
    }

    public String getDescriptionForAttribute(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributesDescription[i];
        return "";
    }

    public String getTypeForAttribute(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributeTypes[i];
        return "String";
    }

    public LocalAttributeMetadataImpl[] getAttributes(){
        LocalAttributeMetadataImpl[] res =
                new LocalAttributeMetadataImpl[attributeNames.length];
        for (int i = 0; i<res.length;i++)
            res[i] = new LocalAttributeMetadataImpl(attributeNames[i],
                                    attributeTypes[i],
                                    attributeDisplayNames[i],
                                    attributesIsVisible[i],
                                    attributesIsAdministrative[i],
                                    attributesDescription[i]);
        return res;
    }

    @Override
    public String toString(){
        return this.className;
    }
}
