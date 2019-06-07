/*
 * Copyright (c) 2019 johnyortega.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    johnyortega - initial API and implementation and/or initial documentation
 */
package com.neotropic.inventory.modules.cpe.nodes.actions;

import java.awt.event.ActionEvent;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;

/**
 *
 * @author johnyortega
 */
public class ReleaseFromEvlanAction extends GenericObjectNodeAction {
    
    public ReleaseFromEvlanAction() {
    }

    @Override
    public LocalValidator[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public String[] appliesTo() {
        return null; //Enable this action for any object
    }

    @Override
    public int numberOfNodes() {
        return -1;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CPE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
