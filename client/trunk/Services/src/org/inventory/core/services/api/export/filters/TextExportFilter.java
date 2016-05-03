/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.services.api.export.filters;

import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JPanel;

/**
 * All filters must implement this interface
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class TextExportFilter {
    /**
     * To be used as label in the formats combo box
     * @return the name to be displayed
     */
    public abstract String getDisplayName();
    /**
     * Gets the file extension
     * @return the file extension (including the dot)
     */
    public abstract String getExtension();
    /**
     * The export action. This method WON'T close the stream!
     * @throws 
     */
    public abstract boolean export(Object[][] result, FileOutputStream out) throws IOException;
    /**
     * The settings panel
     * @return 
     */
    public abstract JPanel getExportSettingsPanel();
    
    @Override
    public String toString(){
        return getDisplayName();
    }
}
