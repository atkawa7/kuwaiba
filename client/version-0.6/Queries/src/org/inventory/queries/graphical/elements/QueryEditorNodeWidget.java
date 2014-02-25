/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.queries.graphical.elements;

import org.inventory.queries.graphical.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.vmd.VMDNodeWidget;

/**
 * Abstract class root of all possible node widgets within the scene
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class QueryEditorNodeWidget extends VMDNodeWidget{

    protected String defaultPinId;

    public QueryEditorNodeWidget(QueryEditorScene scene, VMDColorScheme scheme) {
        super (scene,scheme);
    }

    public String getDefaultPinId(){
        return defaultPinId;
    }
    
    public abstract void build(String id);
}
