/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.customization.attributecustomizer.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.Constants;
import org.inventory.customization.attributecustomizer.nodes.AttributeMetadataNode;
import org.inventory.customization.attributecustomizer.nodes.ClassMetadataNode;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 * Property associated to each attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AttributeCustomizerNodeProperty extends PropertySupport.ReadWrite {
    private Object value;
    private AttributeMetadataNode node;

    public AttributeCustomizerNodeProperty(String name, Object value,
            String displayName,String toolTextTip, AttributeMetadataNode node) {
        super(name,value.getClass(),displayName,toolTextTip);
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
        LocalClassMetadataLight myClass = ((ClassMetadataNode)node.getParentNode()).getObject();

        if(com.setAttributeProperties(myClass.getOid(), node.getObject().getId(), getName().equals(Constants.PROPERTY_NAME) ? (String)t : null, 
                getName().equals(Constants.PROPERTY_DISPLAYNAME) ? (String)t : null, 
                getName().equals(Constants.PROPERTY_TYPE) ? (String)t : null, 
                getName().equals(Constants.PROPERTY_DESCRIPTION) ? (String)t : null, 
                getName().equals(Constants.PROPERTY_ADMINISTRATIVE) ? (Boolean)t : null,
                getName().equals(Constants.PROPERTY_VISIBLE) ? (Boolean)t : null,
                getName().equals(Constants.PROPERTY_READONLY) ? (Boolean)t : null,
                getName().equals(Constants.PROPERTY_NOCOPY) ? (Boolean)t : null,
                getName().equals(Constants.PROPERTY_UNIQUE) ? (Boolean)t : null)){
            this.value = t;
            //Refresh the cache
            com.getMetaForClass(myClass.getClassName(), true);
        }else
            nu.showSimplePopup("Attribute Property Update", NotificationUtil.ERROR, com.getError());
    }

    @Override
    public boolean canWrite(){
        if (getName().equals("name") || getName().equals("type"))
            return false;
        else
            return true;
    }
}