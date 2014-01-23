/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.notifications;

import java.util.Timer;
import java.util.TimerTask;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class provides mechanisms to perform different notifications. For now this is
 * a wrapper module for existing notifications but we'll be looking forward to extend the scope
 * adding connectors for remote notifications or integration with IM services, to name some
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=NotificationUtil.class)
public class NotificationUtilImpl implements NotificationUtil {
    static final String ERROR_ICON_PATH="/org/inventory/notifications/res/error.png";
    static final String WARNING_ICON_PATH="/org/inventory/notifications/res/warning.png";
    static final String INFO_ICON_PATH="/org/inventory/notifications/res/info.png";
    /**
     * Temporal workaround to clear the last notification from the tray
     */
    private Timer controller;

    public void showSimplePopup(String title, int icon, String details){
        Icon popupIcon;
        switch(icon){
            case ERROR:
                popupIcon = new ImageIcon(getClass().getResource(ERROR_ICON_PATH));
                break;
            case WARNING:
                popupIcon = new ImageIcon(getClass().getResource(WARNING_ICON_PATH));
                break;
            case INFO:
            default:
                popupIcon = new ImageIcon(getClass().getResource(INFO_ICON_PATH));
        }
        if (NotificationDisplayer.getDefault() != null){
            final Notification lastNotification = NotificationDisplayer.getDefault().notify(title,popupIcon, details, null);

            //Thanks to Luca Dazi for this suggestion
            if (lastNotification != null){
                if (null == controller)
                    controller = new Timer();
                TimerTask tt = new TimerTask() {
                        @Override
                        public void run() {
                            lastNotification.clear();
                        }
                    };
                controller.schedule(tt, 10000);
            }
        }
    }

    public void showStatusMessage(String message, boolean important){
        if (StatusDisplayer.getDefault() != null){
            if (important)
                StatusDisplayer.getDefault().setStatusText(message, StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
            else
                StatusDisplayer.getDefault().setStatusText(message);
        }
    }
}
