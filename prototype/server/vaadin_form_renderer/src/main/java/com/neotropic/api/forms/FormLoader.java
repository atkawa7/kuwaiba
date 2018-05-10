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
public class FormLoader {
    public static final QName TAG_ROOT = new QName(Constants.Tag.ROOT);
    public static final QName TAG_FORM = new QName(Constants.Tag.FORM);
    public static final QName TAG_GRID_LAYOUT = new QName(Constants.Tag.GRID_LAYOUT);
    public static final QName TAG_I18N = new QName(Constants.Tag.I18N);
    public static final QName TAG_LABEL = new QName(Constants.Tag.LABEL);
    public static final QName TAG_TEXT_FIELD = new QName(Constants.Tag.TEXT_FIELD);
    public static final QName TAG_VERTICAL_LAYOUT = new QName(Constants.Tag.VERTICAL_LAYOUT);
    public static final QName TAG_TEXT_AREA = new QName(Constants.Tag.TEXT_AREA);
    public static final QName TAG_DATE_FIELD = new QName(Constants.Tag.DATE_FIELD);
    public static final QName TAG_COMBO_BOX = new QName(Constants.Tag.COMBO_BOX);
    public static final QName TAG_GRID = new QName(Constants.Tag.GRID);
    public static final QName TAG_BUTTON = new QName(Constants.Tag.BUTTON);
    public static final QName TAG_SUBFORM = new QName(Constants.Tag.SUBFORM);
    public static final QName TAG_HORIZONTAL_LAYOUT = new QName(Constants.Tag.HORIZONTAL_LAYOUT);
    public static final QName TAG_IMAGE = new QName(Constants.Tag.IMAGE);
    public static final QName TAG_SCRIPT = new QName(Constants.Tag.SCRIPT);
    public static final QName TAG_PANEL = new QName(Constants.Tag.PANEL);
    public static final QName TAG_TREE = new QName(Constants.Tag.TREE);
    public static final QName TAG_LIST_SELECT_FILTER = new QName(Constants.Tag.LIST_SELECT_FILTER);
    public static final QName TAG_UPLOAD = new QName(Constants.Tag.UPLOAD);
    
    private final List<QName> containers;
    
    private ElementForm root;
        
    private final List<AbstractElement> elements = new ArrayList();
    private final ElementScript elementScript = new ElementScript();
    private ElementI18N elementI18N;
    
    private byte[] structure;
        
    public FormLoader(byte[] structure) {
        this.structure = structure;        
        containers = new ArrayList();
        containers.add(TAG_GRID_LAYOUT);
        containers.add(TAG_VERTICAL_LAYOUT);
        containers.add(TAG_SUBFORM);
        containers.add(TAG_HORIZONTAL_LAYOUT);
        containers.add(TAG_PANEL);
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
                
                if (reader.getName().equals(TAG_GRID_LAYOUT)) {
                    child = new ElementGridLayout();
                    
                } else if (reader.getName().equals(TAG_VERTICAL_LAYOUT)) {
                    child = new ElementVerticalLayout();
                    
                } else if (reader.getName().equals(TAG_SUBFORM)) {
                    child = new ElementSubform();
                    
                } else if (reader.getName().equals(TAG_HORIZONTAL_LAYOUT)) {
                    child = new ElementHorizontalLayout();
                    
                } else if (reader.getName().equals(TAG_LABEL)) {
                    child = new ElementLabel();
                    
                } else if (reader.getName().equals(TAG_TEXT_FIELD)) {
                    child = new ElementTextField();
                    
                } else if (reader.getName().equals(TAG_TEXT_AREA)) {
                    child = new ElementTextArea();
                    
                } else if (reader.getName().equals(TAG_DATE_FIELD)) {
                    child = new ElementDateField();
                    
                } else if (reader.getName().equals(TAG_COMBO_BOX)) {
                    child = new ElementComboBox();
                    
                } else if (reader.getName().equals(TAG_GRID)) {
                    child = new ElementGrid();
                    
                } else if (reader.getName().equals(TAG_BUTTON)) {
                    child = new ElementButton();
                    
                } else if (reader.getName().equals(TAG_IMAGE)) {
                    child = new ElementImage();
                    
                } else if (reader.getName().equals(TAG_PANEL)) {
                    child = new ElementPanel();
                    
                } else if (reader.getName().equals(TAG_TREE)) {
                    child = new ElementTree();
                    
                } else if (reader.getName().equals(TAG_LIST_SELECT_FILTER)) {
                    child = new ElementListSelectFilter();
                    
                } else if (reader.getName().equals(TAG_UPLOAD)) {
                    child = new ElementUpload();
                    
                } else if (reader.getName().equals(TAG_I18N)) {
                    return event;
                    
                } else if (reader.getName().equals(TAG_SCRIPT)) {
                    return event;
                    
                }
                                
                if (child != null) {
                    child.initFromXML(reader);
                    
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
                    
                    if (reader.getName().equals(TAG_FORM)) {
                        root = new ElementForm();
                        root.initFromXML(reader);
                                                
                        elements.add(root);
                                                                                    
                        event = createFormContaimentHierarchy(root, reader, event);
                    }
                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(TAG_I18N)) {
                        elementI18N = new ElementI18N();
                        elementI18N.initFromXML(reader);
                    }
                    if (reader.getName().equals(TAG_SCRIPT))
                        elementScript.initFromXML(reader);
                }
            }
            reader.close();
            
            FormStructure formStructure = new FormStructure(elements, elementScript, elementI18N);
                    
                        
            for (AbstractElement element : elements)
                element.setFormStructure(formStructure);
                
            if (elementScript != null && elementScript.getFunctions() != null)
                elementScript.getFunctions().put(Constants.Function.I18N, new FunctionI18N(elementI18N));
                
        } catch (XMLStreamException ex) {
            Logger.getLogger(FormLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void fireOnload() {
        for (AbstractElement element : elements)
            element.fireOnload();
    }
    
}
