/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.queries.scene.filters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.inventory.communications.core.queries.LocalTransientQuery;
import org.inventory.queries.scene.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ComponentWidget;

/**
 * Represents a filter for numeric values (integers, floats and longs)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DateFilterNodeWidget extends SimpleCriteriaNodeWidget{

    protected JTextField insideText;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public DateFilterNodeWidget(QueryEditorScene scene) {
        super(scene);
    }

    @Override
    public void build(String id) {
        insideText = new JTextField(dateFormat.format(Calendar.getInstance().getTime()), 10);
        setNodeProperties(null, "Date", "Filter", null);
        defaultPinId = "DefaultPin_"+new Random().nextInt(1000);
        QueryEditorScene scene = ((QueryEditorScene)getScene());
        VMDPinWidget dummyPin = (VMDPinWidget)scene.addPin(id, defaultPinId);
        condition = new JComboBox(new Object[]{
                                                LocalTransientQuery.Criteria.EQUAL,
                                                LocalTransientQuery.Criteria.BETWEEN,
                                                LocalTransientQuery.Criteria.GREATER_THAN,
                                                LocalTransientQuery.Criteria.LESS_THAN
                          });
        dummyPin.addChild(new ComponentWidget(scene, condition));
        dummyPin.addChild(new ComponentWidget(scene, insideText));
    }

    @Override
    public String getValue() {
        return insideText.getText();
    }
}