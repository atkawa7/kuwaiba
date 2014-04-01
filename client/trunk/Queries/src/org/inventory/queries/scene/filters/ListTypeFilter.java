/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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

import java.util.List;
import java.util.Random;
import javax.swing.JComboBox;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.queries.LocalTransientQuery;
import org.inventory.queries.scene.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ComponentWidget;

/**
 * This filter is a shortcut for a class filter. It aims to provide an easy way (using a drop-down list)
 * to choose a value for a list type attribute. Example: if you're searching for Router whose vendor is Cisco,
 * you have 2 options: The simplest is using this filter, which present a list of all available vendors (Cisco, Juniper, Huawei) or
 * a class filter, which will let you choose advanced options (i.e. you will be able to filter for all those vendor whose score is greater that 3)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ListTypeFilter extends SimpleCriteriaNodeWidget{

    private JComboBox listItems;
    private LocalClassMetadataLight wrappedClass;

    public ListTypeFilter(QueryEditorScene scene, LocalClassMetadataLight lcml) {
        super(scene);
        this.setNodeName(lcml.getClassName());
        this.setNodeType("Filter");
        this.wrappedClass = lcml;
    }
    
    @Override
    public Object getValue() {
        return listItems.getSelectedItem();
    }

    @Override
    public void build(String id) {
        defaultPinId = "DefaultPin_"+new Random().nextInt(100000);
        //Add the default pin to be used as anchor for all incoming connections
        ((QueryEditorScene)this.getScene()).addPin(wrappedClass, defaultPinId);
        //Add another pin to hold de actual combobox
        VMDPinWidget dummyPin = (VMDPinWidget)((QueryEditorScene)this.getScene()).addPin(wrappedClass, defaultPinId+"_2"); //NOI18N
        if (listItems == null)
            listItems = new JComboBox(new LocalObjectListItem[0]);
        dummyPin.addChild(new ComponentWidget(this.getScene(), listItems));
    }

    public void build(List<LocalObjectListItem> items){
        listItems = new JComboBox(items.toArray());
        build(""); //NOI18N
    }

    @Override
    public LocalTransientQuery.Criteria getCondition() {
        return LocalTransientQuery.Criteria.EQUAL;
    }

    public LocalClassMetadataLight getWrappedClass() {
        return wrappedClass;
    }
}
