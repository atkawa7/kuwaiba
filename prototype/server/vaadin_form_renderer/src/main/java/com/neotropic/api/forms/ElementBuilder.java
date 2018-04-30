/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.api.forms;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * This class reads an structure xml and create the form elements containment 
 * hierarchy.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ElementBuilder {
    private final QName tagForm = new QName(Constants.Tag.FORM);
    private final QName tagGridLayout = new QName(Constants.Tag.GRID_LAYOUT);
    private final QName tagI18N = new QName(Constants.Tag.I18N);
    private final QName tagLabel = new QName(Constants.Tag.LABEL);
    private final QName tagTextField = new QName(Constants.Tag.TEXT_FIELD);
    private final QName tagVerticalLayout = new QName(Constants.Tag.VERTICAL_LAYOUT);
    private final QName tagTextArea = new QName(Constants.Tag.TEXT_AREA);
    private final QName tagDateField = new QName(Constants.Tag.DATE_FIELD);
    private final QName tagComboBox = new QName(Constants.Tag.COMBO_BOX);
    private final QName tagGrid = new QName(Constants.Tag.GRID);
    private final QName tagButton = new QName(Constants.Tag.BUTTON);
    private final QName tagSubform = new QName(Constants.Tag.SUBFORM);
    private final QName tagHorizontalLayout = new QName(Constants.Tag.HORIZONTAL_LAYOUT);
    private final QName tagImage = new QName(Constants.Tag.IMAGE);
    private final QName tagScript = new QName(Constants.Tag.SCRIPT);
    private final QName tagPanel = new QName(Constants.Tag.PANEL);
    private final QName tagTree = new QName(Constants.Tag.TREE);
    private final QName tagListSelectFilter = new QName(Constants.Tag.LIST_SELECT_FILTER);
    
    private final List<QName> containers;
    
    private ElementForm root;
        
    private final List<AbstractElement> elements = new ArrayList();
    private final ElementScript elementScript = new ElementScript();
    private ElementI18N elementI18N;
    
    private byte[] structure;
        
    public ElementBuilder(byte[] structure) {
        this.structure = structure;        
        containers = new ArrayList();
        containers.add(tagGridLayout);
        containers.add(tagVerticalLayout);
        containers.add(tagSubform);
        containers.add(tagHorizontalLayout);
        containers.add(tagPanel);
    }
        
    public ElementForm getRoot() {
        return root;
    }
    
    private int createFormContaimentHierarchy(AbstractElement parent, XMLStreamReader reader, int event) throws XMLStreamException {
        
        while (reader.hasNext()) {
            event = reader.next();
            
            if (event == XMLStreamConstants.END_ELEMENT) {
                if (containers.contains(reader.getName()))
                    return event;
            }
            
            if (event == XMLStreamConstants.START_ELEMENT) {
                
                AbstractElement child = null;                
                
                if (reader.getName().equals(tagGridLayout)) {
                    child = new ElementGridLayout();
                    
                } else if (reader.getName().equals(tagVerticalLayout)) {
                    child = new ElementVerticalLayout();
                    
                } else if (reader.getName().equals(tagSubform)) {
                    child = new ElementSubform();
                    
                } else if (reader.getName().equals(tagHorizontalLayout)) {
                    child = new ElementHorizontalLayout();
                    
                } else if (reader.getName().equals(tagLabel)) {
                    child = new ElementLabel();
                    
                } else if (reader.getName().equals(tagTextField)) {
                    child = new ElementTextField();
                    
                } else if (reader.getName().equals(tagTextArea)) {
                    child = new ElementTextArea();
                    
                } else if (reader.getName().equals(tagDateField)) {
                    child = new ElementDateField();
                    
                } else if (reader.getName().equals(tagComboBox)) {
                    child = new ElementComboBox();
                    
                } else if (reader.getName().equals(tagGrid)) {
                    child = new ElementGrid();
                    
                } else if (reader.getName().equals(tagButton)) {
                    child = new ElementButton();
                    
                } else if (reader.getName().equals(tagImage)) {
                    child = new ElementImage();
                    
                } else if (reader.getName().equals(tagPanel)) {
                    child = new ElementPanel();
                    
                } else if (reader.getName().equals(tagTree)) {
                    child = new ElementTree();
                    
                } else if (reader.getName().equals(tagListSelectFilter)) {
                    child = new ElementListSelectFilter();
                    
                } else if (reader.getName().equals(tagI18N)) {
                    return event;
                    
                } else if (reader.getName().equals(tagScript)) {
                    return event;
                    
                }
                                
                if (child != null) {
                    child.initFromXMl(reader);
                    
                    elements.add(child);
                                                            
                    if (parent instanceof AbstractElementContainer)
                        ((AbstractElementContainer) parent).addChild(child);
                    
                    if (child instanceof AbstractElementContainer)
                        event = createFormContaimentHierarchy(child, reader, event);
                }
            }
        }
        return event;
    }    
    
    public void build() {

        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
                                    
            while (reader.hasNext()) {
                int event = reader.next();
                
                if (event == XMLStreamConstants.START_ELEMENT) {
                    
                    if (reader.getName().equals(tagForm)) {
                        root = new ElementForm();
                        root.initFromXMl(reader);
                                                
                        elements.add(root);
                                                                                    
                        event = createFormContaimentHierarchy(root, reader, event);
                    }
                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagI18N)) {
                        elementI18N = new ElementI18N();
                        elementI18N.initFromXMl(reader);
                    }
                    if (reader.getName().equals(tagScript))
                        elementScript.initFromXMl(reader);
                }
            }
            reader.close();
            
            FormStructure formStructure = new FormStructure(elements, elementScript, elementI18N);
                    
                        
            for (AbstractElement element : elements)
                element.setFormStructure(formStructure);
                
            if (elementScript != null && elementScript.getFunctions() != null)
                elementScript.getFunctions().put(Constants.Function.I18N, new FunctionI18N(elementI18N));
                
        } catch (XMLStreamException ex) {
            Logger.getLogger(ElementBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void fireOnload() {
        for (AbstractElement element : elements)
            element.fireOnload();
    }
    
}
