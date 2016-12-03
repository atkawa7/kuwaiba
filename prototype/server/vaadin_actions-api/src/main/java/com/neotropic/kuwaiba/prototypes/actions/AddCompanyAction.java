/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.actions;

import com.neotropic.kuwaiba.prototypes.model.Company;
import com.neotropic.kuwaiba.prototypes.nodes.AbstractNode;
import com.neotropic.kuwaiba.prototypes.nodes.CompanyNode;
import com.neotropic.kuwaiba.prototypes.windows.FormWindow;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AddCompanyAction extends AbstractAction {
    
    public AddCompanyAction() {
        super("Add Company");
    }

    @Override
    public void actionPerformed(Object source, Object target) {
        
        TextField txtCompanyName = new TextField();
        txtCompanyName.setData("txtCompanyName");
        UI.getCurrent().addWindow(new FormWindow("New Company", 
                new String[] {"Name"}, 
                new AbstractField[] {txtCompanyName}, 
                new FormWindow.FormEventListener() {
                    @Override
                    public void formEvent(FormWindow.FormEvent event) {
                        if (event.getOptionChosen() == FormWindow.FormEvent.EVENT_OK) {
                            String newCompanyName = ((TextField)event.getComponents().get("txtCompanyName")).getValue();
                            AbstractNode companyNode = new CompanyNode(new Company(newCompanyName, ""), (Tree)source);
                            ((Tree)source).addItem(companyNode);
                            ((Tree)source).setParent(companyNode, target);
                        }
                    }
            }));
    }
    
    
}
