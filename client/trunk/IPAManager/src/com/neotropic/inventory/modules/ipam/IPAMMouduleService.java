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
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 *
 * @author adrian
 */
public class IPAMMouduleService {
    
    private CommunicationsStub com;
    private IPAMmoduleTopComponentTopComponent ipamtc;

    IPAMMouduleService(IPAMmoduleTopComponentTopComponent ipamtc) {
        this.ipamtc = ipamtc;
        com = CommunicationsStub.getInstance();
    }
    
    public LocalObjectLight[] getRootChildren(){
        List<LocalObjectLight> rootChildren = com.getPools(null);
        if(rootChildren != null)
            return rootChildren.toArray(new LocalObjectLight[0]);
        else{
            ipamtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return new LocalObjectLight[0];
        }
    }
}
