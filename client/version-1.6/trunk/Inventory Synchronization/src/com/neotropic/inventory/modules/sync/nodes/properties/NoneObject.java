/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.sync.nodes.properties;

import org.inventory.communications.core.LocalObjectLight;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NoneObject extends LocalObjectLight {
    private static final String NONE = "None";
    private static NoneObject instance;

    private NoneObject() {
        super(-1, NONE, NONE);
    }

    public static NoneObject getInstance() {
        return instance == null ? instance = new NoneObject() : instance;
    }

    @Override
    public String toString() {
        return NONE;
    }
}
