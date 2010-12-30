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

package org.inventory.queries.graphical.elements.filters;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import org.inventory.queries.graphical.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ComponentWidget;

/**
 * Represents a filter for boolean values
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class BooleanFilterNodeWidget extends SimpleCriteriaNodeWidget{
    
    public BooleanFilterNodeWidget(QueryEditorScene scene) {
        super(scene);
        //scene.addNode("NumericFilter"+scene.getChildren().size()); //NOI18N
        setNodeProperties(null, "Numeric", "Filter", null);
        dummyPin = new VMDPinWidget(scene);
        dummyPin.setPinName("Dummy");
        dummyPin.addChild(new ComponentWidget(scene, new JComboBox(new Object[]{Criteria.EQUAL})));
        ButtonGroup myGroup = new ButtonGroup();
        JRadioButton myTrue = new JRadioButton("True");
        JRadioButton myFalse = new JRadioButton("False");
        myGroup.add(myTrue);
        myGroup.add(myFalse);
        dummyPin.addChild(new ComponentWidget(scene, myTrue));
        dummyPin.addChild(new ComponentWidget(scene, myFalse));
    }

    @Override
    public Object getValue() {
        return null;
    }
}
