/*
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>
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
 *  under the License.
 */
package org.inventory.core.containment.nodes;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
import org.inventory.communications.core.LocalClassMetadataLight;

/**
 * This class implements DragGestureListener and extends from TransferHandler in order to manage
 * the transfer operation between the JList and the BeanTreeView
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ClassMetadataTransferManager extends TransferHandler implements DragGestureListener, DragSourceListener{

    private JList list;

    public ClassMetadataTransferManager (JList _list){
        this.list = _list;
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        //To avoid bogus drag and drop events trigger this behavior
        if (dge == null)
            return;
        ClassMetadataTransferManager tf = (ClassMetadataTransferManager)list.getTransferHandler();
        Transferable t = tf.createTransferable(list);
        //To avoid bogus drag and drop events trigger this behavior
        if (t == null)
            return;
        dge.startDrag(null, t);
    }

    @Override
    public Transferable createTransferable(JComponent c){
        return (LocalClassMetadataLight)list.getSelectedValue();
        //return new MultipleItemsTransferable(list.getSelectedValues());
    }

    public void dragEnter(DragSourceDragEvent dsde) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dragOver(DragSourceDragEvent dsde) {
        //Remove the entry list if the drop was successful
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dragExit(DragSourceEvent dse) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /*
     * This is supposed to support multiple objects dragged... But it didn't work.
     * To be reviewed later
    private class MultipleItemsTransferable extends ArrayList<LocalClassMetadataLight>
            implements Transferable{

        public MultipleItemsTransferable(Object[] objs){
            super();
            for (Object obj : objs)
                this.add((LocalClassMetadataLight)obj);
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {LocalClassMetadataLight.DATA_FLAVOR};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(LocalClassMetadataLight.DATA_FLAVOR);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor))
                return this;
            else
                throw new UnsupportedFlavorException(flavor);
        }
    }
     *
     */

}
