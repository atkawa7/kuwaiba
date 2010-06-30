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
package org.inventory.core.authentication;

import org.openide.modules.ModuleInstall;

/**
 * This installer shows the login window and injects the user profile into the global lookup
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        new AuthenticationFrame().setVisible(true);
    }
}
