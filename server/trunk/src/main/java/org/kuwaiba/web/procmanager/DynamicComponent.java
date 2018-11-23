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
package org.kuwaiba.web.procmanager;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DynamicComponent extends HorizontalLayout {
    private Component componentLeft;
    private Component componentCenter;
    private VerticalLayout hide;
    
    public DynamicComponent() {
    }
        
    public DynamicComponent(Component componentLeft, Component componentCenter) {
        this.componentLeft = componentLeft;
        this.componentCenter = componentCenter;
        
        initializeComponent();
    }
    
    public Component getComponentLeft() {
        return componentLeft;
    }
    
    public void setComponentLeft(Component componentLeft) {   
        if (hide != null && hide.getComponentIndex(this.componentLeft) != -1)
            hide.removeComponent(this.componentLeft);
        
        this.componentLeft = componentLeft;
        
        if (hide != null)
            hide.addComponent(this.componentLeft, 0);
    }
    
    public Component getComponentCenter() {
        return componentCenter;
    }
    
    public void setComponentCenter(Component componentCenter) {
        if (getComponentIndex(this.componentCenter) != -1)
            removeComponent(this.componentCenter);
        
        this.componentCenter = componentCenter;
        
        addComponent(this.componentCenter);
        setExpandRatio(componentCenter, 0.75f); 
    }
    
    public final void initializeComponent() {
        addStyleName("dynamiccomponent");
        setSizeFull();
        setSpacing(false);
        
        HorizontalLayout hideLeft = new HorizontalLayout();
        HorizontalLayout showLeft = new HorizontalLayout();
        
        Button btnShowLeft = new Button();        
        Button btnHideLeft = new Button();
        
        hideLeft.addStyleName("left");
        showLeft.addStyleName("left");
        
        showLeft.setSizeFull();
        hideLeft.setSizeFull();
        
        showLeft.setSpacing(false);
        
        addComponent(hideLeft);
        addComponent(componentCenter);
        // Hides
        btnShowLeft.setIcon(VaadinIcons.CHEVRON_CIRCLE_RIGHT);
        btnShowLeft.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btnShowLeft.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        btnShowLeft.addStyleName(ValoTheme.BUTTON_LARGE);
        
        hideLeft.addComponent(btnShowLeft);
        hideLeft.setComponentAlignment(btnShowLeft, Alignment.TOP_CENTER);
        // Shows
        btnHideLeft.setIcon(VaadinIcons.CHEVRON_CIRCLE_LEFT);
        btnHideLeft.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btnHideLeft.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        btnHideLeft.addStyleName(ValoTheme.BUTTON_LARGE);
                
        hide = new VerticalLayout();        
        hide.addStyleName("left");
        hide.setSizeFull();
        hide.addComponent(btnHideLeft);
        hide.setComponentAlignment(btnHideLeft, Alignment.TOP_CENTER);
        
        showLeft.addComponent(componentLeft);
        showLeft.addComponent(hide);
                
        showLeft.setExpandRatio(componentLeft, 0.90f);
        showLeft.setExpandRatio(hide, 0.10f);
                
        // Actions
        btnHideLeft.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                DynamicComponent.this.removeComponent(showLeft);
                
                DynamicComponent.this.addComponent(hideLeft, 0);
                
                DynamicComponent.this.setExpandRatio(hideLeft, 0.03f);
                DynamicComponent.this.setExpandRatio(componentCenter, 0.97f);
            }
        });
        
        btnShowLeft.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                DynamicComponent.this.removeComponent(hideLeft);
                
                DynamicComponent.this.addComponent(showLeft, 0);
                
                DynamicComponent.this.setExpandRatio(showLeft, 0.25f);
                DynamicComponent.this.setExpandRatio(componentCenter, 0.75f);
            }
        });
        
        setExpandRatio(hideLeft, 0.03f);
        setExpandRatio(componentCenter, 0.97f);        
    }
}
