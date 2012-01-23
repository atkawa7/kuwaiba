package org.inventory.customization.attributecustomizer.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.customization.attributecustomizer.nodes.AttributeMetadataNode;
import org.inventory.customization.attributecustomizer.nodes.ClassMetadataNode;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 * Property associate to each attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
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
        LocalClassMetadataLight myClass = ((ClassMetadataNode)node.getParentNode()).getObject();
        if(com.setAttributePropertyValue(myClass.getOid(),
                node.getObject().getName(),getName(),t.toString())){
            this.value = t;
            //Refresh the cache
            com.getMetaForClass(myClass.getClassName(), true);
            nu.showSimplePopup("Attribute Property Update", NotificationUtil.INFO, "Attribute modified successfully");
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
