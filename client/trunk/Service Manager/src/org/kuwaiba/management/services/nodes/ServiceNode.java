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

import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.favorites.actions.AddObjectToFavoritesFolderAction;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.ExecuteClassLevelReportAction;
import org.inventory.navigation.navigationtree.nodes.actions.ShowMoreInformationAction;
import org.kuwaiba.management.services.nodes.actions.ServiceManagerActionFactory;
import org.kuwaiba.management.services.nodes.actions.ShowEndToEndDetailedViewAction;
import org.kuwaiba.management.services.nodes.actions.ShowEndToEndSimpleViewAction;
import org.kuwaiba.management.services.nodes.actions.ShowEndToEndViewAction;
import org.openide.util.Lookup;

/**
 * Node representing a service
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ServiceNode extends ObjectNode {
    
    public ServiceNode(LocalObjectLight service) {
        super(service);
        setChildren(new ServiceChildren());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action [] { 
            ExecuteClassLevelReportAction.getInstance(),
            ServiceManagerActionFactory.getDeleteServiceAction(),
            null,
            Lookup.getDefault().lookup(ShowEndToEndSimpleViewAction.class),
            //Lookup.getDefault().lookup(ShowEndToEndViewAction.class),
            //Lookup.getDefault().lookup(ShowEndToEndDetailedViewAction.class),
            Lookup.getDefault().lookup(AddObjectToFavoritesFolderAction.class),
            null,
            ShowMoreInformationAction.getInstance(getObject().getOid(), getObject().getClassName())
        };        
    }
}
