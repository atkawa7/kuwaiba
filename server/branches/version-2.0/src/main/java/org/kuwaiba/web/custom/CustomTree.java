/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.custom;

import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseListener;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.web.custom.tree.Node;

/**
 *
 * @author duckman
 */
public class CustomTree extends Tree implements Tree.ExpandListener, CollapseListener {

    public CustomTree(RemoteBusinessObjectLight rootObject, String caption) {
        super(caption);
        this.addItem(new Node(rootObject, this));
        addExpandListener(this);
        addCollapseListener(this);
    }    
    
    @Override
    public void nodeExpand(ExpandEvent event) {
        ((Node)event.getItemId()).expand();
    }

    @Override
    public void nodeCollapse(CollapseEvent event) {
        ((Node)event.getItemId()).collapse();
    }
    
}
