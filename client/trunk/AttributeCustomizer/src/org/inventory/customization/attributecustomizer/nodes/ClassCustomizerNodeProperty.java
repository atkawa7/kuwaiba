package org.inventory.customization.attributecustomizer.nodes;

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.PropertySupport;

/**
 * Property associate to each attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassCustomizerNodeProperty extends PropertySupport.ReadWrite{
    private Object value;

    public ClassCustomizerNodeProperty(String _name, Object _value, String _displayName,String _toolTextTip) {
        super(_name,_value.getClass(),_displayName,_toolTextTip);
        this.value = _value;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return this.value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        //this.lcm = t;
    }

    @Override
    public boolean canWrite(){
        if (getName().equals("name") || getName().equals("type"))
            return false;
        else
            return true;
    }
}
