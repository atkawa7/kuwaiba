/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.inventory.communications.core.queries.LocalTransientQuery;
import org.inventory.queries.scene.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ComponentWidget;

/**
 * Represents a filter for string values
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class StringFilterNodeWidget extends SimpleCriteriaNodeWidget{

    protected JTextField insideText;

    public StringFilterNodeWidget(QueryEditorScene scene) {
        super(scene);
        setNodeProperties(null, "String", "Filter", null);
    }

    @Override
    public void build(String id) {
        insideText = new JTextField(10);
        defaultPinId = "DefaultPin_"+new Random().nextInt(1000);
        VMDPinWidget dummyPin = (VMDPinWidget)((QueryEditorScene)getScene()).addPin(id, defaultPinId);
        condition = new JComboBox(new Object[]{
                                                LocalTransientQuery.Criteria.EQUAL,
                                                LocalTransientQuery.Criteria.LIKE
                          });
        dummyPin.addChild(new ComponentWidget(getScene(), condition));
        dummyPin.addChild(new ComponentWidget(getScene(), insideText));
    }

    @Override
    public String getValue() {
        return insideText.getText();
    }
}