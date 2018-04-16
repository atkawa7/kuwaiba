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
package com.neotropic.web.components;

import com.neotropic.api.forms.EventDescriptor;
import com.neotropic.api.forms.AbstractElement;
import com.neotropic.api.forms.Constants;
import com.neotropic.api.forms.ElementButton;
import com.vaadin.ui.Button;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentButton extends GraphicalComponent {
            
    public ComponentButton() {
        super(new Button());
    }
    
    @Override
    public Button getComponent() {
        return (Button) super.getComponent();
    }
        
    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementButton) {
            ElementButton button = (ElementButton) element;
            
            
            getComponent().setCaption(button.getCaption());
            
            getComponent().addClickListener(new Button.ClickListener() {
                
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    fireComponentEvent(new EventDescriptor(Constants.EventAttribute.ONCLICK));
                }
            });            
        }
        /*
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
        */
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
