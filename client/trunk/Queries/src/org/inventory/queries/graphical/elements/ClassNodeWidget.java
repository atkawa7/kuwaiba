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

import java.awt.Color;
import java.awt.Paint;
import java.util.Random;
import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.queries.graphical.QueryEditorNodeWidget;
import org.inventory.queries.graphical.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDColorScheme;

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
            if (!lam.getIsVisible())
                continue;
            ((QueryEditorScene)getScene()).addPin(myClass, lam);
        }
    }
}
