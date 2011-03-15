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
import org.inventory.core.services.api.queries.LocalTransientQuery;
import org.inventory.queries.graphical.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ComponentWidget;

/**
 * Represents a filter for numeric values (integers, floats and longs)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class NumericFilterNodeWidget extends SimpleCriteriaNodeWidget{

    protected JTextField insideText;

    public NumericFilterNodeWidget(QueryEditorScene scene) {
        super(scene);
        setNodeProperties(null, "Numeric", "Filter", null);
    }

    @Override
    public void build(String id) {
        insideText = new JTextField("0", 10);
        defaultPinId = "DefaultPin_"+new Random().nextInt(1000);
        VMDPinWidget dummyPin = (VMDPinWidget)((QueryEditorScene)getScene()).addPin(id, defaultPinId);
        condition = new JComboBox(new Object[]{
                                                LocalTransientQuery.Criteria.EQUAL,
                                                LocalTransientQuery.Criteria.EQUAL_OR_GREATER_THAN,
                                                LocalTransientQuery.Criteria.GREATER_THAN,
                                                LocalTransientQuery.Criteria.EQUAL_OR_LESS_THAN,
                                                LocalTransientQuery.Criteria.LESS_THAN
                          });
        dummyPin.addChild(new ComponentWidget(getScene(), condition));
        dummyPin.addChild(new ComponentWidget(getScene(), insideText));
    }

    @Override
    public String getValue() {
        try {
            Integer.valueOf(insideText.getText());
            Float.valueOf(insideText.getText());
            Long.valueOf(insideText.getText());
        }catch(NumberFormatException ex){
            //In case of a problem, take a default value of 0
            return "0"; //NOI18N
        }
        return insideText.getText();
    }
}
