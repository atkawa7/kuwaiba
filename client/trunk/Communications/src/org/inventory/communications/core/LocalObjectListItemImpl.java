package org.inventory.communications.core;

import org.inventory.core.services.interfaces.LocalObjectListItem;

/**
 * Esta clase representa un elemento de una lista (para desplegar enumeraciones)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalObjectListItemImpl implements LocalObjectListItem{
    private Long id;
    private String name;
    private String displayName;
    private String className;

    public LocalObjectListItemImpl(){
    }

    public LocalObjectListItemImpl(Long _id, String _className,String _name, String _displayName){
        this.id = _id;
        this.className = _className;
        this.name = _name;
        this.displayName = _displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName(){
        return this.className;
    }

    @Override
    public String toString(){
        return this.displayName;
    }

    public static LocalObjectListItem getNullValue() {
        return new LocalObjectListItemImpl(LocalObjectListItemImpl.NULL_ID,null,"NULL","None");
    }

    //Workaround
    public LocalObjectListItem getNull(){
        return LocalObjectListItemImpl.getNullValue();
    }

}
