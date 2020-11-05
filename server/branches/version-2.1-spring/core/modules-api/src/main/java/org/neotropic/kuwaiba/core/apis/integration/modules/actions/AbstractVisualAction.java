/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.core.apis.integration.modules.actions;

import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/**
 * A module action has two parts: One is the actual, headless (that is, without 
 * graphical interface or any other mechanism to capture the necessary parameters) {@link Abstract}, and 
 * an optional {@link AbstractVisualAction} that in the end, will call the real 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <W> The visual component that will be displayed upon triggering the action.
 */
public abstract class AbstractVisualAction<W> implements Comparable<AbstractVisualAction> {
    /**
     * Icon for buttons, menu entries, widget cards, etc. SVG images are encouraged, because they can be easily rescaled.
     */
    protected byte[] icon;
    /**
     * Settings useful to renderer to display the action. Currently suggested and supported options: bold (boolean) and color (HTML hex RGB value, including #).
     */
    protected Properties formatOptions;
    /**
     * In case this is a composed action with sub-actions.
     */
    protected List<AbstractAction> childrenActions;
    /**
     * Those interested in being notified about the result of an action.
     */
    protected List<ActionCompletedListener> listeners = new ArrayList<>();
    /**
     * What is the expected behavior of the action. By default, the action opens a window.
     */
    protected VisualActionType type = VisualActionType.TYPE_WINDOW;

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

    public List<AbstractAction> getChildrenActions() {
        return this.childrenActions;
    }

    public void setChildrenActions(List<AbstractAction> childrenActions) {
        this.childrenActions = childrenActions;
    }
    
    public void registerActionCompletedLister(ActionCompletedListener listener) {
        listeners.add(listener);
    }
    
    public void unregisterListener(ActionCompletedListener listener) {
        this.listeners.remove(listener);
    }
    
    public void clearListeners() {
        this.listeners.clear();
    }
    
    public void fireActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent ev) {
        this.listeners.stream().forEach(aListener -> {
                aListener.actionCompleted(ev);
        });
    }
    
    /**
     * The visual component (a XXLayout, for example), to be embedded in a Dialog upon triggering the action.
     * @param parameters The initial parameters necessary to build the visual component. Some or all these parameters might be later
     * passed to the underlying {@link AbstractAction}.
     * @return The visual component.
     */
    public abstract W getVisualComponent(ModuleActionParameterSet parameters);
    /**
     * The underlying action wrapped by this visual object.
     * @return The action.
     */
    public abstract AbstractAction getModuleAction();
    
    @Override
    public int compareTo(AbstractVisualAction otherAction) {
        return Integer.compare(getModuleAction().getOrder(), otherAction.getModuleAction().getOrder());
    }
    
    /**
     * The web interface has always two menus: The top one, with access to other modules, and 
     * another one with actions particular to the current module called <code>quick actions</code>. 
     * This method specifies if a given action can be used as a quick actions or not.
     * @return If the current action can be used as a quick action. The default value is false;
     */
    public boolean isQuickAction() {
        return false;
    }
    
    /**
     * An enumeration with the possible behaviors of an action, such as opening a window, redirecting to a specific URL or replacing 
     * the current page contents.
     */
    public enum VisualActionType {
        /**
         * The action opens a new window (a Vaadin Dialog).
         */
        TYPE_WINDOW,
        /**
         * The action builds an embeddable component that will replace the current contents 
         * of the window (a Div or a Layout).
         */
        TYPE_EMBEDDED,
        /**
         * The action redirects to another page.
         */
        TYPE_REDIRECTION
    }
}
