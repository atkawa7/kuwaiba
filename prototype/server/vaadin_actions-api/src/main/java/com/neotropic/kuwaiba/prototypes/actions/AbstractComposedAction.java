/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.actions;

import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.List;

/**
 * An contextual action that has a submenu
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class AbstractComposedAction extends AbstractAction {
    
    public AbstractComposedAction(String caption) {
        super(caption);
    }
    
    public AbstractComposedAction(String caption, Resource icon) {
        super(caption, icon);
    }
    
    /**
     * Actually shows a popup with the options. Handle the existence of items in the calling method (most probably actionPerformed) to customize the error message
     * @param sourceComponent 
     * @param targetObject 
     * @param subMenuOptions 
     */
    public void showSubMenu(Object sourceComponent, Object targetObject, List<?> subMenuOptions) {
        
        final Window selectionWindow = new Window(getCaption());
        selectionWindow.setModal(true);

        final ListSelect lstOptions = new ListSelect("", subMenuOptions);
        lstOptions.setSizeFull();


        Button btnCancel = new Button("Cancel", (Button.ClickEvent event) -> {
                    selectionWindow.close();
                });
        btnCancel.setWidth(100, Sizeable.Unit.PIXELS);

        Button btnOk = new Button("OK", (Button.ClickEvent event) -> {
                    if (lstOptions.getValue() == null)
                        Notification.show("Select a value from the list", Notification.Type.ERROR_MESSAGE);
                    else {
                        finalActionPerformed(sourceComponent, targetObject, lstOptions.getValue());
                        selectionWindow.close();
                    }
                });
        btnOk.setWidth(100, Sizeable.Unit.PIXELS);

        HorizontalLayout actionLayout = new HorizontalLayout(btnCancel, btnOk);
        actionLayout.setWidth("100%");
        actionLayout.setComponentAlignment(btnCancel, Alignment.MIDDLE_CENTER);
        actionLayout.setComponentAlignment(btnOk, Alignment.MIDDLE_CENTER);
        actionLayout.setMargin(true);
        actionLayout.setSpacing(true);

        selectionWindow.center();
        selectionWindow.setResizable(false);

        VerticalLayout layout = new VerticalLayout(lstOptions, actionLayout);
        layout.setMargin(true);
        selectionWindow.setContent(layout);

        UI.getCurrent().addWindow(selectionWindow);
    }
    
    /**
     * This method will be called after selecting an option in the window that replaces the submenu
     * @param sourceComponent The component that triggered the action.
     * @param targetObject The subcomponent that triggered the action.
     * @param selectedOption The option selected from the sub menu list.
     */
    public abstract void finalActionPerformed(Object sourceComponent, Object targetObject, 
            Object selectedOption);
}
