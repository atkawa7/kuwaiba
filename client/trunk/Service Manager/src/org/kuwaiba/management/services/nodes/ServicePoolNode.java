/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.actions.ShowMoreInformationAction;
import org.inventory.navigation.pools.nodes.PoolNode;
import org.kuwaiba.management.services.nodes.actions.ServiceManagerActionFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 * Represents a pool (a set of services)
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class ServicePoolNode extends PoolNode {
    
    private static Image icon = ImageUtilities.loadImage("org/kuwaiba/management/services/res/servicePool.png");
    
    public ServicePoolNode(LocalPool service) {
        super(service);
        setChildren(new ServicePoolChildren());
    }

    @Override
    public String getDisplayName(){
        return getPool().getName() +" ["+java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_SERVICES_POOL")+"]";
    }
    
    @Override
    public Action[] getActions(boolean context){
        return new Action[] {
            ServiceManagerActionFactory.getCreateServiceAction(), 
            ServiceManagerActionFactory.getDeleteServicePoolAction(),
            ShowMoreInformationAction.getInstance(getPool().getOid(), getPool().getClassName())
        };
    }
    
    @Override
    public Image getIcon(int i) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
    
    public static class ServicePoolChildren extends Children.Keys<LocalObjectLight> {

        @Override
        public void addNotify() {
            LocalPool servicePool = ((ServicePoolNode)getNode()).getPool();
            List<LocalObjectLight> services = CommunicationsStub.getInstance().getPoolItems(servicePool.getOid());
            if (services == null) {
                setKeys(Collections.EMPTY_SET);
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
            else {
                Collections.sort(services);
                setKeys(services);
            }
        }

        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }

        @Override
        protected Node[] createNodes(LocalObjectLight key) {
            return new Node[] {new ServiceNode(key)};
        }
    }
}