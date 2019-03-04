/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
 */
package org.inventory.vaadin.push;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Singleton
public class ServiceImpl implements Service {
    @Asynchronous
    @Override
    public void runService(UI ui) {
        try {
            Thread.sleep(7000);
            
            ui.access(new Runnable() {
                @Override
                public void run() {
                    Window wnd = new Window();
                    wnd.setModal(true);
                    wnd.setWidth(80, Sizeable.Unit.PERCENTAGE);
                    wnd.setHeight(80, Sizeable.Unit.PERCENTAGE);

                    VerticalLayout vly = new VerticalLayout();
                    vly.setSizeFull();
                    vly.addComponent(new Label("New window"));
                    wnd.setContent(vly);

                    ui.addWindow(wnd);
                }
            });
        } catch (InterruptedException ex) {
        }
    }
    
}
