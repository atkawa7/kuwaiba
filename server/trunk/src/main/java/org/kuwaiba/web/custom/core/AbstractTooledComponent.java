/**
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.custom.core;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;

/**
 * The root of all components with a toolbar.
 * @author Charles Edward Bedon Cortazar{@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractTooledComponent extends CustomComponent {
    /**
     * Toolbar size small (icon size 16x16)
     */
    public static final int TOOLBAR_SIZE_SMALL = 1;
    /**
     * Toolbar size small (icon size 24x24)
     */
    public static final int TOOLBAR_SIZE_NORMAL = 2;
    /**
     * Toolbar size small (icon size 32x32)
     */
    public static final int TOOLBAR_SIZE_BIG = 3;
    /**
     * Toolbar orientation vertical
     */
    public static final int TOOLBAR_ORIENTATION_VERTICAL = 1;
    /**
     * Toolbar orientation horizontal
     */
    public static final int TOOLBAR_ORIENTATION_HORIZONTAL = 2;
    /**
     * Vertical padding used to place the icon inside the button 
     */
    public static final int TOOLBAR_VERTICAL_PADDING = 5;
    /**
     * Horizontal padding used to place the icon inside the button 
     */
    public static final int TOOLBAR_HORIZONTAL_PADDING = 5;
    /**
     * Toolbar orientation
     */
    private int orientation;
    /**
     * Toolbar size
     */
    private int size;
    /**
     * Main layout
     */
    private Layout componentLayout;
    
    private Component mainComponent;
    /**
     * Main constructor
     * @param actions The list of actions to be represented as buttons in the toolbar
     * @param orientation The orientation of the toolbar. Use TOOLBAR_ORIENTATION_VERTICAL or TOOLBAR_ORIENTATION_HORIZONTAL. If TOOLBAR_ORIENTATION_VERTICAL, the toolbar will be placed on the left side of the screen
     * @param toolBarSize The size of the icons.
     */
    public AbstractTooledComponent(AbstractAction[] actions, int orientation, ToolBarSize toolBarSize) {
        Layout toolbarLayout;
        
        if (orientation == TOOLBAR_ORIENTATION_HORIZONTAL) {
            componentLayout = new VerticalLayout();
            toolbarLayout = new HorizontalLayout();
            toolbarLayout.setHeight(toolBarSize.size + 2 * TOOLBAR_VERTICAL_PADDING, Unit.PIXELS);
        } else {
            componentLayout = new HorizontalLayout();
            toolbarLayout = new VerticalLayout();
            toolbarLayout.setWidth(toolBarSize.size + 2 * TOOLBAR_HORIZONTAL_PADDING, Unit.PIXELS);
        }
        
        for (final AbstractAction action : actions) {
            final Button btnAction = new Button(action.getIcon());
            btnAction.setDescription(action.getCaption());
            btnAction.setStyleName("v-button-icon-only");
            btnAction.setWidth(toolBarSize.size + 2 * TOOLBAR_HORIZONTAL_PADDING, Unit.PIXELS);
            btnAction.setHeight(toolBarSize.size + 2 * TOOLBAR_VERTICAL_PADDING, Unit.PIXELS);
            btnAction.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    action.actionPerformed(btnAction, event);
                }
            });
            toolbarLayout.addComponent(btnAction);
        }
        
        componentLayout.addComponent(toolbarLayout);
        
        componentLayout.setSizeFull();
        
        setCompositionRoot(componentLayout);
        setSizeFull();
    }
    
    public Component getToolbarLayout() {
        return ((AbstractOrderedLayout) componentLayout).getComponent(0);
    }
        
    public void setMainComponent(Component mainComponent) {
        if (mainComponent != null) {
            this.mainComponent = mainComponent;
            componentLayout.addComponent(this.mainComponent);
            ((AbstractOrderedLayout)componentLayout).setExpandRatio(this.mainComponent, 2);
        }
        else {
            componentLayout.removeComponent(this.mainComponent);
            this.mainComponent = null;
        }
    }
    
    public Component getMainComponent() {
        return mainComponent;
    }
        
    public enum ToolBarSize {
        SMALL(16), NORMAL(24), BIG(32);
        
        private final int size;
        
        private ToolBarSize(int size) {        
            this.size = size;
        }
        
        public int size() { return size; }
    }
    
}
