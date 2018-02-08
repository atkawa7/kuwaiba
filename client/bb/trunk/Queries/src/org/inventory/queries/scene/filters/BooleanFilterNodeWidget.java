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

package org.inventory.queries.scene.filters;

import java.util.Random;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import org.inventory.communications.core.queries.LocalTransientQuery;
import org.inventory.queries.scene.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ComponentWidget;

/**
 * Represents a filter for boolean values
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class BooleanFilterNodeWidget extends SimpleCriteriaNodeWidget{

    protected JRadioButton trueButton;
    protected JRadioButton falseButton;

    public BooleanFilterNodeWidget(QueryEditorScene scene) {
        super(scene);        
    }

    @Override
    public void build(String id) {
        QueryEditorScene scene = (QueryEditorScene)getScene();
        setNodeProperties(null, "Boolean", "Filter", null);
        defaultPinId = "DefaultPin_"+new Random().nextInt(1000);
        VMDPinWidget dummyPin = (VMDPinWidget)scene.addPin(id, defaultPinId);
        condition = new JComboBox(new Object[]{LocalTransientQuery.Criteria.EQUAL});
        dummyPin.addChild(new ComponentWidget(scene, condition));
        ButtonGroup myGroup = new ButtonGroup();
        trueButton = new JRadioButton("True");
        trueButton.setSelected(true);
        trueButton.setOpaque(false);
        falseButton = new JRadioButton("False");
        falseButton.setOpaque(false);
        myGroup.add(trueButton);
        myGroup.add(falseButton);
        dummyPin.addChild(new ComponentWidget(scene, trueButton));
        dummyPin.addChild(new ComponentWidget(scene, falseButton));
    }

    @Override
    public Object getValue() {
        return trueButton.isSelected()?"true":"false";
    }
}
