package org.inventory.customization.hierarchycustomizer;

import java.util.Arrays;
import java.util.Vector;
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
public class HierarchyCustomizerService implements LookupListener{

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

    //LookupListener methods
    public void resultChanged(LookupEvent le) {
        Object obj = result.allInstances().iterator().next();
        if (obj != null){

            Vector content = new Vector(Arrays.asList(allMeta));

            ClassMetadataNode currentSelection;
            currentSelection = (ClassMetadataNode) obj;

            hctc.getbTreeView().expandNode(currentSelection);

            for (Node child : currentSelection.getChildren().getNodes())
                content.remove(((ClassMetadataNode)child).getObject());

            content.remove(currentSelection.getObject());
            hctc.getLstClasses().setListData(content);
        }
    }
}