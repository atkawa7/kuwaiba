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
package org.inventory.navigation.special.attachments.nodes;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalFileObjectLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.special.attachments.AttachmentsTopComponent;
import org.inventory.navigation.special.attachments.nodes.actions.AttachmentsActionFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

/**
 * Node that represents a file object (a file attached to an inventory object)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class FileObjectNode extends AbstractNode implements VetoableChangeListener {

    private static Image ICON_OTHER = Toolkit.getDefaultToolkit().createImage(AttachmentsTopComponent.class.getResource("/org/inventory/navigation/special/res/icon_other.png"));
    private static Image ICON_IMAGE = Toolkit.getDefaultToolkit().createImage(AttachmentsTopComponent.class.getResource("/org/inventory/navigation/special/res/icon_image.png"));
    private static Image ICON_PDF = Toolkit.getDefaultToolkit().createImage(AttachmentsTopComponent.class.getResource("/org/inventory/navigation/special/res/icon_pdf.png"));
    
    private Image icon;
    
    public FileObjectNode(LocalFileObjectLight fileObject) {
        super(Children.LEAF, Lookups.singleton(fileObject));
        int fileNameLength = fileObject.getName().length();
        if (fileNameLength < 3)
            this.icon = ICON_OTHER;
        else {
            switch (fileObject.getName().substring(fileObject.getName().lastIndexOf('.') + 1, fileNameLength)) {
                case "jpg":
                case "png":
                case "jpeg":
                    this.icon = ICON_IMAGE;
                    break;
                case "pdf":
                    this.icon = ICON_PDF;
                    break;
                default:
                    this.icon = ICON_OTHER;
            }
        }
        fileObject.addActionListener(this);
    }

    @Override
    public String getHtmlDisplayName() {
        LocalFileObjectLight currentFileObject = getLookup().lookup(LocalFileObjectLight.class);
        return String.format("%s <i>[%s]</i>", currentFileObject.getName(), 
                currentFileObject.getTags() == null || currentFileObject.getTags().isEmpty() ? I18N.gm("no_tags_defined") : currentFileObject.getTags()); //NOI18N
    }
    
    @Override
    public String getName() {
        return getLookup().lookup(LocalFileObjectLight.class).toString();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            AttachmentsActionFactory.getDownloadAttachment(),
            null,
            AttachmentsActionFactory.getDetachAction()
        };
    }

    @Override
    public Action getPreferredAction() {
        return AttachmentsActionFactory.getDownloadAttachment();
    }
    
    @Override
    protected Sheet createSheet() {
        Sheet fileObjectSheet = Sheet.createDefault();
        Sheet.Set fileObjectProperties = Sheet.createPropertiesSet();
        
        try {
            PropertySupport.Reflection propertyName = new PropertySupport.Reflection(getLookup().
                    lookup(LocalFileObjectLight.class), String.class, "name");
        
        
            PropertySupport.Reflection propertyTags = new PropertySupport.Reflection(getLookup().
                    lookup(LocalFileObjectLight.class), String.class, "tags");
            
            fileObjectProperties.put(propertyName);
            fileObjectProperties.put(propertyTags);
            
            fileObjectSheet.put(fileObjectProperties);
            
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex); //Shoud not happen
        }
        
        return fileObjectSheet;
    }
    
    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return true; 
    }

    @Override
    public boolean canRename() {
        return false; 
    }

    @Override
    public Image getOpenedIcon(int type) {
        return this.icon;
    }

    @Override
    public Image getIcon(int type) {
        return this.icon;
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        LocalObjectLight currentObject = getParentNode().getLookup().lookup(LocalObjectLight.class);
        LocalFileObjectLight currentFileObject = (LocalFileObjectLight)evt.getSource();
        if (!CommunicationsStub.getInstance().updateFileProperties(currentFileObject.getFileOjectId(), 
                evt.getPropertyName(), (String)evt.getNewValue(), currentObject.getClassName(), currentObject.getId()))
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME))
                fireDisplayNameChange((String)evt.getOldValue(), (String)evt.getNewValue());
        }
    }
    
}
