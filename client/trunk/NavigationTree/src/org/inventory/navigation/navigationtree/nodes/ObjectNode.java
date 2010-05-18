package org.inventory.navigation.navigationtree.nodes;

import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.LocalObjectListItem;
import org.inventory.navigation.navigationtree.actions.Create;
import org.inventory.navigation.navigationtree.actions.Delete;
import org.inventory.navigation.navigationtree.actions.Edit;
import org.inventory.navigation.navigationtree.nodes.properties.ObjectNodeProperty;
import org.inventory.navigation.navigationtree.nodes.properties.ObjectNodePropertyChangeListener;
import org.openide.actions.OpenLocalExplorerAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.lookup.Lookups;

/**
 * Represents a node within the navigation tree
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectNode extends AbstractNode{
    
    private LocalObjectLight object;
    private ObjectNodePropertyChangeListener pcl;
    //There can be only one instance for OpenLocalExplorerAction, this attribute is a kind of singleton
    private static OpenLocalExplorerAction explorerAction = new OpenLocalExplorerAction();

    private Create createAction;
    private Delete deleteAction;
    private Edit editAction;

    private Sheet sheet;

    public ObjectNode(LocalObjectLight _lol){
        super(new ObjectChildren(), Lookups.singleton(_lol));
        this.object = _lol;
        
        explorerAction.putValue(OpenLocalExplorerAction.NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_EXPLORE"));

        //The children must be the listener so it can set the modified keys (after an object creation or removal)
        createAction = new Create(object,this);
        createAction.addPropertyChangeListener((ObjectChildren)this.getChildren());
        //Since the listener for the delete action is the parent node, and as itis not been set here yet,
        //the listener have to be added later (see getActions)

        //Does not need listener
        editAction = new Edit(this);
    }

    /*
     * Returns the wrapped object
     * @return returns the related business object
     */
    public LocalObjectLight getObject(){
        return this.object;
    }
    
    @Override
    public String getDisplayName(){
        String displayName = (object.getDisplayname().equals("") ||
                                    object.getDisplayname().equals(null))?java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_NONAME"):object.getDisplayname();
        return displayName + " ["+object.getClassName()+"]";
    }

    @Override
    protected Sheet createSheet(){
        sheet = Sheet.createDefault();
        
        Set generalPropertySet = Sheet.createPropertiesSet(); //General attributes category
        Set administrativePropertySet = Sheet.createPropertiesSet(); //Administrative attributes category

        LocalClassMetadata meta = CommunicationsStub.getInstance().getMetaForClass(object.getClassName());

        LocalObject lo = CommunicationsStub.getInstance().getObjectInfo(
                object.getClassName(), object.getOid(), meta);

        int i = 0;
        for(LocalAttributeMetadata lam:meta.getAttributes()){
            if(lam.getIsVisible()){

                ObjectNodeProperty property = null;

                if (meta.isMultiple(lam.getName())){
                    //TODO take this from the local cache
                    LocalObjectListItem[] list = CommunicationsStub.getInstance().getList(lam.getType());
                    LocalObjectListItem val = null;

                    for (LocalObjectListItem loli : list)
                        if(loli.getId().equals(lo.getAttribute(lam.getName()))){
                            val = loli;
                            break;
                        }

                    /****************************/

                    property = new ObjectNodeProperty(
                                           lam.getName(),
                                           LocalObjectListItem.class,
                                           val,
                                           lam.getDisplayName().equals("")?lam.getName():lam.getDisplayName(),
                                           lam.getDescription(),
                                           list,
                                           lo);
                }
                else{
                    property = new ObjectNodeProperty(
                                                        lam.getName(),
                                                        (lo.getAttribute(lam.getName())== null)?null:lo.getAttribute(lam.getName()).getClass(),
                                                        lo.getAttribute(lam.getName()),
                                                        lam.getDisplayName().equals("")?lam.getName():lam.getDisplayName(),
                                                        lam.getDescription(),lo);
                }

                if(lam.getIsAdministrative())
                    administrativePropertySet.put(property);
                else
                    generalPropertySet.put(property);
            }
            i++;
        }

        generalPropertySet.setName("1");
        administrativePropertySet.setName("2");

        generalPropertySet.setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_GENERAL_ATTRIBUTES"));
        administrativePropertySet.setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_ADMINISTRATIVE_ATTRIBUTES"));

        
        sheet.put(generalPropertySet);
        sheet.put(administrativePropertySet);

        return sheet;
    }

    //This method is called for the very first time when the firt context menu is created, and
    //called everytime
    @Override
    public Action[] getActions(boolean context){
        if(deleteAction == null){
            deleteAction = new Delete(this);
            deleteAction.addPropertyChangeListener((ObjectChildren)this.getParentNode().getChildren());
        }
        return new Action[]{createAction, editAction,deleteAction,explorerAction};
    }

    //TODO Set this to false is the object is locked
    @Override
    public boolean canRename(){
        return true;
    }

    @Override
    public boolean canCut(){
        return true;
    }
}