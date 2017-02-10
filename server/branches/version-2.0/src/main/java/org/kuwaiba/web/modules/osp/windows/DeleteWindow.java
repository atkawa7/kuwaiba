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
package org.kuwaiba.web.modules.osp.windows;

import org.kuwaiba.apis.web.gui.windows.ConfirmDialogWindow;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeleteWindow extends ConfirmDialogWindow {
    private boolean ok = false;
    
    public DeleteWindow(Window.CloseListener closeListener) {
        super(closeListener, "Confirmation", 
                ConfirmDialogWindow.OK_CANCEL_OPTION);
    }
    
    @Override
    public VerticalLayout initContent() {
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        
        Label lblMsg = new Label("Are you sure you want to delete the current view?");
        content.addComponent(lblMsg);
        
        return content;
    }
}
