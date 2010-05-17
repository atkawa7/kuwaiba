package org.inventory.customization.hierarchycustomizer.nodes;

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
import org.inventory.core.services.interfaces.LocalClassMetadataLight;

/**
 * This class implements DragGestureListener and extends from TransferHandler in order to manage
 * the transfer operation between the JList and the BeanTreeView
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class TransferManager extends TransferHandler implements DragGestureListener, DragSourceListener{

    private JList list;

    public TransferManager (JList _list){
        this.list = _list;
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        TransferManager tf = (TransferManager)list.getTransferHandler();
        Transferable t = tf.createTransferable(list);
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
        //throw new UnsupportedOperationException("Not supported yet.");
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
     * To be reviewd later
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
