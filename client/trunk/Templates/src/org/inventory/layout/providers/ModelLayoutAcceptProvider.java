/**
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.layout.providers;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.layout.RenderModelLayout;
import org.inventory.layout.lookup.SharedContentLookup;
import org.inventory.layout.model.CircleShape;
import org.inventory.layout.model.LabelShape;
import org.inventory.layout.model.PolygonShape;
import org.inventory.layout.model.CustomShape;
import org.inventory.layout.model.RectangleShape;
import org.inventory.layout.model.Shape;
import org.inventory.layout.scene.ModelLayoutScene;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;

/**
 * Drop. Accept provider to add shapes from palette
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ModelLayoutAcceptProvider implements AcceptProvider {

    @Override
    public ConnectorState isAcceptable(Widget widget, Point point, Transferable t) {
        if (!t.isDataFlavorSupported(Shape.DATA_FLAVOR))
            return ConnectorState.REJECT;
        return ConnectorState.ACCEPT;
    }

    @Override
    public void accept(Widget widget, Point point, Transferable t) { 
        Shape shape;
        try {
            shape = (Shape) t.getTransferData(Shape.DATA_FLAVOR);
            if (widget instanceof ModelLayoutScene) {
                if (!(shape instanceof RectangleShape)) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), NotificationUtil.WARNING_MESSAGE, "The first shape has to be a rectangle");
                    return;
                }
            }
        }
        catch (IOException | UnsupportedFlavorException ufe) {
            return;
        }
        
        JTextField txtName = new JTextField(20);
        txtName.setName("txtName");
        JCheckBox chkEquipment = new JCheckBox();
        chkEquipment.setName("chkEquipment");
        
        JComplexDialogPanel pnlShape = new JComplexDialogPanel(
            new String[] {"Shape name", "Does the shape represent an inventory object?"}, 
            new JComponent[] {txtName, chkEquipment});
        if (JOptionPane.showConfirmDialog(null, pnlShape, "New Shape", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            String shapeName = txtName.getText();
            Boolean isEquipment = chkEquipment.isSelected();
            
            ModelLayoutScene scene;
            if (widget.getScene() instanceof ModelLayoutScene) {
                scene = (ModelLayoutScene) widget.getScene();
                                
                for (Shape node : scene.getNodes()) {
                    if (!shapeName.isEmpty() && shapeName.equals(node.getName())) {
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                            NotificationUtil.WARNING_MESSAGE, "There can not be two shapes with the same name");
                    }
                }
                try {                              
                    Widget newWidget = null;
                    
                    Object parent = scene.findObject(widget);
                                        
                    Shape newShape = null;
                    if (shape instanceof LabelShape)
                        newShape = new LabelShape();
                    else if (shape instanceof RectangleShape)
                        newShape = new RectangleShape();
                    else if (shape instanceof CircleShape)
                        newShape = new CircleShape();
                    else if (shape instanceof PolygonShape)
                        newShape = new PolygonShape();
                    else if (shape instanceof CustomShape) {
                        if (parent == null)
                            return;
                        
                        RenderModelLayout render = new RenderModelLayout(((CustomShape) shape).getObject(), widget.getParentWidget(), -1, -1, -1, -1);
                                                
                        if (render.hasEquipmentModelLayout()) {
                            scene.setIsNewCustomShape(true);
                            scene.setNewCustomShapeParent((Shape) parent);
                            
                            scene.renderCustomShape(render.getEquipmentModelView().getStructure(), (Shape) parent);
                            
                            if (scene.getNewCustomShape() != null) {
                                newWidget = scene.findWidget(scene.getNewCustomShape());
                            }
                            scene.setIsNewCustomShape(false);
                            scene.setNewCustomShapeParent(null);
                            scene.setNewCustomShape(null);
                        }
                    }
                    if (newShape != null) {
                        
                        newShape.setParent((Shape) parent);
                        newShape.setName(shapeName);
                        newShape.setIsEquipment(isEquipment);
                        
                        if (widget instanceof ModelLayoutScene) {
                            newWidget = scene.addNode(newShape);
                            newWidget.setVisible(false);
                            scene.validate();
                            if  (JOptionPane.showConfirmDialog(null, "Can this device be mounted in a rack?", 
                                    I18N.gm("information"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                JSpinner numberOfRackUnits = new JSpinner();
                                numberOfRackUnits.setValue(1);
                                
                                numberOfRackUnits.setName("numberOfRackUnits");
                                JComplexDialogPanel pnlRackUnits = new JComplexDialogPanel(
                                    new String[] {"Number of Rack Units", }, 
                                    new JComponent[] {numberOfRackUnits});
                                
                                scene.validate();
                                if (JOptionPane.showConfirmDialog(null, pnlRackUnits, "Rack Units", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                                    int numberOfRU = (int) numberOfRackUnits.getValue();
                                    if (numberOfRU <= 0)
                                        numberOfRU = 1;
                                    // The values 15 (bottom margin), 1086 (width) and 100 (height) are the default values to the rack view which show connections
                                    Dimension dimension = new Dimension(1086, (numberOfRU * 100) + (numberOfRU > 1 ? 15 : 0));
                                    newShape.setWidth(dimension.width);
                                    newShape.setHeight(dimension.height);
                                    newWidget.setPreferredSize(dimension);
                                }
                            }
                            newWidget.setVisible(true);
                        } else
                            newWidget = scene.addNode(newShape);
                    }
                    if (newWidget != null) {
                        
                        newWidget.setPreferredLocation(point);
                        scene.repaint();

                        newShape.setX(point.x);
                        newShape.setY(point.y);

                        if (newWidget instanceof SharedContentLookup)
                            ((SharedContentLookup) newWidget).fixLookup();
                    }
                } catch (Exception ex) {  }
            }
        }
    }
}
