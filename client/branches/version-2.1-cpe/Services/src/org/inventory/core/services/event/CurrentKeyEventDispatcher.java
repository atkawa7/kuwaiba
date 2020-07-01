/**
 *  Copyright 2010-2019, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.core.services.event;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.util.HashMap;
import java.util.Map;
import org.openide.windows.TopComponent;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CurrentKeyEventDispatcher {
    private final Map<TopComponent, KeyEventDispatcher> keyEventDispatchers = new HashMap();
    private TopComponent currentTopComponent;
    
    private static CurrentKeyEventDispatcher instance;
    
    private CurrentKeyEventDispatcher() {
    }
    
    public static CurrentKeyEventDispatcher getInstance() {
        return instance == null ? instance = new CurrentKeyEventDispatcher() : instance;
    }
    
    public TopComponent getCurrentTopComponent() {
        return currentTopComponent;
    }
    
    public void addKeyEventDispatcher(TopComponent topComponent, KeyEventDispatcher keyEventDispatcher) {
        keyEventDispatchers.put(topComponent, keyEventDispatcher);
    }
    
    public void updateKeyEventDispatcher(TopComponent topComponent) {
        setKeyEventDispatcher(topComponent);
    }
    
    private void setKeyEventDispatcher(TopComponent topComponent) {
        if (this.currentTopComponent != null)
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyEventDispatchers.get(currentTopComponent));
        
        this.currentTopComponent = topComponent;  
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatchers.get(topComponent));
    }    
}
