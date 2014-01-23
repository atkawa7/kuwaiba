/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

import java.awt.Dimension;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.util.Constants;
import org.inventory.queries.graphical.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ComponentWidget;

/**
 * A pin representing an attribute. Has a checkbox and a label
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AttributePinWidget extends VMDPinWidget{
    /**
     * Checkbox that enables an attribute to be used as filter
     */
    private JCheckBox insideCheck;
    /**
     * This button is used to indicate if this attribute should be shown as a column 
     * in the result list
     */
    private JToggleButton isVisible;
    private JLabel insideLabel;
    private LocalAttributeMetadata myAttribute;
    private static Icon isVisibleIcon =
            new ImageIcon(AttributePinWidget.class.getResource("/org/inventory/queries/res/eye.png"));
    private static Icon isVisibleIconDeselected =
            new ImageIcon(AttributePinWidget.class.getResource("/org/inventory/queries/res/no-eye.png"));

    protected AttributePinWidget(QueryEditorScene scene, String label, VMDColorScheme scheme){
        super(scene, scheme);
        insideCheck = new JCheckBox();
        insideCheck.addItemListener((QueryEditorScene)getScene());
        insideCheck.setOpaque(false);
        addChild(new ComponentWidget(getScene(), insideCheck));

        isVisible = new JToggleButton(isVisibleIcon);
        isVisible.setPreferredSize(new Dimension(17, 17));
        isVisible.setSelectedIcon(isVisibleIconDeselected);
        isVisible.setRolloverEnabled(false);
        isVisible.setToolTipText("Show/hide this attribute in the query results");
        addChild(new ComponentWidget(scene, isVisible));
        
        insideLabel = new JLabel(label);
        addChild(new ComponentWidget(getScene(), insideLabel));
    }

    public AttributePinWidget(QueryEditorScene scene, LocalAttributeMetadata lam,
            String attributeClassName, VMDColorScheme scheme) {
        this(scene, lam.getDisplayName(), scheme);
        myAttribute = lam;

        //We set the type of attribute associated to the check so the filter can be created
        insideCheck.putClientProperty("attribute", lam); //NOI18N     

        if (lam.getMapping() == Constants.MAPPING_MANYTOONE){ //If this is a list type attribute, force to select the columns
                                  //to be shown manually
            //If this attribute is a list type, we save the class name to create the related extended filter node
            insideCheck.putClientProperty("className", attributeClassName);
            isVisible.setEnabled(false);
            isVisible.setToolTipText("Select the columns for this list type attribute manually (select the checkbox)");
        }
    }

    public JCheckBox getInsideCheck() {
        return insideCheck;
    }

    public JToggleButton getIsVisible(){
        return this.isVisible;
    }

    public LocalAttributeMetadata getAttribute() {
        return myAttribute;
    }
}
