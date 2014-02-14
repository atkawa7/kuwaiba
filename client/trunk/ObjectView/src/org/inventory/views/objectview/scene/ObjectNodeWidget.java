/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.views.objectview.scene;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.ImageUtilities;

/**
 * This widget represents a node (as in the navigation tree)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectNodeWidget extends IconNodeWidget implements ActionListener, SelectableWidget{
    private ObjectNode node;
    public static final Font defaultFont = new Font(Font.SANS_SERIF,Font.BOLD,12);

    public ObjectNodeWidget(ViewScene scene, LocalObjectLight object){
        super(scene);
        this.node = new ObjectNode(object, true);
        setLabel(node.getName());
        getLabelWidget().setFont(defaultFont);
        setToolTipText(object.toString());
        
        Image myIcon = CommunicationsStub.getInstance().getMetaForClass(object.getClassName(), false).getIcon();
        if(myIcon == null)
            myIcon = ImageUtilities.loadImage("org/inventory/views/objectview/res/default_32.png");
        setImage(myIcon);
        setImage(myIcon);

        //The difference between using getActions().addAction() and createActions("tool").addAction()
        //is that the first enable the action no matter what's the active scene tool. The second
        //enables the action to be used *only* when the specified "tool" is active in the scene
        createActions(ViewScene.ACTION_SELECT).addAction(scene.createSelectAction());
        createActions(ViewScene.ACTION_SELECT).addAction(scene.getMoveAction());
        scene.getMoveAction().addActionListener(this);
        createActions(ViewScene.ACTION_CONNECT).addAction(ActionFactory.createConnectAction(scene.getInteractionLayer(), scene.getConnectionProvider()));
        getActions().addAction(ActionFactory.createInplaceEditorAction(scene.getInplaceEditor()));
    }

    /**
     * Returns the wrapped business object
     * @return
     */
    public LocalObjectLight getObject(){
        return node.getObject();
    }
    
    @Override
    public ObjectNode getNode() {
        return node;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ((ViewScene)getScene()).fireChangeEvent(e);
    }
}