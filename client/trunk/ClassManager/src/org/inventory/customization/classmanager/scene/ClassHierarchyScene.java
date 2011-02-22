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
import java.util.ArrayList;
import java.util.List;
import org.inventory.core.services.interfaces.LocalAttributeWrapper;
import org.inventory.core.services.interfaces.LocalClassWrapper;
import org.inventory.core.visual.decorators.ColorSchemeFactory;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;

/**
 * Scene to contain the application's data model as a VMD scene
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ClassHierarchyScene extends GraphPinScene<LocalClassWrapper, String, LocalAttributeWrapper>{

    private LayerWidget nodesLayer;
    private LayerWidget connectionsLayer;
    private LayerWidget interactionsLayer;
    private Image glyphDummy = ImageUtilities.loadImage("org/inventory/customization/classmanager/res/dummy-glyph.png");
    private Image glyphNoCopy = ImageUtilities.loadImage("org/inventory/customization/classmanager/res/no-copy-glyph.png");
    private Image glyphNoSerialize = ImageUtilities.loadImage("org/inventory/customization/classmanager/res/no-serialize-glyph.png");

    public ClassHierarchyScene(List<LocalClassWrapper> roots) {
        nodesLayer = new LayerWidget(this);
        connectionsLayer = new LayerWidget(this);
        interactionsLayer = new LayerWidget(this);
        addChild(interactionsLayer);
        addChild(nodesLayer);
        addChild(connectionsLayer);

        getActions ().addAction (ActionFactory.createPanAction ());
        getActions ().addAction (ActionFactory.createRectangularSelectAction (this, interactionsLayer));
        renderLevel(roots,0);
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
        if (nodeClass.getApplicationModifiers() != 0){
            List<Image> glyphs = new ArrayList<Image>();
            glyphs.add (glyphDummy); //NOI18N
            nodeWidget.setGlyphs(glyphs);
        }
        //ClassNodeWidget myClassNode = new ClassNodeWidget(nodeClass, this, ColorSchemeFactory.);
        nodeWidget.getActions().addAction(createSelectAction());
        nodeWidget.getActions().addAction(ActionFactory.createMoveAction());
        nodesLayer.addChild(nodeWidget);
        return nodeWidget;
    }

    @Override
    protected Widget attachEdgeWidget(String edge) {
        VMDConnectionWidget connectionWidget = new VMDConnectionWidget(this, RouterFactory.createOrthogonalSearchRouter(nodesLayer,connectionsLayer));
        connectionsLayer.addChild(connectionWidget);
        connectionWidget.getActions ().addAction (createObjectHoverAction ());
        connectionWidget.getActions ().addAction (createSelectAction ());
        connectionWidget.getActions ().addAction (ActionFactory.createMoveAction());
        return connectionWidget;
    }

    @Override
    protected Widget attachPinWidget(LocalClassWrapper node, LocalAttributeWrapper pin) {
       VMDPinWidget pinWidget = new VMDPinWidget(this);
       pinWidget.setPinName(pin.getName() + " ["+pin.getType()+"]"); //NOI18N
       List<Image> glyphs = new ArrayList<Image>();
       if (!pin.canCopy())
           glyphs.add(glyphNoCopy);
       if (!pin.canSerialize())
           glyphs.add(glyphNoSerialize);
       VMDNodeWidget nodeWidget = (VMDNodeWidget) findWidget(node);
       assert nodeWidget != null;
       nodeWidget.addChild(pinWidget);
       return pinWidget;
    }

    @Override
    protected void attachEdgeSourceAnchor(String edge, LocalAttributeWrapper oldSourcePin, LocalAttributeWrapper sourcePin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void attachEdgeTargetAnchor(String edge, LocalAttributeWrapper oldTargetPin, LocalAttributeWrapper targetPin) {
        throw new UnsupportedOperationException("Not supported yet.");
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
    private void renderLevel(List<LocalClassWrapper> roots, int yOffset) {
        int xOffset = 0;
        for (LocalClassWrapper aClass : roots){
            addNode(aClass).setPreferredLocation(new Point(xOffset, yOffset));
            renderLevel(aClass.getDirectSubClasses(), yOffset + 200);
            xOffset += 70;
        }
    }
}
