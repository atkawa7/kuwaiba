/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.inventory.queries.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.queries.scene.QueryEditorScene;
import org.inventory.queries.scene.ClassNodeWidget;
import org.inventory.queries.scene.QueryEditorNodeWidget;
import org.inventory.queries.scene.filters.ListTypeFilter;
import org.netbeans.api.visual.widget.Widget;

/**
 * A simple action to toggle a class node into simple/complex. The simple will present
 * a combo box with all the list type elements for the selected attribute type. If complex, the
 * node will present instead, a list of attributes for the given list type
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SwitchClassNodeWidgetFilterAction extends AbstractAction{

        private Widget classNode;

        public SwitchClassNodeWidgetFilterAction() {
            putValue(NAME, "Toggle Simple/Detailed view");
        }

        public SwitchClassNodeWidgetFilterAction(ClassNodeWidget node){
            this();
            classNode = node;
        }

        public SwitchClassNodeWidgetFilterAction(ListTypeFilter node){
            this();
            classNode = node;
        }

    @Override
        public void actionPerformed(ActionEvent e) {
            String incomingConnectionObject = ((QueryEditorScene)classNode.getScene()).
                    findPinEdges(((QueryEditorNodeWidget)classNode).getDefaultPinId(), false, true).iterator().next();
            if (classNode instanceof ClassNodeWidget){ //Extended to simple
                ClassNodeWidget node = (ClassNodeWidget)classNode;
                ((QueryEditorScene)node.getScene()).removeAllRelatedNodes(node.getWrappedClass(), false);

                ListTypeFilter newNode = (ListTypeFilter) ((QueryEditorScene)node.getScene()).
                        addNode(node.getWrappedClassLigth());
                List<LocalObjectListItem> items = CommunicationsStub.getInstance().getList(node.getWrappedClassLigth().getClassName(), true, false);
                if (items == null)
                    newNode.build(new ArrayList<LocalObjectListItem>());
                else
                    newNode.build(items);
                newNode.getScene().validate();
                newNode.setPreferredLocation(node.getPreferredLocation());
                ((QueryEditorScene)node.getScene()).setEdgeTarget(incomingConnectionObject, newNode.getDefaultPinId());
                
            }else{ //Simple to extended
               ListTypeFilter node = (ListTypeFilter)classNode;
               
               ((QueryEditorScene)node.getScene()).removeNode(node.getWrappedClass());
               LocalClassMetadata lcm = CommunicationsStub.getInstance().getMetaForClass(node.getNodeName(), false);
               ClassNodeWidget newNode;
               if (lcm != null)
                    newNode = (ClassNodeWidget) ((QueryEditorScene)node.getScene()).addNode(lcm);
               else return;
               newNode.build(null);
               newNode.getScene().validate();
               newNode.setPreferredLocation(node.getPreferredLocation());
               ((QueryEditorScene)node.getScene()).setEdgeTarget(incomingConnectionObject, newNode.getDefaultPinId());
            }


        }
    }
