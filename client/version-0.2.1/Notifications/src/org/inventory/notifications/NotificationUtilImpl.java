package org.inventory.notifications;

import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;

/**
 * This class provides mechanisms to perform different notifications. By now this is
 * a wrapper module for existing notifications but we'll be looking forward to extend the scope
 * adding connectors for remote notifications or integration with IM services, to name some
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class NotificationUtilImpl extends NotificationDisplayer
        implements NotificationUtil {
    static final String ERROR_ICON_PATH="/org/inventory/notifications/res/error.png";
    static final String WARNING_ICON_PATH="/org/inventory/notifications/res/warning.png";
    static final String INFO_ICON_PATH="/org/inventory/notifications/res/info.png";
    /**
     * Temporal workaround to clear the last notification from the tray
     */
    private Notification lastNotification;

    @Override
    public Notification notify(String string, Icon icon, String string1, ActionListener al, Priority prt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Notification notify(String string, Icon icon, JComponent jc, JComponent jc1, Priority prt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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
            if (lastNotification != null)
                lastNotification.clear();
            lastNotification = NotificationDisplayer.getDefault().notify(title,popupIcon, details, null);
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
