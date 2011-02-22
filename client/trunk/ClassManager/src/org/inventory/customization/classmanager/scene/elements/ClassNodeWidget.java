/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.inventory.customization.classmanager.scene.elements;

import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.customization.classmanager.scene.ClassHierarchyScene;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.vmd.VMDNodeWidget;

/**
 * Represents a single class node. This is very similar to that one in the module Queries and I think
 * I will merge them somehow in the future
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ClassNodeWidget extends VMDNodeWidget{
    private LocalClassMetadata wrappedClass;

    public ClassNodeWidget(LocalClassMetadata lcm, ClassHierarchyScene scene, VMDColorScheme scheme) {
        super(scene, scheme);
        this.wrappedClass = lcm;
    }

    public LocalClassMetadata getWrappedClass() {
        return wrappedClass;
    }

    private void setWrappedClass(LocalClassMetadata wrappedClass) {
        this.wrappedClass = wrappedClass;
    }
}
