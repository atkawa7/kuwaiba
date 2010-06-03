package org.inventory.customization.attributecustomizer.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.customization.attributecustomizer.nodes.AttributeMetadataNode;
import org.inventory.customization.attributecustomizer.nodes.ClassMetadataNode;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 * Property associate to each attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassCustomizerNodeProperty extends PropertySupport.ReadWrite{
    private Object value;
    private AttributeMetadataNode node;

    public ClassCustomizerNodeProperty(String _name, Object _value,
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
        if(com.setAttributePropertyValue(((ClassMetadataNode)node.getParentNode()).getObject().getId(),
                node.getObject().getName(),getName(),t.toString())){
            this.value = t;
            //Refresh the cache
            com.refreshCache(true, false, false, false);
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
