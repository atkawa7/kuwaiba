package org.inventory.customization.attributecustomizer.nodes.reflection;

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.PropertySupport;

/**
 * Representa cada propiedad asociada al metadata de una clase
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
}
