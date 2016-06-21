/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.management.software.nodes;

import java.awt.Image;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 * The root of the Software Management module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SoftwareManagerRootNode extends AbstractNode {
    private Image icon;
  
    public SoftwareManagerRootNode(List<LocalObjectLight> softwareAssets) {
        super(new Children.Array());
        icon = ImageUtilities.loadImage("org/kuwaiba/management/software/res/root.png");
        setDisplayName("Root");
        for (LocalObjectLight softwareAsset : softwareAssets)
            getChildren().add(new SoftwareAssetNode[]{new SoftwareAssetNode(softwareAsset)});
    }

    @Override
    public Action[] getActions(boolean context){
        //return new Action[]{new CreateSoftwareAssetAction(this)};
        return new Action[0];
    }
    
    @Override
    public Image getIcon(int i){
        return icon;
    }
    
    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
}
