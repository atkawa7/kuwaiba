/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.actions;

import com.vaadin.server.Resource;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import de.steinwedel.messagebox.MessageBox;
import java.util.List;
import org.kuwaiba.beans.WebserviceBean;

/**
 * An contextual action that has a submenu
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public abstract class AbstractComposedAction extends AbstractAction {
    
    public AbstractComposedAction(String caption, WebserviceBean wsBean) {
        super(caption, wsBean);
    }
    
    public AbstractComposedAction(String caption, Resource icon) {
        super(caption, icon);
    }
    
    /**
     * Actually shows a popup with the options. Handle the existence of items in the calling method (most probably actionPerformed) to customize the error message
     * @param sourceComponent The visual component this action is attached to.
     * @param targetObject The object related to the action (usually a node)
     * @param subMenuOptions The options in the submenu
     */
    public void showSubMenu(Object sourceComponent, Object targetObject, List<?> subMenuOptions) {
        ListSelect lstOptions = new ListSelect("", subMenuOptions);
        
        MessageBox mbSelection = MessageBox.create()
            .withCaption(getCaption())
            .withMessage(lstOptions)
            .withOkButton(() -> {
                if (lstOptions.getValue() == null)
                    Notification.show("Select a value from the list", Notification.Type.ERROR_MESSAGE);
                else {
                    finalActionPerformed(sourceComponent, targetObject, lstOptions.getValue());
                }
            })
            .withCancelButton();
        mbSelection.open();
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
