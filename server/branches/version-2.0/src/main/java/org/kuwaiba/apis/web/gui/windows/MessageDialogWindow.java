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
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Custom dialog message implementation
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class MessageDialogWindow extends Window {
    public static final int YES_NO_OPTION = 0;
    public static final int YES_NO_CANCEL_OPTION = 1;
    public static final int OK_CANCEL_OPTION = 2;
    public static final int ONLY_OK_OPTION = 3;
    
    public static final int YES_OPTION = 0;
    public static final int NO_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    public static final int OK_OPTION = 3;
    
    private int option = CANCEL_OPTION;

    private final String BTN_YES_CAPTION = "Yes";
    private final String BTN_NO_CAPTION = "No";
    private final String BTN_OK_CAPTION = "Ok";
    private final String BTN_CANCEL_CAPTION = "Cancel";
    
    private Button btnYes;
    private Button btnNo;
    private Button btnOk;
    private Button btnCancel;
    /**
     * Listener for all buttons
     */
    Button.ClickListener clickListener;
    VerticalLayout content;
    
    public MessageDialogWindow(Window.CloseListener closeListener, 
            String title, int options) {
        super(title);
        center();
        if (closeListener != null)
            addCloseListener(closeListener);
        
        content = new VerticalLayout();
        
        Component simpleMainComponent = initSimpleMainComponent();
        
        if (simpleMainComponent != null)
            setMainComponent(simpleMainComponent);
        
        content.addComponent(initButtons(options));
                        
        setResizable(false);
        setClosable(false);
        setModal(true);
        
        setContent(content);
    }
    
    protected void setMainComponent(Component mainComponent) {
        content.addComponent(mainComponent, 0);
    }
    /**
     * A simple main component is a Message Dialog Window
     * that show a simple message for confirmation.
     */
    public abstract Component initSimpleMainComponent();
    /**
     * Initializes the contents of the window
     * @return content
     */
    public abstract void initComplexMainComponent();
    
    private HorizontalLayout initButtons(int options) {
        clickListener = new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (event.getButton().equals(btnYes))
                    option = YES_OPTION;
                if (event.getButton().equals(btnNo))
                    option = NO_OPTION;
                if (event.getButton().equals(btnOk))
                    option = OK_OPTION;
                if (event.getButton().equals(btnCancel))
                    option = CANCEL_OPTION;
                close();
            }
        };
        
        HorizontalLayout content = new HorizontalLayout();
        content.setSpacing(true);
        content.setMargin(true);
        
        switch (options) {
            case YES_NO_OPTION:
                btnYes = new Button(BTN_YES_CAPTION);
                btnYes.setWidth("120px"); //NOI18N
                btnYes.addClickListener(clickListener);
                
                content.addComponent(btnYes);
                
                btnNo = new Button(BTN_NO_CAPTION);
                btnNo.setWidth("120px"); //NOI18N
                btnNo.addClickListener(clickListener);
                
                content.addComponent(btnNo);
                break;
            case YES_NO_CANCEL_OPTION:
                btnYes = new Button(BTN_YES_CAPTION);
                btnYes.setWidth("120px"); //NOI18N
                btnYes.addClickListener(clickListener);
                
                content.addComponent(btnYes);
                
                btnNo = new Button(BTN_NO_CAPTION);
                btnNo.setWidth("120px"); //NOI18N
                btnNo.addClickListener(clickListener);
                
                content.addComponent(btnNo);
                
                btnCancel = new Button(BTN_CANCEL_CAPTION);
                btnCancel.setWidth("120px");
                btnCancel.addClickListener(clickListener);
                
                content.addComponent(btnCancel);
                break;
            case OK_CANCEL_OPTION:
                btnOk = new Button(BTN_OK_CAPTION);
                btnOk.setWidth("120px");
                btnOk.addClickListener(clickListener);
                
                content.addComponent(btnOk);
                
                btnCancel = new Button(BTN_CANCEL_CAPTION);
                btnCancel.setWidth("120px");
                btnCancel.addClickListener(clickListener);
                
                content.addComponent(btnCancel);
                break;
            case ONLY_OK_OPTION:
                btnOk = new Button(BTN_OK_CAPTION);
                btnOk.setWidth("120px");
                btnOk.addClickListener(clickListener);
                
                content.addComponent(btnOk);
                break;
        }
        if (btnOk != null)
            btnOk.focus();
        
        return content;
    }
    
    public int getOption() {
        return option;
    }
    
    public void setOption(int option) {
        this.option = option;
    }
    
    public Button.ClickListener getClickListener() {
        return clickListener;
    }
    
    public Button getBtnOk() {
        return btnOk;
    }
    
    public void setBtnOk(Button btnOk) {
        this.btnOk = btnOk;
    }
    
    @Override
    public void close() {
        if (btnYes != null)
            btnYes.removeClickListener(clickListener);
        
        if (btnNo != null)
            btnNo.removeClickListener(clickListener);
            
        if (btnOk != null)
            btnOk.removeClickListener(clickListener);
        
        if (btnCancel != null)
            btnCancel.removeClickListener(clickListener);
        
        super.close();
    }
}