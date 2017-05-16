/**
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.graphicalrepresentation.specialrelationships.widgets;

import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Widget that represents Local Object Light Wrapper
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SpecialRelatedObjectNodeWidget extends IconNodeWidget {
    private Lookup lookup;

    public SpecialRelatedObjectNodeWidget(Scene scene, LocalObjectLight businessObject) {
        super(scene);
        lookup = Lookups.singleton(new ObjectNode(businessObject));
        setToolTipText(businessObject.toString());
    }
    
    @Override
    public Lookup getLookup() {
        return lookup;
    }
}