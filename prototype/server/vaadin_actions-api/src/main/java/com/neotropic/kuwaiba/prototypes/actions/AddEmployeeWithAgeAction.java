/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.actions;

import com.neotropic.kuwaiba.prototypes.model.Employee;
import com.neotropic.kuwaiba.prototypes.nodes.AbstractNode;
import com.neotropic.kuwaiba.prototypes.nodes.EmployeeNode;
import com.neotropic.kuwaiba.prototypes.windows.FormWindow;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import java.util.Arrays;

/**
 *
 * @author duckman
 */
public class AddEmployeeWithAgeAction extends AbstractComposedAction {
    
    public AddEmployeeWithAgeAction() {
        super("Add Employee with Age", Arrays.asList(10, 20, 30));
    }

    @Override
    public void finalActionPerformed(Object sourceComponent, Object targetObject, Object selectedOption) {
        TextField txtEmployeeName = new TextField();
        txtEmployeeName.setData("txtEmployeeName");
        
        TextField txtEmployeeLastName = new TextField();
        txtEmployeeLastName.setData("txtEmployeeLastName");
        
        UI.getCurrent().addWindow(new FormWindow("New Employee", 
                new String[] { "Name", "Last Name" }, 
                new AbstractField[] { txtEmployeeName, txtEmployeeLastName }, 
                new FormWindow.FormEventListener() {
                    @Override
                    public void formEvent(FormWindow.FormEvent event) {
                        if (event.getOptionChosen() == FormWindow.FormEvent.EVENT_OK) {
                            String newEmployeeName = ((TextField)event.getComponents().get("txtEmployeeName")).getValue();
                            String newEmployeeLastName = ((TextField)event.getComponents().get("txtEmployeeLastName")).getValue();
                            
                            AbstractNode employeeNode = new EmployeeNode(
                                    new Employee(newEmployeeName, newEmployeeLastName, (int)selectedOption), (Tree)sourceComponent);
                            ((Tree)sourceComponent).addItem(employeeNode);
                            ((Tree)sourceComponent).setParent(employeeNode, targetObject);
                        }
                    }
            }));
    }
}
