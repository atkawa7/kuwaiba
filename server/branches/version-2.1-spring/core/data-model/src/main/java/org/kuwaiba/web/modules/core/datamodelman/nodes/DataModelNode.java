/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.modules.core.datamodelman.nodes;

import org.neotropic.kuwaiba.core.apis.integration.AbstractAction;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * Represents a node in the data model manager tree.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>}
 */
public class DataModelNode extends AbstractNode<ClassMetadataLight>{

    public DataModelNode(ClassMetadataLight object) {
        super(object);
    }

    public DataModelNode(ClassMetadataLight object, String className) {
        super(object, className);
    }
    
    @Override
    public AbstractAction[] getActions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(boolean recursive) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
