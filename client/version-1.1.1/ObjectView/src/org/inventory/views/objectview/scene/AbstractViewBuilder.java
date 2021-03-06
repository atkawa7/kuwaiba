/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.views.objectview.scene;

import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.scene.AbstractScene;

/**
 * This is the template for all views related to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface  AbstractViewBuilder {
    public String getName();
    public AbstractScene getScene();
    public void buildView(LocalObjectLight object) throws IllegalArgumentException;
    public boolean supportsClass(String className);
    public void refresh();
    public void saveView();
}
