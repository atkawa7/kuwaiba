/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.views.objectview.scene;

import java.awt.Color;
import org.inventory.core.services.interfaces.LocalObject;
import org.netbeans.api.visual.widget.ConnectionWidget;

/**
 * Extends the functionality of a simple connection widget
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectConnectionWidget extends ConnectionWidget{

    /**
     * Some constants
     */
    public static Color COLOR_WIRE = new Color(255, 0, 0);
    public static Color COLOR_WIRELESS = new Color(0, 0, 255);

    /**
     * The wrapped business object
     */
    private LocalObject object;

    public ObjectConnectionWidget(ViewScene scene, LocalObject connection){
        super(scene);
        this.object = connection;
        getActions().addAction(scene.createSelectAction());
    }

    public LocalObject getObject() {
        return object;
    }
}
