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
import java.awt.Dimension;
import org.inventory.communications.core.LocalObjectLight;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Root to all widgets representing and object node
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AbstractObjectNodeWidget extends Widget {
    /**
     * Default color to be used to paint widget
     */
    public static final Color DEFAULT_COLOR = Color.ORANGE;
    /**
     * Default widget size
     */
    public static final Dimension DEFAULT_DIMENSION = new Dimension(10, 10);
    /**
     * String for Selection tool
     */
    public final static String ACTION_SELECT = "selection"; //NOI18
    /**
     * String for Connect tool
     */
    public final static String ACTION_CONNECT = "connect"; //NOI18
    /**
     * Wrapped object
     */
    protected LocalObjectLight object;
    /**
     * Widget's lookup
     */
    private Lookup lookup;

    public AbstractObjectNodeWidget(Scene scene, LocalObjectLight object) {
        super(scene);
        this.object = object;
        setPreferredSize(DEFAULT_DIMENSION);
        setBackground(DEFAULT_COLOR);
        setToolTipText(object.toString());
        this.lookup = Lookups.singleton(object);
        createActions(ACTION_SELECT);
        createActions(ACTION_CONNECT);
        setOpaque(true);
    }

    public LocalObjectLight getObject() {
        return object;
    }

    public void setObject(LocalObjectLight object) {
        this.object = object;
    }
    
    @Override
    public Lookup getLookup(){
        return lookup;
    }
    
    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        if (state.isSelected())
            setBackground(Color.BLACK);
        else
            setBackground(DEFAULT_COLOR);
    }
    
}