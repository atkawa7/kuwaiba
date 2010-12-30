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
 * Modified By Charles Edward Bedon Cortazar <charles.bedon@zoho.com> for project Kuwaiba
 */

package org.inventory.queries.graphical;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;
import javax.swing.JCheckBox;
import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.queries.graphical.elements.filters.BooleanFilterNodeWidget;
import org.inventory.queries.graphical.elements.filters.DateFilterNodeWidget;
import org.inventory.queries.graphical.elements.filters.NumericFilterNodeWidget;
import org.inventory.queries.graphical.elements.filters.SimpleCriteriaNodeWidget;
import org.inventory.queries.graphical.elements.filters.StringFilterNodeWidget;
import org.netbeans.api.visual.action.ActionFactory;
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
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.EventProcessingType;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * This scene is used in the graphical query editor. Due to a plain VMDGraph scene stores the object
 * keys as Strings, it's not suitable for our purposes, so we rather inherit from GraphPinScene and use
 * the same base code
 * @author David Kaspar
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class QueryEditorScene extends GraphPinScene<LocalClassMetadata, String, LocalAttributeMetadata>
        implements ItemListener{

    private LayerWidget backgroundLayer = new LayerWidget (this);
    private LayerWidget mainLayer = new LayerWidget (this);
    private LayerWidget connectionLayer = new LayerWidget (this);
    private LayerWidget upperLayer = new LayerWidget (this);

    private Router router;

    private WidgetAction moveControlPointAction = ActionFactory.createOrthogonalMoveControlPointAction ();
    private WidgetAction moveAction = ActionFactory.createMoveAction ();

    private SceneLayout sceneLayout;
    private VMDColorScheme scheme;

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

        getActions ().addAction (ActionFactory.createZoomAction ());
        getActions ().addAction (ActionFactory.createPanAction ());
        getActions ().addAction (ActionFactory.createRectangularSelectAction (this, backgroundLayer));

        sceneLayout = LayoutFactory.createSceneGraphLayout(this, new GridGraphLayout<LocalClassMetadata, String> ().setChecker (true));
    }

    /**
     * Implements attaching a widget to a node. The widget is VMDNodeWidget and has object-hover, select, popup-menu and move actions.
     * @param node the node
     * @return the widget attached to the node
     */
    protected Widget attachNodeWidget (LocalClassMetadata node) {
        VMDNodeWidget widget = new VMDNodeWidget (this, scheme);
        mainLayer.addChild (widget);

        widget.getHeader ().getActions ().addAction (createObjectHoverAction ());
        widget.getActions ().addAction (createSelectAction ());
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
    protected Widget attachPinWidget (LocalClassMetadata node, LocalAttributeMetadata pin) {
        VMDPinWidget widget = new VMDPinWidget (this, scheme);
        widget.setPinName(pin.getDisplayName());
        JCheckBox insideCheck = new JCheckBox();
        insideCheck.addItemListener(this);
        //We set the type of attribute associated to the check so the filter can be created
        insideCheck.putClientProperty("filterType", pin.getType()); //NOI18N
        insideCheck.putClientProperty("attribute", pin); //NOI18N
        widget.addChild(new ComponentWidget(this, insideCheck));
        ((VMDNodeWidget) findWidget (node)).attachPinWidget (widget);
        widget.getActions ().addAction (createObjectHoverAction ());
        widget.getActions ().addAction (createSelectAction ());

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
        connectionWidget.getActions ().addAction (moveControlPointAction);

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
    protected void attachEdgeSourceAnchor (String edge, LocalAttributeMetadata oldSourcePin, LocalAttributeMetadata sourcePin) {
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
    protected void attachEdgeTargetAnchor (String edge, LocalAttributeMetadata oldTargetPin, LocalAttributeMetadata targetPin) {
        ((ConnectionWidget) findWidget (edge)).setTargetAnchor (getPinAnchor (targetPin));
    }

    private Anchor getPinAnchor (LocalAttributeMetadata pin) {
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


    /**
     * Listen for checkbox selections
     * @param e
     */
    public void itemStateChanged(ItemEvent e) {
        JCheckBox insideCheck = (JCheckBox) e.getSource();
        if (insideCheck.isSelected()){
            if (insideCheck.getClientProperty("filterType").equals(LocalObjectLight.class)){ //NOI18N

            }else{
                SimpleCriteriaNodeWidget simpleFilter=null;
                if (insideCheck.getClientProperty("filterType").equals(String.class)) //NOI18N
                    simpleFilter = new StringFilterNodeWidget(this);
                else
                    if (insideCheck.getClientProperty("filterType").equals(Integer.class) || //NOI18N
                            insideCheck.getClientProperty("filterType").equals(Float.class) || //NOI18N
                            insideCheck.getClientProperty("filterType").equals(Long.class)) //NOI18N
                        simpleFilter = new NumericFilterNodeWidget(this);
                    else
                        if (insideCheck.getClientProperty("filterType").equals(Boolean.class)) //NOI18N
                            simpleFilter = new BooleanFilterNodeWidget(this);
                        else
                            if (insideCheck.getClientProperty("filterType").equals(Date.class)) //NOI18N
                                simpleFilter = new DateFilterNodeWidget(this);
                if (simpleFilter != null){
                    //addEdge("");
                    //addNode(null)
                    setEdgeSource("",(LocalAttributeMetadata)insideCheck.getClientProperty("attribute"));
                    //setEdgeTarget("",simpleFilter.getPin());
                }
            }

        }
    }
}
