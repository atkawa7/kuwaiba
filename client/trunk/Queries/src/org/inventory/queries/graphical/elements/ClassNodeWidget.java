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

import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.queries.graphical.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;

/**
 * This class represents the nodes that wrap a particular class
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassNodeWidget extends VMDNodeWidget{

    private LocalClassMetadata myClass;

    public ClassNodeWidget(QueryEditorScene scene,LocalClassMetadata lcm) {
        super(scene);
        this.myClass = lcm;
        for (LocalAttributeMetadata lam : myClass.getAttributes()){
            VMDPinWidget myPin = new VMDPinWidget(scene);
            myPin.setPinName(lam.getDisplayName());
            addChild(myPin);
        }
    }

    public LocalClassMetadata getMyClass() {
        return myClass;
    }

    public void setMyClass(LocalClassMetadata myClass) {
        this.myClass = myClass;
    }
}
