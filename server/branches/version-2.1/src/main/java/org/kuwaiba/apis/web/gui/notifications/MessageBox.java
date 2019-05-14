/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.apis.web.gui.notifications;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.kuwaiba.util.i18n.I18N;
/**
 *
 * @author johnyortega
 */
public class MessageBox {
    public enum Type {
        YES_NO        
    }
    public enum NotificationType {
        WARNING,
        ERROR,
        INFORMATION, 
        SUCCESS
    }
    private boolean continues = false;
    private static MessageBox instance;
    private Button.ClickListener clickListener;
    
    private MessageBox() {
    }
    
    public static MessageBox getInstance() {
        return instance == null ? instance = new MessageBox() : instance;
    }
    
    public MessageBox showMessage(Component message) {
        
        Window window = new Window();
                
        GridLayout gridLayout = new GridLayout();
        gridLayout.setMargin(true);
        gridLayout.setSpacing(true);
        gridLayout.setColumns(2);
        gridLayout.setRows(2);
        
        Button btnNo = new Button(I18N.gm("no"), VaadinIcons.CLOSE);
        
        btnNo.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                continues = false;
                window.close();
                clickListener.buttonClick(event);
            }
        });
        Button btnYes = new Button(I18N.gm("yes"), VaadinIcons.CHECK);
        btnYes.addStyleName(ValoTheme.BUTTON_PRIMARY);
        btnYes.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
        
        btnYes.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                continues = true;                
                window.close();
                clickListener.buttonClick(event);
            }
        });
        gridLayout.addComponent(message, 0, 0, 1, 0);
        gridLayout.addComponent(btnYes, 0, 1);
        gridLayout.addComponent(btnNo, 1, 1);
        
                        
        gridLayout.setComponentAlignment(message, Alignment.MIDDLE_CENTER);
        gridLayout.setComponentAlignment(btnYes, Alignment.MIDDLE_RIGHT);
        gridLayout.setComponentAlignment(btnNo, Alignment.MIDDLE_LEFT);
        
        window.setModal(true);
        window.center();
        
        window.setHeight(20, Sizeable.Unit.PERCENTAGE);
        window.setWidth(20, Sizeable.Unit.PERCENTAGE);        
        gridLayout.setSizeFull();
        
        window.setContent(gridLayout);
        
        Page.getCurrent().getUI().addWindow(window);
                
        return getInstance();
    }    
    
    public void addClickListener(Button.ClickListener clickListener) {
        this.clickListener = clickListener;
    }
    
    public boolean continues() {
        return continues;
    }
}
