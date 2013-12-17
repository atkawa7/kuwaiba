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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import org.inventory.views.objectview.scene.ViewScene;

/**
 * @author David Kaspar, modified by Charles Edward Bedon Cortazar <charles.bedon@zoho.com> for project Kuwaiba 2010
 */
public final class CustomMoveAction extends WidgetAction.LockedAdapter {

    private MoveStrategy strategy;
    private MoveProvider provider;

    private Widget movingWidget = null;
    private Point dragSceneLocation = null;
    private Point originalSceneLocation = null;
    private Point initialMouseLocation = null;
    private ArrayList<ActionListener> listeners;

    public CustomMoveAction (MoveStrategy strategy, MoveProvider provider) {
        this.strategy = strategy;
        this.provider = provider;
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
            movingWidget = widget;
            initialMouseLocation = event.getPoint ();
            originalSceneLocation = provider.getOriginalLocation (widget);
            if (originalSceneLocation == null)
                originalSceneLocation = new Point ();
            dragSceneLocation = widget.convertLocalToScene (event.getPoint ());
            provider.movementStarted (widget);
            return State.createLocked (widget, this);
        }
        return State.REJECTED;
    }

    @Override
    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        boolean state;
        if (initialMouseLocation != null  &&  initialMouseLocation.equals (event.getPoint ()))
            state = true;
        else
            state = move (widget, event.getPoint ());
        if (state) {
            movingWidget = null;
            dragSceneLocation = null;
            originalSceneLocation = null;
            initialMouseLocation = null;
            provider.movementFinished (widget);
        }           
        return state ? State.CONSUMED : State.REJECTED;
    }

    @Override
    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGE, "node-move-operation"));
        return move (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean move (Widget widget, Point newLocation) {
        if (movingWidget != widget)
            return false;
        initialMouseLocation = null;
        newLocation = widget.convertLocalToScene (newLocation);
        Point location = new Point (originalSceneLocation.x + newLocation.x - dragSceneLocation.x, originalSceneLocation.y + newLocation.y - dragSceneLocation.y);
        provider.setNewLocation (widget, strategy.locationSuggested (widget, originalSceneLocation, location));
        return true;
    }
}