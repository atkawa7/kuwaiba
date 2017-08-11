/*
 * Copyright (c) 2017 johnyortega.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    johnyortega - initial API and implementation and/or initial documentation
 */
package org.inventory.core.services.api.actions;

import java.awt.event.ActionEvent;

/**
 * A composed action is an action used to show subMenus or an set of instructions
 * to do after an actionPerformed call
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public interface ComposedAction {
    void finalActionPerformed(ActionEvent e);
}
