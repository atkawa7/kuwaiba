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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.widget.FreeConnectionWidget;

/**
 * Extends the functionality of a simple connection widget
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectConnectionWidget extends FreeConnectionWidget implements ActionListener, SelectableWidget{

    /**
     * Some constants
     */
    public static final Color COLOR_WIRE = new Color(255, 0, 0);
    public static final Color COLOR_WIRELESS = new Color(0, 0, 255);
    /**
     * Connection colors
     */
    public static final Color COLOR_ELECTRICALLINK = new Color(255, 102, 0);
    public static final Color COLOR_OPTICALLINK = new Color(0, 128, 0);
    public static final Color COLOR_WIRELESSLINK = new Color(102, 0, 128);

    /**
     * The wrapped business object
     */
    //private LocalObjectLight object;
    private ObjectNode node;

    /**
     * We don't take the router from the scene directly because it's possible that in some
     * cases the caller need to specify other type of routing
     * @param scene
     * @param connection
     * @param router
     */
    public ObjectConnectionWidget(ViewScene scene, LocalObjectLight connection, Router router){
        super(scene);
        this.node = new ObjectNode(connection);
        setToolTipText(connection.toString()); //NOI18N
        setRouter(router);
        setStroke(new BasicStroke(2));
        setLineColor(getConnectionColor(connection.getClassName()));
        setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        getActions().addAction(scene.createSelectAction());
        //I don't like this workaround but it seems to be the only way to realize that 
        //the action has been performed
        scene.getAddRemoveControlPointAction().addActionListener(this);
        getActions().addAction(scene.getAddRemoveControlPointAction());

        scene.getMoveControlPointAction().addActionListener(this);
        getActions().addAction(scene.getMoveControlPointAction());
    }

    public LocalObjectLight getObject() {
        return node.getObject();
    }

    @Override
    public ObjectNode getNode() {
        return node;
    }

    public static Color getConnectionColor(String connectionClass){
        if (connectionClass.equals(Constants.CLASS_ELECTRICALLINK))
            return COLOR_ELECTRICALLINK;
        if (connectionClass.equals(Constants.CLASS_OPTICALLINK))
            return COLOR_OPTICALLINK;
        if (connectionClass.equals(Constants.CLASS_WIRELESSLINK))
            return COLOR_WIRELESSLINK;
        if (connectionClass.equals(Constants.CLASS_WIRECONTAINER))
            return COLOR_WIRE;
        if (connectionClass.equals(Constants.CLASS_WIRELESSCONTAINER))
            return COLOR_WIRELESS;
        return Color.BLACK;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ((ViewScene)getScene()).fireChangeEvent(e);
    }
}
