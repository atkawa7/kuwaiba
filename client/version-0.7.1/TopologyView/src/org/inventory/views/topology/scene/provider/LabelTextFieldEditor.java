/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.inventory.views.topology.scene.provider;

import org.inventory.views.topology.scene.ObjectLabelWidget;
import org.inventory.views.topology.scene.ObjectNodeWidget;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author adrian
 */
public class LabelTextFieldEditor implements TextFieldInplaceEditor {

    @Override
    public boolean isEnabled (Widget widget) {
        return true;
    }

    @Override
    public String getText (Widget widget) {
        if(widget instanceof ObjectLabelWidget){
            return ((ObjectLabelWidget) widget).getLabel ();
        }
        else if(widget instanceof ObjectNodeWidget){
            return ((ObjectNodeWidget) widget).getObject().getName();
        }
        return "";
    }

    @Override
    public void setText (Widget widget, String text) {
        if(widget instanceof ObjectNodeWidget){
            ((ObjectNodeWidget) widget).getObject().setName(text);
            ((ObjectNodeWidget) widget).setLabel(text);
        }
        else{
            ((LabelWidget) widget).setLabel (text);
            ((ObjectLabelWidget) widget).setLabelText(text);
        }
    }

}
