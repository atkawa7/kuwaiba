/*
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.pools;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.actions.ShowObjectIdAction;
import org.inventory.navigation.applicationnodes.pools.actions.DeletePoolAction;
import org.inventory.navigation.applicationnodes.pools.actions.NewPoolItemAction;
import org.openide.actions.PasteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
/**
 * Represents a pool (a set of objects of a certain kind)
 * @author Charles edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PoolNode extends AbstractNode {
    
    private static Image defaultIcon = ImageUtilities.loadImage("org/inventory/navigation/applicationnodes/res/pool.png");
    private NewPoolItemAction newPoolItemAction;
    private DeletePoolAction deletePoolAction;
    private ShowObjectIdAction showObjectIdAction;
    private LocalObjectLight pool;
    
    public PoolNode(LocalObjectLight pool) {
        super(new PoolChildren(pool));
        this.pool = pool;
    }
    
    @Override
    public String getName(){
        return pool.getName() +" [" + pool.getClassName() + "]";
    }
    
    @Override
    public Action[] getActions(boolean context){
        if (newPoolItemAction == null){
            newPoolItemAction = new NewPoolItemAction(this);
            deletePoolAction = new DeletePoolAction(this);
            showObjectIdAction = new ShowObjectIdAction (pool.getOid(), pool.getClassName());
        }
        return new Action[]{SystemAction.get(PasteAction.class), null, newPoolItemAction, deletePoolAction, showObjectIdAction};
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
    protected Sheet createSheet(){
        return Sheet.createDefault();
    }
    
    public LocalObjectLight getPool() {
        return pool;
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
                            System.out.println("Hohoho");
                            break;
                        case DnDConstants.ACTION_MOVE:
                            System.out.println("Hehehe");
                            break;    
                    }
                    return  null;
                }
            };
    }
}