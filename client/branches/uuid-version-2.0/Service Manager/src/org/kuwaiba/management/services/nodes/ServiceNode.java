/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.management.services.nodes;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.favorites.actions.AddObjectToFavoritesFolderAction;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.ExecuteClassLevelReportAction;
import org.inventory.navigation.navigationtree.nodes.actions.ShowMoreInformationAction;
import org.inventory.navigation.special.attachments.nodes.actions.AttachFileAction;
import org.kuwaiba.management.services.nodes.actions.ServiceManagerActionFactory;
import org.kuwaiba.management.services.nodes.actions.ShowEndToEndViewAction;
import org.kuwaiba.management.services.nodes.actions.ShowServiceTopologyViewAction;
import org.openide.actions.PasteAction;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;

/**
 * Node representing a service
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ServiceNode extends ObjectNode {
    
    public ServiceNode(LocalObjectLight service) {
        super(service);
        setChildren(new ServiceChildren());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action pasteAction = SystemAction.get(PasteAction.class);
        pasteAction.putValue(Action.NAME, I18N.gm("lbl_paste_action"));
        
        return new Action [] {
            pasteAction, 
            null, 
            ExecuteClassLevelReportAction.getInstance(),
            ServiceManagerActionFactory.getDeleteServiceAction(),
            null,
            Lookup.getDefault().lookup(ShowEndToEndViewAction.class),
            Lookup.getDefault().lookup(ShowServiceTopologyViewAction.class),
            Lookup.getDefault().lookup(AttachFileAction.class),
            Lookup.getDefault().lookup(AddObjectToFavoritesFolderAction.class),
            null,
            ShowMoreInformationAction.getInstance(getObject().getId(), getObject().getClassName())
        };        
    }
    
    @Override
    public PasteType getDropType(Transferable _obj, final int action, int index) {
        final Node dropNode = NodeTransfer.node(_obj,
                NodeTransfer.DND_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
        
        //When there's no an actual drag/drop operation, but a simple node selection
        if (dropNode == null) 
            return null;
        
        //The clipboard does not contain an Favorites Item Node
        if (!ObjectNode.class.isInstance(dropNode))
            return null;
        
        //Can't move to the same parent, only copy
        if (this.equals(dropNode.getParentNode()) && (action == DnDConstants.ACTION_MOVE)) 
            return null;
        
        return new PasteType() {
            @Override
            public Transferable paste() throws IOException {
                if (action == DnDConstants.ACTION_COPY) {
                    if (dropNode instanceof ObjectNode) {
                        ObjectNode objNode = (ObjectNode) dropNode;                        
                        
                        if (objNode.getParentNode() instanceof ServiceNode) {
                            List<String> classNames = new ArrayList<>();
                            List<String> objectIds = new ArrayList<>();
                            
                            classNames.add(objNode.getObject().getClassName());
                            objectIds.add(objNode.getObject().getId());
                            
                            if (CommunicationsStub.getInstance().associateObjectsToService(classNames, objectIds, 
                                getObject().getClassName(), getObject().getId())) {
                                
                                ((ServiceChildren) getChildren()).addNotify();
                            } else
                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                        }
                    }
                }
                if (action == DnDConstants.ACTION_MOVE) {
                    if (dropNode instanceof ObjectNode) {
                        ObjectNode objectNode = (ObjectNode) dropNode;
                        List<String> classNames = new ArrayList<>();
                        List<String> objectIds = new ArrayList<>();
                        
                        classNames.add(objectNode.getObject().getClassName());
                        objectIds.add(objectNode.getObject().getId());
                        
                        if (objectNode.getParentNode() instanceof ServiceNode) {
                            ServiceNode serviceNode = (ServiceNode) objectNode.getParentNode();
                            
                            if (CommunicationsStub.getInstance().associateObjectsToService(classNames, objectIds, 
                                getObject().getClassName(), getObject().getId())) {
                                
                                ((ServiceChildren) getChildren()).addNotify();
                                
                                if (CommunicationsStub.getInstance().releaseObjectFromService(
                                    serviceNode.getObject().getClassName(), serviceNode.getObject().getId(), objectNode.getObject().getId())) {

                                    ((ServiceChildren) serviceNode.getChildren()).addNotify();
                                } else
                                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                            } else
                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                        }
                    }
                }
                return null;
            }
        };
    }
}
