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

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import org.inventory.connections.physicalconnections.wizards.PhysicalConnectionWizardWizardAction;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.connections.LocalPhysicalConnection;
import org.inventory.communications.core.views.LocalEdge;
import org.inventory.core.services.interfaces.LocalObject;
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
     * What are we trying to create
     */
    private String connectionClass;
    /**
     * Reference to the common CommunicationsStub
     */
    private CommunicationsStub com;
    /**
     * Reference to the common notifier
     */
    private NotificationUtil nu;
    /**
     * Action to be called when a physical connection is requested
     */
    private PhysicalConnectionWizardWizardAction physicalConnectAction;

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

    public void setConnectionClass(String connectionClass) {
        this.connectionClass = connectionClass;
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
        LocalObject myConnection = null;
        

        switch (currentConnectionSelection){
            case ObjectViewTopComponent.CONNECTION_WIRECONTAINER:
            case ObjectViewTopComponent.CONNECTION_WIRELESSCONTAINER:
                myConnection = com.createPhysicalContainerConnection(
                    ((ObjectNodeWidget)sourceWidget).getObject().getOid(),
                    ((ObjectNodeWidget)targetWidget).getObject().getOid(),
                    currentConnectionSelection == ObjectViewTopComponent.CONNECTION_WIRECONTAINER ? LocalEdge.CLASSNAME_WIRECONTAINER:LocalEdge.CLASSNAME_WIRELESSCONTAINER,
                    ((ViewScene)((ObjectNodeWidget)sourceWidget).getScene()).getCurrentObject().getOid());
                break;
            case ObjectViewTopComponent.CONNECTION_ELECTRICALLINK:
            case ObjectViewTopComponent.CONNECTION_OPTICALLINK:
            case ObjectViewTopComponent.CONNECTION_WIRELESSLINK:
                if (physicalConnectAction == null)
                    physicalConnectAction = new PhysicalConnectionWizardWizardAction(((ObjectNodeWidget)sourceWidget).getObject(),
                                                            ((ObjectNodeWidget)targetWidget).getObject(),
                                                            connectionClass);
                else{
                    physicalConnectAction.setASide(((ObjectNodeWidget)sourceWidget).getObject());
                    physicalConnectAction.setBSide(((ObjectNodeWidget)targetWidget).getObject());
                    physicalConnectAction.setConnectionClass(connectionClass);
                }
                physicalConnectAction.actionPerformed(new ActionEvent(this, 1, "create"));
                return;
        }


        if (myConnection != null){
            ViewScene scene =(ViewScene)sourceWidget.getScene();
            ObjectConnectionWidget line = new ObjectConnectionWidget(scene,myConnection);

            line.setControlPointShape(PointShape.SQUARE_FILLED_SMALL);
            line.setLineColor(getCurrentLineColor());
            line.setEndPointShape(PointShape.SQUARE_FILLED_SMALL);
            line.setTargetAnchor(AnchorFactory.createRectangularAnchor(targetWidget,true));
            line.setSourceAnchor(AnchorFactory.createRectangularAnchor(sourceWidget,true));

            scene.getEdgesLayer().addChild(line);
        }else
            nu.showSimplePopup("New Connection", NotificationUtil.ERROR, com.getError());
    }
}
