/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.web.nodes.actions;


import com.vaadin.ui.Tree;

/**
 *
 * @author duckman
 */
public class AddObjectAction extends AbstractAction {
    
    public AddObjectAction() {
        super("Add Object");
    }

//    @Override
//    public void actionPerformed(Object source, Object target) {
//        
//        TextField txtCompanyName = new TextField();
//        txtCompanyName.setData("txtCompanyName");
//        UI.getCurrent().addWindow(new FormWindow("New Company", 
//                new String[] {"Name"}, 
//                new AbstractField[] {txtCompanyName}, 
//                new FormWindow.FormEventListener() {
//                    @Override
//                    public void formEvent(FormWindow.FormEvent event) {
//                        if (event.getOptionChosen() == FormWindow.FormEvent.EVENT_OK) {
//                            String newCompanyName = ((TextField)event.getComponents().get("txtCompanyName")).getValue();
//                            AbstractNode companyNode = new CompanyNode(new Company(newCompanyName, ""), (Tree)source);
//                            ((Tree)source).addItem(companyNode);
//                            ((Tree)source).setParent(companyNode, target);
//                        }
//                    }
//            }));
//    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
