/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.core.services.interfaces.connections;

import java.awt.Color;

/**
This interface is used to map the physical connections (electrical, optical and wireless)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface LocalPhysicalConnection {
    public static Color COLOR_ELECTRICAL = new Color(255, 102, 0);
    public static Color COLOR_OPTICAL = new Color(0, 128, 0);
    public static Color COLOR_WIRELESS = new Color(102, 0, 128);
}
