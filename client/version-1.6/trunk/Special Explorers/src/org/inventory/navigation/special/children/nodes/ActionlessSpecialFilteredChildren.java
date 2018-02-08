/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package org.inventory.navigation.special.children.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Node;

/**
 * The same SpecialChildren, but creates ActionlessSpecialObjectNodes instead of SpecialObjectNodes
 * in addition this children only show the class WireContainer
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ActionlessSpecialFilteredChildren extends SpecialChildren {
    
    /**
     * Used when you want to show only children of a given className for 
     * example is used in the connect physical link to show only the containers
     * and ignore de links
     */
    private String classNameFiltered;
    
    public ActionlessSpecialFilteredChildren(String classNameFiltered) {
        this.classNameFiltered = classNameFiltered;
    }

    public String getChildrenOfClass() {
        return classNameFiltered;
    }
    
    @Override
    public void addNotify(){
        LocalObjectLight parentObject = ((ActionlessSpecialFilteredObjectNode)getNode()).getObject();

        List<LocalObjectLight> specialChildren = CommunicationsStub.getInstance().
                getObjectSpecialChildren(parentObject.getClassName(), parentObject.getOid());
       
        if (specialChildren == null){
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            setKeys(Collections.EMPTY_SET);
        } else {
            List<LocalObjectLight> onlyContainers = new ArrayList<>();
            for (LocalObjectLight specialChild : specialChildren) {
                if(specialChild.getClassName().equals(classNameFiltered))
                    onlyContainers.add(specialChild);
            }
            
            Collections.sort(onlyContainers);
            setKeys(onlyContainers);
        }
    }
    
    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        return new Node[]  { new ActionlessSpecialFilteredObjectNode(key, classNameFiltered) };
    }
}
