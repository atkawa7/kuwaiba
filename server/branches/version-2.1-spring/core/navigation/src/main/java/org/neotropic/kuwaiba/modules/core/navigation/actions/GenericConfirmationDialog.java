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
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.flow.component.HtmlContainer;
import org.springframework.stereotype.Component;
//import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 *
 * @author adr
 */
//@Component
public class GenericConfirmationDialog extends Dialog{
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
   
    private final VerticalLayout lytContentDialog;
    private HtmlContainer lblHeaderTitle;
    private final VerticalLayout lytMainContentDialog;
    private final HorizontalLayout lytButtons;
    //private final List<IDialogCloseListener> listeners;
    private final Button btnAccept;
    private final Button btnCancel;
    private boolean cancelable;
    private boolean acceptable;

    public GenericConfirmationDialog() { 
        this.lytMainContentDialog = new VerticalLayout();
        this.lytContentDialog = new VerticalLayout();
        this.lytButtons = new HorizontalLayout();
        //this.listeners = new ArrayList<>();
        this.lblHeaderTitle = new Label();
        this.cancelable = true;
        this.acceptable = true;
        this.btnAccept = new Button("Aceptar");
        this.btnCancel = new Button("Cancelar");
        initElements();
    }

    public GenericConfirmationDialog( boolean cancelable, boolean acceptable) {
        this.lytMainContentDialog = new VerticalLayout();
        this.lytContentDialog = new VerticalLayout();
        this.lytButtons = new HorizontalLayout();
        //this.listeners = new ArrayList<>();
        this.lblHeaderTitle = new Label("");
        this.cancelable = cancelable;
        this.acceptable = acceptable;
        this.btnAccept = new Button("Aceptar");
        this.btnCancel = new Button("Cancelar");
        initElements();
    }

    public GenericConfirmationDialog(boolean cancelable, boolean acceptable, String header ) {
        this.lytMainContentDialog = new VerticalLayout();
        this.lytContentDialog = new VerticalLayout();
        this.lytButtons = new HorizontalLayout();
        //this.listeners = new ArrayList<>();
        this.lblHeaderTitle = new Label();
        this.acceptable = acceptable;
        this.cancelable = cancelable;
        this.btnAccept = new Button("Aceptar");
        this.btnCancel = new Button("Cancelar");
        initElements();
    }

    private void initElements(){
        this.lytMainContentDialog.setWidthFull();
        this.lytMainContentDialog.setId("lyt-maincontent");
        getLytContentDialog().setWidthFull();
        setCloseOnOutsideClick(false);
        setCloseOnEsc(false);
        getBtnAccept().setClassName("accept-buttons");
        getBtnAccept().setId("btn-accept");
        getBtnAccept().setVisible(isAcceptable());
//        getBtnAccept().addClickListener(event -> {
//            getBtnAccept().setEnabled(false);
//            fireDialogCloseEvent();
//        });
        getBtnCancel().setId("btn-cancel");
        getBtnCancel().setClassName("cancel-buttons");
        getBtnCancel().setVisible(isCancelable());
        getBtnCancel().addClickListener(event -> close());
        //set layout
        getLytButtons().setId("lyt-btns");
        getLytButtons().add(getBtnCancel(), getBtnAccept());
        getLytContentDialog().setSpacing(true);
        getLytContentDialog().setMargin(false);
        getLytContentDialog().setPadding(false);
        //set main layout
        this.lytMainContentDialog.add(lblHeaderTitle);
        this.lytMainContentDialog.add(getLytContentDialog(),getLytButtons());
        this.lytMainContentDialog.setHorizontalComponentAlignment(FlexComponent.Alignment.END,getLytButtons());
        add(this.lytMainContentDialog);
    }

    public void setLblHeaderTitle(HtmlContainer lblHeaderTitle) {
        this.lblHeaderTitle = lblHeaderTitle;
    }

    /**
     * @param content the content
     */
    public void setContent(com.vaadin.flow.component.Component content){
        getLytContentDialog().add(content);
    }

    /**
//     * Due to Vaadin's specific implementation of DialogCloseEventAction this
//     * method was needed to fire the event with a custom button inside the
//     * Dialog
//     */
//    public void fireDialogCloseEvent() {
//        for (IDialogCloseListener l : listeners) 
//            l.onDialogCloseEvent();
//        
//        this.close();
//    }

    /**
     * add events to close listener
     * @param listener
     */
//    public void addCloseEventListener(IDialogCloseListener listener) {
//        listeners.add(listener);
//    }
//
//    public List<IDialogCloseListener> getListeners() {
//        return listeners;
//    }
//    
    public VerticalLayout getLytContentDialog() {
        return lytContentDialog;
    }

    public HorizontalLayout getLytButtons() {
        return lytButtons;
    }

    public Button getBtnAccept() {
        return btnAccept;
    }

    public Button getBtnCancel() {
        return btnCancel;
    }

    /**
     * if true cancel button is not visible
     */
    public boolean isCancelable() {
        return cancelable;
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    /**
     * if true accept button is not visible
     */
    public boolean isAcceptable() {
        return acceptable;
    }

    public void setAcceptable(boolean acceptable) {
        this.acceptable = acceptable;
    }
}
