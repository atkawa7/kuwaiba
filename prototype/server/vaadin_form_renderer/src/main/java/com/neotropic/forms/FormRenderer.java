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

import com.neotropic.api.forms.Constants;
import com.neotropic.api.forms.ElementButton;
import com.neotropic.api.forms.ElementColumn;
import com.neotropic.api.forms.ElementComboBox;
import com.neotropic.api.forms.ElementDateField;
import com.neotropic.api.forms.AbstractElement;
import com.neotropic.api.forms.ElementBuilder;
import com.neotropic.api.forms.Evaluator;
import com.neotropic.api.forms.ElementTextField;
import com.neotropic.api.forms.ElementLabel;
import com.neotropic.api.forms.ElementGridLayout;
import com.neotropic.api.forms.ElementForm;
import com.neotropic.api.forms.ElementGrid;
import com.neotropic.api.forms.ElementHorizontalLayout;
import com.neotropic.api.forms.ElementImage;
import com.neotropic.api.forms.ElementSubform;
import com.neotropic.api.forms.ElementTextArea;
import com.neotropic.api.forms.ElementVerticalLayout;
import com.vaadin.data.HasValue;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
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
        
        builder.getI18N().setLang("en_US");
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
        
        for (AbstractElement childElement : parentElement.getChildren()) {
            
            Component childComponent = null;

            if (childElement instanceof ElementForm) {
            } else if (childElement instanceof ElementGridLayout) {
                ElementGridLayout gridLayout = (ElementGridLayout) childElement;

                childComponent = new GridLayout();
                ((GridLayout) childComponent).setColumns(gridLayout.getColumns());
                ((GridLayout) childComponent).setRows(gridLayout.getRows());
                
            } else if (childElement instanceof ElementVerticalLayout) {
                childComponent = new VerticalLayout();                
                
            } else if (childElement instanceof ElementLabel) {
                childComponent = new Label();
                String value = evaluator.getValue(((ElementLabel) childElement).getValue());
                ((Label) childComponent).setValue(value != null ? value : "");
                ((Label) childComponent).setContentMode(ContentMode.HTML);
                ((Label) childComponent).setSizeFull();
                
                String styleName = ((ElementLabel) childElement).getStyleName();
                
                if (styleName != null)
                    ((Label) childComponent).setStyleName(styleName);
            } else if (childElement instanceof ElementTextField) {
                childComponent = new TextField();
                String value = evaluator.getValue(((ElementTextField) childElement).getValue());
                ((TextField) childComponent).setValue(value != null ? value : "");
                ((TextField) childComponent).setEnabled(((ElementTextField) childElement).isEnabled());
                ((TextField) childComponent).addValueChangeListener(new HasValue.ValueChangeListener() {
                    @Override
                    public void valueChange(HasValue.ValueChangeEvent event) {
                        
                    }
                });
            } else if (childElement instanceof ElementTextArea) {
                childComponent = new TextArea();
                ((TextArea) childComponent).setSizeFull();
                
            } else if (childElement instanceof ElementDateField) {
                childComponent = new DateField();
                ((DateField) childComponent).setValue(LocalDate.now());
            } else if (childElement instanceof ElementComboBox) {
                childComponent = new ComboBox();
                List items = ((ElementComboBox) childElement).getItems();
                if (items != null)
                    ((ComboBox) childComponent).setItems(items);// .addItems(items);
                
                ((ComboBox) childComponent).addValueChangeListener(new HasValue.ValueChangeListener() {
                    @Override
                    public void valueChange(HasValue.ValueChangeEvent event) {
                        if (childElement.getEvents() != null) {
                            
                            if (childElement.getEvents().containsKey(Constants.EventAttribute.ONVALUECHANGE))
                                comboboxOnvaluechange(childElement.getEvents().get(Constants.EventAttribute.ONVALUECHANGE));                               
                        }                        
                    }
                });
                
            } else if (childElement instanceof ElementGrid) {
                Grid<HashMap<String, String>> gridComponent = new Grid<>();
                childComponent = gridComponent;
                
                ((Grid) childComponent).setSizeFull();
                                
                ElementGrid grid = (ElementGrid) childElement;
                if (grid.getColums() != null) {
                    for (ElementColumn column : grid.getColums())
                        gridComponent.addColumn(row -> row.get(column.getCaption())).setCaption(column.getCaption());
////                        ((Grid) childComponent).addColumn(column.getCaption());
                }
            } else if (childElement instanceof ElementButton) {
                childComponent = new Button();
                                
                ((Button) childComponent).setCaption(((ElementButton) childElement).getCaption());
                
                ((Button) childComponent).addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        ElementButton button = (ElementButton) childElement;
                        
                        HashMap<String, HashMap<String, List<String>>> events = button.getEvents();
                        
                        if (events != null && events.containsKey(Constants.EventAttribute.ONCLICK)) {
                            
                            HashMap<String, List<String>> functions = events.get(Constants.EventAttribute.ONCLICK);
                            
                            
                            
                            if (functions != null) {
                                String openSubform = "openSubform";
                                String closeSubform = "closeSubform";
                                String addGridRow = "addGridRow";
                            
                                if (functions.containsKey(openSubform)) {
                                    List<String> params = functions.get(openSubform);

                                    if (params != null) {
                                        String elementId = params.get(0);
                                        
                                        AbstractElement elementSubform = getElement(elementId);
                                        Component componentSubform = getComponent(elementId);

                                        if (componentSubform != null) {
                                            Window subwindow = new Window();
                                            subwindow.setModal(true);
                                            subwindow.setContent(componentSubform);
                                            subwindow.center();
                                            //subwindow.setSizeFull();
                                            if (UI.getCurrent() != null) {
                                                UI.getCurrent().addWindow(subwindow);
                                                openedWindows.put(elementSubform, subwindow);
                                            } else {
                                                // TODO: notification can no be open the subform
                                            }
                                        }
                                    }                                
                                } else if (functions.containsKey(closeSubform)) {
                                    List<String> params = functions.get(closeSubform);
                                    
                                    if (params != null) {
                                        String elementId = params.get(0);
                                        closeWindow(elementId);
                                    }
                                } else if (functions.containsKey(addGridRow)) {
                                    List<String> params = functions.get(addGridRow);
                                    
                                    if (params != null) {
                                        String subformId = params.get(0);
                                        String elementId = params.get(1);
                                                                                
                                        ElementGrid elementGrid = (ElementGrid) getElement(elementId);
                                        Grid<HashMap<String, String>> grid = (Grid) getComponent(elementId);
                                        
                                        if (grid != null) {

                                            int ncolumns = grid.getColumns().size();
                                            String [] values = new String [ncolumns];

                                            for (int i = 0; i < ncolumns; i += 1) {
                                                String paramId = params.get(i + 2);
                                                Component component = getComponent(paramId);
                                                if (component != null && component instanceof AbstractField) {
                                                    Object value = ((AbstractField) component).getValue();
                                                    if (value != null)
                                                        values[i] = value.toString();
                                                    else
                                                        values[i] = "no set";
                                                } else {
                                                    values[i] = "no set";
                                                }
                                            }
                                            
                                            HashMap<String, String> columnValues = new HashMap<>();
                                            
                                            List<ElementColumn> columns = elementGrid.getColums();
                                            
                                            for (int i = 0; i < ncolumns; i+= 1)
                                                columnValues.put(columns.get(i).getCaption(), values[i]);
                                            
                                            List lst = new ArrayList();
                                            lst.add(columnValues);
                                            
                                            grid.setItems(lst);
                                            closeWindow(subformId);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            } else if (childElement instanceof ElementSubform) {
                childComponent = new VerticalLayout();
                
            } else if (childElement instanceof ElementHorizontalLayout) {
                childComponent = new HorizontalLayout();
                
            } else if (childElement instanceof ElementImage) {
                String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
                FileResource resource = new FileResource(new File(basepath + "/WEB-INF/images/image001.png"));
                
                childComponent = new Image(null,resource);                
//                ((Image) childComponent).setHeight("102px");
//                ((Image) childComponent).setWidth("306px");
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
