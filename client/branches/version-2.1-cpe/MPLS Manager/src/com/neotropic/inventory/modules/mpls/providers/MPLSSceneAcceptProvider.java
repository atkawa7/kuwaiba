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
package com.neotropic.inventory.modules.mpls.providers;

import com.neotropic.inventory.modules.mpls.scene.MPLSModuleScene;
import com.neotropic.inventory.modules.mpls.windows.EditMPLSLinkEnpointsFrame;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;

/**
 * Provider used to accept the drag and drop of devices in the MPLS scene, also allows
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MPLSSceneAcceptProvider implements AcceptProvider {

    /**
     * Only subclasses of this class will be allowed to be dropped on the scene
     */
    private final String filterClass;
    
    /**
     * This constructor allows to specify the instances of what classes (as in inventory classes) can be dropped where
     * @param scene The related scene
     * @param filterClass The class name of the instances allowed to be dropped here. It'd be useful to use a root, abstract class such as InventoryObject or GenericSomething. Null (or using the other constructor) will allow any inventory object to be added to the scene
     */
    public MPLSSceneAcceptProvider(String filterClass) {
        this.filterClass = filterClass;
    }
   

    @Override
    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
        if (transferable.isDataFlavorSupported(ExTransferable.multiFlavor) || transferable.isDataFlavorSupported(LocalObjectLight.DATA_FLAVOR))
            return ConnectorState.ACCEPT;
        
        return ConnectorState.REJECT_AND_STOP;
    }

    @Override
    public void accept(Widget widget, Point point, Transferable transferable) {
        try {
            List<LocalObjectLight> droppedObjects = new ArrayList<>();
            
            if (transferable.isDataFlavorSupported(ExTransferable.multiFlavor)) {//Many objects are being dragged
                MultiTransferObject mto = (MultiTransferObject)transferable.getTransferData(ExTransferable.multiFlavor);
                for (int i = 0; i < mto.getCount(); i++ ) {
                    if (mto.isDataFlavorSupported(i, LocalObjectLight.DATA_FLAVOR))
                        droppedObjects.add((LocalObjectLight)mto.getTransferData(i, LocalObjectLight.DATA_FLAVOR));
                }
            } else //Only one object is being dragged
                droppedObjects.add((LocalObjectLight)transferable.getTransferData(LocalObjectLight.DATA_FLAVOR));
            MPLSModuleScene scene = (MPLSModuleScene) widget.getScene();
            for (LocalObjectLight droppedObject : droppedObjects) {
                if (!scene.isNode(droppedObject)){
                    if (!CommunicationsStub.getInstance().isSubclassOf(droppedObject.getClassName(), filterClass))
                        JOptionPane.showMessageDialog(null, String.format(I18N.gm("only_allowed_in_this_view"), filterClass), I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
                    else {
                        Widget nodeToConnect = scene.addNode(droppedObject); 
                        //validate is called here, otherwise, the widget won't be able to resolve its bounds and the next line will raise a NullPointerException
                        scene.validate();
                        nodeToConnect.setPreferredLocation(new Point(point.x - nodeToConnect.getBounds().width / 2, point.y)); //A position correction is needed
                                                                                                                   //because the widget is positioned using the top left corner, not the center
                                                                                                                   //Since getBounds is called AFTER validating the scene, its value is never null
                        Map<LocalObjectLight, LocalObjectLight[]> connections = scene.getConnections();
                        if(!connections.isEmpty()){
                            for (Map.Entry<LocalObjectLight, LocalObjectLight[]> entry : connections.entrySet()) {
                                LocalObjectLight mplsLink = entry.getKey();
                                LocalObjectLight[] endpoints = entry.getValue();
                                LocalObjectLight parentA = null, parentB = null;
                                if(endpoints[0] != null && endpoints[0].getId().contains("-*") &&
                                        CommunicationsStub.getInstance().isSubclassOf(endpoints[0].getClassName(), Constants.CLASS_GENERICPORT)){
                                    parentA = CommunicationsStub.getInstance().getFirstParentOfClass(endpoints[0].getClassName(), endpoints[0].getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                                }
                                if(endpoints[1] != null && endpoints[1].getId().contains("-*") &&
                                        CommunicationsStub.getInstance().isSubclassOf(endpoints[1].getClassName(), Constants.CLASS_GENERICPORT)){
                                    parentB = CommunicationsStub.getInstance().getFirstParentOfClass(endpoints[1].getClassName(), endpoints[1].getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                                }
                                EditMPLSLinkEnpointsFrame frame = new EditMPLSLinkEnpointsFrame(mplsLink,parentA == null ? droppedObject : parentA, 
                                        parentB == null ? droppedObject : parentB);
                                
                                frame.setLocationRelativeTo(null);
                                frame.setVisible(true);
                                frame.addWindowListener(new WindowAdapter(){
                                    @Override
                                    public void windowClosing(WindowEvent e){
                                        e.getWindow().dispose();
                                        scene.update(mplsLink);
                                    }
                                });
                            }
                        }                                                                                
                        scene.fireChangeEvent(new ActionEvent(this, AbstractScene.SCENE_CHANGE, "attachNode")); //NOI18N
                    }
                } else
                    JOptionPane.showMessageDialog(null, String.format(I18N.gm("view_already_contains_this_object"), filterClass), I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
            }
            scene.validate();
                    
        } catch (UnsupportedFlavorException | IOException ex) {
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_INFO || Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
                Exceptions.printStackTrace(ex);
        }
    }
}