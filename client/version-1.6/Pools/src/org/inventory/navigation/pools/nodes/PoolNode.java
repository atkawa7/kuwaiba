/*
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.pools.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.ShowMoreInformationAction;
import org.inventory.navigation.pools.nodes.actions.DeletePoolAction;
import org.inventory.navigation.pools.nodes.actions.NewPoolItemAction;
import org.inventory.navigation.pools.nodes.properties.PoolNativeTypeProperty;
import org.openide.actions.PasteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
/**
 * Represents a pool (a set of objects of a certain kind)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PoolNode extends AbstractNode implements PropertyChangeListener {
    private static final Image defaultIcon = ImageUtilities.loadImage("org/inventory/navigation/pools/res/pool.png");
    private NewPoolItemAction newPoolItemAction;
    private DeletePoolAction deletePoolAction;
    private LocalPool pool;
    protected Sheet sheet;
    
    public PoolNode(LocalPool pool) {
        super(new PoolChildren(pool));
        this.pool = pool;
        pool.addPropertyChangeListener(WeakListeners.propertyChange(this, pool));
    }
    
    @Override
    public void setName(String newName) {
        if (CommunicationsStub.getInstance().setPoolProperties(pool.getOid(), newName, null)) {
            pool.setName(newName);
            if (getSheet() != null)
                setSheet(createSheet());
        }
        else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            
    }
    
    @Override
    public String getName(){
        return getEditableText();
    }

    public String getEditableText() {
        return pool.getName() == null ? "" : pool.getName();
    }
    
    @Override
    public String getDisplayName() {
        return pool.toString();
    }

    
    @Override
    public Action[] getActions(boolean context){
        if (newPoolItemAction == null){
            newPoolItemAction = new NewPoolItemAction(this);
            deletePoolAction = new DeletePoolAction(this);
        }
        return new Action[]{ 
            SystemAction.get(PasteAction.class), 
            null, 
            newPoolItemAction, 
            deletePoolAction, 
            ShowMoreInformationAction.getInstance(pool.getOid(), pool.getClassName())};
    }
 
    @Override
    public Image getIcon(int i){
        return defaultIcon;
    }

    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
    
    @Override
    protected Sheet createSheet () {
        sheet = Sheet.createDefault();
        Set generalPropertySet = Sheet.createPropertiesSet(); // General attributes category
        LocalPool lp = CommunicationsStub.getInstance().getPoolInfo(pool.getOid());
        if (lp == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return sheet;
        }
        pool.setName(lp.getName());
        
        PropertySupport.ReadWrite propertyClassName = new PoolNativeTypeProperty(
                Constants.PROPERTY_CLASSNAME, 
                String.class, Constants.PROPERTY_CLASSNAME, 
                Constants.PROPERTY_CLASSNAME, this, lp.getClassName());
        generalPropertySet.put(propertyClassName);
        
        PropertySupport.ReadWrite propertyName = new PoolNativeTypeProperty(
                Constants.PROPERTY_NAME, String.class, Constants.PROPERTY_NAME, 
                Constants.PROPERTY_NAME, this, lp.getName());
        generalPropertySet.put(propertyName);
        
        PropertySupport.ReadWrite propertyDescription = new PoolNativeTypeProperty(
                Constants.PROPERTY_DESCRIPTION, String.class, 
                Constants.PROPERTY_DESCRIPTION, Constants.PROPERTY_DESCRIPTION, 
                this, lp.getDescription());
        generalPropertySet.put(propertyDescription);
        
        generalPropertySet.setDisplayName("General");
        sheet.put(generalPropertySet);
        return sheet;
    }
    
    public LocalPool getPool() {
        return pool;
    }
    
    @Override
    protected void createPasteTypes(Transferable t, List s) {
        super.createPasteTypes(t, s);
        //From the transferable we figure out if it comes from a copy or a cut operation
        PasteType paste = getDropType(t, NodeTransfer.node(t, NodeTransfer.CLIPBOARD_COPY) != null
                ? DnDConstants.ACTION_COPY : DnDConstants.ACTION_MOVE, -1);
        //It's also possible to define many paste types (like "normal paste" and "special paste")
        //by adding more entries to the list. Those will appear as options in the context menu
        if (paste != null) {
            s.add(paste);
        }
    }
    
    @Override
    public PasteType getDropType(Transferable obj, final int action, int index) {
        final ObjectNode dropNode = (ObjectNode) NodeTransfer.node(obj,
                NodeTransfer.CLIPBOARD_CUT + NodeTransfer.CLIPBOARD_COPY);
        
        if (dropNode == null) {
            return null;
        }
        
        //Can't move to the same parent, only copy
        if (this.equals(dropNode.getParentNode()) && (action == DnDConstants.ACTION_MOVE)) {
            return null;
        }
        
        //Only copy or paste if the object class can be contained into the pool
        if (!CommunicationsStub.getInstance().isSubclassOf(dropNode.getObject().getClassName(), this.getPool().getClassName()))
            return null;
        
        return new PasteType() {

                @Override
                public Transferable paste() throws IOException {                    
                    switch (action) {
                        case DnDConstants.ACTION_COPY:
                            if (dropNode instanceof ObjectNode) {
                                ObjectNode objNode = (ObjectNode) dropNode;                        

                                if (objNode.getParentNode() instanceof PoolNode) {
                                    if (CommunicationsStub.getInstance().copyPoolItem(getPool().getOid(), objNode.getObject().getClassName(), objNode.getObject().getOid(), true))
                                        ((AbstractChildren) getChildren()).addNotify();
                                    else
                                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                                }
                            }
                            break;
                        case DnDConstants.ACTION_MOVE:
                            if (dropNode instanceof ObjectNode) {
                                ObjectNode objectNode = (ObjectNode) dropNode;
                                if (objectNode.getParentNode() instanceof PoolNode) {
                                    if (CommunicationsStub.getInstance().movePoolItem(getPool().getOid(), objectNode.getObject().getClassName(), objectNode.getObject().getOid())) {
                                        
                                        ((AbstractChildren) objectNode.getParentNode().getChildren()).addNotify();
                                        ((AbstractChildren) getChildren()).addNotify();
                                    } else
                                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                            NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                                }
                            }
                            break;    
                    }
                    return  null;
                }
            };
    }
    
    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(pool)) {
            pool = (LocalPool) evt.getSource();
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME)) {
                setDisplayName(getDisplayName());
                fireNameChange(null, pool.getName());
            }
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PoolNode) {
            return ((PoolNode) obj).getPool().equals(this.getPool());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.pool != null ? this.pool.hashCode() : 0);
        return hash;
    }
}
