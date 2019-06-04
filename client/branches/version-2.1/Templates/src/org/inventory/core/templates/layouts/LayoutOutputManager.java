/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.templates.layouts;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Manager to show in an output top component, messages from the layout designer
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class LayoutOutputManager {
    HashMap<LocalObjectListItem, LayoutOutput> outputs;
    
    private static LayoutOutputManager instance;
    
    private LayoutOutputManager() {
        outputs = new HashMap();
    }   
    
    public static LayoutOutputManager getInstance() {
        return instance == null ? instance = new LayoutOutputManager() : instance;
    }
    
    public LayoutOutput getLayoutOutput(LocalObjectListItem listItem) {
        if (!outputs.containsKey(listItem))
            outputs.put(listItem, new LayoutOutput(listItem));
        return outputs.get(listItem);
    }
        
    public class LayoutOutput {
        private final InputOutput io;

        public LayoutOutput(LocalObjectLight object) {
            io = IOProvider.getDefault().getIO(String.format("Layout Output | %s", object.getName()), false);
        }
        
        public void printLine(String text, Color color) {
            try {
                IOColorLines.println(io, text, color);
            } catch (IOException ex) {
            }
        }
    }
}
