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

package org.inventory.views.objectview.scene;

import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.widget.Widget;

/**
 * The editor for node widget
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LabelInplaceTextEditor implements TextFieldInplaceEditor{

    public LabelInplaceTextEditor() {
    }

    public boolean isEnabled(Widget widget) {
        return true;
    }

    public String getText(Widget widget) {
        return ((ObjectNodeWidget)widget).getObject().getDisplayname();
    }

    public void setText(Widget widget, String text) {
        LocalObjectLight myObject = ((ObjectNodeWidget)widget).getObject();
        myObject.firePropertyChangeEvent(ObjectNode.PROP_NAME, myObject.getDisplayname(), text);
        if (myObject.getDisplayname().equals(text))
            ((ObjectNodeWidget)widget).setLabel(text);
    }
}
