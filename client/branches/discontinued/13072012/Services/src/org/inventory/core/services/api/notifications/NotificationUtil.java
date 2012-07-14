package org.inventory.core.services.api.notifications;

/**
 * Exposes notification services
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface NotificationUtil {
    public static int ERROR=1;
    public static int WARNING=2;
    public static int INFO=3;

    public void showSimplePopup(String title, int icon, String details);
    public void showStatusMessage(String message, boolean important);
}
