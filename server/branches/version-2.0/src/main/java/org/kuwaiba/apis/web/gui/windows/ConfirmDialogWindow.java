/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.windows;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class ConfirmDialogWindow extends Window {
    public static final int YES_NO_OPTION = 0;
    public static final int YES_NO_CANCEL_OPTION = 1;
    public static final int OK_CANCEL_OPTION = 2;
    public static final int ONLY_OK_OPTION = 3;
    
    public static final int YES_OPTION = 0;
    public static final int NO_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    public static final int OK_OPTION = 0;
    
    private int option = CANCEL_OPTION;
    
    public ConfirmDialogWindow(Window.CloseListener closeListener, 
            String caption, int options) {
        super(caption);
        center();
        addCloseListener(closeListener);
        
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        
        content.addComponent(initContent());
        content.addComponent(initButtons(options));
                        
        setResizable(false);
        setClosable(false);
        setContent(content);
    }
    /**
     * 
     * @return content
     */
    public abstract VerticalLayout initContent();
    
    private HorizontalLayout initButtons(int options) {
        HorizontalLayout content = new HorizontalLayout();
        content.setMargin(true);
        
        Button btnYes = new Button("Yes");
        btnYes.addClickListener(new Button.ClickListener() {
            
            @Override
            public void buttonClick(Button.ClickEvent event) {
                option = YES_OPTION;
                close();
            }
        });
        
        Button btnNo = new Button("No");
        btnNo.addClickListener(new Button.ClickListener() {
            
            @Override
            public void buttonClick(Button.ClickEvent event) {
                option = NO_OPTION;
                close();
            }
        });
        
        Button btnOk = new Button("Ok");
        btnOk.addClickListener(new Button.ClickListener() {
            
            @Override
            public void buttonClick(Button.ClickEvent event) {
                option = OK_OPTION;
                close();
            }
        });
                
        Button btnCancel = new Button("Cancel");
        btnCancel.addClickListener(new Button.ClickListener() {
            
            @Override
            public void buttonClick(Button.ClickEvent event) {
                option = CANCEL_OPTION;
                close();
            }
        });
        
        
        switch (options) {
            case YES_NO_OPTION:
                content.addComponent(btnYes);
                content.addComponent(btnNo);
                break;
            case YES_NO_CANCEL_OPTION:
                content.addComponent(btnYes);
                content.addComponent(btnNo);
                content.addComponent(btnCancel);
                break;
            case OK_CANCEL_OPTION:
                content.addComponent(btnOk);
                content.addComponent(btnCancel);
                break;
            case ONLY_OK_OPTION:
                content.addComponent(btnOk);
                break;
        }
        return content;
    }
    
    public int getOption() {
        return option;
    }
    
    public void setOption(int option) {
        this.option = option;
    }
}