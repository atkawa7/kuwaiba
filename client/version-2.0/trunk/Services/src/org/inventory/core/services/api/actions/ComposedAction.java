/*
 * Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.core.services.api.actions;

import java.awt.event.ActionEvent;

/**
 * A composed action is an action used to show sub-menus or to perform a set of instructions
 * to after the main actionPerformed is called. Most of these actions are used so the sub-menu is created lazily 
 * as an external JFrame/JDialog instead of an actual sub-menu as in JMenu. When node actions show sub-menus using
 * JMenus, the options are not loaded lazily and if they involve calls to the server to be generated, that might imply
 * extra, unnecessary requests to the server.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface ComposedAction {
    void finalActionPerformed(ActionEvent e);
}
