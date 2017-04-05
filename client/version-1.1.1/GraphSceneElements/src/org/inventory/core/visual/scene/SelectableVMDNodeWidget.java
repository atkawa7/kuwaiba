/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.visual.scene;

import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.navigation.applicationnodes.classmetadatanodes.ClassMetadataNode;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.widget.Scene;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SelectableVMDNodeWidget extends VMDNodeWidget {
    private ClassMetadataNode node;
    private Lookup lookup;

    public SelectableVMDNodeWidget(Scene scene, LocalClassMetadata lcm) {
        super(scene);
        LocalClassMetadata thelcm = CommunicationsStub.getInstance().getMetaForClass(lcm.getClassName(), true);
        node = new ClassMetadataNode(thelcm);
        lookup = Lookups.singleton(node);
    }

    public ClassMetadataNode getNode() {
        return node;
    }

    public void setNode(ClassMetadataNode node) {
        this.node = node;
        lookup = Lookups.singleton(node);
    }
    
    @Override
    public Lookup getLookup() {
        return lookup;
    }
}
