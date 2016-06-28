/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This is a general purpose component used to display complex dialogs. Provides capabilities to retrieve the fields. It
 * uses a BoxLayout.PAGE_AXIS layout, so the components should be provided in pair label-component
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class JComplexDialogPanel extends JPanel {

    private HashMap<String, JComponent> components;

    public JComplexDialogPanel(String[] labels, JComponent[] components) {
        if (labels.length != components.length)
            throw new RuntimeException("You must provide the same number of labels and components");
        
        this.components = new HashMap<>();
        setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(2, 2, 2, 2);
        for (int i = 0 ; i < components.length;  i++ ){
            this.components.put(components[i].getName(), components[i]);
            gc.gridy = i;
            gc.gridx = 0;
            add(new JLabel(labels[i]), gc);
            gc.gridx = 1;
            add(components[i], gc);
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
