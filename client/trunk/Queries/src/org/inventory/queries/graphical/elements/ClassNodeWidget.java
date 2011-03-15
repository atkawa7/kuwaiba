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

package org.inventory.queries.graphical.elements;

import java.util.List;
import java.util.Random;
import javax.swing.JCheckBox;
import org.inventory.core.services.api.metadata.LocalAttributeMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.queries.LocalTransientQuery;
import org.inventory.queries.graphical.QueryEditorNodeWidget;
import org.inventory.queries.graphical.QueryEditorScene;
import org.inventory.queries.graphical.elements.filters.SimpleCriteriaNodeWidget;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.widget.Widget;

/**
 * This class represents the nodes that wrap a particular class
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassNodeWidget extends QueryEditorNodeWidget{

    private LocalClassMetadata myClass;

    public ClassNodeWidget(QueryEditorScene scene, LocalClassMetadata lcm,VMDColorScheme scheme) {
        super(scene,scheme);
        this.myClass = lcm;
        setNodeName(lcm.getClassName());
    }
    
    public LocalClassMetadata getWrappedClass() {
        return myClass;
    }

    @Override
    public void build(String id) {
        defaultPinId = "DefaultPin_"+new Random().nextInt(1000);
        ((QueryEditorScene)getScene()).addPin(myClass, defaultPinId);
        for (LocalAttributeMetadata lam : myClass.getAttributes()){
            if (!lam.isVisible())
                continue;
            ((QueryEditorScene)getScene()).addPin(myClass, lam);
        }
    }

    public void setVisibleAttributes(List<String> visibleAttributes){
        for (Widget child : getChildren()){
            if (child instanceof AttributePinWidget){
                if (visibleAttributes.contains(((AttributePinWidget)child).getAttribute().getName()))
                    ((AttributePinWidget)child).getIsVisible().setSelected(true);
            }
        }
    }

    public void setFilteredAttributes(List<String> visibleAttributes, List<Integer> conditions){
        for (Widget child : getChildren()){
            if (child instanceof AttributePinWidget){
                for (int i = 0; i < visibleAttributes.size(); i++)
                    if (visibleAttributes.get(i).equals(((AttributePinWidget)child).getAttribute().getName())){
                        JCheckBox insideCheck = ((AttributePinWidget)child).getInsideCheck();
                        insideCheck.setSelected(true);
                        //If the related node is not a ClassNodeWidget, we set the condition
                        if (!((AttributePinWidget)child).getAttribute().isMultiple()){
                            String filterNodeKey = (String) insideCheck.getClientProperty("related-node");
                            SimpleCriteriaNodeWidget filterNode = (SimpleCriteriaNodeWidget) ((QueryEditorScene)getScene()).findWidget(filterNodeKey); //NOI18N
                            filterNode.setCondition(LocalTransientQuery.Criteria.fromId(conditions.get(i)));
                        }
                        break;
                    }
            }
        }
    }
}
