/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.events;

import java.io.Serializable;

/**
 * A simple interface that defines a listener to operation results. The aim of this 
 * listener (very similar to AWT's ActionListener) is to help actors waiting for some operation 
 * or action to be performed to update their respective GUI
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@FunctionalInterface
public interface OperationResultListener extends Serializable {
    public void doIt();
}
