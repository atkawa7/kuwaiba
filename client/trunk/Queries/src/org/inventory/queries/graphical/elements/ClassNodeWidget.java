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

import java.util.Random;
import javax.swing.JCheckBox;
import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.queries.graphical.QueryEditorNodeWidget;
import org.inventory.queries.graphical.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ComponentWidget;

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

    public LocalClassMetadata getMyClass() {
        return myClass;
    }

    public void setMyClass(LocalClassMetadata myClass) {
        this.myClass = myClass;
    }

    @Override
    public void build(String id) {
        defaultPinId = "DefaultPin_"+new Random().nextInt(1000);
        ((QueryEditorScene)getScene()).addPin(myClass, defaultPinId);
        for (LocalAttributeMetadata lam : myClass.getAttributes()){
            if (!lam.getIsVisible())
                continue;
            VMDPinWidget newPin = (VMDPinWidget) ((QueryEditorScene)getScene()).addPin(myClass, lam);
            newPin.setPinName(lam.getDisplayName());
            JCheckBox insideCheck = new JCheckBox();
            insideCheck.addItemListener((QueryEditorScene)getScene());
            //We set the type of attribute associated to the check so the filter can be created
            insideCheck.putClientProperty("filterType", lam.getType()); //NOI18N
            insideCheck.putClientProperty("attribute", lam); //NOI18N
            
            //If this attribute is a list type, we save the class name to create
            if (lam.getIsMultiple())
                insideCheck.putClientProperty("className", myClass.getTypeForAttribute(lam.getName())); //NOI18N
            newPin.addChild(new ComponentWidget(getScene(), insideCheck));
        }
    }
}
