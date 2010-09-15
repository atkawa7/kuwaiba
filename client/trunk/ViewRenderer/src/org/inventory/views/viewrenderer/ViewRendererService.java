/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
 * 
 */

package org.inventory.views.viewrenderer;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.List;
import javax.swing.JFileChooser;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.core.services.utils.Utils;
import org.inventory.views.viewrenderer.scene.ObjectNodeWidget;
import org.netbeans.api.visual.widget.ImageWidget;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * Contains the business logic for the associated TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ViewRendererService implements LookupListener{
    
    private ViewRendererTopComponent vrtc;
    private Lookup.Result selectedNodes;
    private CommunicationsStub com;

    public ViewRendererService(ViewRendererTopComponent _vrtc){
        this.vrtc = _vrtc;
        this.com = CommunicationsStub.getInstance();
    }

    /**
     * Add this instance as listener for the selected nodes in the NavigationTree.
     * Should be called when the TopComponent is opened
     */
    public void initializeLookListener(){
        selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        selectedNodes.addLookupListener(this);
    }

    /**
     * Removes this instance as listener for the selected nodes in the NavigationTree.
     * Should be called when the TopComponent is closed
     */
    public void terminateLookupListener(){
        selectedNodes.removeLookupListener(this);
    }

    /**
     * Updates the view when a new object is selected
     * @param ev
     */
    public void resultChanged(LookupEvent ev) {
        Lookup.Result lookupResult = (Lookup.Result)ev.getSource();
        if(
           lookupResult.allInstances().size() == 1){
           //We clean the scene...
           vrtc.getScene().getNodesLayer().removeChildren();
           vrtc.getScene().getEdgesLayer().removeChildren();
           vrtc.getScene().getBackgroundLayer().removeChildren();
           vrtc.getScene().getInteractionLayer().removeChildren();

           LocalObjectLight myObject = (LocalObjectLight)lookupResult.allInstances().iterator().next();
           List<LocalObjectLight> myChildren = com.getObjectChildren(myObject.getOid(), com.getMetaForClass(myObject.getClassName(), false).getOid());
           for (LocalObjectLight myChild : myChildren){
               ObjectNodeWidget widget = new ObjectNodeWidget(vrtc.getScene(), myChild);
               vrtc.getScene().getNodesLayer().addChild(widget);
               vrtc.getScene().validate();
               vrtc.getScene().repaint();
           }
        } else
            if(!lookupResult.allInstances().isEmpty())
                vrtc.getNotifier().showStatusMessage("More than one object selected. No view available", false);
    }

    /**
     * Adds a background (removing the old one if existing) to the view
     */
    public void addBackground() {
        JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fChooser.setFileFilter(Utils.getImageFileFilter());
        if (fChooser.showOpenDialog(vrtc.getScene().getView()) == JFileChooser.APPROVE_OPTION){
            Image myBackgroundImage = Toolkit.getDefaultToolkit().createImage(fChooser.getSelectedFile().getAbsolutePath());
            if (myBackgroundImage == null)
                 vrtc.getNotifier().showSimplePopup("Image load", NotificationUtil.ERROR, "Error loading image. Please try another");
            else{
                if (!vrtc.getScene().getBackgroundLayer().getChildren().isEmpty())
                    vrtc.getScene().getBackgroundLayer().removeChildren(); //Clean the layer
                ImageWidget background = new ImageWidget(vrtc.getScene(),myBackgroundImage);
                background.bringToBack();
                vrtc.getScene().getBackgroundLayer().addChild(background);
                vrtc.getScene().validate();
            }
        }
    }

    /**
     * Removes the current background
     */
    public void removeBackground() {
        vrtc.getScene().getBackgroundLayer().removeChildren();
    }
}
