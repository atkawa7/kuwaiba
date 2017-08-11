/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Modified By Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org> for project Kuwaiba
 */

package org.inventory.queries.scene;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JPopupMenu;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.queries.LocalTransientQuery;
import org.inventory.core.visual.decorators.ColorSchemeFactory;
import org.inventory.queries.actions.SwitchClassNodeWidgetFilterAction;
import org.inventory.queries.scene.filters.BooleanFilterNodeWidget;
import org.inventory.queries.scene.filters.DateFilterNodeWidget;
import org.inventory.queries.scene.filters.ListTypeFilter;
import org.inventory.queries.scene.filters.NumericFilterNodeWidget;
import org.inventory.queries.scene.filters.SimpleCriteriaNodeWidget;
import org.inventory.queries.scene.filters.StringFilterNodeWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.api.visual.vmd.VMDFactory;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.EventProcessingType;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * This scene is used in the graphical query editor. Due to a plain VMDGraph scene stores the object
 * keys as Strings, it's not suitable for our purposes, so we rather inherit from GraphPinScene and use
 * the same base code
 * @author David Kaspar
 * @contributor Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class QueryEditorScene extends GraphPinScene<Object, String, Object>
        implements ItemListener{

    /**
     * Event raised when an attribute is used as filter(its checkbox is selected)
     */
    public static final int SCENE_FILTERENABLED = 1;
    /**
     * Event raised when an attribute is not used as filter anymore(its checkbox is deselected)
     */
    public static final int SCENE_FILTERDISABLED = 2;
    /**
     * Offset to place the nodes horizontally
     **/
    public static final int X_OFFSET = 50;
    /**
     * Offset to place the nodes vertically
     **/
    public static final int Y_OFFSET = 50;

    private LayerWidget backgroundLayer = new LayerWidget (this);
    private LayerWidget mainLayer = new LayerWidget (this);
    private LayerWidget connectionLayer = new LayerWidget (this);
    private LayerWidget upperLayer = new LayerWidget (this);

    private Router router;

    private WidgetAction moveControlPointAction = ActionFactory.createOrthogonalMoveControlPointAction ();
    private WidgetAction moveAction = ActionFactory.createMoveAction ();

    private SceneLayout sceneLayout;
    private VMDColorScheme scheme;
    private ArrayList<ActionListener> listeners;
    private LocalClassMetadata currentSearchedClass;

    /**
     * Creates a Query Editor graph scene.
     */
    public QueryEditorScene () {
        this (VMDFactory.getOriginalScheme ());
    }

    /**
     * Creates a Query Editor graph scene with a specific color scheme.
     * @param scheme the color scheme
     */
    public QueryEditorScene (VMDColorScheme scheme) {
        this.scheme = scheme;
        setKeyEventProcessingType (EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);

        addChild (backgroundLayer);
        addChild (mainLayer);
        addChild (connectionLayer);
        addChild (upperLayer);

        router = RouterFactory.createOrthogonalSearchRouter (mainLayer, connectionLayer);

        getActions ().addAction (ActionFactory.createPanAction ());
        sceneLayout = LayoutFactory.createSceneGraphLayout(this, new GridGraphLayout<Object, String> ().setChecker (true));
    }

    /**
     * Implements attaching a widget to a node. The widget is VMDNodeWidget and has select and move actions.
     * @param node the node
     * @return the widget attached to the node
     */
    protected Widget attachNodeWidget (Object node) {
        QueryEditorNodeWidget widget = null;
        if (node instanceof LocalClassMetadata){ //A complex class filter node
            if(getNodes().isEmpty()) //It's the first node, this is, the root one. In this case we use a different scheme
                widget = new ClassNodeWidget(this, (LocalClassMetadata)node, true, true,
                        ColorSchemeFactory.getGreenScheme());
            else{
                widget = new ClassNodeWidget(this, (LocalClassMetadata)node, false, false,
                        ColorSchemeFactory.getYellowScheme());
                widget.getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider() {

                            public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                                JPopupMenu myMenu = new JPopupMenu();
                                myMenu.add(new SwitchClassNodeWidgetFilterAction((ClassNodeWidget)widget));
                                return myMenu;
                            }
                        }));
            }
        }
        else{
            if (node instanceof LocalClassMetadataLight){ //A simplified class filter node
                widget = new ListTypeFilter(this, (LocalClassMetadataLight)node);
                widget.getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider() {

                            public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                                JPopupMenu myMenu = new JPopupMenu();
                                myMenu.add(new SwitchClassNodeWidgetFilterAction((ListTypeFilter)widget));
                                return myMenu;
                            }
                        }));
            }else{
                String type = ((String)node).substring(0, ((String)node).indexOf('_'));

                if (type.equals("String")) //NOI18N
                    widget = new StringFilterNodeWidget(this);
                else
                    if (type.equals("Integer") || //NOI18N
                            type.equals("Float") || //NOI18N
                            type.equals("Long")) //NOI18N
                        widget = new NumericFilterNodeWidget(this);
                    else
                        if (type.equals("Boolean")) //NOI18N
                            widget = new BooleanFilterNodeWidget(this);
                        else
                            if (type.equals("Date")) //NOI18N
                                widget = new DateFilterNodeWidget(this);
            }
        }


        mainLayer.addChild (widget);
        widget.getActions ().addAction (moveAction);            
        return widget;
    }

    /**
     * Implements attaching a widget to a pin. The widget is VMDPinWidget and has object-hover and select action.
     * The the node id ends with "#default" then the pin is the default pin of a node and therefore it is non-visual.
     * @param node the node
     * @param pin the pin
     * @return the widget attached to the pin, null, if it is a default pin
     */
    protected Widget attachPinWidget (Object node, Object pin) {
        VMDPinWidget widget;
            if (pin instanceof LocalAttributeMetadata){
            widget = new AttributePinWidget(this, (LocalAttributeMetadata)pin,
                    ((LocalClassMetadata)node).getTypeForAttribute(((LocalAttributeMetadata)pin).getName()),
                    scheme);
            widget.getActions ().addAction (createSelectAction ());
        }
        else
            widget = new VMDPinWidget(this, scheme);
        
        ((VMDNodeWidget) findWidget (node)).attachPinWidget (widget);
        
        return widget;
    }

    /**
     * Implements attaching a widget to an edge. the widget is ConnectionWidget and has object-hover, select and move-control-point actions.
     * @param edge the edge
     * @return the widget attached to the edge
     */
    protected Widget attachEdgeWidget (String edge) {
        VMDConnectionWidget connectionWidget = new VMDConnectionWidget (this, scheme);
        connectionWidget.setRouter (router);
        connectionLayer.addChild (connectionWidget);

        connectionWidget.getActions ().addAction (createObjectHoverAction ());
        connectionWidget.getActions ().addAction (createSelectAction ());

        return connectionWidget;
    }

    /**
     * Attaches an anchor of a source pin an edge.
     * The anchor is a ProxyAnchor that switches between the anchor attached to the pin widget directly and
     * the anchor attached to the pin node widget based on the minimize-state of the node.
     * @param edge the edge
     * @param oldSourcePin the old source pin
     * @param sourcePin the new source pin
     */
    protected void attachEdgeSourceAnchor (String edge, Object oldSourcePin, Object sourcePin) {
        ((ConnectionWidget) findWidget (edge)).setSourceAnchor (getPinAnchor (sourcePin));
    }

    /**
     * Attaches an anchor of a target pin an edge.
     * The anchor is a ProxyAnchor that switches between the anchor attached to the pin widget directly and
     * the anchor attached to the pin node widget based on the minimize-state of the node.
     * @param edge the edge
     * @param oldTargetPin the old target pin
     * @param targetPin the new target pin
     */
    protected void attachEdgeTargetAnchor (String edge, Object oldTargetPin, Object targetPin) {
        ((ConnectionWidget) findWidget (edge)).setTargetAnchor (getPinAnchor (targetPin));
    }

    private Anchor getPinAnchor (Object pin) {
        if (pin == null)
            return null;
        VMDNodeWidget nodeWidget = (VMDNodeWidget) findWidget (getPinNode (pin));
        Widget pinMainWidget = findWidget (pin);
        Anchor anchor;
        if (pinMainWidget != null) {
            anchor = AnchorFactory.createDirectionalAnchor (pinMainWidget, AnchorFactory.DirectionalAnchorKind.HORIZONTAL, 8);
            anchor = nodeWidget.createAnchorPin (anchor);
        } else
            anchor = nodeWidget.getNodeAnchor ();
        return anchor;
    }

    /**
     * Invokes layout of the scene.
     */
    public void layoutScene () {
        sceneLayout.invokeLayout ();
    }

    public void setCurrentSearchedClass(LocalClassMetadata currentSearchedClass) {
        this.currentSearchedClass = currentSearchedClass;
    }

    public LocalTransientQuery getTransientQuery(LocalClassMetadata mainClass,
            int logicalConnector, int limit, int page, boolean isJoin) {
        LocalTransientQuery myQuery = new LocalTransientQuery(mainClass.getClassName(),
                logicalConnector, isJoin, limit, page);
        try{
            Widget[] attributePins = ((ClassNodeWidget)findWidget(mainClass)).getChildren().toArray(new Widget[0]);
            for (Widget myPin : attributePins){
                if (myPin instanceof AttributePinWidget){
                    if (((AttributePinWidget)myPin).getIsVisible().isSelected()){
                        myQuery.getVisibleAttributeNames().add(((AttributePinWidget)myPin).getAttribute().getName());
                    }
                    if (!((AttributePinWidget)myPin).getInsideCheck().isSelected())
                        continue;
                    String[] myEdges = findPinEdges(((AttributePinWidget)myPin).getAttribute(), true, false).toArray(new String[0]);
                    for (String edge : myEdges){
                        VMDConnectionWidget myEdge = (VMDConnectionWidget)findWidget(edge);
                        VMDNodeWidget nextHop = (VMDNodeWidget) myEdge.getTargetAnchor().getRelatedWidget().getParentWidget();
                        myQuery.getAttributeNames().add(((AttributePinWidget)myPin).getAttribute().getName());
                        if(nextHop instanceof SimpleCriteriaNodeWidget){
                            if (nextHop instanceof ListTypeFilter){
                                myQuery.getConditions().add(null); //padding
                                myQuery.getAttributeValues().add(null); //padding
                                if (((LocalObjectListItem)((ListTypeFilter)nextHop).getValue()).equals(new LocalObjectListItem())){
                                    myQuery.getJoins().add(null);
                                }else{
                                    LocalTransientQuery simplifiedQuery = new LocalTransientQuery(((ListTypeFilter)nextHop).getNodeName(),logicalConnector,false,0,0);
                                    simplifiedQuery.getAttributeNames().add("id"); //NOI18N
                                    simplifiedQuery.getAttributeValues().add(String.valueOf(((LocalObjectListItem)((ListTypeFilter)nextHop).getValue()).getOid()));
                                    simplifiedQuery.getJoins().add(null); //padding
                                    simplifiedQuery.getConditions().add(((ListTypeFilter)nextHop).getCondition().id());
                                    myQuery.getJoins().add(simplifiedQuery);
                                }
                            }else{
                                myQuery.getConditions().add(((SimpleCriteriaNodeWidget)nextHop).getCondition().id());
                                myQuery.getAttributeValues().add(String.valueOf(((SimpleCriteriaNodeWidget)nextHop).getValue()));
                                myQuery.getJoins().add(null); //padding
                            }
                        }else{
                            if (nextHop instanceof ClassNodeWidget){
                                myQuery.getConditions().add(null); //padding
                                myQuery.getAttributeValues().add(null); //padding
                                myQuery.getJoins().add(getTransientQuery(((ClassNodeWidget)nextHop).getWrappedClass(),logicalConnector,0,0,true));
                            }
                        }
                    }
                }
            }
            return myQuery;
        }catch(java.lang.NullPointerException ex){
            return null;
        }
    }    

    /**
     * The default removeNode implementation removes the provided node plus the related
     * pins, however, as this editor chains many nodes in a tree fashion is necessary to
     * remove the whole branch, not only the one provided
     * @param node the root node to start deleting
     * @param goBackwards should the method delete the edges coming from backwards the root node?
     */
    public void removeAllRelatedNodes(Object node, boolean goBackwards){
        QueryEditorNodeWidget currentNode = ((QueryEditorNodeWidget)findWidget(node));
        List<Widget> widgetsInside = currentNode.getChildren();
        for (Widget widget : widgetsInside){
            if (widget instanceof VMDPinWidget){
                String[] forwardEdges = findPinEdges(findObject(widget), true, false).toArray(new String[0]);
                for (String edge : forwardEdges){
                    VMDConnectionWidget myEdge = (VMDConnectionWidget)findWidget(edge);
                    VMDNodeWidget nextHop = (VMDNodeWidget) myEdge.getTargetAnchor().getRelatedWidget().getParentWidget();
                    removeAllRelatedNodes(findObject(nextHop));
                }

                if (goBackwards){
                    String[] backwardEdges = findPinEdges(findObject(widget), false, true).toArray(new String[0]);
                    for (String edge : backwardEdges)
                        removeEdge(edge);
                }
            }
        }
        removeNode(node);
    }

    public void removeAllRelatedNodes(Object node){
        removeAllRelatedNodes(node, true);
    }

    public void organizeNodes(LocalClassMetadata rootObject, int x, int y){

        ClassNodeWidget rootNode = (ClassNodeWidget)findWidget(rootObject);

        assert (rootNode != null);

        rootNode.setPreferredLocation(new Point(x, y));

        Widget[] attributePins = rootNode.getChildren().toArray(new Widget[0]);
        int nextX = x + rootNode.getClientArea().width +100;
        int nextY = y;

        for (Widget myPin : attributePins){
            if (myPin instanceof AttributePinWidget){

                if (!((AttributePinWidget)myPin).getInsideCheck().isSelected())
                    continue;
                String[] myEdges = findPinEdges(((AttributePinWidget)myPin).getAttribute(), true, false).toArray(new String[0]);

                VMDConnectionWidget myEdge = (VMDConnectionWidget)findWidget(myEdges[0]);
                VMDNodeWidget nextHop = (VMDNodeWidget) myEdge.getTargetAnchor().getRelatedWidget().getParentWidget();
                if(nextHop instanceof SimpleCriteriaNodeWidget)
                    nextHop.setPreferredLocation(new Point(nextX, nextY));
                else{
                    if (nextHop instanceof ClassNodeWidget)
                        organizeNodes(((ClassNodeWidget)nextHop).getWrappedClass(), nextX,nextY);
                }
                nextY += nextHop.getClientArea().height + 50;
            }
        }
    }
    public void clear(){
        currentSearchedClass = null;
        while (!getNodes().isEmpty())
            removeNode(getNodes().iterator().next());
        while (!getEdges().isEmpty())
            removeEdge(getEdges().iterator().next());
    }

    public LocalClassMetadata getCurrentSearchedClass() {
        return currentSearchedClass;
    }

    /**
     * To listen for scene changes implementing the observer design pattern
     * @param listener
     */
    public void addActionListener(ActionListener listener){
        if (listeners == null)
            listeners = new ArrayList<ActionListener>();
        listeners.add(listener);
    }

    public void removeActionListener(ActionListener listener){
        if (listeners == null)
            return;
        listeners.remove(listener);
    }

    public void fireChangeEvent(ActionEvent ev){
        for (ActionListener listener : listeners)
            listener.actionPerformed(ev);
    }
    /**
     * Listen for checkbox selections
     * @param e
     */
    public void itemStateChanged(ItemEvent e) {
        fireChangeEvent(new ActionEvent(e.getSource(), 
                ((JCheckBox)e.getSource()).isSelected() ?
                    SCENE_FILTERENABLED : SCENE_FILTERDISABLED, "chechbox-enabled")); //NOI18N
    }
}
