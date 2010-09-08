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

package org.inventory.views.viewrenderer;

import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Contains the business logic for the associated TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ViewRendererService implements LookupListener{
    
    private ViewRendererTopComponent vrtc;
    private Lookup.Result selectedNodes;

    public ViewRendererService(ViewRendererTopComponent _vrtc){
        this.vrtc = _vrtc;
    }

    /**
     * Add this instance as listener for the selected nodes in the NavigationTree.
     * Should be called when the TopComponent is opened
     */
    public void initializeLookListener(){
        selectedNodes = Lookup.getDefault().lookupResult(LocalObjectLight.class);
        selectedNodes.addLookupListener(this);
    }

    /**
     * Removes this instance as listener for the selected nodes in the NavigationTree.
     * Should be called when the TopComponent is closed
     */
    public void terminateLookupListener(){
        selectedNodes.removeLookupListener(this);
    }

    public void resultChanged(LookupEvent ev) {
        Lookup.Result lookupResult = (Lookup.Result)ev.getSource();
        if(lookupResult.allInstances().size() == 1){
           LocalObjectLight myObject = (LocalObjectLight)lookupResult.allInstances().iterator().next();

        } else
            vrtc.getNotifier().showStatusMessage("More than one object selected. No view available", true);
    }
}
