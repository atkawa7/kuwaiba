/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.nodes;

import com.neotropic.kuwaiba.prototypes.actions.AbstractAction;
import com.neotropic.kuwaiba.prototypes.actions.ActionsFactory;
import com.neotropic.kuwaiba.prototypes.model.Employee;
import com.vaadin.ui.Tree;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class EmployeeNode extends AbstractNode {

    public EmployeeNode(Employee employee, Tree tree) {
        super(employee, tree);
    }

    @Override
    public AbstractAction[] getActions() {
        return new AbstractAction[] { ActionsFactory.createDeleteAction() };
    }

    @Override
    public void refresh(boolean recursive) {}
  
}
