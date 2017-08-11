/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.views.objectview.scene;

import java.awt.Point;
import java.awt.event.ActionEvent;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.inventory.models.physicalconnections.wizards.NewContainerWizard;
import org.inventory.models.physicalconnections.wizards.NewLinkWizard;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.core.visual.configuration.ObjectViewConfigurationObject;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * This class controls the physical connections behavior
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PhysicalConnectionProvider implements ConnectProvider {
    
    private ChildrenViewScene scene;

    public PhysicalConnectionProvider(ChildrenViewScene scene) {
        this.scene = scene;
    }

    @Override
    public boolean isSourceWidget(Widget sourceWidget) {
        return true;
    }

    @Override
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        if (sourceWidget != targetWidget && targetWidget instanceof ObjectNodeWidget)
            return ConnectorState.ACCEPT;

        return ConnectorState.REJECT;
    }

    @Override
    public boolean hasCustomTargetWidgetResolver(Scene scene) {
        return false;
    }

    @Override
    public Widget resolveTargetWidget(Scene scene, Point sceneLocation) {
        return null;
    }

    @Override
    public void createConnection(Widget sourceWidget, Widget targetWidget) {
        ObjectViewConfigurationObject configObject = scene.getConfigObject();
        LocalObjectLight newConnection;
        
        if ((boolean)configObject.getProperty("connectContainer")) {
            NewContainerWizard newContainerWizard = new NewContainerWizard(sourceWidget.getLookup().lookup(ObjectNode.class), 
                    targetWidget.getLookup().lookup(ObjectNode.class), (LocalObjectLight)configObject.getProperty("currentObject"));
            newContainerWizard.show();
            newConnection = newContainerWizard.getNewConnection();
        } else {
            NewLinkWizard newLinkWizard = new NewLinkWizard(sourceWidget.getLookup().lookup(ObjectNode.class), 
                    targetWidget.getLookup().lookup(ObjectNode.class), (LocalObjectLight)configObject.getProperty("currentObject"));
            newLinkWizard.show();
            newConnection = newLinkWizard.getNewConnection();
        }
        
        if (newConnection != null){
            ConnectionWidget line = (ConnectionWidget)scene.addEdge(newConnection);

            line.setTargetAnchor(AnchorFactory.createCenterAnchor(targetWidget));
            line.setSourceAnchor(AnchorFactory.createCenterAnchor(sourceWidget));
            
            scene.validate();
            scene.fireChangeEvent(new ActionEvent(this, AbstractScene.SCENE_CHANGE, "New Connection"));
        }
    }
}