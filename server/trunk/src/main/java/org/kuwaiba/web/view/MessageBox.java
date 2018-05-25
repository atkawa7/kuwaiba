/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.view;

import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
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
        window.setClosable(false);
        window.setResizable(false);
        window.setModal(true);
        window.setHeight("35%");
        window.setWidth("35%");
        
        VerticalSplitPanel vsp = new VerticalSplitPanel();
        vsp.setSplitPosition(85, Sizeable.Unit.PERCENTAGE);
        vsp.setSizeFull();
        //GridLayout main = new GridLayout();
                
        vsp.setFirstComponent(message);
        
        GridLayout gl = new GridLayout();
        gl.setSizeFull();
        gl.setColumns(2);
        gl.setRows(1);
        
        Button btnYes = new Button("Yes");
        btnYes.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                continues = true;     
                window.close();
                clickListener.buttonClick(event);
            }
        });
        
        Button btnNo = new Button("No");
        btnYes.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                continues = false;                
                window.close();
                clickListener.buttonClick(event);
            }
        });
        
        gl.addComponent(btnYes);
        gl.addComponent(btnNo);
        
        gl.setComponentAlignment(btnYes, Alignment.MIDDLE_RIGHT);
        gl.setComponentAlignment(btnNo, Alignment.MIDDLE_LEFT);
        
        vsp.setSecondComponent(gl);
        
        window.setContent(vsp);
        
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
