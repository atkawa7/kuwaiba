/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.windows;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.HashMap;

/**
 * A variant of a JComplexPanel, that is, a modal window that serves forms.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class FormWindow extends Window {
    /**
     * The list of contained components. Note that each component must have set its 
     * "data" to a String (that is, use the setData method) that uniquely identifies the component within the form
     */
    private HashMap<String, AbstractField> components;
    private FormEventListener okClickListener;

    /**
     * Main constructor
     * @param caption The title of the window.
     * @param labels The labels that will be placed before the components.
     * @param fields The list of fields to be displayed in the form. Please note that the size of this array must be the same as the labels array.
     * @param listener Who will listens for the "OK" action. The "Cancel" action will simply close the window.
     */
    public FormWindow(String caption, String[] labels, AbstractField[] fields, FormEventListener listener) {
        super(caption);
        this.okClickListener = listener;
        
        setModal(true);
        
        components = new HashMap<>();
        
        GridLayout componentLayout = new GridLayout(2, labels.length + 1);
        componentLayout.setMargin(true);
        componentLayout.setSpacing(true);
        
        for (int i = 0; i < labels.length; i ++) {
            componentLayout.addComponent(new Label(labels[i]));
            componentLayout.addComponent(fields[i], 1, i);
            
            components.put((String)fields[i].getData(), fields[i]);
        }
        
        Button btnCancel = new Button("Cancel", new Button.ClickListener() {

                                @Override
                                public void buttonClick(Button.ClickEvent event) {
                                    okClickListener.formEvent(new FormEvent(components, FormEvent.EVENT_CANCEL));
                                    close();
                                }
                            });
        btnCancel.setWidth(100, Unit.PIXELS);
        
        Button btnOk = new Button("OK", new Button.ClickListener() {

                                @Override
                                public void buttonClick(Button.ClickEvent event) {
                                    okClickListener.formEvent(new FormEvent(components, FormEvent.EVENT_OK));
                                    close();
                                }
                            });
        btnOk.setWidth(100, Unit.PIXELS);
        
        HorizontalLayout actionLayout = new HorizontalLayout(btnCancel, btnOk);
        actionLayout.setWidth("100%");
        actionLayout.setComponentAlignment(btnCancel, Alignment.MIDDLE_CENTER);
        actionLayout.setComponentAlignment(btnOk, Alignment.MIDDLE_CENTER);
        actionLayout.setMargin(true);
        
        center();
        setResizable(false);
        
        setContent(new VerticalLayout(componentLayout, actionLayout));
    }
    
    /**
     * Interface to be implemented by all classes to listen to clicks on the OK button of the form.
     */
    public interface FormEventListener {
        /**
         * Event raised when the form is either accepted or rejected
         * @param event 
         */
        public void formEvent(FormEvent event);
    }
    
    public static class FormEvent {
        /**
         * The user accepted the form
         */
        public static int EVENT_OK = 1;
        /**
         * The user rejected the form
         */
        public static int EVENT_CANCEL = 2;
        
        /**
         * The fields in the form as a hashmap
         */
        private HashMap<String, AbstractField> components;
        /**
         * Option chosen by the user.
         */
        private int optionChosen;
        
        public FormEvent(HashMap<String, AbstractField> components, int optionChosen) {
            this.components = components;
            this.optionChosen = optionChosen;
        }
        
        public HashMap<String, AbstractField> getComponents(){
            return this.components;
        }

        public int getOptionChosen() {
            return optionChosen;
        }
        
    }
}
