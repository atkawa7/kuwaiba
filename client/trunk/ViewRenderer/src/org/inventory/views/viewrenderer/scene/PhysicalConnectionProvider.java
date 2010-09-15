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

package org.inventory.views.viewrenderer.scene;

import java.awt.Color;
import java.awt.Point;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;

/**
 * This class controls the physical connections behavior
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class PhysicalConnectionProvider implements ConnectProvider{

    public static Color COLOR_ELECTRICAL = new Color(255, 102, 0);
    public static Color COLOR_OPTICAL = new Color(0, 128, 0);
    public static Color COLOR_WIRELESS = new Color(102, 0, 128);

    private Color currentLineColor;

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

    @Override
    public boolean isSourceWidget(Widget sourceWidget) {
        return true;
    }

    @Override
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        return sourceWidget != targetWidget && targetWidget instanceof IconNodeWidget? ConnectorState.ACCEPT : ConnectorState.REJECT;
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
        ViewScene scene =(ViewScene)sourceWidget.getScene();
        ConnectionWidget line = new ConnectionWidget(scene);
        
        line.setControlPointShape(PointShape.SQUARE_FILLED_SMALL);
        line.setLineColor(getCurrentLineColor());
        line.setEndPointShape(PointShape.SQUARE_FILLED_SMALL);
        line.setTargetAnchor(AnchorFactory.createRectangularAnchor(targetWidget));
        line.setSourceAnchor(AnchorFactory.createRectangularAnchor(sourceWidget));
        
        scene.getEdgesLayer().addChild(line);
    }

}
