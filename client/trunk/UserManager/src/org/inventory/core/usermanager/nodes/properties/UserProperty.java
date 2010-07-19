/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.core.usermanager.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.PropertySupport.ReadWrite;

/**
 * Represents a single user's property
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class UserProperty extends ReadWrite{

    private Object object;

    public UserProperty(String _name,String _displayName,String _toolTextTip,Object _value){
        super(_name,_value.getClass(),_displayName,_toolTextTip);
        this.object = _value;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return object;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canWrite(){
        return true;
    }
}
