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

package org.inventory.views.objectview.scene;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.views.LocalEdge;
import org.inventory.connections.physicalconnections.wizards.ConnectionWizard;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.views.objectview.ObjectViewTopComponent;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.Lookup;

/**
 * This class controls the physical connections behavior
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class PhysicalConnectionProvider implements ConnectProvider{

    /**
     * The color use to draw the connections
     */
    private Color currentLineColor;
    /**
     * Says what button is selected
     */
    private int currentConnectionSelection;
    /**
     * Reference to the common CommunicationsStub
     */
    private CommunicationsStub com;
    /**
     * Reference to the common notifier
     */
    private NotificationUtil nu;

    public PhysicalConnectionProvider(){
        this.com = CommunicationsStub.getInstance();
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
        LocalObjectLight myObject = ((ObjectNodeWidget)sourceWidget).getObject();
        switch (currentConnectionSelection){
            case ObjectViewTopComponent.CONNECTION_WIRECONTAINER:
            case ObjectViewTopComponent.CONNECTION_WIRELESSCONTAINER:
                if (com.getMetaForClass(myObject.getClassName(), false).isPhysicalNode())
                    return true;
                break;
            case ObjectViewTopComponent.CONNECTION_ELECTRICALLINK:
            case ObjectViewTopComponent.CONNECTION_OPTICALLINK:
            case ObjectViewTopComponent.CONNECTION_WIRELESSLINK:
                return true;
        }
        return false;
    }

    @Override
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        if (sourceWidget != targetWidget && targetWidget instanceof IconNodeWidget)
            if (isSourceWidget(targetWidget))
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
                connectionClass = LocalEdge.CLASS_WIRECONTAINER;
                wizardType = ConnectionWizard.WIZARDTYPE_CONTAINERS;
                break;
            case ObjectViewTopComponent.CONNECTION_WIRELESSCONTAINER:
                connectionClass = LocalEdge.CLASS_WIRELESSCONTAINER;
                wizardType = ConnectionWizard.WIZARDTYPE_CONTAINERS;
                break;
            case ObjectViewTopComponent.CONNECTION_ELECTRICALLINK:
                connectionClass = LocalEdge.CLASS_ELECTRICALLINK;
                wizardType = ConnectionWizard.WIZARDTYPE_CONNECTIONS;
                break;
            case ObjectViewTopComponent.CONNECTION_OPTICALLINK:
                connectionClass = LocalEdge.CLASS_OPTICALLINK;
                wizardType = ConnectionWizard.WIZARDTYPE_CONNECTIONS;
                break;
            case ObjectViewTopComponent.CONNECTION_WIRELESSLINK:
                connectionClass = LocalEdge.CLASS_WIRELESSLINK;
                wizardType = ConnectionWizard.WIZARDTYPE_CONNECTIONS;
                break;
            default:
                nu.showSimplePopup("Create Connection", NotificationUtil.ERROR, "No connection was selected");
                return;
        }

        ConnectionWizard myWizard =new ConnectionWizard(wizardType,((ObjectNodeWidget)sourceWidget).getObject(),
                ((ObjectNodeWidget)targetWidget).getObject(), connectionClass,
                ((ViewScene)sourceWidget.getScene()).getCurrentObject().getOid());
        
        myWizard.show();
        if (myWizard.getNewConnection() != null){

            ViewScene scene =(ViewScene)sourceWidget.getScene();
            ObjectConnectionWidget line = new ObjectConnectionWidget(scene,
                    myWizard.getNewConnection(), scene.getFreeRouter(), getCurrentLineColor());

            line.setTargetAnchor(AnchorFactory.createRectangularAnchor(targetWidget,true));
            line.setSourceAnchor(AnchorFactory.createRectangularAnchor(sourceWidget,true));

            scene.getEdgesLayer().addChild(line);
            scene.addObject(line.getObject(), line);
            scene.fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGE, "New Connection"));
        }
    }
}
