/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.inventory.customization.attributecustomizer.nodes;

import org.inventory.customization.attributecustomizer.nodes.reflection.ClassCustomizerNodeProperty;
import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Esta clase representa los nodos de atributos
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
    //
public class AttributeMetadataNode extends AbstractNode{
    static final String ICON_PATH = "org/inventory/customization/attributecustomizer/res/flag-blue.png";
    private LocalAttributeMetadata lam;

    public AttributeMetadataNode(LocalAttributeMetadata lam) {
        super(Children.LEAF,Lookups.singleton(lam));
        setIconBaseWithExtension(ICON_PATH);
        this.lam = lam;
    }

    @Override
    public String getDisplayName(){
       return this.lam.getName();
    }

    //Este método expone las propiedades del nodo, de tal manera que pueda ser mapeadas por el
   //treetable para las columnas
   @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        if (ss == null) {
            ss = Sheet.createPropertiesSet();
            s.put(ss);
        }

        ss.put(new ClassCustomizerNodeProperty("name",lam.getName(),"Name","Nombre del atributo"));
        ss.put(new ClassCustomizerNodeProperty("type",lam.getType(),"Type","Tipo de dato del atributo"));
        ss.put(new ClassCustomizerNodeProperty("displayName",lam.getDisplayName(),"Label","Nombre de despliegue en la interfaz"));
        ss.put(new ClassCustomizerNodeProperty("isVisible",lam.getIsVisible(),"Visible","Se debe mostrar en la interfaz?"));
        ss.put(new ClassCustomizerNodeProperty("isAdministrative",lam.getIsAdministrative(),"Administrative","Es un atributo administrativo?"));
        ss.put(new ClassCustomizerNodeProperty("description",lam.getDescription(),"Description","Descripción de atributo"));
        return s;
    }
}