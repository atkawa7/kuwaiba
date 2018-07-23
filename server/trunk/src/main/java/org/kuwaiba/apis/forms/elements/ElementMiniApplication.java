/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.apis.forms.elements;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ElementMiniApplication extends AbstractElement {
    private String mode;
    private String classPackage;
    private String className;
        
    public ElementMiniApplication() {
                                                
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.MINI_APPLICATION;
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
            
    public String getClassPackage() {
        return classPackage;        
    }
    
    public void setClassPackage(String classPackage) {
        this.classPackage = classPackage;        
    }
    
    public String getClassName() {
        return className;        
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
        
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
        setMode(reader);
        setClassPackage(reader);
        setClassName(reader);
        
    }
    
    public void setMode(XMLStreamReader reader) {
        mode = reader.getAttributeValue(null, Constants.Attribute.MODE);
    }
    
    public void setClassPackage(XMLStreamReader reader) {
        classPackage = reader.getAttributeValue(null, Constants.Attribute.PACKAGE);
    }
        
    public void setClassName(XMLStreamReader reader) {
        className = reader.getAttributeValue(null, Constants.Attribute.CLASS_NAME);
    }
        
}
