/*
 * Copyright (c) 2016 gir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    gir - initial API and implementation and/or initial documentation
 */
package org.inventory.core.templates.nodes;

import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author gir
 */
public class TemplateElementNode extends AbstractNode {

    public TemplateElementNode(LocalObjectLight object) {
        super(new TemplateElementChildren(), Lookups.singleton(object));
    }

    @Override
    public Action[] getActions(boolean context) {
        return null;
    }
    
    
    
    public static class TemplateElementChildren extends Children.Keys<LocalObjectLight> {
        @Override
        public void addNotify() {
            LocalObjectLight templateElement = getNode().getLookup().lookup(LocalObjectLight.class);
            List<LocalObjectLight> templateElementChildren = CommunicationsStub.getInstance().
                    getTemplateElementChildren(templateElement.getClassName(), templateElement.getOid());
            
            if (templateElementChildren == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                setKeys(Collections.EMPTY_SET);
            } else
                setKeys(templateElementChildren);
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        
        @Override
        protected Node[] createNodes(LocalObjectLight t) {
            return new Node[] {new TemplateElementNode(t)};
        }
    }
}
