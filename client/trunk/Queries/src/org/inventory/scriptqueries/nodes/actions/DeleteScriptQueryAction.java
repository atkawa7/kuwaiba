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
package org.inventory.scriptqueries.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalScriptQuery;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.scriptqueries.nodes.ScriptQueryNode;
import org.inventory.scriptqueries.nodes.ScriptQueryRootChildren;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Deletes a Script Query
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeleteScriptQueryAction extends GenericInventoryAction implements Presenter.Popup {
    
    private final JMenuItem popupPresenter;
    
    public DeleteScriptQueryAction() {
        putValue(NAME, "Delete Script Query");
        
        popupPresenter = new JMenuItem((String) getValue(NAME), ImageIconResource.WARNING_ICON);
        popupPresenter.addActionListener(this);
        
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_QUERY_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ScriptQueryNode scriptQueryNode = Utilities.actionsGlobalContext().lookup(ScriptQueryNode.class);
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this Script Query?", 
            "Delete Script Query", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            
            LocalScriptQuery scriptQuery = scriptQueryNode.getLookup().lookup(LocalScriptQuery.class);
            
            if (CommunicationsStub.getInstance().deleteScriptQuery(scriptQuery.getId())) {
                ((ScriptQueryRootChildren) scriptQueryNode.getParentNode().getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "Script Query deleted successfully");
            } else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
}
