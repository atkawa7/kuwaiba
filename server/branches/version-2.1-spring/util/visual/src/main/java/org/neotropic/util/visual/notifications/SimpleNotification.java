/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.util.visual.notifications;

import com.vaadin.flow.component.notification.Notification;

/**
 * Default implementation of a notification component.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SimpleNotification extends AbstractNotification {
    
    public SimpleNotification(String title, String text) {
        super(title, text);
    }

    @Override
    public void open() {
        Notification.show(text, 4000, Notification.Position.BOTTOM_CENTER);
    }

    @Override
    public void close() { }

}
