/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.queries.graphical.elements;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.inventory.queries.graphical.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ComponentWidget;

/**
 * A pin representing an attribute. Has a checkbox and a label
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class AttributePinWidget extends VMDPinWidget{
    private JCheckBox insideCheck;
    private JLabel insideLabel;
    private LocalAttributeMetadata myAttribute;

    public AttributePinWidget(QueryEditorScene scene, LocalAttributeMetadata lam,
            String attributeClassName,VMDColorScheme scheme) {
        super(scene,scheme);
        myAttribute = lam;
        insideCheck = new JCheckBox();
        insideCheck.addItemListener((QueryEditorScene)getScene());
        //We set the type of attribute associated to the check so the filter can be created
        insideCheck.putClientProperty("filterType", lam.getType()); //NOI18N
        insideCheck.putClientProperty("attribute", lam); //NOI18N

        //If this attribute is a list type, we save the class name to create
        if (lam.getIsMultiple())
            insideCheck.putClientProperty("className", attributeClassName); //NOI18N
        addChild(new ComponentWidget(getScene(), insideCheck));
        insideLabel = new JLabel(lam.getDisplayName());
        addChild(new ComponentWidget(getScene(), insideLabel));
    }

    public JCheckBox getInsideCheck() {
        return insideCheck;
    }

    public LocalAttributeMetadata getAttribute() {
        return myAttribute;
    }
}
