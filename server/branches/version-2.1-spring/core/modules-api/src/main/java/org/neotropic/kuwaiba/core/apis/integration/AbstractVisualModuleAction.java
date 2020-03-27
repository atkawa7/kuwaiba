/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.core.apis.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A module action has two parts: One is the actual, headless (that is, without 
 * graphical interface or any other mechanism to capture the necessary parameters) {@link Abstract}, and 
 * an optional {@link AbstractVisualModuleAction} that in the end, will call the real 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <W> The visual component that will be displayed upon triggering the action.
 */
public abstract class AbstractVisualModuleAction<W> {
    /**
     * Icon for buttons, menu entries, widget cards, etc. SVG images are encouraged, because they can be easily rescaled.
     */
    protected byte[] icon;
    /**
     * Settings useful to renderer to display the action. Currently suggested and supported options: bold (boolean) and color (HTML hex RGB value).
     */
    protected Properties formatOptions;
    /**
     * In case this is a composed action with sub-actions.
     */
    protected List<AbstractModuleAction> childrenActions;
    /**
     * Those interested in being notified about the result of an action.
     */
    protected List<ActionCompletedListener> listeners;

    public AbstractVisualModuleAction() {
        this.listeners = new ArrayList<>();
    }
    
    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public Properties getFormatOptions() {
        return formatOptions;
    }

    public void setFormatOptions(Properties formatOptions) {
        this.formatOptions = formatOptions;
    }

    public List<AbstractModuleAction> getChildrenActions() {
        return childrenActions;
    }

    public void setChildrenActions(List<AbstractModuleAction> childrenActions) {
        this.childrenActions = childrenActions;
    }
    
    public void registerActionCompletedLister(ActionCompletedListener listener) {
        listeners.add(listener);
    }
    
    public void unregisterListeners() {
        listeners.clear();
    }
    
    public void fireActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent ev) {
        listeners.stream().forEach(aListener -> {
                aListener.actionCompleted(ev);
            });
    }
    
    /**
     * The visual component (a XXLayout, for example), to be embedded in a Dialog upon triggering the action.
     * @param parameters The initial parameters necessary to build the visual component. Some or all these parameters might be later
     * passed to the underlying {@link AbstractModuleAction}.
     * @return The visual component.
     */
    public abstract W getVisualComponent(ModuleActionParameter... parameters);
    /**
     * The underlying action wrapped by this visual object.
     * @return The action.
     */
    public abstract AbstractModuleAction getModuleAction();
}
