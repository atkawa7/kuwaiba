/**
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.customization.datamodelmanager.properties;

import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.metadataclassnodes.ClassMetadataNode;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 * Property associate to each class metadata
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ClassMetadataCustomizerNodeProperty  extends PropertySupport.ReadWrite{
    private Object value;
    ClassMetadataNode node;

    public ClassMetadataCustomizerNodeProperty(Object value, ClassMetadataNode node, String name, Class type, String displayName, String shortDescription) {
        super(name, type, displayName, shortDescription);
        this.value = value;
        this.node = node;
    }
            
    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return this.value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        CommunicationsStub com = CommunicationsStub.getInstance();
        LocalClassMetadataLight myClass = ((ClassMetadataNode)node.getParentNode()).getClassMetadata();
        boolean r = false;
        
        if(getName().equals("className")){
        }
        if(getName().equals("displayName")){
        }
        if(getName().equals("description")){
        }
        if(getName().equals("abstract")){
        }
        if(getName().equals("inDesign")){
        }
        if(getName().equals("custom")){
        }
        if(getName().equals("color")){
        }
        if(getName().equals("countable")){
        }
        if(r){
            this.value = t;
            //Refresh the cache
            com.getMetaForClass(myClass.getClassName(), true);
            nu.showSimplePopup("ClassMetadata Property Update", NotificationUtil.INFO, "ClassMetadata modified successfully");
        }
        else{
            nu.showSimplePopup("ClassMetadata Property Update", NotificationUtil.ERROR, com.getError());
        }
    }
    
    @Override
    public boolean canWrite(){
        return true;
    }
}
