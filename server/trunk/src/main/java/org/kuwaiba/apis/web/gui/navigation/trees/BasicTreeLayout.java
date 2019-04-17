/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
 */
package org.kuwaiba.apis.web.gui.navigation.trees;

import com.vaadin.server.Resource;
import com.vaadin.ui.IconGenerator;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.ChildrenProvider;
import org.kuwaiba.apis.web.gui.navigation.nodes.InventoryObjectNode;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteValidator;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * A tree that extends the features of the TreeLayout and makes use of the Nodes API
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class BasicTreeLayout extends TreeLayout<AbstractNode> {
    private final IconGenerator<AbstractNode> iconGenerator;
        
    public BasicTreeLayout(ChildrenProvider childrenProvider, IconGenerator<AbstractNode> iconGenerator, AbstractNode... roots) {
        this.iconGenerator = iconGenerator;
        if (roots != null) {
            for (AbstractNode root : roots) {
                String rootCaption = root.getObject() != null ? root.getObject().toString() : "[Not Set]";
                
                if (root.getObject() instanceof RemoteObjectLight) {
                    String validatorColor = null;
                    
                    for (RemoteValidator validator : ((RemoteObjectLight) root.getObject()).getValidators())
                        validatorColor = validator.getProperty(Constants.PROPERTY_COLOR);
                    
                    if (validatorColor != null)
                        rootCaption = String.format("<font color=\"#%s\">%s</font>", validatorColor, root.getObject().toString());
                }
                Resource rootResource = null;
                if (iconGenerator != null) {
                    rootResource = iconGenerator.apply(root);
                }
                addRootItem(root, rootCaption, rootResource);
            }
        }
        if (childrenProvider != null) {
            addExpandItemListener(new ExpandItemListener() {
                @Override
                public void expandItem(ItemLayout itemLayout) {
                    Object item = itemLayout.getItem();
                    if (item instanceof AbstractNode && ((AbstractNode) item).getObject() != null) {
                        List children = childrenProvider.getChildren(((AbstractNode) item).getObject());
                        if (children != null) {
                            for (Object child : children) {
                                AbstractNode childAbstractNode;
                                if (child instanceof RemoteObjectLight) {
                                    childAbstractNode = new InventoryObjectNode((RemoteObjectLight) child);
                                }
                                else {
                                    childAbstractNode = new AbstractNode(child) {
                                            @Override
                                            public AbstractAction[] getActions() { return new AbstractAction[0]; }

                                            @Override
                                            public void refresh(boolean recursive) { }
                                    };
                                }
                                
                                String childCaption = child != null ? child.toString() : "[Not Set]";

                                if (child instanceof RemoteObjectLight) {
                                    String validatorColor = null;

                                    for (RemoteValidator validator : ((RemoteObjectLight) child).getValidators())
                                        validatorColor = validator.getProperty(Constants.PROPERTY_COLOR);

                                    if (validatorColor != null)
                                        childCaption = String.format("<font color=\"#%s\">%s</font>", validatorColor, child.toString());
                                }
                                
                                Resource childResource = null;
                                if (iconGenerator != null) {
                                    childResource = iconGenerator.apply(childAbstractNode);
                                }

                                addItem((AbstractNode) item, childAbstractNode, childCaption, childResource);
                            }
                        }
                    }
                }
            });
        }
    }

    public void resetTo(AbstractNode... newRoots) {
        List<AbstractNode> rootItems = getRootItems();
        while (rootItems.size() > 0)
            removeItem(rootItems.get(0));
        
        if (newRoots != null) {
            for (AbstractNode root : newRoots) {
                String rootCaption = root.getObject() != null ? root.getObject().toString() : "[Not Set]";
                
                if (root.getObject() instanceof RemoteObjectLight) {
                    String validatorColor = null;
                    
                    for (RemoteValidator validator : ((RemoteObjectLight) root.getObject()).getValidators())
                        validatorColor = validator.getProperty(Constants.PROPERTY_COLOR);
                    
                    if (validatorColor != null)
                        rootCaption = String.format("<font color=\"#%s\">%s</font>", validatorColor, root.getObject().toString());
                }
                Resource rootResource = null;
                if (iconGenerator != null) {
                    rootResource = iconGenerator.apply(root);
                }
                addRootItem(root, rootCaption, rootResource);
            }
        }
    }
}