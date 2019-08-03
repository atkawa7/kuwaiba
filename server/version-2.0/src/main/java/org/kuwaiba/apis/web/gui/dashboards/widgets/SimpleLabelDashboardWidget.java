/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.web.gui.dashboards.widgets;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;

/**
 * A simple dashboard widget with no cover that displays a custom title and subtitle.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SimpleLabelDashboardWidget extends AbstractDashboardWidget {

    public SimpleLabelDashboardWidget(String title, String subtitle) {
        super(title);
        addComponents(new Label(String.format("<h2>%s</h2>", title), ContentMode.HTML),
                new Label(subtitle));
    }

    @Override
    public void createCover() { }

    @Override
    public void createContent() { }
}
