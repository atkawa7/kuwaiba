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

package org.inventory.core.visual.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.export.ExportableScene;
import org.inventory.core.visual.export.Layer;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Root class to all GraphScenes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class AbstractScene extends GraphScene<LocalObjectLight, LocalObjectLight> 
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
    public final static int SCENE_CHANGETOSAVE = 2;
    /**
     * Default font
     */
    public static final Font defaultFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    /**
     * Default foreground color
     */
    public static final Color defaultForegroundColor = Color.BLACK;
    /**
     * Default background color
     */
    public static final Color defaultBackgroundColor = Color.LIGHT_GRAY;
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
    private SceneLookup lookup;
    /**
     * Change listeners
     */
    private ArrayList<ActionListener> changeListeners;

    public AbstractScene() {
        this.lookup = new SceneLookup(Lookup.EMPTY);
        this.changeListeners = new ArrayList<ActionListener>();
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
        for (Widget aLabel : labelsLayer.getChildren())
            aLabel.setFont(getFont());
    }
    
    public void setSceneForegroundColor (Color foregroundColor) {
        setForeground(foregroundColor == null ? defaultForegroundColor : foregroundColor);
        for (Widget aLabel : labelsLayer.getChildren())
            aLabel.setForeground(getForeground());
    }

    public void setSceneBackgroundColor (Color backgroundColor) {
        setForeground(backgroundColor == null ? defaultForegroundColor : backgroundColor);
        for (Widget aLabel : labelsLayer.getChildren())
            aLabel.setBackground(getBackground());
    }
    
    public void clear(){
        while (!getNodes().isEmpty())
            removeNode(getNodes().iterator().next());

        while (!getEdges().isEmpty())
            removeNode(getEdges().iterator().next());
        
        labelsLayer.removeChildren();
        validate();
    }
    
    @Override
    public Lookup getLookup(){
        return this.lookup;
    }
    
    public abstract byte[] getAsXML();
    
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
