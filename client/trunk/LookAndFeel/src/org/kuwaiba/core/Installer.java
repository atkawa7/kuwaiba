/*
 *  Copyright 2011 zim.
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
package org.kuwaiba.core;

import javax.swing.UIManager;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        try{
           //UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceNebulaBrickWallLookAndFeel");
           //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
           UIManager.setLookAndFeel("com.nilo.plaf.nimrod.NimRODLookAndFeel");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
