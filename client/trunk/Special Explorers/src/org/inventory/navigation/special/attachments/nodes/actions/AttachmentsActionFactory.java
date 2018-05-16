/*
 * Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 * 
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.navigation.special.attachments.nodes.actions;

import org.inventory.core.services.api.actions.GenericInventoryAction;

/**
 * Action factory for the attachments explorer
 * @author lulita
 */
public class AttachmentsActionFactory {
    private static GenericInventoryAction detachFileAction;
    private static GenericInventoryAction downloadAttachmentAction;
    
    public static GenericInventoryAction getDetachAction() {
        return detachFileAction == null ? detachFileAction = new DetachFileAction() : detachFileAction;
    }
    
    public static GenericInventoryAction getDownloadAttachment() {
        return downloadAttachmentAction == null ? downloadAttachmentAction = new DownloadAttachmentAction(): downloadAttachmentAction;
    }
}
