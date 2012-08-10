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

package org.inventory.core.services.utils;

import java.awt.LayoutManager;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * This is a general purpose component used to display complex dialogs. Provides capabilities to retrieve the fields
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class JComplexDialogPanel extends JPanel{

    private HashMap<String, JComponent> components;

    public JComplexDialogPanel(LayoutManager layout, JComponent ... components) {
        this.components = new HashMap<String, JComponent>();
        setLayout(layout);
        int i = 0;
        for (JComponent component : components){
            this.components.put(component.getName() == null ? String.valueOf(i) : component.getName(), component);
            add(component);
            i++;
        }
    }

    /**
     * Adds a new component to the list of components. Replaces the old one if it already existed
     * @param name name for this component
     * @param component the component itself
     */
    public void addComponent(String name, JComponent component){
        components.put(name, component);
    }

    /**
     * Removes a component from the panel if it exists
     * @param name The component's name
     */
    public void removeComponent(String name){
        components.remove(name);
    }

    /**
     * Returns a named component
     * @param name Component's name
     * @return The component
     */
    public JComponent getComponent(String name){
        return components.get(name);
    }
}
