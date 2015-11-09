/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.queries.scene;

import java.util.List;
import java.util.Random;
import javax.swing.JCheckBox;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.queries.LocalTransientQuery;
import org.inventory.communications.util.Constants;
import org.inventory.queries.scene.QueryEditorScene;
import org.inventory.queries.scene.filters.SimpleCriteriaNodeWidget;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.widget.Widget;

/**
 * This class represents the nodes that wrap a particular class
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ClassNodeWidget extends QueryEditorNodeWidget{

    /**
     * The model object to be represented by this widget
     */
    private LocalClassMetadata myClass;
    private LocalClassMetadataLight myClassLight;
    /**
     * Should this widget show  the <strong>parent</strong> field
     */
    private boolean hasParentField;
    /**
     * Should this widget show  the <strong>id</strong> field
     */
    private boolean hasIdField;

    public ClassNodeWidget(QueryEditorScene scene, LocalClassMetadata lcm, boolean hasParentField, boolean hasIdField, VMDColorScheme scheme) {
        super(scene,scheme);
        this.myClass = lcm;
        this.hasParentField = hasParentField;
        this.hasIdField = hasIdField;
        this.myClassLight = new LocalClassMetadataLight();
        this.myClassLight.setClassName(lcm.getClassName());
        setNodeName(lcm.getClassName());
    }
    
    public LocalClassMetadata getWrappedClass() {
        return myClass;
    }
    
    public LocalClassMetadataLight getWrappedClassLigth() {
        return myClassLight;
    }

    @Override
    public void build(String id) {
        defaultPinId = "DefaultPin_" + new Random().nextInt(1000);
        ((QueryEditorScene)getScene()).addPin(myClass, defaultPinId);

        if (hasParentField){
            LocalAttributeMetadata attributeParent = new LocalAttributeMetadata(0, 
                    Constants.PROPERTY_PARENT,LocalObjectLight.class, null, true, 
                    Constants.MAPPING_MANYTOONE, null);
            ((QueryEditorScene)getScene()).addPin(myClass, attributeParent);
        }

        //We add the attribute "id" manually since it's a special one and it's not in the metadata
        if (hasIdField){
            LocalAttributeMetadata attributeId = new LocalAttributeMetadata(-1, 
                    Constants.PROPERTY_ID,Long.class, null, true, 
                    Constants.MAPPING_PRIMITIVE, null);
            ((QueryEditorScene)getScene()).addPin(myClass, attributeId);
        }

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
                        if (((AttributePinWidget)child).getAttribute().getMapping() != Constants.MAPPING_MANYTOONE){
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
