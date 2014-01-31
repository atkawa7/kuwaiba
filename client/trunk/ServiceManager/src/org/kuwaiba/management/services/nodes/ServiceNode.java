/*
 *  Copyright 2010 - 2014 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * Node representing a customer
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ServiceNode extends AbstractNode {
    private LocalObjectLight service;
    private Image icon;
    
    public ServiceNode(LocalObjectLight service) {
        super(new ServiceChildren(service), Lookups.singleton(service));
        this.service = service;
        icon = ImageUtilities.loadImage("org/kuwaiba/management/services/res/service.png");
    }

    public LocalObjectLight getService() {
        return service;
    }
    
    @Override
    public String getDisplayName(){
        return service.toString();
    }
    
    @Override
    public boolean canRename(){
        return true;
    }
    
    @Override
    public void setName(String newName){
        LocalObject lo = new LocalObject(service.getClassName(), service.getOid(), 
                new String[]{Constants.PROPERTY_NAME}, new String[]{newName});
        if (CommunicationsStub.getInstance().saveObject(lo)){
            this.service.setName(newName);
            setDisplayName(newName);
            fireDisplayNameChange(newName, newName);
        }
        
    }
    
    @Override
    public Image getIcon(int i){
        return icon;
    }
    
    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
}
