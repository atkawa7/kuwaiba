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
package org.kuwaiba.web.menucommand;

import com.neotropic.kuwaiba.modules.reporting.img.SceneExporter;
import com.neotropic.kuwaiba.modules.reporting.img.rackview.RackViewImage;
import com.neotropic.kuwaiba.modules.reporting.img.rackview.RackViewScene;
import com.neotropic.kuwaiba.modules.reporting.img.rackview.RackViewService;
import com.vaadin.ui.MenuBar;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * This menu bar command updates the rack views show in the Kuwaiba Web Client
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RackViewUpdater implements MenuBar.Command {
    private final WebserviceBean wsBean;
    private final RemoteSession session;
    
    public RackViewUpdater(WebserviceBean wsBean, RemoteSession session) {
        this.wsBean = wsBean;
        this.session = session;
    }

    @Override
    public void menuSelected(MenuBar.MenuItem selectedItem) {
        try {
            List<RemoteObjectLight> racks = wsBean.getObjectsOfClassLight("Rack", -1, session.getIpAddress(), session.getSessionId()); //NOI18N
            String oldPath = SceneExporter.PATH;                    
            String newPath = (String) new InitialContext().lookup("java:comp/env/attachmentsPath"); //NOI18N
            SceneExporter.PATH = newPath;
            for (RemoteObjectLight rack : racks) {
                RackViewImage rackViewImage = RackViewImage.getInstance();
                rackViewImage.setIpAddress(session.getIpAddress());
                rackViewImage.setRemoteSession(session);
                rackViewImage.setWebserviceBean(wsBean);

                try {
                    RemoteObject rackObject = wsBean.getObject(rack.getClassName(), rack.getId(), session.getIpAddress(), session.getSessionId());

                    RackViewScene rackViewScene = new RackViewScene(RackViewImage.getInstance().getDevices(rackObject));
                    rackViewScene.setShowConnections(true);

                    RackViewService service = new RackViewService(rackViewScene, rackObject);

                    service.shownRack();

                    try {
                        org.netbeans.api.visual.export.SceneExporter.createImage(rackViewScene,
                                new File(SceneExporter.PATH + "/tmpRackView_" + rack.getId() + ".png"),
                                org.netbeans.api.visual.export.SceneExporter.ImageType.PNG,
                                org.netbeans.api.visual.export.SceneExporter.ZoomType.ACTUAL_SIZE,
                                false, false, 100,
                                0,  //Not used
                                0); //Not used
                    } catch (IOException ex) {
                        Notifications.showError(ex.getMessage());
                    }

                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                }
            }
            SceneExporter.PATH = oldPath;
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
        Notifications.showInfo("Rack Views Updated");                
    }
    }