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

package org.inventory.core.visual.actions.providers;

import java.awt.Point;
import org.inventory.core.visual.scene.AbstractNodeWidget;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Action invoked when an element try to connect to other on the scene
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public abstract class SceneConnectProvider implements ConnectProvider {

    private GraphScene scene;

    public SceneConnectProvider(GraphScene scene){
        this.scene=scene;
    }

    @Override
    public boolean isSourceWidget(Widget sourceWidget) {
        return sourceWidget instanceof AbstractNodeWidget; 
    }

    @Override
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        if (targetWidget instanceof AbstractNodeWidget)
            return  ConnectorState.ACCEPT;
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
    public abstract void createConnection(Widget sourceWidget, Widget targetWidget);

}
