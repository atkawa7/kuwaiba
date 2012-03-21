/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.core.services.exceptions;

/**
 * An exception raised when performing an action over an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectActionException extends Exception{

    public static final int SEVERE = 0;
    public static final int WARNING = 1;
    private int level;

    public ObjectActionException(String msg, int severity) {
        super(msg);
        this.level = severity;
    }

}
