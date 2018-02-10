/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.services.utils;

import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 * The ImageIconResource groups the common icons used by the application modules
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ImageIconResource {
    private static final String WARNING_ICON_PATH = "org/inventory/core/services/res/icons/warning.png";
    public static ImageIcon WARNING_ICON = ImageUtilities.loadImageIcon(WARNING_ICON_PATH, false);
    
    private static final String EXPORT_ICON_PATH = "org/inventory/core/services/res/icons/export.png";
    public static ImageIcon EXPORT_ICON = ImageUtilities.loadImageIcon(EXPORT_ICON_PATH, false);
    
    private static final String IMPORT_ICON_PATH = "org/inventory/core/services/res/icons/import.png";
    public static ImageIcon IMPORT_ICON = ImageUtilities.loadImageIcon(IMPORT_ICON_PATH, false);
    
    private static final String REFRESH_ICON_PATH = "org/inventory/core/services/res/icons/import.png";
    public static ImageIcon REFRESH_ICON = ImageUtilities.loadImageIcon(REFRESH_ICON_PATH, false);
}
