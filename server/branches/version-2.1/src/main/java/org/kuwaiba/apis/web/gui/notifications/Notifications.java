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

import com.vaadin.flow.component.notification.Notification;

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
        Notification wndError = new Notification(message);
        wndError.setDuration(POPUP_DELAY);
        wndError.setPosition(Notification.Position.BOTTOM_CENTER);
        wndError.open();                
//        wdwError.setIcon(new ThemeResource("icons/icon_error.png")); //NOI18N
//        wdwError.setStyleName("gray"); //NOI18N
    }
    
    public static void showInfo(String message) {
        Notification wndInfo = new Notification(message);
        wndInfo.setDuration(POPUP_DELAY);
        wndInfo.setPosition(Notification.Position.BOTTOM_CENTER);
        wndInfo.open();
//        wdwInfo.setStyleName("gray"); //NOI18N
//        wdwInfo.setIcon(new ThemeResource("icons/icon_info.png")); //NOI18N
    }
    
    /**
     * Shows a simple warning message at the bottom of the screen for POPUP_DELAY milliseconds
     * @param message The message to be displayed
     */
    public static void showWarning(String message) {
        Notification wndWarning = new Notification(message);
        wndWarning.setDuration(POPUP_DELAY);
        wndWarning.setPosition(Notification.Position.BOTTOM_CENTER);
        wndWarning.open();
//        wdwInfo.setStyleName("gray"); //NOI18N
//        wdwInfo.setIcon(new ThemeResource("icons/icon_warning.png")); //NOI18N
    }    
}
