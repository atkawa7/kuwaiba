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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.views.gis.scene.GISViewScene;
import org.inventory.views.gis.scene.GeoPositionedNodeWidget;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * Action invoked when an element is dragged and dropped on the scene
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AcceptActionProvider implements AcceptProvider{

    /**
     * Reference to the GIS view scene
     */
    private GISViewScene scene;

    public AcceptActionProvider(GISViewScene scene) {
        this.scene = scene;
    }

    @Override
    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
//        if (transferable.isDataFlavorSupported(LocalObjectLight.DATA_FLAVOR)&& scene.hasView()){
            return ConnectorState.ACCEPT;
//        }else return ConnectorState.REJECT_AND_STOP;
    }

    @Override
    public void accept(Widget widget, Point point, Transferable transferable) {
        try {
            LocalObjectLight droppedObject = (LocalObjectLight) transferable.getTransferData(LocalObjectLight.DATA_FLAVOR);
            if (!scene.isNode(droppedObject)){
                Widget newNode = scene.addNode(droppedObject);
//                double[] coordinates = scene.pixelToCoordinate(point);
//                ((GeoPositionedNodeWidget)newNode).setCoordinates(coordinates[0], coordinates[1]);
                newNode.setPreferredLocation(point);
                scene.repaint();
            }else
                JOptionPane.showMessageDialog(null, "The view already contains this object","Error",JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedFlavorException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
