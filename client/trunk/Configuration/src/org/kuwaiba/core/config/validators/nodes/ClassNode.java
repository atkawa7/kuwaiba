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

package org.kuwaiba.core.config.validators.nodes;

import java.awt.Color;
import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalValidatorDefinition;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.kuwaiba.core.config.validators.nodes.actions.ValidatorDefinitionsActionFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * A simple node used to represent a class with a set of validator definitions associated to it
 * @author Charles Edward Bedon Cortazar {@literal {@literal <charles.bedon@kuwaiba.org>}}
 */
public class ClassNode extends AbstractNode {
    
    private static Image ICON = Utils.createRectangleIcon(Color.GRAY, 
            Utils.DEFAULT_ICON_WIDTH, Utils.DEFAULT_ICON_HEIGHT);

    public ClassNode(LocalClassMetadataLight classMetadata) {
        super(new ClassNodeChildren(), Lookups.singleton(classMetadata));
        setDisplayName(classMetadata.toString());
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }

    @Override
    public Image getIcon(int type) {
        return ICON;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { ValidatorDefinitionsActionFactory.getAddValidatorDefinitionAction() };
    }
    
    public static class ClassNodeChildren extends Children.Keys <LocalValidatorDefinition> {

        @Override
        public void addNotify() {
            List<LocalValidatorDefinition> validatorDefinitions = CommunicationsStub.getInstance().getValidatorDefinitionsForClass(getNode().getLookup().lookup(LocalClassMetadataLight.class).getClassName());
            
            if (validatorDefinitions == null) {
                setKeys(Collections.EMPTY_LIST);
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
            }
            else 
                setKeys(validatorDefinitions);
        }
        
        @Override
        protected Node[] createNodes(LocalValidatorDefinition key) {
            return new Node[] { new ValidatorDefinitionNode(key) };
        }
    }
}
