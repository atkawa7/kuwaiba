package org.inventory.notifications;

import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;

/**
 * Esta clase se encarga de proveer mecanismos para realizar notificaciones. Se
 * crea un módulo aparte para este tema (la notificaciones igual se podrían hacer desde
 * las instancias que lo necesiten directamente) debido a que se espera que en el futuro
 * los mecanismos de notificación permitan realizar acciones avanzadas relativas a trabajo
 * colaborativo, además de poder personalizar el look 'n feel
 * IMPORTANTE: Esta clase debe estar registrada en el META-INF del módulo, de tal manera
 * que quienes lo usen por medio de la llamada Lookup.getDefault().lookup(NotificationUtil.class)
 * puedan encontrar la implementación, esto es, crear un archico en el directorio
 * META-INF/services/nombre_y_paquete_de_la_interfaz con una línea dentro con el paquete_nombre de
 * la implementación
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class NotificationUtilImpl extends NotificationDisplayer implements NotificationUtil {
    static final String ERROR_ICON_PATH="/org/inventory/notifications/res/error.png";
    static final String WARNING_ICON_PATH="/org/inventory/notifications/res/warning.png";
    static final String INFO_ICON_PATH="/org/inventory/notifications/res/info.png";

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
        NotificationDisplayer.getDefault().notify(title,popupIcon, details, null);
    }
}
