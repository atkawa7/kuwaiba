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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Default implementation of a notification component.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SimpleNotification extends AbstractNotification {
    
    public SimpleNotification(String title, String text, NotificationType type, TranslationService ts) {
        super(title, text, type, ts);
    }

    @Override
    public void open() {
        Notification notification = new Notification();
        VerticalLayout lytContent = new VerticalLayout();                      
        H4 lblTitle = new H4(this.title);
        Label lblText = new Label(this.text);
        Button btnCopyToClipboard = new Button(ts.getTranslatedString("module.general.labels.copy-to-clipboard"));
        //content properties
        lytContent.setSpacing(false);
        lytContent.setPadding(false);
        lytContent.setMargin(false);
        lytContent.setWidth("100%");
        
        switch (type) {
            default:
            case INFO:
                lblTitle.setClassName("simple-notification-title-info");
                break;
            case WARNING:
                lblTitle.setClassName("simple-notification-title-warning");
                break;
            case ERROR:
                lblTitle.setClassName("simple-notification-title-error");
                btnCopyToClipboard.setVisible(true);                
                
        }

        lytContent.add(lblTitle, lblText);        
        lytContent.add(btnCopyToClipboard);
        lytContent.setHorizontalComponentAlignment(FlexComponent.Alignment.END, btnCopyToClipboard);                
        lytContent.addClickListener(e-> notification.close());

        notification.setThemeName("simple-notification");
        notification.add(lytContent);        
        notification.setDuration(300000);
        notification.setPosition(Notification.Position.BOTTOM_CENTER);
        notification.open();
    }

    @Override
    public void close() { }
    
    
}
