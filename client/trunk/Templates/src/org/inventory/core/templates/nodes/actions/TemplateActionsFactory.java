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
package org.inventory.core.templates.nodes.actions;

/**
 * Factory for all actions to be used by nodes in this module
 * @author gir
 */
public class TemplateActionsFactory {
    static CreateTemplateAction createTemplateAction;
    static CreateTemplateElementAction createTemplateElementAction;
    static DeleteTemplateElementAction deleteTemplateElementAction;
    
    public static CreateTemplateAction getCreateTemplateAction() {
        if (createTemplateAction == null)
            createTemplateAction = new CreateTemplateAction();
        return createTemplateAction;
    }
    
    public static CreateTemplateElementAction getCreateTemplateElementAction() {
        if (createTemplateElementAction == null)
            createTemplateElementAction = new CreateTemplateElementAction();
        return createTemplateElementAction;
    }
    
    public static DeleteTemplateElementAction getDeleteTemplateElementAction() {
        if (deleteTemplateElementAction == null)
            deleteTemplateElementAction = new DeleteTemplateElementAction();
        return deleteTemplateElementAction;
    }
}
