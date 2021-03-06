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
 * Modified by Charles Bedon for project Kuwaiba 2010
 */

package org.inventory.views.objectview.scene.actions;

import java.awt.Point;
import org.netbeans.api.visual.action.MoveControlPointProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import org.inventory.views.objectview.scene.ViewScene;

/**
 * This class is used to fire an action event whenever this is triggered so we can
 * track the change and notify the TopComponent to mark it as unsaved. It's a modified
 * copy of org.netbeans.modules.visual.action.MoveControlPointAction since this is not
 * reachable from the current code.
 * This class was licensed under CDDL and keeps the licensing
 * @author Alex, modified by Charles Edward Bedon Cortazar <charles.bedon@zoho.com> for project Kuwaiba 2010
 */
public final class CustomMoveControlPointAction extends WidgetAction.LockedAdapter {

    private MoveControlPointProvider provider;
    private ConnectionWidget.RoutingPolicy routingPolicy;

    private ConnectionWidget movingWidget = null;
    private Point controlPointLocation;
    private int controlPointIndex;
    private Point lastLocation = null;
    private ArrayList<ActionListener> listeners;

    public CustomMoveControlPointAction (MoveControlPointProvider provider, ConnectionWidget.RoutingPolicy routingPolicy) {
        this.provider = provider;
        this.routingPolicy = routingPolicy;
    }

    public void addActionListener(ActionListener listener){
        if (listeners == null)
            listeners = new ArrayList<ActionListener>();
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeActionListener(ActionListener listener){
        if (listeners == null)
            return;
        listeners.remove(listener);
    }

    public void clearActionListeners(){
        if (listeners != null)
            listeners.clear();
    }

    public void fireChangeEvent(ActionEvent ev){
        for (ActionListener listener : listeners)
            listener.actionPerformed(ev);
    }

    protected boolean isLocked () {
        return movingWidget != null;
    }

    @Override
    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (isLocked ())
            return State.createLocked (widget, this);
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 1) {
            if (widget instanceof ConnectionWidget) {
                ConnectionWidget conn = (ConnectionWidget) widget;
                controlPointIndex = conn.getControlPointHitAt (event.getPoint ());
                if (controlPointIndex >= 0) {
                    movingWidget = conn;
                    controlPointLocation = new Point (conn.getControlPoints (). get (controlPointIndex));
                    lastLocation = new Point (event.getPoint ());
                    return State.createLocked (widget, this);
                } else {
                    movingWidget = null;
                }
            }
        }
        return State.REJECTED;
    }

    @Override
    public State mouseReleased(Widget widget, WidgetMouseEvent event) {
        State state = move(widget, event.getPoint()) ? State.CONSUMED : State.REJECTED;
        movingWidget = null;
        if (state == State.CONSUMED)
            fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGE, "move-controlpoint-operation"));
        return state;
    }

    @Override
    public State mouseDragged(Widget widget, WidgetMouseEvent event) {
        if (move(widget, event.getPoint())) {
            return State.createLocked(widget, this);
        } else {
            movingWidget = null;
            return State.REJECTED;
        }
    }

    private boolean move(Widget widget, Point newLocation) {
        if (movingWidget != widget)
            return false;

        java.util.List<Point> controlPoints = movingWidget.getControlPoints ();
        if (controlPointIndex < 0  ||  controlPointIndex >= controlPoints.size ())
            return false;

        Point location = new Point (controlPointLocation);
        location.translate (newLocation.x - lastLocation.x, newLocation.y - lastLocation.y);

        controlPoints = provider.locationSuggested (movingWidget, controlPointIndex, location);
        if (controlPoints == null)
            return false;

        if (routingPolicy != null)
            movingWidget.setRoutingPolicy (routingPolicy);
        movingWidget.setControlPoints (controlPoints, false);
        return true;
    }

}