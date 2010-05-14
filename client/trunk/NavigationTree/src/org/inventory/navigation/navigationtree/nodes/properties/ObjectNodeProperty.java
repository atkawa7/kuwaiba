/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.inventory.navigation.navigationtree.nodes.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectListItem;
import org.openide.nodes.PropertySupport.ReadWrite;
import org.openide.util.Lookup;

/**
 * Provides a valid representation of LocalObjects attributes as Properties,
 * as LocalObject is just a proxy and can't be a bean itself
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectNodeProperty extends ReadWrite{
    private Object value;
    private LocalObject lo;
    private LocalObjectListItem[] list;



    public ObjectNodeProperty(String _name, Class _valueType, Object _value,
            String _displayName,String _toolTextTip, LocalObject _lo) {
        super(_name,_valueType,_displayName,_toolTextTip);
        this.value = _value;
        this.lo = _lo;
    }

    /*
     This constructor is called when the property is a list
     *@param _name
     */
    public ObjectNodeProperty(String _name, Class _valueType, Object _value,
            String _displayName,String _toolTextTip, LocalObjectListItem[] _list, LocalObject _lo) {
        super(_name,_valueType,_displayName,_toolTextTip);
        if (_value != null)
            this.value = _value;
        else
            //If it is a null value, we create a dummy null value from the generic method available in the interface
            this.value = Lookup.getDefault().lookup(LocalObjectListItem.class).getNullValue();
        this.list = _list;
        this.lo = _lo;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return this.value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try{
            LocalObject update = Lookup.getDefault().lookup(LocalObject.class);
            value = t;

            if (t instanceof LocalObjectListItem)
                update.setLocalObject(lo.getClassName(),
                    new String[]{this.getName()}, new Object[]{((LocalObjectListItem)t).getId()});
            else
                update.setLocalObject(lo.getClassName(),
                    new String[]{this.getName()}, new Object[]{t});
            update.setOid(lo.getOid());
            if(!CommunicationsStub.getInstance().saveObject(update))
                System.out.println("[saveObject]: Error "+ CommunicationsStub.getInstance().getError());
            }catch(Exception e){
                e.printStackTrace();
            }
    }

    
    @Override
    public PropertyEditor getPropertyEditor(){
        if (value instanceof LocalObjectListItem)
            return new ItemListPropertyEditor(list);
        else
            return super.getPropertyEditor();
    }
}