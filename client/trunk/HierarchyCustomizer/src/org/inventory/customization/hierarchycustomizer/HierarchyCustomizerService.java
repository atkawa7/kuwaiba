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
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.JList;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.customization.hierarchycustomizer.nodes.ClassMetadataNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Contains the business logic for the related TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class HierarchyCustomizerService implements DragSourceListener,LookupListener,DropTargetListener{

    private HierarchyCustomizerTopComponent hctc;
    private LocalClassMetadataLight[] allMeta;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private NotificationUtil notifier;
    private Result result;

    public HierarchyCustomizerService(HierarchyCustomizerTopComponent _hctc){
        notifier = Lookup.getDefault().lookup(NotificationUtil.class);
        allMeta = com.getAllLightMeta();
        this.hctc = _hctc;

        if (allMeta==null){
           notifier.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_RETRIEVE_HIERARCHY_TEXT"),
                        notifier.ERROR, com.getError());
           allMeta = new LocalClassMetadata[0];
        }

        //result = hctc.getLookup().lookupResult(LocalClassMetadataLight.class);
        result = hctc.getLookup().lookupResult(ClassMetadataNode.class);
        //This is really curious. If this line is omitted, the instances within the lookup never
        //will be found. Please refer to http://netbeans.dzone.com/articles/netbeans-lookups-explained
        //He doesn't explain it, but he uses it. It's important to point out that this workaround
        //is not neccessary if you're going to listen from other module than the one with the explorer view (BeanTreeView or whatever else)
        result.allInstances();
        result.addLookupListener(this);
    }
    public LocalClassMetadataLight[] getAllMeta() {
        return allMeta;
    }

    /*
     * Removes the DummyRoot, since it won't be used anymore
     */
    public void removeDummyRoot(){
        LocalClassMetadataLight[] tempMeta = new LocalClassMetadataLight[allMeta.length - 1];
        
        int i = 0;
        for (LocalClassMetadataLight item : allMeta)
            if (!item.getClassName().equals(com.getRootClass())){
                tempMeta[i] = item;
                i++;
            }
        allMeta = tempMeta;
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
                    possibleChildrenIds.add(((LocalClassMetadataLight)obj).getId());

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
        Object obj = result.allInstances().iterator().next();
        if (obj != null){

            Vector content = new Vector(Arrays.asList(allMeta));
            //LocalClassMetadataLight currentSelection;
            ClassMetadataNode currentSelection;
            //currentSelection = (LocalClassMetadataLight) obj;
            currentSelection = (ClassMetadataNode) obj;

            hctc.getbTreeView().expandNode(currentSelection);

            for (Node child : currentSelection.getChildren().getNodes())
                content.remove(((ClassMetadataNode)child).getObject());

            content.remove(currentSelection.getObject());
            hctc.getLstClasses().setListData(content);
        }
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