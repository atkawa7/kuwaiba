/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp.google.overlays;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public interface Overlay {
    /**
     * A overlay can be removed by code or from view use this operation to 
     * specify the method.
     * 
     * @param removed the overlay was removed by code (false) or from view (true)
     */
    public void removedFromView(boolean removed);
    /**
     * The overlay was removed by code (false) from view (true).
     * 
     * @return true, if the overlay was removed
     */
    public boolean getRemoved();
}
