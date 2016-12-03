/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.actions;

import com.vaadin.event.Action;
import com.vaadin.server.Resource;

/**
 * Root of all actions in the system
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class AbstractAction extends Action {
    public AbstractAction(String caption) {
        super(caption);
    }
    
    public AbstractAction(String caption, Resource icon) {
        super(caption, icon);
    }
    
    /**
     * What to do when the action is triggered 
     * @param sourceComponent The parent component that
     * @param targetObject
     */
    public abstract void actionPerformed (Object sourceComponent, Object targetObject);
}
