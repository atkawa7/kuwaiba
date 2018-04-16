/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
 */
package com.neotropic.api.forms;

import java.util.ArrayList;
import java.util.List;

/**
 * A element container is an element which can contain other elements containers 
 * and fields.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class AbstractElementContainer extends AbstractElement {
    private List<AbstractElement> children;

    public List<AbstractElement> getChildren() {
        return children;
    }
    
    public void addChild(AbstractElement child) {
        if (children == null)
            children = new ArrayList();
        children.add(child);
    }
    
    public void removeChild(AbstractElement child) {
        if (children != null)
            children.remove(child);
    }
}
