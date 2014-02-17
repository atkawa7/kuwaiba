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

package org.inventory.views.gis.scene;

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import java.awt.BasicStroke;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.views.gis.scene.actions.MapWidgetPanAction;
import org.inventory.views.gis.scene.actions.MoveAction;
import org.inventory.views.gis.scene.actions.ZoomAction;
import org.inventory.views.gis.scene.providers.AcceptActionProvider;
import org.inventory.views.gis.scene.providers.PhysicalConnectionProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Scene used by the GISView component
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GISViewScene extends GraphScene<LocalObjectLight, LocalObjectLight> implements PropertyChangeListener, Lookup.Provider{

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void attachEdgeSourceAnchor(LocalObjectLight edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObjectLight edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    /**
//     * Default node icon path
//     */
//    private final String GENERIC_ICON_PATH="org/inventory/views/gis/res/default.png"; //NOI18
//    /**
//     * Icon radius
//     */
//    private final int ICON_RADIUS = 8;
//    /**
//     * Default coordinates to center the map
//     */
//    public final GeoPosition DEFAULT_CENTER_POSITION = new GeoPosition(2.451627, -76.624424);
//    /**
//     * Default icon
//     */
//    private final Image defaultIcon = ImageUtilities.loadImage(GENERIC_ICON_PATH);
//    /**
//     * Layer to contain the map and its additional components
//     */
//    private LayerWidget mapLayer;
//    /**
//     * Layer to contain the nodes (poles, cabinets, etc)
//     */
//    private LayerWidget nodesLayer;
//    /**
//     * Layer to contain the connections (containers, links, etc)
//     */
//    private LayerWidget connectionsLayer;
//    /**
//     * Layer to contain additional labels (free text)
//     */
//    private LayerWidget labelsLayer;
//    /**
//     * Layer to contain cosmetic polygons
//     */
//    private LayerWidget polygonsLayer;
//    /**
//     * The widget to contain the map component
//     */
//    private ComponentWidget mapWidget;
//    /**
//     * Scene lookup
//     */
//    private SceneLookup lookup;
//    /**
//     * Local connect provider
//     */
//    private PhysicalConnectionProvider connectProvider;
//
//    public GISViewScene() {
//        
//        mapLayer = new LayerWidget(this);
//        nodesLayer = new LayerWidget(this);
//        connectionsLayer = new LayerWidget(this);
//        labelsLayer = new LayerWidget(this);
//        polygonsLayer = new LayerWidget(this);
//
//        addChild(mapLayer);
//        addChild(connectionsLayer);
//        addChild(nodesLayer);
//
//        this.lookup = new SceneLookup(Lookup.EMPTY);
//        this.connectProvider = new PhysicalConnectionProvider(this);
//        
//        
//
//        addObjectSceneListener(new ObjectSceneListener() {
//            @Override
//            public void objectAdded(ObjectSceneEvent event, Object addedObject) { }
//            @Override
//            public void objectRemoved(ObjectSceneEvent event, Object removedObject) {}
//            @Override
//            public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {}
//            @Override
//            public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
//                if (newSelection.size() == 1)
//                    lookup.updateLookup((LocalObjectLight)newSelection.iterator().next());
//            }
//            @Override
//            public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {}
//            @Override
//            public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {}
//            @Override
//            public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {}
//        }, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
//        //Actions
//        getActions().addAction(new ZoomAction());
//        getActions().addAction(ActionFactory.createAcceptAction(new AcceptActionProvider(this)));
//
//        setActiveTool(ObjectNodeWidget.ACTION_SELECT);
//    }
//
//    @Override
//    protected Widget attachNodeWidget(LocalObjectLight node) {
//        GeoPositionedNodeWidget myWidget =  new GeoPositionedNodeWidget(this,node, 0, 0);
//        nodesLayer.addChild(myWidget);
//        LocalClassMetadataLight classInfo = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);
//        myWidget.setImage(classInfo.getSmallIcon() == null ? defaultIcon : classInfo.getSmallIcon());
//        myWidget.getActions(ObjectNodeWidget.ACTION_SELECT).addAction(createSelectAction());
//        myWidget.getActions(ObjectNodeWidget.ACTION_SELECT).addAction(new MoveAction());
//        myWidget.getActions(ObjectNodeWidget.ACTION_CONNECT).addAction(ActionFactory.createConnectAction(connectionsLayer, connectProvider));
//        myWidget.setToolTipText(node.getName() + " [" + node.getClassName() + "]");
//        myWidget.setLabel(myWidget.getToolTipText());
//        return myWidget;
//    }
//
//    @Override
//    protected Widget attachEdgeWidget(LocalObjectLight edge) {
//        GeoPositionedConnectionWidget myWidget =  new GeoPositionedConnectionWidget(this, edge);
//        connectionsLayer.addChild(myWidget);
//        myWidget.getActions().addAction(createSelectAction());
//        myWidget.getActions().addAction(ActionFactory.createAddRemoveControlPointAction());
//        myWidget.getActions().addAction(ActionFactory.createMoveControlPointAction(ActionFactory.createFreeMoveControlPointProvider()));
//        myWidget.setStroke(new BasicStroke(3));
//        myWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
//        myWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
//        myWidget.setRouter(RouterFactory.createFreeRouter());
//        myWidget.setToolTipText(edge.getName() + " [" + edge.getClassName() + "]");
//        return myWidget;
//    }
//
//    @Override
//    protected void attachEdgeSourceAnchor(LocalObjectLight edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {
//    }
//
//    @Override
//    protected void attachEdgeTargetAnchor(LocalObjectLight edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {
//    }
//
//    /**
//     * This method adds the map to the scene. Due to the nature of the JXMapViewer component, The map is built 
//     * when the component is painted. If there are network problems, you could get some nasty exceptions.
//     */
//    public void activateMap(){
//        if (mapWidget == null){
//            MapPanel myMap = new MapPanel();
//            myMap.setProvider(MapPanel.Providers.OSM);
//            myMap.getMainMap().setCenterPosition(DEFAULT_CENTER_POSITION);
//            myMap.addPropertyChangeListener("painted", this);
//            mapWidget = new ComponentWidget(this, myMap);
//
//            mapWidget.getActions().addAction(new MapWidgetPanAction(myMap, MouseEvent.BUTTON1));
//        }
//        mapLayer.addChild(mapWidget);
//        ((MapPanel)mapWidget.getComponent()).getMainMap().setZoom(MapPanel.DEFAULT_ZOOM_LEVEL);
//        ((MapPanel)mapWidget.getComponent()).getMainMap().setCenterPosition(DEFAULT_CENTER_POSITION);
//        updateMapBounds();
//    }
//
//    /**
//     * Updates the map widget bounds to fit the container's ones
//     */
//    public void updateMapBounds() {
//        if (mapWidget != null){
//            mapWidget.setPreferredSize(this.getBounds().getSize());
//            validate();
//        }
//    }
//
//    /**
//     * Called when the map is repainted and the nodes must be updated depending on the zoom
//     * @param evt
//     */
//    @Override
//    public void propertyChange(PropertyChangeEvent evt) {
//        //repaint();
//        /*JXMapViewer map = ((MapPanel)mapWidget.getComponent()).getMainMap();
//        Rectangle realViewport = map.getViewportBounds();
//
//        for (Widget node : nodesLayer.getChildren()){
//            Point2D point2D = map.getTileFactory().geoToPixel(
//                    new GeoPosition(((GeoPositionedNodeWidget)node).getLatitude(), ((GeoPositionedNodeWidget)node).getLongitude()),
//                    map.getZoom());
//
//            int newX = (int)point2D.getX() - realViewport.x, newY = (int)point2D.getY() - realViewport.y;
//            if (newX < 0 || newY < 0)
//                node.setVisible(false);
//            else
//                node.setVisible(true);
//            node.setPreferredLocation(new Point(newX - ICON_RADIUS, newY - ICON_RADIUS));
////            System.out.println(((GeoPositionedNodeWidget)node).getLatitude() + ", " + ((GeoPositionedNodeWidget)node).getLongitude() +
////                    " ("+newX+", "+newY+")");
//        }
//
//        for (Widget edge : connectionsLayer.getChildren()){
//
//            if (!(edge instanceof GeoPositionedConnectionWidget)) //For some reason, the edges created during the connection process are placed on this layer
//                continue;
//
//            boolean visible = true;
//
//            if (!((ConnectionWidget)edge).getSourceAnchor().getRelatedWidget().isVisible() || !((ConnectionWidget)edge).getTargetAnchor().getRelatedWidget().isVisible())
//                visible = false;
//            else{
////                List<Point> newControlPoints = new ArrayList<Point>();
////                for (double[] controlPoint : ((GeoPositionedConnectionWidget)edge).getGeoPositionedControlPoints()){
////                    Point2D point2D = map.getTileFactory().geoToPixel(new GeoPosition(controlPoint[0], controlPoint[1]), map.getZoom());
////
////                    int newX = (int)point2D.getX() - realViewport.x, newY = (int)point2D.getY() - realViewport.y;
////                    if (newX <= 0 || newY <= 0)
////                        visible = false;
////
////                    newControlPoints.add(new Point(newX, newY));
////                    System.out.println(controlPoint[0] + ", " + controlPoint[1] +
////                    " ("+newX+", "+newY+")");
////                }
////
////                if (!newControlPoints.isEmpty())
////                    ((GeoPositionedConnectionWidget)edge).setControlPoints(newControlPoints);
//                //In the meantime...
//                for (Point cPoint : ((ConnectionWidget)edge).getControlPoints()){
//                    if (cPoint.x <= 0 || cPoint.y <= 0)
//                        visible = false;
//                }
//            }
//            
//            edge.setVisible(visible);            
//        }
//
//        validate();
//        repaint();*/
//        validate();
//    }
//
//    public PhysicalConnectionProvider getConnectProvider() {
//        return connectProvider;
//    }
//
//    /**
//     * Translate a point (Cartesian coordinates) within the map viewport into a GeoPosition object
//     * @param point Point to be translated
//     * @param zoom the zoom to be used to perform the calculation (note that this might NOT be the current map zoom)
//     * @return the resulting coordinates as a pair (latitude, longitude)
//     */
//    public double[] pixelToCoordinate(Point point, int zoom){
//        JXMapViewer map = ((MapPanel)mapWidget.getComponent()).getMainMap();
//        int currentZoom = map.getZoom();
//        map.setZoom(zoom);
//        Rectangle realViewport = map.getViewportBounds();
//        GeoPosition coordinates = map.getTileFactory().pixelToGeo(new Point(point.x + realViewport.x, point.y + realViewport.y), map.getZoom());
//        map.setZoom(currentZoom);
//        return new double[]{coordinates.getLatitude(), coordinates.getLongitude()};
//    }
//
//    /**
//     * Translate a point (Cartesian coordinates) within the map viewport into a GeoPosition object using the current zoom level
//     * @param point point to be translated
//     * @return the resulting coordinates as a pair (latitude, longitude)
//     */
//    public double[] pixelToCoordinate(Point point){
//        return pixelToCoordinate(point, ((MapPanel)mapWidget.getComponent()).getMainMap().getZoom());
//    }
//
//    /**
//     * Translate a point (Polar coordinates) into a Point object (Cartesian coordinates)  within the map viewport
//     * @param latitude latitude
//     * @param longitude longitude
//     * @param zoom the zoom to be used to perform the calculation (note that this might NOT be the current map zoom)
//     * @return the resulting Point object
//     */
//    public Point coordinateToPixel(double latitude, double longitude, int zoom){
//        JXMapViewer map = ((MapPanel)mapWidget.getComponent()).getMainMap();
//        int currentZoom = map.getZoom();
//        map.setZoom(zoom);
//        Rectangle realViewport = map.getViewportBounds();
//        Point2D point2D = map.getTileFactory().geoToPixel(new GeoPosition(latitude, longitude), zoom);
//        map.setZoom(currentZoom);
//        return new Point((int)point2D.getX() - realViewport.x, (int)point2D.getY() - realViewport.y);
//    }
//
//    @Override
//    public Lookup getLookup(){
//        return this.lookup;
//    }
//
//    /**
//     * Zooms in the inner map
//     */
//    public void zoomIn() {
//        MapPanel mapComponent = (MapPanel)mapWidget.getComponent();
//        int currentZoom = mapComponent.getMainMap().getZoom();
//        if (currentZoom > mapComponent.getMinZoom()){
//
//            for (Widget node : nodesLayer.getChildren()){
//                double[] geoControlPoint = pixelToCoordinate(node.getPreferredLocation(), mapComponent.getMainMap().getZoom());
//                Point newLocation = coordinateToPixel(geoControlPoint[0], geoControlPoint[1], mapComponent.getMainMap().getZoom() - 1);
//                node.setPreferredLocation(newLocation);
//            }
//
//            for (Widget edge : connectionsLayer.getChildren()){
//                List<Point> newControlPoints = new ArrayList<Point>();
//                for (Point oldControlPoint : ((GeoPositionedConnectionWidget)edge).getControlPoints()){
//                    double[] geoControlPoint = pixelToCoordinate(oldControlPoint, mapComponent.getMainMap().getZoom());
//                    newControlPoints.add(coordinateToPixel(geoControlPoint[1], geoControlPoint[0], mapComponent.getMainMap().getZoom() - 1));
//                }
//                if (!newControlPoints.isEmpty())
//                    ((GeoPositionedConnectionWidget)edge).setControlPoints(newControlPoints, false);
//            }
//
//            mapComponent.getMainMap().setZoom(currentZoom - 1);
//        }else
//            JOptionPane.showMessageDialog(null, "The maximum zoom level has been reached");
//    }
//
//    /**
//     * Zooms out the inner map
//     */
//    public void zoomOut() {
//        MapPanel mapComponent = (MapPanel)mapWidget.getComponent();
//        int currentZoom = mapComponent.getMainMap().getZoom();
//        if (currentZoom < mapComponent.getMaxZoom()){
//
//            for (Widget node : nodesLayer.getChildren()){
//                double[] geoControlPoint = pixelToCoordinate(node.getPreferredLocation(), mapComponent.getMainMap().getZoom());
//                Point newLocation = coordinateToPixel(geoControlPoint[0], geoControlPoint[1], mapComponent.getMainMap().getZoom() + 1);
//                node.setPreferredLocation(newLocation);
//            }
//            for (Widget edge : connectionsLayer.getChildren()){
//                List<Point> newControlPoints = new ArrayList<Point>();
//                for (Point oldControlPoint : ((GeoPositionedConnectionWidget)edge).getControlPoints()){
//                    double[] geoControlPoint = pixelToCoordinate(oldControlPoint);
//                    newControlPoints.add(coordinateToPixel(geoControlPoint[1], geoControlPoint[0], mapComponent.getMainMap().getZoom() + 1));
//                }
//                if (!newControlPoints.isEmpty())
//                    ((GeoPositionedConnectionWidget)edge).setControlPoints(newControlPoints, false);
//            }
//
//            mapComponent.getMainMap().setZoom(currentZoom + 1);
//        }
//        else
//            JOptionPane.showMessageDialog(null, "The minimum zoom level has been reached");
//    }
//
//    /**
//     * Cleans up the scene and release resources
//     */
//    public void clear() {
//        List<LocalObjectLight> clonedNodes = new ArrayList(getNodes());
//        List<LocalObjectLight> clonedEdges = new ArrayList(getEdges());
//
//        for (LocalObjectLight node : clonedNodes)
//            removeNode(node); //RemoveNodeWithEdges didn't work
//
//        for (LocalObjectLight edge : clonedEdges)
//            removeEdge(edge);
//        
//        mapLayer.removeChildren();
//        labelsLayer.removeChildren();
//        polygonsLayer.removeChildren();
//    }
//
//    public byte[] getAsXML() {
//
//        MapPanel mapComponent = ((MapPanel)mapWidget.getComponent());
//
//        ByteArrayOutputStream bas = new ByteArrayOutputStream();
//        WAX xmlWriter = new WAX(bas);
//        StartTagWAX mainTag = xmlWriter.start("view");
//        mainTag.attr("version", Constants.VIEW_FORMAT_VERSION); //NOI18N
//        //TODO: Get the class name from some else
//        mainTag.start("class").text("GISView").end();
//        mainTag.start("zoom").text(String.valueOf(mapComponent.getMainMap().getZoom())).end();
//        mainTag.start("center").attr("x", mapComponent.getMainMap().getCenterPosition().getLongitude()).attr("y", mapComponent.getMainMap().getCenterPosition().getLatitude()).end();
//        StartTagWAX nodesTag = mainTag.start("nodes");
//        for (Widget nodeWidget : nodesLayer.getChildren())
//            nodesTag.start("node").attr("x", ((GeoPositionedNodeWidget)nodeWidget).getLongitude()).
//            attr("y", ((GeoPositionedNodeWidget)nodeWidget).getLatitude()).
//            attr("class", ((GeoPositionedNodeWidget)nodeWidget).getObject().getClassName()).
//            text(String.valueOf(((GeoPositionedNodeWidget)nodeWidget).getObject().getOid())).end();
//        nodesTag.end();
//
//        StartTagWAX edgesTag = mainTag.start("edges");
//        for (Widget edgeWidget : connectionsLayer.getChildren()){
//            StartTagWAX edgeTag = edgesTag.start("edge");
//            edgeTag.attr("id", ((GeoPositionedConnectionWidget)edgeWidget).getObject().getOid());
//            edgeTag.attr("class", ((GeoPositionedConnectionWidget)edgeWidget).getObject().getClassName());
//            edgeTag.attr("aside", ((GeoPositionedNodeWidget)((ObjectConnectionWidget)edgeWidget).getSourceAnchor().getRelatedWidget()).getObject().getOid());
//            edgeTag.attr("bside", ((GeoPositionedNodeWidget)((ObjectConnectionWidget)edgeWidget).getTargetAnchor().getRelatedWidget()).getObject().getOid());
//            //for (double[] point : ((GeoPositionedConnectionWidget)edgeWidget).getGeoPositionedControlPoints())
//            for (Point point : ((ConnectionWidget)edgeWidget).getControlPoints()){
//                double[] geoPosition = pixelToCoordinate(point, mapComponent.getMainMap().getZoom());
//                edgeTag.start("controlpoint").attr("x", geoPosition[1]).attr("y", geoPosition[0]).end();
//            }
//            edgeTag.end();
//        }
//        edgesTag.end();
//        mainTag.end().close();
//
//        /*Comment this out for debugging purposes
//        try{
//            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/before-to-save_"+Calendar.getInstance().getTimeInMillis()+".xml");
//            fos.write(bas.toByteArray());
//            fos.close();
//        }catch(Exception e){}*/
//
//        return bas.toByteArray();
//    }
//
//    public void setCenterPosition(double latitude, double longitude) {
//        ((MapPanel)mapWidget.getComponent()).getMainMap().setCenterPosition(new GeoPosition(latitude, longitude));
//    }
//
//    public void zoom(int zoom) {
//        ((MapPanel)mapWidget.getComponent()).getMainMap().setZoom(zoom);
//    }
//
//    public void pan(int deltaX, int deltaY) {
//        if (deltaX == 0 && deltaY == 0)
//            return;
//
//        for (Widget node : nodesLayer.getChildren()){
//            node.setPreferredLocation(new Point(node.getPreferredLocation().x - deltaX, node.getPreferredLocation().y - deltaY));
//            if (node.getPreferredLocation().x < 0 || node.getPreferredLocation().y < 0)
//                node.setVisible(false);
//            else
//                node.setVisible(true);
//        }
//
//        for (Widget con : connectionsLayer.getChildren()){
//            List<Point> newControlPoints = new ArrayList<Point>();
//            boolean visible = true;
//            for (Point controlPoint : ((ConnectionWidget)con).getControlPoints()){
//                Point newControlPoint = new Point(controlPoint.x - deltaX, controlPoint.y - deltaY);
//                newControlPoints.add(newControlPoint);
//                if (newControlPoint.x <= 0 || newControlPoint.y <= 0)
//                    visible = false;
//            }
//            con.setVisible(visible);
//            ((ConnectionWidget)con).setControlPoints(newControlPoints, false);
//        }
//    }
//
//    public boolean hasView() {
//        return !mapLayer.getChildren().isEmpty();
//    }
//
//    public LayerWidget getNodesLayer() {
//        return nodesLayer;
//    }
//    
//    /**
//     * Helper class to let us launch a lookup event every time a widget is selected
//     */
//    private class SceneLookup extends ProxyLookup{
//
//        public SceneLookup(Lookup initialLookup) {
//            super(initialLookup);
//        }
//
//        public void updateLookup(Lookup newLookup){
//            setLookups(newLookup);
//        }
//
//        public void updateLookup(LocalObjectLight newElement){
//            setLookups(Lookups.singleton(new ObjectNode(newElement)));
//        }
//    }
}
