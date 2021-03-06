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
package org.inventory.communications.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.inventory.objectcache.Cache;

/**
 * Refreshes the local cache
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public final class RefreshLocalCache implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        Cache.getInstace().resetLists();
        Cache.getInstace().resetLightMetadataIndex();
        Cache.getInstace().resetMetadataIndex();
        Cache.getInstace().resetPossibleChildrenCached();
        JOptionPane.showMessageDialog(null, "The local cache was reset",
                "Refresh Operation",JOptionPane.INFORMATION_MESSAGE);
    }
}
