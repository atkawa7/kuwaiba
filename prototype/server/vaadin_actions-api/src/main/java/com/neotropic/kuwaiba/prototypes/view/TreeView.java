/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.view;

import com.neotropic.kuwaiba.prototypes.actions.AbstractAction;
import com.neotropic.kuwaiba.prototypes.model.Company;
import com.neotropic.kuwaiba.prototypes.model.Employee;
import com.neotropic.kuwaiba.prototypes.nodes.AbstractNode;
import com.neotropic.kuwaiba.prototypes.nodes.CompanyNode;
import com.neotropic.kuwaiba.prototypes.nodes.EmployeeNode;
import com.neotropic.kuwaiba.prototypes.nodes.RootNode;
import com.vaadin.event.Action;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Tree;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TreeView extends CustomComponent {
    
    private Tree tree;
    
    public TreeView() {
        tree = new Tree("Employment Tree");
        tree.setDragMode(Tree.TreeDragMode.NODE);
        RootNode rootNode = new RootNode(tree);
        
        CompanyNode oracle = new CompanyNode(new Company("Oracle", "Technology"), tree);
        CompanyNode neotropic = new CompanyNode(new Company("Neotropic", "Technology"), tree);
        CompanyNode gnuProject = new CompanyNode(new Company("GNU Project", "Activism"), tree);
        
        EmployeeNode adrian = new EmployeeNode(new Employee("Adrián", "Martínez", 10), tree);
        EmployeeNode charles = new EmployeeNode(new Employee("Charles", "Bedón", 10), tree);
        EmployeeNode johny = new EmployeeNode(new Employee("Johny", "Ortega", 10), tree);
        EmployeeNode larry = new EmployeeNode(new Employee("Larry", "Ellison", 10), tree);   
        EmployeeNode richard = new EmployeeNode(new Employee("Richard", "Stallman", 10), tree);
        
        tree.addItem(rootNode);
        
        tree.addItem(oracle);
        tree.addItem(neotropic);
        tree.addItem(gnuProject);
        tree.addItem(adrian);
        tree.addItem(charles);
        tree.addItem(johny);
        tree.addItem(larry);
        tree.addItem(richard);
        
        tree.setParent(oracle, rootNode);
        tree.setParent(neotropic, rootNode);
        tree.setParent(gnuProject, rootNode);
        
        tree.setParent(adrian, neotropic);
        tree.setParent(charles, neotropic);
        tree.setParent(johny, neotropic);
        tree.setParent(larry, oracle);
        tree.setParent(richard, gnuProject);
        
        tree.addActionHandler(new Action.Handler() {

            @Override
            public Action[] getActions(Object target, Object sender) {
                if (target instanceof AbstractNode)
                    return ((AbstractNode)target).getActions();
                else
                    return null;
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                ((AbstractAction)action).actionPerformed(sender, target);
            }
        });
        
        tree.expandItem(rootNode);
        
        setCompositionRoot(tree);
    }
    
}
