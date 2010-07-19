/*
 *  Copyright 2010 zim.
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
package org.inventory.core.usermanager.actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import org.inventory.core.services.interfaces.LocalUserObject;

public final class DeleteUser implements ActionListener {

    private final List<LocalUserObject> context;

    public DeleteUser(List<LocalUserObject> context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        for (LocalUserObject localUserObject : context) {
            // TODO use localUserObject
        }
    }
}
