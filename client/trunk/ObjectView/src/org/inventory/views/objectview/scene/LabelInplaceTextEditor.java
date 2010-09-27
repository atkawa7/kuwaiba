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

import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;

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
        LocalObject lo = Lookup.getDefault().lookup(LocalObject.class);
        lo.setOid(myObject.getOid());
        lo.setLocalObject(myObject.getPackageName()+"."+myObject.getClassName(), new String[] {"name"}, new Object[]{text});

        if (CommunicationsStub.getInstance().saveObject(lo)){
            ((ObjectNodeWidget)widget).setLabel(text);
            myObject.setDisplayName(text);
        }
    }
}
