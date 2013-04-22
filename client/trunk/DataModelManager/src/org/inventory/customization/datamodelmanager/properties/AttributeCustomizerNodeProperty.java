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
import org.inventory.customization.datamodelmanager.nodes.AttributeMetadataNode;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 * Property associate to each attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AttributeCustomizerNodeProperty extends PropertySupport.ReadWrite{
    private Object value;
    private AttributeMetadataNode node;

    public AttributeCustomizerNodeProperty(String _name, Object _value,
            String _displayName,String _toolTextTip, AttributeMetadataNode _node) {
        super(_name,_value.getClass(),_displayName,_toolTextTip);
        this.value = _value;
        this.node = _node;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return this.value;
    }
    

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        CommunicationsStub com = CommunicationsStub.getInstance();
//        LocalClassMetadataLight myClass = ((ClassMetadataNode)node.getParentNode()).getObject();
//        boolean r = false;
//
//        if(getName().equals("displayName"))
//            r = com.setAttributePropertyValue(myClass.getOid(), node.getObject().getName(), t.toString(), null, null, false, node.getObject().isVisible(), node.getObject().getMapping(), false, false);
//        if(getName().equals("isVisible"))
//            r = com.setAttributePropertyValue(myClass.getOid(), node.getObject().getName(), null, null, null, false, Boolean.valueOf(t.toString()), node.getObject().getMapping(), false, false);
//        if(getName().equals("administrative"))
//            r = com.setAttributePropertyValue(myClass.getOid(), node.getObject().getName(), null, null, null, Boolean.valueOf(t.toString()), node.getObject().isVisible(), node.getObject().getMapping(), false, false);
//        if(getName().equals("description"))
//            r = com.setAttributePropertyValue(myClass.getOid(), node.getObject().getName(), null, null, t.toString(), false, node.getObject().isVisible(), node.getObject().getMapping(), false, false);
//
//        if(r){
//            this.value = t;
//            //Refresh the cache
//            com.getMetaForClass(myClass.getClassName(), true);
//            nu.showSimplePopup("Attribute Property Update", NotificationUtil.INFO, "Attribute modified successfully");
//        }else
//            nu.showSimplePopup("Attribute Property Update", NotificationUtil.ERROR, com.getError());
    }

    @Override
    public boolean canWrite(){
        if (getName().equals("name") || getName().equals("type"))
            return false;
        else
            return true;
    }
}
