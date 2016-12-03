/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.view;

import com.neotropic.kuwaiba.prototypes.actions.AbstractAction;
import com.sun.org.apache.xerces.internal.util.DOMUtil;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

/**
 * The root of all components with a toolbar.
 * @author duckman
 */
public class AbstractTooledComponent extends CustomComponent {
    /**
     * Toolbar size small (icon size 16x16)
     */
    public static int TOOLBAR_SIZE_SMALL = 1;
    /**
     * Toolbar size small (icon size 22x22)
     */
    public static int TOOLBAR_SIZE_NORMAL = 2;
    /**
     * Toolbar size small (icon size 32x32)
     */
    public static int TOOLBAR_SIZE_BIG = 3;
    /**
     * Toolbar orientation vertical
     */
    public static int TOOLBAR_ORIENTATION_VERTICAL = 1;
    /**
     * Toolbar orientation horizontal
     */
    public static int TOOLBAR_ORIENTATION_HORIZONTAL = 2;
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
            toolbarLayout.setHeight(toolBarSize.size, Unit.PIXELS);
            toolbarLayout.setWidth("100%");
        } else {
            componentLayout = new HorizontalLayout();
            toolbarLayout = new VerticalLayout();
            toolbarLayout.setWidth(toolBarSize.size, Unit.PIXELS);
            toolbarLayout.setHeight("100%");
        }
        
        for (AbstractAction action : actions) {
            final Button btnAction = new Button(action.getIcon());
            btnAction.setDescription(action.getCaption());
            btnAction.setWidth(toolBarSize.size, Unit.PIXELS);
            btnAction.setHeight(toolBarSize.size, Unit.PIXELS);
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
    
    public void setMainComponent(Component mainComponent) {
        componentLayout.addComponent(mainComponent);
        ((AbstractOrderedLayout)componentLayout).setExpandRatio(mainComponent, 2);
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
