/**
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.visual.scene;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.visual.export.ExportableScene;
import org.inventory.core.visual.export.Layer;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Root class to all GraphScenes
 * TODO: This should inherit from ObjectScene
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class AbstractScene <N, E> extends GraphScene<N, E> 
        implements ExportableScene {
    /**
     * Constant to represent the selection tool
     */
    public final static String ACTION_SELECT = "selection"; //NOI18
    /**
     * Constant to represent the connection tool
     */
    public final static String ACTION_CONNECT = "connect"; //NOI18
    /**
     * Event ID to indicate a change in the scene (saving is not mandatory)
     */
    public final static int SCENE_CHANGE = 1;
    /**
     * Event ID to indicate a change in the scene (saving is mandatory)
     */
    public final static int SCENE_CHANGEANDSAVE = 2;
    /**
     * Default font
     */
    public static final Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    /**
     * Default foreground color
     */
    public static final Color defaultForegroundColor = Color.BLACK;
    /**
     * Default background color
     */
    public static final Color defaultBackgroundColor = Color.LIGHT_GRAY;
    /**
     * Color to be assigned to the new lines 
     */
    protected Color newLineColor;
    /**
     * Used to contain the background image
     */
    protected LayerWidget backgroundLayer;
    /**
     * This layer is used to paint the auxiliary elements 
     */
    protected LayerWidget interactionLayer;
    /**
     * Used to hold the nodes
     */
    protected LayerWidget nodesLayer;
    /**
     * Used to hold the connections
     */
    protected LayerWidget edgesLayer;
    /**
     * Used to hold misc messages
     */
    protected LayerWidget labelsLayer;
    /**
     * Shared popup menu for widgets
     */
    protected PopupMenuProvider defaultPopupMenuProvider;
    /**
     * Scene lookup
     */
    private SceneLookup lookup = new SceneLookup(Lookup.EMPTY);
    /**
     * Change listeners
     */
    private ArrayList<ActionListener> changeListeners = new ArrayList<ActionListener>();

    public AbstractScene() {
        setActiveTool(ACTION_SELECT);
    }
    
    public void toggleLabels(boolean visible){
        labelsLayer.setVisible(visible);
        if (getView() != null)
            getView().repaint();
    }
       
    @Override
    public Scene getExportable(){
        return this;
    }
    
    @Override
    public Layer[] getLayers(){
        return null;
    }
    
    public void initSelectionListener(){
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
    }
    
    /**
     * Adds a change listener
     * @param listener 
     */
    public void addChangeListener(ActionListener listener){
        if (!changeListeners.contains(listener))
            changeListeners.add(listener);
    }
    
    /**
     * Removes a change listener
     * @param listener 
     */
    public void removeChangeListener(ActionListener listener){
        changeListeners.remove(listener);
    }
    
    /**
     * Releases all listeners
     */
    public void removeAllListeners (){
        while (!changeListeners.isEmpty())
            changeListeners.remove(changeListeners.get(0));
    }
    
    public void fireChangeEvent(ActionEvent ev){
        for (ActionListener listener : changeListeners)
            listener.actionPerformed(ev);
    }
    
    /**
     * Sets the font used by all text elements in the scene
     * @param newFont A new font. Null to set to default
     */
    public void setSceneFont (Font newFont) {
        setFont(newFont == null ? defaultFont : newFont);
        if (labelsLayer != null) { //Not all views will have a layer to place labels
            for (Widget aLabel : labelsLayer.getChildren())
                aLabel.setFont(getFont());
        }
    }
    
    public void setSceneForegroundColor (Color foregroundColor) {
        setForeground(foregroundColor == null ? defaultForegroundColor : foregroundColor);
        if (labelsLayer != null) { //Not all views will have a layer to place labels
            for (Widget aLabel : labelsLayer.getChildren())
                aLabel.setForeground(getForeground());
        }
    }

    public void setSceneBackgroundColor (Color backgroundColor) {
        setForeground(backgroundColor == null ? defaultForegroundColor : backgroundColor);
        if (labelsLayer != null) { //Not all views will have a layer to place labels
            for (Widget aLabel : labelsLayer.getChildren())
                aLabel.setBackground(getBackground());
        }
    }
    
    public void clear(){
        while (!getNodes().isEmpty())
            removeNode(getNodes().iterator().next());

        while (!getEdges().isEmpty())
            removeEdge(getEdges().iterator().next());
        
        if (labelsLayer != null)
            labelsLayer.removeChildren();
        validate();
    }
    
    @Override
    public Lookup getLookup(){
        return this.lookup;
    }
    
    public void setNewLineColor(Color newColor){
        newLineColor = newColor;
    }
    
    /**
     * Gets the background image
     * @return
     */
    public byte[] getBackgroundImage(){
        if (backgroundLayer.getChildren().isEmpty())
            return null;
        try {
            return Utils.getByteArrayFromImage(((ImageWidget) backgroundLayer.getChildren().iterator().next()).getImage(), "png"); //NOI18n
        } catch (IOException ex) {
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_INFO || Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
                Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    /**
     * Sets the current background. Do nothing if supportsBackgrounds always returns false
     * @param im The image. Null of you want to remove the existing one
     */
    public void setBackgroundImage(Image im){
        backgroundLayer.removeChildren();
        
        if (im != null)
            backgroundLayer.addChild(new ImageWidget(this, im));
        
        validate();
    }
    
    /**
     * The XML representation of the view. Typically used to serialize it
     * @return XML document as a byte arrays
     */
    public abstract byte[] getAsXML();
    /**
     * Get the active connect provider. Null if supportsConnections returns false.
     * @return 
     */
    public abstract PhysicalConnectionProvider getConnectProvider();
    /**
     * Does this view support connections
     * @return 
     */
    public abstract boolean supportsConnections();
    /**
     * Does this view support backgrounds?
     * @return 
     */
    public abstract boolean supportsBackgrounds();
    
    
    /**
     * Helper class to let us launch a lookup event every time a widget is selected
     */
    private class SceneLookup extends ProxyLookup {

        public SceneLookup(Lookup aLookup) {
            super(aLookup);
        }
        public SceneLookup(LocalObjectLight object) {
            updateLookup(object);
        }

        public final void updateLookup(LocalObjectLight object){
            setLookups(Lookups.singleton(new ObjectNode(object)));
        }
    }
}
