package org.inventory.customization.attributecustomizer.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.inventory.core.services.interfaces.LocalClassMetadata;


public class NewAttributeAction extends AbstractAction{

    public NewAttributeAction(LocalClassMetadata lcm){
        putValue(NAME, "Adicionar Atributo");
    }

    public void actionPerformed(ActionEvent ae) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}