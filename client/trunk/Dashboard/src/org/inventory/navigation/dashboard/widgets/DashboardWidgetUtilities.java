/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.dashboard.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * A set of utility methods to create widget components
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DashboardWidgetUtilities {
    //Color palette
    public static Color DARK_GREEN = new Color(160, 200, 40);
    public static Color GREEN = new Color(170, 220, 60);
    public static Color LIGHT_GREEN = new Color(190, 240, 70);
    public static Color DARK_BLUE = new Color(15, 120, 160);
    public static Color BLUE = new Color(60, 180, 220);
    public static Color LIGHT_BLUE = new Color(100, 200, 240);
    public static Color DARK_YELLOW = new Color(240, 190, 10);
    public static Color YELLOW = new Color(255, 220, 70);
    public static Color LIGHT_YELLOW = new Color(255, 240, 100);
    public static Color DARK_RED = new Color(200, 0, 0);
    public static Color RED = new Color(240, 40, 40);
    public static Color LIGHT_RED = new Color(255, 130, 130);
    
    /**
     * Creates an opaque JLabel to use it as an entry in a result table
     * @param text The text of the label
     * @param color The background color
     * @return The label object
     */
    public static JLabel buildOpaqueLabel(String text, Color color) {
        JLabel theLabel = new JLabel(text);
        theLabel.setOpaque(true);
        theLabel.setBackground(color);
        theLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return theLabel;
    }
    
    public static JLabel buildDecoratedOpaqueLabel(String text, Color color, Icon icon) {
        JLabel theLabel = buildOpaqueLabel(text, color);
        theLabel.setIcon(icon);
        return theLabel;
    }
    
    /**
     * Creates a minimalistic ScrollBarUI with the set of colors specified
     * @param isDragging The color used when dragging the thumb
     * @param isRollover The color on the hover event
     * @param normal The usual color of the thimb
     * @return The UI object ready to be installed on the scrollbar
     */
    public static ScrollBarUI createSimpleScrollBarUI(Color isDragging, Color isRollover, Color normal) {
        return new CustomScrollBarUI(isDragging, isRollover, normal);
    }
    
    
    private static class CustomScrollBarUI extends BasicScrollBarUI {
        private final Dimension d = new Dimension(20, 20);
        private Color draggingColor;
        private Color rollingOverColor;
        private Color normalColor;

        public CustomScrollBarUI(Color dragging, Color rollingOver, Color normal) {
            this.draggingColor = dragging;
            this.rollingOverColor = rollingOver;
            this.normalColor = normal;
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
          JButton decreaseButton = new JButton();
          decreaseButton.setBackground(Color.LIGHT_GRAY);
          decreaseButton.setBorder(null);
          decreaseButton.setPreferredSize(d);
          return decreaseButton;
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
          return createDecreaseButton(orientation);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent scrollBarComponent, Rectangle thumbSpace) {
            Graphics2D graphics2D = (Graphics2D) g.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color;
            JScrollBar sb = (JScrollBar) scrollBarComponent;
            if (!sb.isEnabled())
              return;
            
            else if (isDragging) 
              color = draggingColor;
                else if (isThumbRollover()) 
                    color = rollingOverColor;
                    else 
                        color = normalColor;
            
            graphics2D.setPaint(color);
            graphics2D.fillRect(thumbSpace.x, thumbSpace.y, thumbSpace.width, thumbSpace.height);
            graphics2D.dispose();
        }

        @Override
        protected void setThumbBounds(int x, int y, int width, int height) {
          super.setThumbBounds(x, y, width, height);
          scrollbar.repaint();
        }
    }
}
