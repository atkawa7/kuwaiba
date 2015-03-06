/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.management.services.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.actions.ShowObjectIdAction;
import org.inventory.navigation.applicationnodes.pools.PoolNode;
import org.inventory.navigation.applicationnodes.pools.actions.DeletePoolAction;
import org.kuwaiba.management.services.nodes.actions.CreateServiceAction;
import org.openide.util.ImageUtilities;

/**
 * Represents a pool (a set of services)
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class ServicesPoolNode extends PoolNode {
    
    private static Image icon = ImageUtilities.loadImage("org/kuwaiba/management/services/res/servicesPool.png");
    
    public ServicesPoolNode(LocalObjectLight service) {
        super(service);
        this.object = service;
        setChildren(new ServiceChildren(service));
    }

    @Override
    public String getName(){
        return object.getName() +" ["+java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_SERVICES_POOL")+"]";
    }
    
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{new CreateServiceAction(this), 
            new DeletePoolAction(this),
            new ShowObjectIdAction(object.getOid(), object.getClassName())};
    }
    
    @Override
    public Image getIcon(int i) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
}