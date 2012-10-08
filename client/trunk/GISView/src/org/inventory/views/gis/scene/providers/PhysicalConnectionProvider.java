/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.views.gis.scene.providers;

import java.awt.Point;
import javax.swing.JOptionPane;
import org.inventory.communications.SharedInformation;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.wizards.physicalconnections.ConnectionWizard;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;

/**
 * Connection provider to the GIS view
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PhysicalConnectionProvider implements ConnectProvider{

    private int currentConnectionSelection;
    private GraphScene currentScene;
    public static final int CONNECTION_WIRECONTAINER = 1;
    public static final int CONNECTION_WIRELESSCONTAINER = 2;
    public static final int CONNECTION_ELECTRICALLINK = 3;
    public static final int CONNECTION_OPTICALLINK = 4;
    public static final int CONNECTION_WIRELESSLINK = 5;

    public PhysicalConnectionProvider(GraphScene currentScene) {
        this.currentScene = currentScene;
    }

    @Override
    public boolean isSourceWidget(Widget sourceWidget) {
        if (sourceWidget instanceof IconNodeWidget)
            return true;
        else
            return false;
    }

    @Override
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        if (isSourceWidget(targetWidget))
            return ConnectorState.ACCEPT;
        else
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
        LocalObjectLight sourceObject = (LocalObjectLight)((GraphScene)sourceWidget.getScene()).findObject(sourceWidget);
        LocalObjectLight targetObject = (LocalObjectLight)((GraphScene)sourceWidget.getScene()).findObject(targetWidget);

        int wizardType;
        switch (currentConnectionSelection){
            case CONNECTION_WIRECONTAINER:
                connectionClass = SharedInformation.CLASS_WIRECONTAINER;
                wizardType = ConnectionWizard.WIZARDTYPE_CONTAINERS;
                break;
            case CONNECTION_WIRELESSCONTAINER:
                connectionClass = SharedInformation.CLASS_WIRELESSCONTAINER;
                wizardType = ConnectionWizard.WIZARDTYPE_CONTAINERS;
                break;
            case CONNECTION_ELECTRICALLINK:
                connectionClass = SharedInformation.CLASS_ELECTRICALLINK;
                wizardType = ConnectionWizard.WIZARDTYPE_CONNECTIONS;
                break;
            case CONNECTION_OPTICALLINK:
                connectionClass = SharedInformation.CLASS_OPTICALLINK;
                wizardType = ConnectionWizard.WIZARDTYPE_CONNECTIONS;
                break;
            case CONNECTION_WIRELESSLINK:
                connectionClass = SharedInformation.CLASS_WIRELESSLINK;
                wizardType = ConnectionWizard.WIZARDTYPE_CONNECTIONS;
                break;
            default:
                JOptionPane.showMessageDialog(null, "No connection type is selected", "New Connection",JOptionPane.ERROR_MESSAGE);
                return;
        }

        ConnectionWizard myWizard =new ConnectionWizard(wizardType,sourceObject,
                targetObject, connectionClass,
                null);

        myWizard.show();

        if (myWizard.getNewConnection() != null){
            ConnectionWidget newEdge = (ConnectionWidget)currentScene.addEdge(myWizard.getNewConnection());
            newEdge.setSourceAnchor(AnchorFactory.createCircularAnchor(sourceWidget, 3));
            newEdge.setTargetAnchor(AnchorFactory.createCircularAnchor(targetWidget, 3));
        }


    }

    public void setCurrentConnectionSelection(int currentConnectionSelection) {
        this.currentConnectionSelection = currentConnectionSelection;
    }
}
