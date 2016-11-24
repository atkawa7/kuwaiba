/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.actions;

import com.neotropic.kuwaiba.prototypes.nodes.AbstractNode;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Notification;

/**
 *
 * @author duckman
 */
public class DeleteElementAction extends AbstractAction {
    
    public DeleteElementAction() {
        super("Delete", new ThemeResource("img/warning.gif"));
    }

    @Override
    public void actionPerformed(Object source, Object target) {
        ((AbstractNode)target).delete();
        Notification.show("Element successfully deleted", Notification.Type.TRAY_NOTIFICATION);
    }
}
