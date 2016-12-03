/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.nodes;

import com.neotropic.kuwaiba.prototypes.actions.AbstractAction;
import com.neotropic.kuwaiba.prototypes.actions.AddCompanyAction;
import com.vaadin.ui.Tree;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RootNode extends AbstractNode {

    public RootNode(Tree tree) {
        super(tree);
        setDisplayName("Companies All Over the World");
    }

    @Override
    public AbstractAction[] getActions() {
        return new AbstractAction[] { new AddCompanyAction() };
    }

    @Override
    public void refresh(boolean recursive) {}
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof RootNode; //There should be only one root node
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }    
}
