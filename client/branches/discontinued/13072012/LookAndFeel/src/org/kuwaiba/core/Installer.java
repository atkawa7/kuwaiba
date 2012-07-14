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

import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;
import java.awt.Color;
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

            if (System.getProperty("no-laf") == null){
                NimRODTheme nt = new NimRODTheme();
                nt.setPrimary1( new Color(170,136,0));
                nt.setPrimary2( new Color(211,177,45));
                nt.setPrimary3( new Color(236,207,92));
                nt.setSecondary1( new Color(220,220,220));
                nt.setSecondary2( new Color(230,230,230));
                nt.setSecondary3( new Color(240,240,240));
                nt.setWhite(new Color(250, 250, 250));
                nt.setBlack(Color.BLACK);
                nt.setMenuOpacity(195);
                nt.setFrameOpacity(180);
                NimRODLookAndFeel NimRODLF = new NimRODLookAndFeel();
                NimRODLookAndFeel.setCurrentTheme( nt);
                UIManager.setLookAndFeel( NimRODLF);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
