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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import org.inventory.views.gis.scene.providers.AcceptActionProvider;
import org.inventory.views.gis.scene.providers.PhysicalConnectionProvider;
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
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;

/**
 * Scene used by the GISView component
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GISViewScene extends AbstractScene implements Lookup.Provider{
    /**
     * Default coordinates to center the map
     */
    public static final Coordinate DEFAULT_CENTER_POSITION = new Coordinate(4.740675, -73.762207);
    /**
     * Default zoom level
     */
    public static final int DEFAULT_ZOOM_LEVEL = 6;
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
     * The map itself
     */
    private JMapViewer map;
    /**
     * Last map center X coordinate. Used to pan the scene
     */
    private int lastXPosition;
    /**
     * Last map center Y coordinate. Used to pan the scene
     */
    private int lastYPosition;
    /**
     * Current zoom level. Used to recalculate the widgets coordinates after a Zoom event
     */
    private int lastZoomLevel;
    /**
     * Local connect provider
     */
    private PhysicalConnectionProvider connectionProvider;
    
    /**
     * Scene lookup
     */
    private SceneLookup lookup;  

    public GISViewScene(JMapViewer map) {
        this.map = map;
        this.map.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                for (MouseListener ml : getView().getMouseListeners())
                    ml.mouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                for (MouseListener ml : getView().getMouseListeners())
                    ml.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                for (MouseListener ml : getView().getMouseListeners())
                    ml.mouseReleased(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                for (MouseListener ml : getView().getMouseListeners())
                    ml.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                for (MouseListener ml : getView().getMouseListeners())
                    ml.mouseExited(e);
            }
        });
        
        this.map.addJMVListener(new JMapViewerEventListener() {

            @Override
            public void processCommand(JMVCommandEvent jmvce) {
                if (jmvce.getCommand().equals(JMVCommandEvent.COMMAND.MOVE))
                    pan();
                else
                    zoom();
                getView().repaint();
            }
        });
        
        this.map.setTileLoader(new CustomTileLoader(new CustomTileLoaderListener()));
        
        this.lastXPosition = map.getCenter().x;
        this.lastYPosition = map.getCenter().y;
        this.lastZoomLevel = map.getZoom();
        
        this.connectionProvider = new PhysicalConnectionProvider(this);
        
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
        getActions().addAction(ActionFactory.createAcceptAction(new AcceptActionProvider(this)));
        setActiveTool(AbstractScene.ACTION_SELECT);
        setOpaque(false);
    }
    
    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        GeoPositionedNodeWidget myWidget =  new GeoPositionedNodeWidget(this,node, 0, 0);
        nodesLayer.addChild(myWidget);
        myWidget.getActions(AbstractScene.ACTION_SELECT).addAction(createSelectAction());
        myWidget.getActions(AbstractScene.ACTION_SELECT).addAction(ActionFactory.createMoveAction());
        myWidget.getActions(AbstractScene.ACTION_CONNECT).addAction(ActionFactory.createConnectAction(interactionLayer, connectionProvider));
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
    protected void attachEdgeSourceAnchor(LocalObjectLight edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {}
    @Override
    protected void attachEdgeTargetAnchor(LocalObjectLight edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {}

    public PhysicalConnectionProvider getConnectProvider() {
        return connectionProvider;
    }

    public JMapViewer getMap() {
        return map;
    }
    
    @Override
    public Lookup getLookup(){
        return this.lookup;
    }

    /**
     * Called on a pan event
     */
    public void pan() {
        int deltaX = map.getCenter().x - lastXPosition;
        int deltaY = map.getCenter().y - lastYPosition;
        
        for (Widget node : nodesLayer.getChildren())
            node.setPreferredLocation(new Point(node.getPreferredLocation().x - 
                    deltaX, node.getPreferredLocation().y - deltaY));

        for (Widget connection : edgesLayer.getChildren()){
            List<Point> controlPoints = ((AbstractConnectionWidget)connection).getControlPoints();
            for (int i = 1; i < controlPoints.size() - 1; i++) {
                controlPoints.get(i).x -= deltaX;
                controlPoints.get(i).y -= deltaY;
            }
        }
        
        revalidate();
        lastXPosition = map.getCenter().x;
        lastYPosition = map.getCenter().y;
    }
    
    /**
     * Called on a zoom event
     */
    public void zoom() {
        
        for (Widget node : nodesLayer.getChildren()){
            Coordinate geoPosition = getLastPosition(node.getPreferredLocation().x, node.getPreferredLocation().y);
            Point newLocation = map.getMapPosition(geoPosition, true);
            node.setPreferredLocation(newLocation);
        }
        for (Widget connection : edgesLayer.getChildren()){
            for (Point controlPoint : ((AbstractConnectionWidget)connection).getControlPoints()){
                Coordinate geoPosition = getLastPosition(controlPoint.x, controlPoint.y);
                Point newLocation = map.getMapPosition(geoPosition.getLat(), geoPosition.getLon(), true);
                controlPoint.x = newLocation.x;
                controlPoint.y = newLocation.y;
            }
        }
        
        revalidate();
        lastZoomLevel = map.getZoom();
        lastXPosition = map.getCenter().x;
        lastYPosition = map.getCenter().y;
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
        map.setDisplayPosition(DEFAULT_CENTER_POSITION, DEFAULT_ZOOM_LEVEL);
        
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
        mainTag.start("zoom").text(String.valueOf(map.getZoom())).end();
        mainTag.start("center").attr("x", map.getPosition().
                getLon()).attr("y", map.getPosition().getLat()).end();
        StartTagWAX nodesTag = mainTag.start("nodes");
        for (Widget nodeWidget : nodesLayer.getChildren()){
            Coordinate geoPosition = map.getPosition(nodeWidget.getPreferredLocation());
            nodesTag.start("node").attr("x", geoPosition.getLon()).
            attr("y", geoPosition.getLat()).
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
                Coordinate geoPosition = map.getPosition(point);
                edgeTag.start("controlpoint").attr("x", geoPosition.getLon()).attr("y", geoPosition.getLat()).end();
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
    
    public void toggleLabels(boolean visible){
        labelsLayer.setVisible(visible);
        repaint();
    }
    
    /**
     * A convenience method cloning the behavior of JMapviewer.getPosition, but using
     * lastZoomLevel and lastX/YPosition
     * @param mapPointX
     * @param mapPointY
     * @return 
     */
    public Coordinate getLastPosition(int mapPointX, int mapPointY){
        int x = lastXPosition + mapPointX - map.getWidth() / 2;
        int y = lastYPosition + mapPointY - map.getHeight() / 2;
        double lon = map.getTileController().getTileSource().XToLon(x, lastZoomLevel);
        double lat = map.getTileController().getTileSource().YToLat(y, lastZoomLevel);
        return new Coordinate(lat, lon);
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
            setPreferredBounds(getScene().getBounds());
            nodesLayer.setPreferredBounds(getScene().getBounds());
            labelsLayer.setPreferredBounds(getScene().getBounds());
            validate();
        }
    }
    
    /**
     * Custom TileLoader used to set our own TileLoaderListener so we can repaint the scene view
     * when a tile is done loading
     */
    private class CustomTileLoader extends OsmTileLoader{
        public CustomTileLoader(TileLoaderListener listener) {
            super(listener);
        }
    }
    
    /**
     * Custom CustomTileLoaderListener, used to repaint the scene view
     * when a tile is done loading
     */
    private class CustomTileLoaderListener implements TileLoaderListener {

        @Override
        public void tileLoadingFinished(Tile tile, boolean bln) {
            getView().repaint();
        }

        @Override
        public TileCache getTileCache() {
            return null;
        }
    }
}
