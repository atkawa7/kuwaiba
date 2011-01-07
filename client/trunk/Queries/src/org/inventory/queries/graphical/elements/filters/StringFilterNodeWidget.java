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

import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.inventory.communications.core.queries.LocalQuery;
import org.inventory.queries.graphical.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ComponentWidget;

/**
 * Represents a filter for string values
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class StringFilterNodeWidget extends SimpleCriteriaNodeWidget{
    public StringFilterNodeWidget(QueryEditorScene scene) {
        super(scene);
        //scene.addNode("NumericFilter"+scene.getChildren().size()); //NOI18N
        setNodeProperties(null, "String", "Filter", null);
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void build(String id) {
        defaultPinId = "DefaultPin_"+new Random().nextInt(1000);
        VMDPinWidget dummyPin = (VMDPinWidget)((QueryEditorScene)getScene()).addPin(id, defaultPinId);
        dummyPin.addChild(new ComponentWidget(getScene(), new JComboBox(new Object[]{
                                                LocalQuery.Criteria.EQUAL,
                                                LocalQuery.Criteria.LIKE
                          })));
        dummyPin.addChild(new ComponentWidget(getScene(), new JTextField(10)));
    }
}
