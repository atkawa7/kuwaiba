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

import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.export.ExportableScene;
import org.inventory.core.visual.export.Layer;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.Scene;

/**
 * Root class to all GraphScenes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class AbstractScene extends GraphScene<LocalObjectLight, LocalObjectLight> 
        implements ExportableScene{
    @Override
    public Scene getExportable(){
        return this;
    }
    
    @Override
    public Layer[] getLayers(){
        return null;
    }
}
