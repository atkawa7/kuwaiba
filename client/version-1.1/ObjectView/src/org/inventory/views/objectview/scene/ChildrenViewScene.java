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

package org.inventory.views.objectview.scene;

import org.inventory.core.visual.scene.PhysicalConnectionProvider;
import java.awt.Color;
import java.awt.Point;
import java.io.ByteArrayOutputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.menu.ObjectWidgetMenu;
import org.inventory.core.visual.scene.AbstractConnectionWidget;
import org.inventory.core.visual.scene.AbstractNodeWidget;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveAction;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * This is the main scene for an object's view
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class ChildrenViewScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {
    
    /**
     * The common connection provider
     */
    private PhysicalConnectionProvider myConnectionProvider;
    /**
     * Default control point move action (shared by all connection widgets)
     */
    private CustomMoveControlPointAction moveControlPointAction =
            new CustomMoveControlPointAction(this);
    /**
     * Default add/remove control point action (shared by all connection widgets)
     */
    private CustomAddRemoveControlPointAction addRemoveControlPointAction =
            new CustomAddRemoveControlPointAction(this);
    /**
     * Default move widget action (shared by all connection widgets)
     */
    private CustomMoveAction moveAction = new CustomMoveAction(this);
    /**
     * Popup provider for all nodes and connections
     */
    private PopupMenuProvider defaultPopupMenuProvider;
    
    
    public ChildrenViewScene () {
        interactionLayer = new LayerWidget(this);
        backgroundLayer = new LayerWidget(this);
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        labelsLayer = new LayerWidget(this);
        myConnectionProvider = new PhysicalConnectionProvider(this);
        
        addChild(backgroundLayer);
        addChild(edgeLayer);
        addChild(nodeLayer);
        addChild(labelsLayer);
	addChild(interactionLayer);
        
        getActions().addAction(ActionFactory.createZoomAction());
        getInputBindings ().setZoomActionModifiers(0); //No keystroke combinations
        getActions().addAction(ActionFactory.createPanAction());

        defaultPopupMenuProvider = new ObjectWidgetMenu();
        
        setActiveTool(ACTION_SELECT);
        initSelectionListener();
    }
    
    /**
     * This methods are called if addNode/addEdge instead of "addChild"
     * @param node
     * @return
     */
    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        AbstractNodeWidget widget = new AbstractNodeWidget(this, node);
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(defaultPopupMenuProvider));
        //The order the actions are added to a widget matters, if Select goes
        //after Move, you will need a double click to select the widget
        widget.getActions(ACTION_SELECT).addAction(createSelectAction());
        widget.getActions(ACTION_SELECT).addAction(moveAction);
        widget.getActions(ACTION_CONNECT).addAction(ActionFactory.createConnectAction(interactionLayer, myConnectionProvider));
        nodeLayer.addChild(widget);
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        AbstractConnectionWidget widget = new AbstractConnectionWidget(this, edge);
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(defaultPopupMenuProvider));
        widget.getActions().addAction(createSelectAction());
        widget.getActions().addAction(addRemoveControlPointAction);
        widget.getActions().addAction(moveControlPointAction);
        widget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        widget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        widget.setRouter(RouterFactory.createFreeRouter());
        widget.setToolTipText(edge.toString());
        if (newLineColor != null)
            widget.setLineColor(newLineColor);
        edgeLayer.addChild(widget);
        return widget;
    }

    /**
     * These are called when creating anchors based on the past methods
     * @param edge
     * @param oldSourceNode
     * @param sourceNode
     */
    @Override
    protected void attachEdgeSourceAnchor(LocalObjectLight edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObjectLight edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {
    }

    public LayerWidget getNodesLayer(){
        return nodeLayer;
    }

    public LayerWidget getEdgesLayer(){
        return edgeLayer;
    }
   
    @Override
    public ConnectProvider getConnectProvider(){
        return this.myConnectionProvider;
    }

    @Override
    public void clear(){
        backgroundLayer.removeChildren();
        super.clear();
    }

    @Override
    public byte[] getAsXML() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameView = new QName("view");
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), Constants.VIEW_FORMAT_VERSION));
            
            QName qnameClass = new QName("class");
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters("DefaultView"));
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            
            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            
            for (Widget nodeWidget : nodeLayer.getChildren()) {
                QName qnameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(nodeWidget.getPreferredLocation().x)));
                xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(nodeWidget.getPreferredLocation().y)));
                LocalObjectLight lolNode = (LocalObjectLight) findObject(nodeWidget);
                xmlew.add(xmlef.createAttribute(new QName("class"), lolNode.getClassName()));
                xmlew.add(xmlef.createCharacters(Long.toString(lolNode.getOid())));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            
            for (Widget edgeWidget : edgeLayer.getChildren()) {
                
                AbstractConnectionWidget acwEdge = (AbstractConnectionWidget) edgeWidget;
                if (acwEdge.getSourceAnchor() == null || acwEdge.getTargetAnchor() == null) //This connection is malformed because one of the endpoints does not exist
                    continue;                                                               //probably, it was moved to another parent
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                
                
                LocalObjectLight lolEdge = (LocalObjectLight) findObject(acwEdge);
                xmlew.add(xmlef.createAttribute(new QName("id"), Long.toString(lolEdge.getOid())));
                xmlew.add(xmlef.createAttribute(new QName("class"), lolEdge.getClassName()));
                
                xmlew.add(xmlef.createAttribute(new QName("aside"), Long.toString(((LocalObjectLight) findObject(acwEdge.getSourceAnchor().getRelatedWidget())).getOid())));
                xmlew.add(xmlef.createAttribute(new QName("bside"), Long.toString(((LocalObjectLight) findObject(acwEdge.getTargetAnchor().getRelatedWidget())).getOid())));
                
                for (Point point : acwEdge.getControlPoints()) {
                    QName qnameControlpoint = new QName("controlpoint");
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(point.x)));
                    xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(point.y)));
                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                }
                xmlew.add(xmlef.createEndElement(qnameEdge, null));
            }
            xmlew.add(xmlef.createEndElement(qnameEdges, null));
            
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
        //TODO: Change the view builder for an implementation of this method
    }
    
    @Override
    public Color getConnectionColor(LocalObjectLight theConnection) {
        //TODO: Calculate the connection color here instead of in Utils
        return null;
    }
    
    @Override
    public boolean supportsConnections() {
        return true;
    }

    @Override
    public boolean supportsBackgrounds() {
        return true;
    }
}
