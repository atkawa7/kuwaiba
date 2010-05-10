package org.inventory.customization.hierarchycustomizer;

import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JList;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;


/**
 * Contains the business logic for the related TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class HierarchyCustomizerService implements DragSourceListener,LookupListener,DropTargetListener{

    private LocalClassMetadataLight[] allMeta;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private NotificationUtil notifier;

    public HierarchyCustomizerService(){
        notifier = Lookup.getDefault().lookup(NotificationUtil.class);
        allMeta = com.getAllLightMeta();

        if (allMeta==null){
           notifier.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_RETRIEVE_HIERARCHY_TEXT"),
                        notifier.ERROR, com.getError());
           allMeta = new LocalClassMetadata[0];
        }

        Lookup.Result result = Utilities.actionsGlobalContext().
                lookupResult(LocalClassMetadataLight.class);
                //lookupResult(LocalObjectLight.class);
        result.addLookupListener(this);
    }
    public LocalClassMetadataLight[] getAllMeta() {
        return allMeta;
    }


    //DragSourceListener methods
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        if(dsde.getDropSuccess()){
            if(dsde.getDropSuccess()){
                Object[] selectedItems = ((JList)
                            ((DragSourceContext) dsde.getSource()).getComponent()).getSelectedValues();
                List<Long> possibleChildrenIds = new ArrayList<Long>();
                
                for (Object obj : selectedItems)
                    possibleChildrenIds.add(((LocalClassMetadata)obj).getId());

                    //Transferable data = ((DragSourceContext) dsde.getSource()).getTransferable();
                    //String className = (String)data.getTransferData(data.getTransferDataFlavors()[3]);
                    
                    //com.addPossibleChildren(String parentClass);
            }else
                notifier.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_UPDATE_HIERARCHY_TEXT"), notifier.ERROR,
                          java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_OPERATION_NOT_PERMITTED"));
        }
    }

    //LookupListener methods
    public void resultChanged(LookupEvent le) {
        
    }


    //DropTargetListener methods
    public void dragEnter(DropTargetDragEvent dtde) {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("NOT SUPPORTED YET."));
    }

    public void dragOver(DropTargetDragEvent dtde) {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("NOT SUPPORTED YET."));
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("NOT SUPPORTED YET."));
    }

    public void dragExit(DropTargetEvent dte) {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("NOT SUPPORTED YET."));
    }

    public void drop(DropTargetDropEvent dtde) {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("NOT SUPPORTED YET."));
    }
}