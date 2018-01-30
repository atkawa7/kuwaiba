/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.core.services.api.notifications;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ServiceProvider(service = StatusLineElementProvider.class)
public class StatusUtil implements StatusLineElementProvider {
    private final JLabel label = new JLabel("");
    private final JPanel panel = new JPanel(new BorderLayout());
        
    public StatusUtil() {
        panel.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.WEST);
        panel.add(label, BorderLayout.CENTER);
    }
    
    public static StatusUtil getInstance() {
        return Lookup.getDefault().lookup(StatusUtil.class);
    }
    
    public void setStatusText(String statusText) {
        label.setText(statusText);
    }

    @Override
    public Component getStatusLineElement() {
        return panel;
    }
    
}
