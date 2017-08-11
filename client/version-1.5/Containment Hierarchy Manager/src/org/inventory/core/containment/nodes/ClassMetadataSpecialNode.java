/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.inventory.core.containment.nodes;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.core.containment.nodes.actions.RemovePossibleSpecialChildAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.datatransfer.PasteType;

/**
 * A node wrapping a ClassMetadataLight
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ClassMetadataSpecialNode extends ClassMetadataNode {
    private static final String PARENT_ICON_PATH = "org/inventory/core/containment/res/special-flag-blue.png";
    private static final String ROOT_PARENT_ICON_PATH = "org/inventory/core/containment/res/special-flag-green.png";
    private static final String CHILDREN_ICON_PATH = "org/inventory/core/containment/res/special-flag-red.png";
    
    public ClassMetadataSpecialNode(LocalClassMetadataLight lcm, boolean isMain) {
        super(lcm, isMain);
        if (lcm.getClassName() == null)
            setIconBaseWithExtension(ROOT_PARENT_ICON_PATH);
        else
            setIconBaseWithExtension(PARENT_ICON_PATH);
        
        setChildren(new ClassMetadataSpecialChildren());
    }
    
    public ClassMetadataSpecialNode(LocalClassMetadataLight lcm) {
        super(lcm);
        setIconBaseWithExtension(CHILDREN_ICON_PATH);
    }
        
    @Override
    public Action[] getActions(boolean context) {
        if (this.isLeaf()) //Return actions only for the nodes representing possible special children
            return new Action[] {RemovePossibleSpecialChildAction.getInstance()};
        else
            return new Action [0];
    }
    
    @Override
    public PasteType getDropType(final Transferable obj, int action, int index) {
        return new PasteType() {

            @Override
            public Transferable paste() throws IOException {
                //Only can be dropped into a parent node (the ones marked with a red flag)
                if (isLeaf())
                    return null;
                
                try {
                    LocalClassMetadataLight data = (LocalClassMetadataLight) 
                        obj.getTransferData(LocalClassMetadataLight.DATA_FLAVOR);
                    
                    long [] tokens = new long[] {data.getOid()};
                    
                    if (CommunicationsStub.getInstance().addPossibleSpecialChildren(getObject().getOid(), tokens)) {
                        
                        ((ClassMetadataSpecialChildren) getChildren()).add(new ClassMetadataSpecialNode[]{new ClassMetadataSpecialNode(data)});
                        CommunicationsStub.getInstance().refreshCache(false, false, false, false, true);

                        NotificationUtil.getInstance().showSimplePopup("Success", 
                            NotificationUtil.INFO_MESSAGE, 
                            java.util.ResourceBundle.getBundle("org/inventory/core/containment/Bundle").getString("LBL_HIERARCHY_UPDATE_TEXT"));
                    }
                    else {
                        NotificationUtil.getInstance().showSimplePopup("Error", 
                            NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    }
                } catch (UnsupportedFlavorException ex) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                }
                return null;
            }
        };
    }
}
