/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
 *
 */
package org.inventory.core.templates.layouts.customshapes.nodes;

import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.customshapes.nodes.actions.CreateCustomShapeAction;
import org.openide.nodes.AbstractNode;

/**
 * The root node of the custom shapes list type
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CustomShapeRootNode extends AbstractNode {
    private final LocalClassMetadataLight customShapeClass;
    private static final String ICON_PATH = "org/inventory/core/templates/res/list-type.png";
    
    public CustomShapeRootNode() {
        super(new CustomShapeChildren());        
        customShapeClass = CommunicationsStub.getInstance().getMetaForClass(Constants.CLASS_CUSTOMSHAPE, false);
        
        if (customShapeClass == null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        
        setIconBaseWithExtension(ICON_PATH);
    }
    
    public LocalClassMetadataLight getCustomShapeClass() {
        return customShapeClass;        
    }
    
    @Override
    public String getDisplayName() {
        return "Custom shapes";
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {new CreateCustomShapeAction()};
    }
}
