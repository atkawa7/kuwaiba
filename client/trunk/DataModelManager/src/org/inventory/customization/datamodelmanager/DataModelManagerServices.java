/*
 * Copyright (c) 2013 adrian.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    adrian - initial API and implementation and/or initial documentation
 */
package org.inventory.customization.datamodelmanager;

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Lookup;

/**
 *
 * @author adrian
 */
public class DataModelManagerServices {

    private DataModelManagerTopComponent dmmtc;
    private CommunicationsStub com = CommunicationsStub.getInstance();

    public DataModelManagerServices(DataModelManagerTopComponent dmmtc) {
        this.dmmtc = dmmtc;
    }
    
    public LocalClassMetadataLight[] getRootChildren(){
        List<LocalClassMetadataLight> rootChildren = new ArrayList<LocalClassMetadataLight>(); 
        rootChildren.add(com.getMetaForClass(301, true)); //InventoryObject
        rootChildren.add(com.getMetaForClass(26, true)); //GenericObjectList
    
        if(rootChildren != null){
            return rootChildren.toArray(new LocalClassMetadataLight[0]);
        }
        else{
            NotificationUtil nu = Lookup.getDefault().
                lookup(NotificationUtil.class);
            if (nu == null){
                System.out.println(java.util.ResourceBundle.getBundle("org/inventory/customization/datamodelmanager/Bundle").getString("DBG_CREATION_ERROR")+com.getError());
            }
            else{
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/datamodelmanager/Bundle").getString("LBL_TITLE_CREATION"), NotificationUtil.ERROR, com.getError());
            }
            return null;
        }
    }
}
