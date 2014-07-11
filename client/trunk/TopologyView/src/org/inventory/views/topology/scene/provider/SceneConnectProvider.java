/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.inventory.views.topology.scene.provider;

import java.awt.Point;
import java.util.Random;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.scene.AbstractNodeWidget;
import org.inventory.views.topology.scene.ObjectNodeWidget;
import org.inventory.views.topology.scene.TopologyViewScene;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Action invoked when an element try to connect to other on the scene
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class SceneConnectProvider implements ConnectProvider {

    private Object source = null;
    private Object target = null;
    
    private TopologyViewScene scene;

    public SceneConnectProvider(TopologyViewScene scene){
        this.scene=scene;
    }

    @Override
    public boolean isSourceWidget(Widget sourceWidget) {
        if (sourceWidget instanceof ObjectNodeWidget || sourceWidget instanceof AbstractNodeWidget){
            Object object = scene.findObject(sourceWidget);
            source = scene.isNode(object) ? (LocalObjectLight)object : null;
            return source != null;
        }
        return false;
    }

    @Override
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        if (targetWidget instanceof ObjectNodeWidget || targetWidget instanceof AbstractNodeWidget){
            Object object = scene.findObject(targetWidget);
            target = scene.isNode(object) ? (LocalObjectLight)object : null;
            if (target != null)
                return ! source.equals(target) ? ConnectorState.ACCEPT : ConnectorState.REJECT_AND_STOP;
            return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
        }
        return ConnectorState.REJECT;
    }

    @Override
    public boolean hasCustomTargetWidgetResolver(Scene scene) {
       return false;
    }

    @Override
    public Widget resolveTargetWidget(Scene scene, Point sceneLocation) {
        return null;
    }

    @Override
    public void createConnection(Widget sourceWidget, Widget targetWidget) {
        Random randomGenerator = new Random();
        String edge = "topologyEdge"+randomGenerator.nextInt(1000);
        scene.addEdge(edge);
        scene.setEdgeSource(edge, source);
        scene.setEdgeTarget(edge, target);
        scene.validate();
    }

}
