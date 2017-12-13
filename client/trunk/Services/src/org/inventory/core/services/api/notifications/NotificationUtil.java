/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
 */
package org.inventory.core.services.api.notifications;

import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;

/**
 * Useful methods to show notifications
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class NotificationUtil {
    public static final int ERROR_MESSAGE = 1;
    public static final int WARNING_MESSAGE = 2;
    public static final int INFO_MESSAGE = 3;
    
    static final ImageIcon ERROR_ICON = new ImageIcon(NotificationUtil.class.getResource("/org/inventory/core/services/res/error.png"));
    static final ImageIcon WARNING_ICON = new ImageIcon(NotificationUtil.class.getResource("/org/inventory/core/services/res/warning.png"));
    static final ImageIcon INFO_ICON = new ImageIcon(NotificationUtil.class.getResource("/org/inventory/core/services/res/info.png"));

    /**
     * Timer to control how long will the pop-up will be visible
     */
    private Timer controller;
    /**
     * Singleton
     */
    private static NotificationUtil self;

    private NotificationUtil() {}

    public static NotificationUtil getInstance() {
        if (self == null)
            self = new NotificationUtil();
        return self;
    }
    
    /**
     * Shows a simple pop-up notification on the bottom right corner of the screen
     * @param title Title
     * @param iconType Icon type (see constants ERROR_MESSAGE, WARNING_MESSAGE and INFO_MESSAGE)
     * @param text The actual message
     */
    public void showSimplePopup(String title, int iconType, String text){
        ImageIcon popupIcon;
        int delay; //Not all notifications will be displayed during the same time
        switch(iconType){
            case ERROR_MESSAGE:
                popupIcon = ERROR_ICON;
                delay = 20000;
                break;
            case WARNING_MESSAGE:
                popupIcon = WARNING_ICON;
                delay = 10000;
                break;
            case INFO_MESSAGE:
            default:
                delay = 5000;
                popupIcon = INFO_ICON;
        }
        
        if (NotificationDisplayer.getDefault() != null) {
            final Notification lastNotification = NotificationDisplayer.getDefault().
                    notify(title, popupIcon, text, null);

            //Thanks to Luca Dazi for this suggestion
            if (lastNotification != null) {
                if (null == controller)
                    controller = new Timer();
                TimerTask tt = new TimerTask() {
                        @Override
                        public void run() {
                            lastNotification.clear();
                        }
                    };
                
                controller.schedule(tt, delay);
            }
        }
    }

    public void showStatusMessage(String message, boolean important) {
        if (StatusDisplayer.getDefault() != null){
            if (important)
                StatusDisplayer.getDefault().setStatusText(message, StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
            else
                StatusDisplayer.getDefault().setStatusText(message);
        }
    }
}
