/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.inventory.customization.classmanager.scene;

import java.awt.Image;
import java.awt.Point;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.inventory.core.services.api.metadata.LocalAttributeWrapper;
import org.inventory.core.services.api.metadata.LocalClassWrapper;
import org.inventory.core.visual.actions.providers.MultipleWidgetMoveActionProvider;
import org.inventory.core.visual.decorators.ColorSchemeFactory;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.EventProcessingType;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;

/**
 * Scene to contain the application's data model as a VMD scene
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ClassHierarchyScene extends GraphScene<LocalClassWrapper, String>{

    private LayerWidget nodesLayer;
    private LayerWidget connectionsLayer;
    private LayerWidget interactionsLayer;
    private Image glyphDummy = ImageUtilities.loadImage("org/inventory/customization/classmanager/res/dummy-glyph.png");
    private Image glyphAbstract = ImageUtilities.loadImage("org/inventory/customization/classmanager/res/abstract-glyph.png");
    private Image glyphNoCount = ImageUtilities.loadImage("org/inventory/customization/classmanager/res/no-count-glyph.png");
    private Image glyphNoCopy = ImageUtilities.loadImage("org/inventory/customization/classmanager/res/no-copy-glyph.png");
    private Image glyphNoSerialize = ImageUtilities.loadImage("org/inventory/customization/classmanager/res/no-serialize-glyph.png");
    private Image glyphReadOnly = ImageUtilities.loadImage("org/inventory/customization/classmanager/res/read-only-glyph.png");
    private HashMap<Integer, List<VMDNodeWidget>> levels;
    private MultipleWidgetMoveActionProvider moveProvider;

    public ClassHierarchyScene(List<LocalClassWrapper> roots) {
        setKeyEventProcessingType (EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);
        moveProvider= new MultipleWidgetMoveActionProvider(this);
        nodesLayer = new LayerWidget(this);
        connectionsLayer = new LayerWidget(this);
        interactionsLayer = new LayerWidget(this);
        addChild(interactionsLayer);
        addChild(nodesLayer);
        addChild(connectionsLayer);

        getActions ().addAction (ActionFactory.createPanAction ());
        getActions ().addAction (ActionFactory.createRectangularSelectAction (this, interactionsLayer));
        levels = new HashMap<Integer, List<VMDNodeWidget>>();
        renderLevel(roots,0);
        organizeLevels();
    }

    @Override
    protected Widget attachNodeWidget(LocalClassWrapper nodeClass) {
        VMDColorScheme scheme;
        switch (nodeClass.getClassType()){
            case LocalClassWrapper.TYPE_APPLICATION:
                scheme = ColorSchemeFactory.getBlueScheme();
                break;
            case LocalClassWrapper.TYPE_INVENTORY:
                scheme = ColorSchemeFactory.getGreenScheme();
                break;
            case LocalClassWrapper.TYPE_METADATA:
                scheme = ColorSchemeFactory.getYellowScheme();
                break;
            default:
                scheme = ColorSchemeFactory.getGrayScheme();
                break;
        }
        VMDNodeWidget nodeWidget = new VMDNodeWidget(this, scheme);
        List<Image> glyphs = new ArrayList<Image>();
        if (nodeClass.isDummy())
            glyphs.add (glyphDummy);
        if (Modifier.isAbstract(nodeClass.getJavaModifiers()))
            glyphs.add (glyphAbstract);
        if (!nodeClass.isCountable())
            glyphs.add (glyphNoCount);
        nodeWidget.setGlyphs(glyphs);
        nodeWidget.setNodeName(nodeClass.getName());
        nodeWidget.getActions().addAction(createSelectAction());
        nodeWidget.getActions().addAction(ActionFactory.createMoveAction(moveProvider,moveProvider));
        nodesLayer.addChild(nodeWidget);
        return nodeWidget;
    }

    @Override
    protected Widget attachEdgeWidget(String edge) {
        ConnectionWidget connectionWidget = new ConnectionWidget(this);
        //connectionWidget.getActions ().addAction (createSelectAction ()); <-- this action seems to be causing a problem with the multiselection move action and it's not actually necessary
        connectionWidget.getActions ().addAction (ActionFactory.createAddRemoveControlPointAction());
        connectionsLayer.addChild(connectionWidget);
        return connectionWidget;
    }

    @Override
    protected void attachEdgeSourceAnchor(String edge, LocalClassWrapper oldSourceNode, LocalClassWrapper sourceNode) {
        //This is done in the renderLevel method, since it's easier
    }

    @Override
    protected void attachEdgeTargetAnchor(String edge, LocalClassWrapper oldTargetNode, LocalClassWrapper targetNode) {
        //This is done in the renderLevel method, since it's easier
    }

    /**
     * Cleans the scene and releases all resources
     */
    public void cleanScene(){
        connectionsLayer.removeChildren();
        nodesLayer.removeChildren();
    }
    /**
     * Recursive method that renders a different leven in the class hierarchy tree
     * @param roots
     */
    private List<VMDNodeWidget> renderLevel(List<LocalClassWrapper> roots, int currentLevel) {
        List<VMDNodeWidget> level = new ArrayList<VMDNodeWidget>(roots.size());
        for (LocalClassWrapper aClassWrapper : roots){
            VMDNodeWidget newClassNode = (VMDNodeWidget) addNode(aClassWrapper);
            for (LocalAttributeWrapper anAttribute : aClassWrapper.getAttributes()){
                VMDPinWidget pinWidget = new VMDPinWidget(this);
                pinWidget.setPinName(anAttribute.getName() + " ["+anAttribute.getType()+"]"); //NOI18N
                List<Image> glyphs = new ArrayList<Image>();
                if (!anAttribute.canCopy())
                   glyphs.add(glyphNoCopy);
                if (!anAttribute.canSerialize())
                   glyphs.add(glyphNoSerialize);
                if (!anAttribute.canWrite())
                   glyphs.add(glyphReadOnly);
                pinWidget.setGlyphs(glyphs);
                newClassNode.attachPinWidget(pinWidget);
            }
            for (VMDNodeWidget aChild : renderLevel(aClassWrapper.getDirectSubClasses(), currentLevel + 1)){
                String edgeName = aClassWrapper.getName()+aChild.getNodeName();
                ConnectionWidget newEdge = (ConnectionWidget) addEdge(edgeName);
                newEdge.setEndPointShape(PointShape.SQUARE_FILLED_SMALL);
                newEdge.setControlPointShape(PointShape.SQUARE_FILLED_SMALL);
                newEdge.getActions().addAction(ActionFactory.createFreeMoveControlPointAction());
                newEdge.setSourceAnchor(AnchorFactory.createFreeRectangularAnchor(newClassNode, true));
                newEdge.setTargetAnchor(AnchorFactory.createFreeRectangularAnchor(aChild, true));
            }
            level.add(newClassNode);
        }
        if (levels.get(currentLevel) == null)
            levels.put(currentLevel,level);
        else
            ((List)levels.get(currentLevel)).addAll(level);
        return level;
    }

    private void organizeLevels() {
        int yOffset = 0;
        for (int i = 0 ; i < levels.size(); i++){
            if (levels.get(i).isEmpty())
                continue;
            int xOffset = 0;
            for (VMDNodeWidget aNode : levels.get(i)){
                aNode.setPreferredLocation(new Point(xOffset, yOffset));
                xOffset += 300;
            }
            yOffset += 350;
        }
    }
}