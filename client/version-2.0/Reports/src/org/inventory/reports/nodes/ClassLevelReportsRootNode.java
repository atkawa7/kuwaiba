/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.reports.nodes;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 * A simple node representing the root of the class level reports
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ClassLevelReportsRootNode extends AbstractNode {

    private static final Image ICON = ImageUtilities.loadImage("org/inventory/reports/res/class_level_reports_node.png");
    
    public ClassLevelReportsRootNode() {
        super(new ClassLevelReportsRootChildren());
        setDisplayName("Class Level Reports");
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }

    @Override
    public Image getIcon(int type) {
        return ICON;
    }
    
    public static class ClassLevelReportsRootChildren extends Children.Keys<LocalClassMetadataLight> {
        
        @Override
        public void addNotify() {
            List<LocalClassMetadataLight> classMetadata = CommunicationsStub.getInstance().
                    getLightSubclasses(Constants.CLASS_INVENTORYOBJECT, true, true);
            
            if (classMetadata == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
                setKeys(Collections.EMPTY_SET);
            } else {
                setKeys(classMetadata);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }

        @Override
        protected Node[] createNodes(LocalClassMetadataLight key) {
            return new Node[] {new ReportsModuleClassNode(key) };
        }
    }  
}
