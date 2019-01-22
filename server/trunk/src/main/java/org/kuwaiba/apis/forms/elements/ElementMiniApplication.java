/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.apis.forms.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ElementMiniApplication extends AbstractElement {
    private String mode;
    private String classPackage;
    private String className;
    private Properties inputParameters;
    private Properties outputParameters;
                    
    public ElementMiniApplication() {
        
    }
    
    public Properties getInputParameters() {
        return inputParameters;
    }
    
    public void setInputParameters(Properties inputParameters) {
        this.inputParameters = inputParameters;
    }
    
    public Properties getOutputParameters() {
        return outputParameters;
    }
        
    public void setOutputParameters(Properties outputParameters) {
        this.outputParameters = outputParameters;
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
    
    @Override
    public void fireOnLoad() {
        super.fireOnLoad();
        if (hasProperty(Constants.EventAttribute.ONLOAD, Constants.Property.INPUT_PARAMETERS)) {
            
            List<String> list = getEvents().get(Constants.EventAttribute.ONLOAD).get(Constants.Property.INPUT_PARAMETERS);
            
            if (list != null && !list.isEmpty()) {
                Properties oldInputParameters = getInputParameters();
                
                String functionName = list.get(0);

                Runner runner = getFormStructure().getElementScript().getFunctionByName(functionName);

                List parameters = new ArrayList();

                for (int i = 1; i < list.size(); i++) {
                    AbstractElement anElement = getFormStructure().getElementById(list.get(i));
                    
                    if (anElement == null) {
                        if (getFormStructure().getElementScript() != null && 
                            getFormStructure().getElementScript().getFunctions() != null) {

                            if (getFormStructure().getElementScript().getFunctions().containsKey(list.get(i))) {

                                Runner paramRunner = getFormStructure().getElementScript().getFunctions().get(list.get(i));

                                if (paramRunner != null) {
                                    parameters.add(paramRunner);
                                    continue;
                                }
                            }
                        }
                    }
                    parameters.add(anElement != null ? anElement : list.get(i));
                }

                Object newValue = runner.run(parameters);

                if (newValue != null) {
                    setInputParameters((Properties) newValue);
                    
                    fireElementEvent(new EventDescriptor(
                        Constants.EventAttribute.ONPROPERTYCHANGE, 
                        Constants.Property.INPUT_PARAMETERS, 
                        getInputParameters(), 
                        oldInputParameters));
                }                
            }
        }
    }  
    
    @Override
    public boolean hasProperty(String propertyName) {
        
        switch (propertyName) {
            case Constants.Property.INPUT_PARAMETERS:
                return true;
            default:
                return super.hasProperty(propertyName);
        }
    }
        
    @Override
    public Object getPropertyValue(String propertyName) {
        
        switch (propertyName) {
            case Constants.Property.INPUT_PARAMETERS:
                return getInputParameters();
            default:
                return getPropertyValue(propertyName);
        }
    }
}
