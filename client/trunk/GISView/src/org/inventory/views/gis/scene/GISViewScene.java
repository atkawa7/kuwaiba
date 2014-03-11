/**
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.views.gis.scene;

import org.inventory.core.visual.widgets.AbstractConnectionWidget;
import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.visual.menu.ObjectWidgetMenu;
import org.inventory.core.visual.widgets.AbstractScene;
import org.inventory.core.visual.widgets.TagLabelWidget;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.views.gis.scene.actions.MapWidgetPanAction;
import org.inventory.views.gis.scene.actions.ZoomAction;
import org.inventory.views.gis.scene.providers.AcceptActionProvider;
import org.inventory.views.gis.scene.providers.PhysicalConnectionProvider;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Scene used by the GISView component
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GISViewScene extends AbstractScene implements Lookup.Provider{
    /**
     * Default coordinates to center the map
     */
    public static final GeoPosition DEFAULT_CENTER_POSITION = new GeoPosition(4.740675, -73.762207);
    /**
     * Default zoom level
     */
    public static final int DEFAULT_ZOOM_LEVEL = 10;
    /**
     * Layer to contain the main map
     */
    private LayerWidget mapLayer;
    /**
     * Layer to contain the nodes (poles, cabinets, etc)
     */
    private LayerWidget nodesLayer;
    /**
     * Layer to contain the connections (containers, links, etc)
     */
    private LayerWidget edgesLayer;
    /**
     * Layer to contain additional labels (free text)
     */
    private LayerWidget labelsLayer;
    /**
     * Layer to contain polylines
     */
    //private LayerWidget polygonsLayer;
    /**
     * Layer to support the creation of connections
     */
    private LayerWidget interactionLayer;
    /**
     * The map panel
     */
    private MapPanel map;
    /**
     * Scene lookup
     */
    private SceneLookup lookup;
    /**
     * Local connect provider
     */
    private PhysicalConnectionProvider connectProvider;
    

    public GISViewScene(MapPanel map) {
        this.map = map;
        mapLayer = new LayerWidget(this);
        nodesLayer = new LayerWidget(this);
        edgesLayer = new LayerWidget(this);
        interactionLayer = new LayerWidget(this);
        labelsLayer = new LayerWidget(this);
        //polygonsLayer = new LayerWidget(this);

        addChild(mapLayer);
        addChild(edgesLayer);
        addChild(nodesLayer);
        addChild(labelsLayer);
        addChild(interactionLayer);
        
        MapComponentWidget mapWidget = new MapComponentWidget(this, map);
        mapLayer.addChild(mapWidget);
        addDependency(mapWidget);
        
        this.lookup = new SceneLookup(Lookup.EMPTY);
        this.connectProvider = new PhysicalConnectionProvider(this);
        this.defaultPopupMenuProvider = new ObjectWidgetMenu();

        addObjectSceneListener(new ObjectSceneListener() {
            @Override
            public void objectAdded(ObjectSceneEvent event, Object addedObject) { }
            @Override
            public void objectRemoved(ObjectSceneEvent event, Object removedObject) {}
            @Override
            public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {}
            @Override
            public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
                if (newSelection.size() == 1)
                    lookup.updateLookup((LocalObjectLight)newSelection.iterator().next());
            }
            @Override
            public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {}
            @Override
            public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {}
            @Override
            public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {}
        }, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        
        //Actions
        getActions().addAction(new ZoomAction());
        getActions().addAction(ActionFactory.createAcceptAction(new AcceptActionProvider(this)));
        getActions().addAction(new MapWidgetPanAction(map, MouseEvent.BUTTON1));
        setActiveTool(AbstractScene.ACTION_SELECT);
        setOpaque(false);
    }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        GeoPositionedNodeWidget myWidget =  new GeoPositionedNodeWidget(this,node, 0, 0);
        nodesLayer.addChild(myWidget);
        myWidget.getActions(AbstractScene.ACTION_SELECT).addAction(createSelectAction());
        myWidget.getActions(AbstractScene.ACTION_SELECT).addAction(ActionFactory.createMoveAction());
        myWidget.getActions(AbstractScene.ACTION_CONNECT).addAction(ActionFactory.createConnectAction(interactionLayer, connectProvider));
        myWidget.getActions().addAction(ActionFactory.createPopupMenuAction(defaultPopupMenuProvider));
        TagLabelWidget aLabelWidget = new TagLabelWidget(this, myWidget);
        myWidget.addDependency(aLabelWidget);
        labelsLayer.addChild(aLabelWidget);
        return myWidget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        GeoPositionedConnectionWidget myWidget =  new GeoPositionedConnectionWidget(this, edge);
        edgesLayer.addChild(myWidget);
        myWidget.getActions().addAction(createSelectAction());
        myWidget.getActions().addAction(ActionFactory.createAddRemoveControlPointAction());
        myWidget.getActions().addAction(ActionFactory.createMoveControlPointAction(ActionFactory.createFreeMoveControlPointProvider()));
        myWidget.getActions().addAction(ActionFactory.createPopupMenuAction(defaultPopupMenuProvider));
        myWidget.setStroke(new BasicStroke(2));
        myWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        myWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        myWidget.setRouter(RouterFactory.createFreeRouter());
        myWidget.setToolTipText(edge.toString());
        return myWidget;
    }

    @Override
    protected void attachEdgeSourceAnchor(LocalObjectLight edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObjectLight edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {
    }

    public PhysicalConnectionProvider getConnectProvider() {
        return connectProvider;
    }

    /**
     * Translate a point (Cartesian coordinates) within the map viewport into a GeoPosition object
     * @param point Point to be translated
     * @param zoom the zoom to be used to perform the calculation (note that this might NOT be the current map zoom)
     * @return the resulting coordinates as a pair (latitude, longitude)
     */
    public double[] pixelToCoordinate(Point point, int zoom){
        int currentZoom = map.getMainMap().getZoom();
        map.getMainMap().setZoom(zoom);
        Rectangle realViewport = map.getMainMap().getViewportBounds();
        GeoPosition coordinates = map.getMainMap().getTileFactory().pixelToGeo(
                new Point(point.x + realViewport.x, point.y + realViewport.y), map.getMainMap().getZoom());
        map.getMainMap().setZoom(currentZoom);
        return new double[]{coordinates.getLatitude(), coordinates.getLongitude()};
    }

    /**
     * Translate a point (Cartesian coordinates) within the map viewport into a GeoPosition object using the current zoom level
     * @param point point to be translated
     * @return the resulting coordinates as a pair (latitude, longitude)
     */
    public double[] pixelToCoordinate(Point point){
        return pixelToCoordinate(point, map.getMainMap().getZoom());
    }

    /**
     * Translate a point (Polar coordinates) into a Point object (Cartesian coordinates)  within the map viewport
     * @param latitude latitude
     * @param longitude longitude
     * @param zoom the zoom to be used to perform the calculation (note that this might NOT be the current map zoom)
     * @return the resulting Point object
     */
    public Point coordinateToPixel(double latitude, double longitude, int zoom){
        int currentZoom = map.getMainMap().getZoom();
        map.getMainMap().setZoom(zoom);
        Rectangle realViewport = map.getMainMap().getViewportBounds();
        Point2D point2D = map.getMainMap().getTileFactory().geoToPixel(new GeoPosition(latitude, longitude), zoom);
        map.getMainMap().setZoom(currentZoom);
        return new Point((int)point2D.getX() - realViewport.x, (int)point2D.getY() - realViewport.y);
    }

    @Override
    public Lookup getLookup(){
        return this.lookup;
    }

    /**
     * Zooms in the inner map
     */
    public void zoomIn() {
        int currentZoom = map.getMainMap().getZoom();
        if (currentZoom > map.getMinZoom()){

            for (Widget node : nodesLayer.getChildren()){
                double[] geoControlPoint = pixelToCoordinate(node.getPreferredLocation(), map.getMainMap().getZoom());
                Point newLocation = coordinateToPixel(geoControlPoint[0], geoControlPoint[1], map.getMainMap().getZoom() - 1);
                node.setPreferredLocation(newLocation);
            }

            for (Widget connection : edgesLayer.getChildren()){
                List<Point> controlPoints = ((AbstractConnectionWidget)connection).getControlPoints();
                for (int i = 1; i < controlPoints.size() - 1; i++) {
                    double[] geoControlPoint = pixelToCoordinate(controlPoints.get(i), map.getMainMap().getZoom());
                    Point newLocation = coordinateToPixel(geoControlPoint[0], geoControlPoint[1], map.getMainMap().getZoom() - 1);
                    controlPoints.get(i).x = newLocation.x;
                    controlPoints.get(i).y = newLocation.y;
                }               
            }
            map.getMainMap().setZoom(currentZoom - 1);
        }
    }

    /**
     * Zooms out the inner map
     */
    public void zoomOut() {
        int currentZoom = map.getMainMap().getZoom();
        if (currentZoom < map.getMaxZoom()){

            for (Widget node : nodesLayer.getChildren()){
                double[] geoControlPoint = pixelToCoordinate(node.getPreferredLocation(), map.getMainMap().getZoom());
                Point newLocation = coordinateToPixel(geoControlPoint[0], geoControlPoint[1], map.getMainMap().getZoom() + 1);
                node.setPreferredLocation(newLocation);
            }
            for (Widget connection : edgesLayer.getChildren()){
                List<Point> controlPoints = ((AbstractConnectionWidget)connection).getControlPoints();
                for (int i = 1; i < controlPoints.size() - 1; i++) {
                    double[] geoControlPoint = pixelToCoordinate(controlPoints.get(i), map.getMainMap().getZoom());
                    Point newLocation = coordinateToPixel(geoControlPoint[0], geoControlPoint[1], map.getMainMap().getZoom() + 1);
                    controlPoints.get(i).x = newLocation.x;
                    controlPoints.get(i).y = newLocation.y;
                }               
            }
            map.getMainMap().setZoom(currentZoom + 1);
        }
    }

    /**
     * Cleans up the scene and release resources
     */
    public void clear() {
        List<LocalObjectLight> clonedNodes = new ArrayList(getNodes());
        List<LocalObjectLight> clonedEdges = new ArrayList(getEdges());

        for (LocalObjectLight node : clonedNodes)
            removeNode(node); //RemoveNodeWithEdges didn't work

        for (LocalObjectLight edge : clonedEdges)
            removeEdge(edge);
        
        labelsLayer.removeChildren();
        
        map.setVisible(false);
        map.getMainMap().setCenterPosition(DEFAULT_CENTER_POSITION);
        map.getMainMap().setZoom(DEFAULT_ZOOM_LEVEL);
        
        validate();
        
        //polygonsLayer.removeChildren();
    }

    public byte[] getAsXML() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        WAX xmlWriter = new WAX(bas);
        StartTagWAX mainTag = xmlWriter.start("view");
        mainTag.attr("version", Constants.VIEW_FORMAT_VERSION); //NOI18N
        //TODO: Get the class name from some else
        mainTag.start("class").text("GISView").end();
        mainTag.start("zoom").text(String.valueOf(map.getMainMap().getZoom())).end();
        mainTag.start("center").attr("x", map.getMainMap().getCenterPosition().
                getLongitude()).attr("y", map.getMainMap().getCenterPosition().getLatitude()).end();
        StartTagWAX nodesTag = mainTag.start("nodes");
        for (Widget nodeWidget : nodesLayer.getChildren()){
            double[] geoPosition = pixelToCoordinate(nodeWidget.getPreferredLocation(), map.getMainMap().getZoom());
            nodesTag.start("node").attr("x", geoPosition[1]).
            attr("y", geoPosition[0]).
            attr("class", ((GeoPositionedNodeWidget)nodeWidget).getObject().getClassName()).
            text(String.valueOf(((GeoPositionedNodeWidget)nodeWidget).getObject().getOid())).end();
        }
        nodesTag.end();

        StartTagWAX edgesTag = mainTag.start("edges");
        for (Widget edgeWidget : edgesLayer.getChildren()){
            StartTagWAX edgeTag = edgesTag.start("edge");
            edgeTag.attr("id", ((GeoPositionedConnectionWidget)edgeWidget).getObject().getOid());
            edgeTag.attr("class", ((GeoPositionedConnectionWidget)edgeWidget).getObject().getClassName());
            edgeTag.attr("aside", ((GeoPositionedNodeWidget)((AbstractConnectionWidget)edgeWidget).getSourceAnchor().getRelatedWidget()).getObject().getOid());
            edgeTag.attr("bside", ((GeoPositionedNodeWidget)((AbstractConnectionWidget)edgeWidget).getTargetAnchor().getRelatedWidget()).getObject().getOid());
            for (Point point : ((ConnectionWidget)edgeWidget).getControlPoints()){
                double[] geoPosition = pixelToCoordinate(point, map.getMainMap().getZoom());
                edgeTag.start("controlpoint").attr("x", geoPosition[1]).attr("y", geoPosition[0]).end();
            }
            edgeTag.end();
        }
        edgesTag.end();
        mainTag.end().close();

        /*Comment this out for debugging purposes
        try{
            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/before-to-save_"+Calendar.getInstance().getTimeInMillis()+".xml");
            fos.write(bas.toByteArray());
            fos.close();
        }catch(Exception e){}*/

        return bas.toByteArray();
    }

    public MapPanel getMapPanel(){
        return map;
    }

    public void pan(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0)
            return;
        
        for (Widget node : nodesLayer.getChildren())
            node.setPreferredLocation(new Point(node.getPreferredLocation().x - deltaX, node.getPreferredLocation().y - deltaY));
        
        for (Widget connection : edgesLayer.getChildren()){
            List<Point> controlPoints = ((AbstractConnectionWidget)connection).getControlPoints();
            for (int i = 1; i < controlPoints.size() - 1; i++) {
                controlPoints.get(i).x -= deltaX;
                controlPoints.get(i).y -= deltaY;
            }
        }
        revalidate();
    }
    
    public void toggleLabels(boolean visible){
        labelsLayer.setVisible(visible);
        repaint();
    }
    
    /**
     * Helper class to let us launch a lookup event every time a widget is selected
     */
    private class SceneLookup extends ProxyLookup{

        public SceneLookup(Lookup initialLookup) {
            super(initialLookup);
        }

        public void updateLookup(Lookup newLookup){
            setLookups(newLookup);
        }

        public void updateLookup(LocalObjectLight newElement){
            setLookups(Lookups.singleton(new ObjectNode(newElement)));
        }
    }
    
    /**
     * Inner class to wrap the map panel and handle scene resize/relocation events
     */
    private class MapComponentWidget extends ComponentWidget implements Widget.Dependency {

        public MapComponentWidget(Scene scene, Component component) {
            super(scene, component);
        }
        
        @Override
        public void revalidateDependency() {
            setPreferredBounds(this.getScene().getBounds());
            validate();
        }
    }
}
