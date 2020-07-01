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
package org.inventory.core.visual.actions.providers;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
 * This provider should check if a given type of object can be dropped on the scene
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class CustomAcceptActionProvider implements AcceptProvider {

    private final AbstractScene scene;
    /**
     * Only subclasses of this class will be allowed to be dropped on the scene
     */
    private final String filterClass;
    
    /**
     * This constructor allows to specify the instances of what classes (as in inventory classes) can be dropped where
     * @param scene The related scene
     * @param filterClass The class name of the instances allowed to be dropped here. It'd be useful to use a root, abstract class such as InventoryObject or GenericSomething. Null (or using the other constructor) will allow any inventory object to be added to the scene
     */
    public CustomAcceptActionProvider(AbstractScene scene, String filterClass) {
        this.scene = scene;
        this.filterClass = filterClass;
    }
   

    @Override
    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
        if (transferable.isDataFlavorSupported(ExTransferable.multiFlavor) || transferable.isDataFlavorSupported(LocalObjectLight.DATA_FLAVOR))
            return ConnectorState.ACCEPT;
        
        return ConnectorState.REJECT_AND_STOP;
//      This will be commented out until we cache properly the data model. This method is called as long as the mouse is over the scene, thus
//        spawning N calls to the method isSubClassOf
//        if (transferable.isDataFlavorSupported(LocalObjectLight.DATA_FLAVOR)) {
//            if (filterClass == null)
//                return ConnectorState.ACCEPT;
//            try {
//                LocalObjectLight objectToBeDropped = (LocalObjectLight) transferable.getTransferData(LocalObjectLight.DATA_FLAVOR);
//                if (CommunicationsStub.getInstance().isSubclassOf(objectToBeDropped.getClassName(), filterClass))
//                    return ConnectorState.ACCEPT;
//            } catch (UnsupportedFlavorException | IOException ex) {
//                if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_INFO || Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
//                    Exceptions.printStackTrace(ex); 
//            }
//        }
//        return ConnectorState.REJECT_AND_STOP;
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
            
            for (LocalObjectLight droppedObject : droppedObjects) {
                if (!scene.isNode(droppedObject)){

                    if (!CommunicationsStub.getInstance().isSubclassOf(droppedObject.getClassName(), filterClass))
                        JOptionPane.showMessageDialog(null, String.format(I18N.gm("only_allowed_in_this_view"), filterClass), I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
                    else {
                        Widget newNode = scene.addNode(droppedObject); 
                        //validate is called here, otherwise, the widget won't be able to resolve its bounds and the next line will raise a NullPointerException
                        scene.validate();
                        newNode.setPreferredLocation(new Point(point.x - newNode.getBounds().width / 2, point.y)); //A position correction is needed
                                                                                                                   //because the widget is positioned using the top left corner, not the center
                                                                                                                   //Since getBounds is called AFTER validating the scene, its value is never null
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