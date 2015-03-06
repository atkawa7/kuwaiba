/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.communications.core;

import java.awt.Color;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.util.Utils;

/**
 * Implementation of the common interface to represent the class metadata in a simple
 * way so it can be shown in trees and lists. This is done because to bring the whole
 * metadata is not necessary (ie. Container Hierarchy Manager)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalClassMetadataLight implements Transferable {
    /**
     * Class id
     */
    protected long id;
    /**
     * Class name
     */
    protected String className;
    /**
     * Class display name
     */
    protected String displayName;
    /**
     * Parent ClassMetada name
     */
    protected String parentName;
    /**
     * Is this class abstract?
     */
    protected boolean _abstract;
    /**
     * Is this class subclass of viewable? (this is, can have views attached)
     */
    protected boolean viewable;
    /**
     * Is this class subclass of GenericObject list? (this is, a list type)
     */
    protected boolean listType;
    /**
     * Shows if this is a core class (the ones provided in the official release) or a custom one
     */
    protected boolean custom;
    /**
     * Is the class "operational" or is it in design process?
     */
    protected boolean inDesign;
    /**
     * Class icon for trees. This is the icon the instances of this class will show in trees
     */
    protected Image smallIcon;
    /**
     * Color for the class. The class will show this color in maps 
     */
    private Color color;
    
    /**
     * Misc validators. Validators tell you states of this class related to business rules
     */
    protected HashMap<String, Integer> validators;
    /**
     * The list of property change listeners
     */
    protected List<PropertyChangeListener> propertyChangeListeners;
    public static final DataFlavor DATA_FLAVOR =
            new DataFlavor(LocalClassMetadataLight.class,"Object/LocalClassMetadataLight");
    /**
     * This constructor is called to create dummy class metadata objects, such as that used to represent the Navigation Tree root
     */
    public LocalClassMetadataLight() {  this.id = -1;  }

    public LocalClassMetadataLight(long id, String className, String displayName, 
            String parentName, boolean _abstract, boolean viewable, boolean listType, 
            boolean custom, boolean inDesign,byte[] smallIcon, int color, HashMap<String, Integer> validators) {
        this.id = id;
        this.className = className;
        this.displayName = displayName;
        this.parentName = parentName;
        this._abstract = _abstract;
        this.viewable = viewable;
        this.listType = listType;
        this.custom = custom;
        this.inDesign = inDesign;
        this.smallIcon = Utils.getIconFromByteArray(smallIcon, new Color(color),
                Utils.DEFAULT_ICON_WIDTH, Utils.DEFAULT_ICON_HEIGHT);
        this.color = new Color(color);
        this.validators = validators;
    }

    public String getClassName() {
        return className;
    }

    //Only the name can be updated so the objects behind de ClassMetadataNode
    public void setClassName(String className) {
        this.className = className;
    }
    
    public long getOid() {
        return id;
    }

    @Override
    public String toString(){
        return className;
    }

    public boolean isAbstract() {
        return _abstract;
    }
    
    public boolean isViewable(){
        return this.viewable;
    }
    
    public boolean isListType(){
        return this.listType;
    }
    
   /**
    * The equals method is overwritten in order to make the comparison based on the id, which is
    * the actual unique identifier (this is used when filtering the list of possible children in the Hierarchy Manager)
    */
   @Override
   public boolean equals(Object obj){
       if(obj == null) {
           return false;
       }
       if (!(obj instanceof LocalClassMetadataLight)) {
           return false;
       }
       return (this.getOid() == ((LocalClassMetadataLight)obj).getOid());
   }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 97 * hash + (this.className != null ? this.className.hashCode() : 0);
        return hash;
    }
   
    public String getDisplayName(){
        if (displayName == null) {
            return className;
        }
        if (displayName.trim().equals("")) {
            return className;
        }
        return displayName;
    }
        
    public Image getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(Image newIcon){
        this.smallIcon = newIcon;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR};
    }

    /**
     * Retrieves the value of a given validator
     * @param validatorName validator's name
     * @return value for the given validator. false if the validator is not present
     */
    public int getValidator(String validatorName){
        return validators.get(validatorName) == null ? 0 : validators.get(validatorName);
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DATA_FLAVOR);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            return this;
        }
        else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public String getParentName() {
        return parentName;
    }

    public boolean isCustom() {
        return custom;
    }
    
    public boolean isInDesign() {
        return inDesign;
    }
        
    public void addPropertyChangeListener(PropertyChangeListener newListener){
        if (propertyChangeListeners == null){
            propertyChangeListeners = new ArrayList<PropertyChangeListener>();
        }
        if (propertyChangeListeners.contains(newListener)){
            return;
        }
        propertyChangeListeners.add(newListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        if (propertyChangeListeners == null){
            return;
        }
        propertyChangeListeners.remove(listener);
    }

    public void firePropertyChangeEvent(String property, Object oldValue, Object newValue){
        for (PropertyChangeListener listener : propertyChangeListeners){
            listener.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }
 }