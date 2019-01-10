/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.dashboard.widgets;

import java.security.InvalidParameterException;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 * Defines what behavior is expected from a dashboard widget
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractWidget extends JPanel {
    /**
     * The widget was just created
     */
    public static int WIDGET_STATE_CREATED = 0;
    /**
     * The widget has been configured
     */
    public static int WIDGET_STATE_SET_UP = 1;
    /**
     * The widget has been initialized
     */
    public static int WIDGET_STATE_INITIALIZED = 2;
    /**
     * The widget has been shut down
     */
    public static int WIDGET_STATE_DONE = 3;
    
    protected int state;

    public AbstractWidget() {
        state = WIDGET_STATE_CREATED;
    }
    
    /**
     * Gets the widget name
     * @return The widget name
     */
    @Override
    public abstract String getName();
    /**
     * Gets the widget title
     * @return The widget title
     */
    public abstract String getTitle();
    /**
     * Gets the widget description
     * @return What the widget does
     */
    public abstract String getDescription();
    /**
     * Gets the widget version
     * @return The widget version
     */
    public abstract String getVersion();
    /**
     * Gets the widget vendor
     * @return The widget vendor
     */
    public abstract String getVendor();
    /**
     * Configures the widget, that usually implies providing initial parameters to it
     * @param parameters The parameters to configure the widget
     * @throws InvalidStateException If the widget is in a state other than created
     * @throws InvalidParameterException If any of the required parameters was not provided
     */
    public abstract void setup(HashMap<String, Object> parameters) throws InvalidStateException, InvalidParameterException;
    /**
     * Initializes the widget
     */
    public abstract void init() throws InvalidStateException;
    /**
     * What happens the the widget is refreshed
     */
    public abstract void refresh() throws InvalidStateException;
    /**
     * Ends the widget's life cycle
     */
    public abstract void done() throws InvalidStateException;
    
    /**
     * Thrown if one of the widget's lifecycle methods is called at an inappropriate moment
     */
    public class InvalidStateException extends Exception {

        public InvalidStateException(String message) {
            super(message);
        }
        
    }
}
