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
package com.neotropic.forms;

import com.neotropic.api.forms.AbstractElement;
import com.neotropic.api.forms.AbstractElementContainer;
import com.neotropic.api.forms.ElementBuilder;
import com.neotropic.api.forms.Evaluator;
import com.neotropic.api.forms.ElementForm;
import com.neotropic.api.forms.ElementSubform;
import com.neotropic.web.components.ComponentFactory;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FormRenderer extends CustomComponent {
    private final VerticalLayout content;
    private final ElementBuilder builder;
    private final Evaluator evaluator;
    private HashMap<String, AbstractElement> elementsIds = new HashMap();
    private HashMap<AbstractElement, Component> elements = new HashMap();
    private HashMap<Component, AbstractElement> components = new HashMap();
    private HashMap<AbstractElement, Window> openedWindows = new HashMap();
    
    public FormRenderer(ElementBuilder builder/*Window window*/) {
//        VerticalLayout popupContent = new VerticalLayout();
//        popupContent.addComponent(new TextField("textField"));
//        
//        Button btnOk = new Button("Ok");
//////        btnOk.setClickShortcut(KeyCode.ENTER);
//        popupContent.addComponent(btnOk);
//        
//        Button btnCancel = new Button("Cancel");
//        btnOk.setClickShortcut(KeyCode.ESCAPE);
//        popupContent.addComponent(btnCancel);        
//        
//        Button btnPrint = new Button("Print");
//        //btnPrint.setClickShortcut(0, modifiers);
//        popupContent.addComponent(btnPrint);
//                
//        btnCancel.addClickListener((Button.ClickEvent event) -> {
//            window.close();
//        });
//        
//        btnPrint.addClickListener((Button.ClickEvent event) -> {
//            JavaScript.getCurrent().execute("print();");
//        });
        this.builder = builder;
        
        if (builder.getScriptRunner() != null && 
            builder.getScriptRunner().getFormStructure() != null &&
            builder.getScriptRunner().getFormStructure().getElementI18N() != null) {
            
            builder.getScriptRunner().getFormStructure().getElementI18N().setLang("en_US");
        }
        evaluator = builder.getEvaluator();
        
        content = new VerticalLayout();
        setCompositionRoot(content);
    }
    
    public void render() {
        content.removeAllComponents();
        
        elements.put(builder.getRoot(), content);
        
        if (builder.getRoot().getId() != null)
            elementsIds.put(builder.getRoot().getId(), builder.getRoot());
                        
        renderRecursive(builder.getRoot(), content);
    }
        
    private void renderRecursive(AbstractElement parentElement, Component parentComponent) {
        
        if (parentElement instanceof AbstractElementContainer) {
            
            for (AbstractElement childElement : ((AbstractElementContainer) parentElement).getChildren()) {

                Component childComponent = null;

                if (childElement instanceof ElementForm) {
                } else {
                    childComponent = ComponentFactory.getInstance().getComponent(childElement);
                }
                
                if (childComponent != null) {

                    if (childElement.getId() != null)
                        elementsIds.put(childElement.getId(), childElement);

                    elements.put(childElement, childComponent);
                    components.put(childComponent, childElement);

                    if (!(childElement instanceof ElementSubform)) {

                        if (parentComponent instanceof AbstractLayout) {
                            if (parentComponent instanceof GridLayout) {

                                List<Integer> area = childElement.getArea();

                                if (area != null) {
                                    if (area.size() == 2) {
                                        int x1 = area.get(0);                                    
                                        int y1 = area.get(1);

                                        ((GridLayout) parentComponent).addComponent(childComponent, x1, y1);
                                    }
                                    if (area.size() == 4) {
                                        int x1 = area.get(0);                                    
                                        int y1 = area.get(1);
                                        int x2 = area.get(2);                                    
                                        int y2 = area.get(3);

                                        ((GridLayout) parentComponent).addComponent(childComponent, x1, y1, x2, y2);
                                    }
                                } else
                                    ((GridLayout) parentComponent).addComponent(childComponent);
                            } else
                                ((AbstractLayout) parentComponent).addComponent(childComponent);
                        }
                    }
                    renderRecursive(childElement, childComponent);
                }
            }
        }
    }
    
    public void comboboxOnvaluechange(HashMap<String, List<String>> values) {
        if (values.containsKey("threeCharacterCode")) {
            ComboBox source = (ComboBox) getComponent(values.get("threeCharacterCode").get(0));
            AbstractField target = (AbstractField) getComponent(values.get("threeCharacterCode").get(1));

            String str = source.getValue().toString();
            str = str.replace(" ", "");
            if (str.length() >= 4)
                str = str.substring(0, 3);
            
            str = str.toUpperCase();
            
            target.setValue(str);
        }        
    }
    
    public void textFieldOnvaluechange(HashMap<String, List<String>> values) {
        int i = 0;
        
        if (values.containsKey("notify")) {
                                    
        }
//        if (values.containsKey("textField")) {
//            ComboBox source = (ComboBox) getComponent(values.get("textField").get(0));
//            AbstractField target = (AbstractField) getComponent(values.get("textField").get(1));
//
//            target.setValue(source.getValue().toString());
//        }
                
    }
    
    private void closeWindow(String elementId) {
        if (elementsIds != null && elementsIds.containsKey(elementId)) {
            AbstractElement elementSubform = elementsIds.get(elementId);
            if (elementSubform != null) {
                if (openedWindows != null && openedWindows.containsKey(elementSubform)) {
                    Window windowSubform = openedWindows.get(elementSubform);
                    openedWindows.remove(elementSubform);

                    windowSubform.close();
                }
            }
        }
    }
    
    private AbstractElement getElement(String elementId) {
        if (elementsIds != null && elementsIds.containsKey(elementId))
            return elementsIds.get(elementId);
        
        return null;
    }
    
    private Component getComponent(String elementId) {
            
        AbstractElement element = getElement(elementId);

        if (element != null) {

            if (elements != null && elements.containsKey(element))
                return elements.get(element);
        }
        return null;
    }
}
