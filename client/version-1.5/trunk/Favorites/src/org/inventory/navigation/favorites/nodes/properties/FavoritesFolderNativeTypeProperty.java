/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.favorites.nodes.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.inventory.navigation.favorites.nodes.FavoritesFolderNode;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.PropertySupport;

/**
 * Favorites folder name property
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FavoritesFolderNativeTypeProperty extends PropertySupport.ReadWrite {

    private final FavoritesFolderNode bookmarkFolderNode;
    private Object value;
    
    public FavoritesFolderNativeTypeProperty(String name, Class type, String displayName, 
            String shortDescription, FavoritesFolderNode bookmarkFolderNode, Object value) {
        super(name, type, displayName, shortDescription);
        this.bookmarkFolderNode = bookmarkFolderNode;
        this.value = value;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        if (value == null && getValueType() == Boolean.class)
            return false;
        return value;
    }

    @Override
    public void setValue(Object value)  {
        boolean flag = false;
        if (this.getName().equals(Constants.PROPERTY_NAME))
            flag = CommunicationsStub.getInstance().updateFavoritesFolder(bookmarkFolderNode.getFavoritesFolder().getId(), (String) value);

        if (flag) {
            this.value = value;
            if (this.getName().equals(Constants.PROPERTY_NAME)) {
                bookmarkFolderNode.getFavoritesFolder().setName((String) value);
            }
        }
        else {
            NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
    @Override
    public PropertyEditor getPropertyEditor() {
        return super.getPropertyEditor();
    }
    
    @Override
    public boolean canWrite(){
        return !getName().equals(Constants.PROPERTY_CLASSNAME);
    }
}
