 /*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.notifications;

import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;

/**
 * A simple utility class that extends the functionality of notifications and popups in Vaadin
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Notifications {
    /**
     * Time an error message will be displayed on screen
     */
    public static int POPUP_DELAY = 4000;
    
    /**
     * Shows a simple error message at the bottom of the screen for ERROR_POPUP_DELAY milliseconds
     * @param message The message to be displayed
     */
    public static void showError(String message) {
        Notification wdwError = new Notification(message, Notification.Type.ERROR_MESSAGE);
        wdwError.setPosition(Position.BOTTOM_CENTER);
        wdwError.setDelayMsec(POPUP_DELAY);
        wdwError.setIcon(new ThemeResource("icons/icon_error.png")); //NOI18N
        wdwError.setStyleName("gray"); //NOI18N
        wdwError.show(Page.getCurrent());
    }
    
    public static void showInfo(String message) {
        Notification wdwInfo = new Notification(message, Notification.Type.ASSISTIVE_NOTIFICATION);
        wdwInfo.setPosition(Position.BOTTOM_CENTER);
        wdwInfo.setDelayMsec(POPUP_DELAY);
        wdwInfo.setStyleName("gray"); //NOI18N
        wdwInfo.setIcon(new ThemeResource("icons/icon_info.png")); //NOI18N
        wdwInfo.show(Page.getCurrent());
    }
    
    /**
     * Shows a simple warning message at the bottom of the screen for POPUP_DELAY milliseconds
     * @param message The message to be displayed
     */
    public static void showWarning(String message) {
        Notification wdwInfo = new Notification(message, Notification.Type.WARNING_MESSAGE);
        wdwInfo.setPosition(Position.BOTTOM_CENTER);
        wdwInfo.setDelayMsec(POPUP_DELAY);
        wdwInfo.setStyleName("gray"); //NOI18N
        wdwInfo.setIcon(new ThemeResource("icons/icon_warning.png")); //NOI18N
        wdwInfo.show(Page.getCurrent());
    }
}
