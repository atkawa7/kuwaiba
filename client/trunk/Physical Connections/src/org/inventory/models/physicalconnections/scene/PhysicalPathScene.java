/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.models.physicalconnections.scene;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Service class for this module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PhysicalPathScene  extends AbstractScene <LocalObjectLight, LocalObjectLight>{
    public static final int X_OFFSET = 50;
    private Router router;

    public PhysicalPathScene() {       
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        router = RouterFactory.createOrthogonalSearchRouter(nodeLayer);
        nodeLayer.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 50));
        addChild(nodeLayer);
        addChild(edgeLayer);
    }
    
    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        Color randomColor = ObjectBoxWidget.colorPalette[new Random().nextInt(12)];
        Widget widget = new ObjectBoxWidget(this, node, randomColor);
        widget.getActions().addAction(createSelectAction());
        initSelectionListener();
        widget.repaint();
        widget.revalidate();
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        SimpleConnectionWidget widget = new SimpleConnectionWidget(this, edge, Color.BLUE);
        widget.getActions().addAction(createSelectAction());
        widget.setStroke(new BasicStroke(1));
        edgeLayer.addChild(widget);
        return widget;
    }
    
    public void addRootWidget (Widget widget){
        widget.getActions().addAction(ActionFactory.createMoveAction());
        nodeLayer.addChild(widget);
    }

    public Router getRouter() {
        return router;
    }
    
    public void organizeNodes() {
        int x = 10;
        for (Widget child : nodeLayer.getChildren()){
            child.resolveBounds (new Point (x, 10), new Rectangle (child.getPreferredBounds().x, 
                    child.getPreferredBounds().y, child.getPreferredBounds().width, child.getPreferredBounds().height));
            x += child.getPreferredBounds().width + X_OFFSET;
        }
    }

    @Override
    public byte[] getAsXML() {
        //For now
        return null;
    }
    
    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
        //TODO: Render here, not in the service
    }

    @Override
    public ConnectProvider getConnectProvider() {
        return null;
    }
    
    @Override
    public boolean supportsConnections() {
        return false;
    }

    @Override
    public boolean supportsBackgrounds() {
        return false;
    }

    @Override
    public void render(LocalObjectLight root) {
    }
}
