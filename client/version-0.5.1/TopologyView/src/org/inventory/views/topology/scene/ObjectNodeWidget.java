/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.views.topology.scene;

import java.awt.Image;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.LocalObjectLight;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * A remake of the original ObjectNodeWidget class formerly coded for the ObjectView module. This one
 * aims to be used in all views in the future, since it improves the behavior of the original in multiple
 * ways
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectNodeWidget extends IconNodeWidget implements Lookup.Provider{

    /**
     * String for Selection tool
     */
    public final static String ACTION_SELECT = "selection"; //NOI18
    /**
     * String for Connect tool
     */
    public final static String ACTION_CONNECT = "connect"; //NOI18
    /**
     * The business object behind the widget
     */
    private LocalObjectLight object;
    /**
     * Widget's lookup
     */
    private Lookup lookup;

    /**
     *
     * @param scene The scene that will contain this widget
     * @param object The business object represented by this widget
     */
    public ObjectNodeWidget(GraphScene scene, LocalObjectLight object) {
        super(scene);
        this.object = object;
        this.lookup = Lookups.singleton(object);
        createActions(ACTION_SELECT);
        createActions(ACTION_CONNECT);
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
}