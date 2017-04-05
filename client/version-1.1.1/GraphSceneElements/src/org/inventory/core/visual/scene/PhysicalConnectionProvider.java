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

import java.awt.Point;
import java.awt.event.ActionEvent;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.wizards.physicalconnections.ConnectionWizard;
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
    /**
     * A wizard for container connections
     */
    public static int WIZARD_CONTAINER = 1;
    /**
     * A wizard for link connections
     */
    public static int WIZARD_LINK = 2;
    /**
     * The new object will be instance of this class
     */
    private String connectionClass;
    /**
     * What kind of wizard will be launched
     */
    private int wizardType;
    /**
     * Object to be used as parent to the new connections
     */
    private LocalObjectLight currentParentObject;
    
    private AbstractScene<LocalObjectLight, LocalObjectLight> scene;

    public PhysicalConnectionProvider(AbstractScene scene) {
        this.scene = scene;
    }

    public void setConnectionClass(String connectionClass) {
        this.connectionClass = connectionClass;
    }
    
    public void setWizardType (int wizardType){
        this.wizardType = wizardType;
    }

    public void setCurrentParentObject(LocalObjectLight currentParentObject) {
        this.currentParentObject = currentParentObject;
    }

    @Override
    public boolean isSourceWidget(Widget sourceWidget) {
        return true;
    }

    @Override
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        if (sourceWidget != targetWidget && targetWidget instanceof AbstractNodeWidget)
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
        
      
        ConnectionWizard myWizard = new ConnectionWizard(wizardType, (LocalObjectLight)scene.findObject(sourceWidget),
                (LocalObjectLight)scene.findObject(targetWidget), connectionClass, currentParentObject);
        
        myWizard.show();
        
        if (myWizard.getNewConnection() != null){

            ConnectionWidget line = (ConnectionWidget)scene.addEdge(myWizard.getNewConnection());

            line.setTargetAnchor(AnchorFactory.createCenterAnchor(targetWidget));
            line.setSourceAnchor(AnchorFactory.createCenterAnchor(sourceWidget));
            
            scene.validate();
            scene.fireChangeEvent(new ActionEvent(this, AbstractScene.SCENE_CHANGE, "New Connection"));
        }
    }
}