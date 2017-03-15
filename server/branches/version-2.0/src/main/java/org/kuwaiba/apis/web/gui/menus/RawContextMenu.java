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

package org.kuwaiba.apis.web.gui.menus;

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
import org.kuwaiba.apis.web.gui.actions.AbstractAction;

/**
 * Some times, the conventional context menus can not be used (like the OSP module, 
 * which uses a map widget that is not very menu friendly). This is a very rough
 * context menu based on a window
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RawContextMenu extends Window {
    /**
     * The list of actions
     */
    private ListSelect lstOptions;
    /**
     * The "accept" button
     */
    private Button btnOk;
    /**
     * The cancel button
     */
    private Button btnCancel;
    /**
     * Main constructor
     * @param actions The list of actions that will be shown
     * @param sourceComponent The visual component this submenu is attached to.
     * @param targetObject The object related to the action (usually a node)
     */
    public RawContextMenu(List<AbstractAction> actions, Object sourceComponent, Object targetObject) {
        lstOptions = new ListSelect("", actions);
        lstOptions.setSizeFull();
        
        btnCancel = new Button("Cancel", (Button.ClickEvent event) -> {
                    close();
                });
        btnCancel.setWidth(100, Sizeable.Unit.PIXELS);

        btnOk = new Button("OK", (Button.ClickEvent event) -> {
                    if (lstOptions.getValue() == null)
                        Notification.show("Select a value from the list", Notification.Type.ERROR_MESSAGE);
                    else {
                        ((AbstractAction)lstOptions.getValue()).actionPerformed(sourceComponent, targetObject);
                        close();
                    }
                });
        btnOk.setWidth(100, Sizeable.Unit.PIXELS);
        
        HorizontalLayout actionLayout = new HorizontalLayout(btnCancel, btnOk);
        actionLayout.setWidth("100%");
        actionLayout.setComponentAlignment(btnCancel, Alignment.MIDDLE_CENTER);
        actionLayout.setComponentAlignment(btnOk, Alignment.MIDDLE_CENTER);
        actionLayout.setMargin(true);
        actionLayout.setSpacing(true);
        
        VerticalLayout layout = new VerticalLayout(lstOptions, actionLayout);
        layout.setMargin(true);
        setContent(layout);
        setModal(true);
    }
    
    /**
     * Shows the submenu window
     */
    public void show() {
        center();
        setResizable(false);
        UI.getCurrent().addWindow(this);
    }
}
