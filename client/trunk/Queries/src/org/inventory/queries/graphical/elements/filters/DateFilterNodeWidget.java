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

import java.util.Calendar;
import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.inventory.queries.graphical.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ComponentWidget;

/**
 * Represents a filter for numeric values (integers, floats and longs)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class DateFilterNodeWidget extends SimpleCriteriaNodeWidget{
    
    public DateFilterNodeWidget(QueryEditorScene scene) {
        super(scene);
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void build(String id) {
        setNodeProperties(null, "Numeric", "Filter", null);
        defaultPinId = "DefaultPin_"+new Random().nextInt(1000);
        QueryEditorScene scene = ((QueryEditorScene)getScene());
        VMDPinWidget dummyPin = (VMDPinWidget)scene.addPin(id, defaultPinId);
        dummyPin.addChild(new ComponentWidget(scene, new JComboBox(new Object[]{
                                                Criteria.EQUAL,
                                                Criteria.BETWEEN,
                                                Criteria.GREATER_THAN,
                                                Criteria.LESS_THAN
                          })));
        dummyPin.addChild(new ComponentWidget(scene, new JTextField(Calendar.getInstance().getTime().toString(), 10)));
    }
}
