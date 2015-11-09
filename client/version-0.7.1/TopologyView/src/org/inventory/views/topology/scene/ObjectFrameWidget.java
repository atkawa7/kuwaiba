/**
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.views.topology.scene;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import org.inventory.views.topology.scene.provider.LabelTextFieldEditor;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;

/**
 * a frame with a title
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ObjectFrameWidget extends Widget {

    private ObjectLabelWidget titleLabel;

    public ObjectFrameWidget(GraphScene<Object, String> scene, String title) {
        super(scene);
        this.setToolTipText ("Double-click to title text, resize on the corners");
        this.setBorder (BorderFactory.createImageBorder (new Insets (5, 5, 5, 5), Utilities.loadImage ("org/inventory/views/topology/res/shadow_normal.png"))); // NOI18N
        this.setLayout (LayoutFactory.createVerticalFlowLayout (LayoutFactory.SerialAlignment.LEFT_TOP, 0));
        this.setPreferredBounds (new Rectangle (200, 200));

        titleLabel = new ObjectLabelWidget(scene, title);
        titleLabel.getActions().addAction (ActionFactory.createInplaceEditorAction (new LabelTextFieldEditor()));
        this.addChild(titleLabel);
    }

    public ObjectLabelWidget getTitleLabel() {
        return titleLabel;
    }
}
