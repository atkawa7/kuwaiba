/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
 */
package org.inventory.customization.hierarchycustomizer;

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.customization.hierarchycustomizer.nodes.ClassMetadataChildren;
import org.inventory.customization.hierarchycustomizer.nodes.ClassMetadataNode;
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

    private List<LocalClassMetadataLight> treeModel;
    private List<LocalClassMetadataLight> listModel;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private NotificationUtil notifier;
    private Result result;

    public HierarchyCustomizerService(HierarchyCustomizerTopComponent _hctc){
        notifier = Lookup.getDefault().lookup(NotificationUtil.class);
        LocalClassMetadataLight[] allMeta;
        allMeta = com.getAllLightMeta();
        this.hctc = _hctc;

        listModel = new ArrayList<LocalClassMetadataLight>();

        treeModel = new ArrayList<LocalClassMetadataLight>();


        if (allMeta==null){
           notifier.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_RETRIEVE_HIERARCHY_TEXT"),
                        notifier.ERROR, com.getError());
           allMeta = new LocalClassMetadata[0];
        }else{

            result = hctc.getLookup().lookupResult(ClassMetadataNode.class);
            //This is really curious. If this line is omitted, the instances within the lookup never
            //will be found. Please refer to http://netbeans.dzone.com/articles/netbeans-lookups-explained
            //He doesn't explain it, but he uses it. It's important to point out that this workaround
            //is not neccessary if you're going to listen from other module than the one with the explorer view (BeanTreeView or whatever else)
            result.allInstances();
            result.addLookupListener(this);

            //Build the lstClasses model, made out of allMeta minus DummyRoot
            //and the bTreeView model, made out of allMeta minus the abstract classes
            //(RootObject, ConfigurationItem, GenericXXX, etc)
            LocalClassMetadata rootClass = com.getDummyRootClass();
            treeModel.add(rootClass);

            for (LocalClassMetadataLight item : allMeta){
                listModel.add(item);

                if (!item.getIsAbstract())
                    treeModel.add(item);
            }
        }
    }

    public List<LocalClassMetadataLight> getListModel(){
        return listModel;
    }

    public List<LocalClassMetadataLight> getTreeModel(){
        return treeModel;
    }


    //LookupListener methods
    public void resultChanged(LookupEvent le) {
        //Sometimes the event is fired but the object is no longer available (i.e., if you remove a node from the tree)
        if (result.allInstances().isEmpty())
            return;
        Object obj = result.allInstances().iterator().next();
        if (obj != null){

            ClassMetadataNode currentSelection = (ClassMetadataNode) obj;

            if(currentSelection.isLeaf()) //Show nothing for ClassMetadataNodes representing to possible children
                hctc.getLstClasses().setListData(new Object[0]);
            else{
                List content = new ArrayList(listModel);

                //Too bad the call to addNotify from expandNode goes in other thread, and
                //getChildren is empty the first time a node is expanded. This is a workaround
                //another is to add a delay here or call the getPossibleChildren from CommunicationsStub
                ((ClassMetadataChildren)currentSelection.getChildren()).addNotify();
                hctc.getbTreeView().expandNode(currentSelection);

                for (LocalClassMetadataLight lcml : ((ClassMetadataChildren)currentSelection.getChildren()).getKeys())
                    content.remove(lcml);

                //Leaves only the possible children available in the list
                content.remove(currentSelection.getObject());
                hctc.getLstClasses().setListData(content.toArray());
            }
        }
    }
}