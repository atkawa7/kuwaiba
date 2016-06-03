/*
 * Copyright (c) 2016 adrian.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    adrian - initial API and implementation and/or initial documentation
 */
package com.neotropic.inventory.modules.ipam;

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 *
 * @author adrian
 */
public class IPAMModuleService {
    
    private CommunicationsStub com;
    private IPAMModuleTopComponentTopComponent ipamtc;

    IPAMModuleService(IPAMModuleTopComponentTopComponent ipamtc) {
        this.ipamtc = ipamtc;
        com = CommunicationsStub.getInstance();
    }
    
    public LocalObjectLight[] getRootChildren(){
        //por ahora dejemos esto aqui pero el que se encarga de enviar las cosas
        //filtradas es el modulo ipam en el server.
        List<LocalObjectLight> rootChildren = com.getPools(-1, Constants.CLASS_SUBNET);
        if(rootChildren != null)
            return rootChildren.toArray(new LocalObjectLight[0]);
        else{
            ipamtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return new LocalObjectLight[0];
        }
    }
}
