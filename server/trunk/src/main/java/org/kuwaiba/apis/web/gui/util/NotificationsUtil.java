 /*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.apis.web.gui.util;

import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;

/**
 * A simple utility class that extends the functionality of notifications and popups in Vaadin
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class NotificationsUtil {
    /**
     * Time an error message will be displayed on screen
     */
    public static int ERROR_POPUP_DELAY = 3000;
    
    /**
     * Shows a simple error message at the bottom of the screen for ERROR_POPUP_DELAY milliseconds
     * @param message The message to be displayed
     */
    public static void showError(String message) {
        Notification ntfLoginError = new Notification(message, Notification.Type.ERROR_MESSAGE);
        ntfLoginError.setPosition(Position.BOTTOM_CENTER);
        ntfLoginError.setDelayMsec(3000);
        ntfLoginError.show(Page.getCurrent());
    }
}
