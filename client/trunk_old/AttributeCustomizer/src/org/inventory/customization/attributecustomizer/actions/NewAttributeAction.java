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
package org.inventory.customization.attributecustomizer.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.inventory.customization.attributecustomizer.nodes.ClassMetadataNode;


public class NewAttributeAction extends AbstractAction{

    private ClassMetadataNode node;

    public NewAttributeAction(ClassMetadataNode _node){
        putValue(NAME, "Add Attribute");
        this.node = _node;
    }

    public void actionPerformed(ActionEvent ae) {
        
    }

    @Override
    public boolean isEnabled(){
        return false;
    }
}