package org.inventory.navigation.navigationtree.nodes.properties;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import javax.swing.JLabel;
import org.inventory.core.services.interfaces.LocalObjectListItem;


/**
 * Provides a custom property editor for list-type values
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ItemListPropertyEditor extends PropertyEditorSupport{

    private LocalObjectListItem[] list;

    public ItemListPropertyEditor(LocalObjectListItem[] _list){
        this.list = _list;
    }

    @Override
    public String getAsText(){
        return getValue().toString();
    }

    @Override
    public void setAsText(String text){
        for (LocalObjectListItem loli : list)
            if (text.equals(loli.getDisplayName())){
                setValue(loli);
                break;
            }
    }

    @Override
    public String[] getTags(){
        //Remember that CommunicationsStub->getList returns the list, but adds the null value as well,
        //so it's not necessary to add it here
        String [] res = new String[list.length];
    
        for (int i = 0; i <list.length; i++)
            res[i] = list[i].getDisplayName().equals("")?
            list[i].getName():list[i].getDisplayName();
        return res;
    }

    @Override
    public Component getCustomEditor(){
        return new JLabel("sfdfdgfdfgdsf");
    }

    @Override
    public boolean supportsCustomEditor(){
        return true;
    }
}