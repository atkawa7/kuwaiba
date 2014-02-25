/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import org.inventory.communications.util.Constants;
import org.inventory.core.wizards.physicalconnections.ConnectionWizard;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.views.objectview.ObjectViewTopComponent;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.Lookup;

/**
 * This class controls the physical connections behavior
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PhysicalConnectionProvider implements ConnectProvider {

    /**
     * The color use to draw the connections
     */
    private Color currentLineColor;
    /**
     * Says what button is selected
     */
    private int currentConnectionSelection;
    /**
     * Reference to the common notifier
     */
    private NotificationUtil nu;

    public PhysicalConnectionProvider(){
        this.nu = Lookup.getDefault().lookup(NotificationUtil.class);
    }

    /**
     * Gets the current line color
     * @return
     */
    public Color getCurrentLineColor(){
        if (currentLineColor == null)
            currentLineColor = new Color(0, 0, 0);
        return currentLineColor;
    }

    public void setCurrentLineColor(Color newColor){
        this.currentLineColor = newColor;
    }

    public void setCurrentConnectionSelection(int currentConnectionSelection) {
        this.currentConnectionSelection = currentConnectionSelection;
    }

    @Override
    public boolean isSourceWidget(Widget sourceWidget) {
        return true;
    }

    @Override
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        if (sourceWidget != targetWidget && targetWidget instanceof IconNodeWidget)
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
        String connectionClass;
        int wizardType;
        switch (currentConnectionSelection){
            case ObjectViewTopComponent.CONNECTION_WIRECONTAINER:
                connectionClass = Constants.CLASS_WIRECONTAINER;
                wizardType = ConnectionWizard.WIZARDTYPE_CONTAINERS;
                break;
            case ObjectViewTopComponent.CONNECTION_WIRELESSCONTAINER:
                connectionClass = Constants.CLASS_WIRELESSCONTAINER;
                wizardType = ConnectionWizard.WIZARDTYPE_CONTAINERS;
                break;
            case ObjectViewTopComponent.CONNECTION_ELECTRICALLINK:
                connectionClass = Constants.CLASS_ELECTRICALLINK;
                wizardType = ConnectionWizard.WIZARDTYPE_CONNECTIONS;
                break;
            case ObjectViewTopComponent.CONNECTION_OPTICALLINK:
                connectionClass = Constants.CLASS_OPTICALLINK;
                wizardType = ConnectionWizard.WIZARDTYPE_CONNECTIONS;
                break;
            case ObjectViewTopComponent.CONNECTION_WIRELESSLINK:
                connectionClass = Constants.CLASS_WIRELESSLINK;
                wizardType = ConnectionWizard.WIZARDTYPE_CONNECTIONS;
                break;
            default:
                nu.showSimplePopup("Create Connection", NotificationUtil.ERROR, "No connection type is selected");
                return;
        }

        ConnectionWizard myWizard =new ConnectionWizard(wizardType,((ObjectNodeWidget)sourceWidget).getObject(),
                ((ObjectNodeWidget)targetWidget).getObject(), connectionClass,
                ((ViewScene)sourceWidget.getScene()).getCurrentObject());
        
        myWizard.show();
        if (myWizard.getNewConnection() != null){

            ViewScene scene =(ViewScene)sourceWidget.getScene();
            ObjectConnectionWidget line = (ObjectConnectionWidget)scene.addEdge(myWizard.getNewConnection());

            line.setTargetAnchor(AnchorFactory.createCenterAnchor(targetWidget));
            line.setSourceAnchor(AnchorFactory.createCenterAnchor(sourceWidget));
            
            scene.validate();
            scene.fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGE, "New Connection"));
        }
    }
}
