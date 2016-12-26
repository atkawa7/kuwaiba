/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.navtree.actions;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Notification;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;

/**
 *
 * @author duckman
 */
public class DeleteObjectAction extends AbstractAction {

    public DeleteObjectAction() {
        super("Delete", new ThemeResource("img/warning.gif"));
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
//    public DeleteObjectAction() {
//        super("Delete", new ThemeResource("img/warning.gif"));
//    }
//
//    @Override
//    public void actionPerformed(Object source, Object target) {
//        ((AbstractNode)target).delete();
//        Notification.show("Element successfully deleted", Notification.Type.TRAY_NOTIFICATION);
//    }
}
