/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.resources;

import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Embedded;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;

/**
 * Class to get icons
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ClassIcon {
    /**
     * Default icon color (used in navigation trees and views). It's a light blue
     */
    public static final Color DEFAULT_ICON_COLOR = new Color(0, 170, 212);
    /**
     * Default icon height (used in navigation trees and views)
     */
    public static final int DEFAULT_ICON_HEIGHT = 10;
    /**
     * Default icon height (used in navigation trees and views)
     */
    public static final int DEFAULT_ICON_WIDTH = 10;
    
    private final Map<String, Resource> icons = new HashMap();
    private final Map<String, Resource> smallIcons = new HashMap();
    

    
    public Resource getSmallIcon(RemoteClassMetadata className) {
        if (smallIcons.containsKey(className.getClassName()))
            return smallIcons.get(className.getClassName());
        else {
            Resource resource = addIcon(className, true);
            
            if (resource != null)
                smallIcons.put(className.getClassName(), resource);
            return resource;
        }
    }
    
    public Resource getIcon(RemoteClassMetadata className) {
        if (icons.containsKey(className.getClassName()))
            return icons.get(className.getClassName());
        else {
            Resource resource = addIcon(className, false);
            if (resource != null)
                icons.put(className.getClassName(), resource);
            return resource;
        }
    }    
    
    public String getIconUrl(ResourceReference resourceReference) {
        String protocol = Page.getCurrent().getLocation().getScheme();
        String currentUrl = Page.getCurrent().getLocation().getSchemeSpecificPart();
        
        return protocol + ":" + currentUrl + resourceReference.getURL().replaceAll("app://", "");
    }
    
    private Resource addIcon(RemoteClassMetadata classInfo, boolean small) {

            final byte [] buf;
            
            byte [] icon;
            int width;
            int height;
            
            if (small) {
                icon = classInfo.getSmallIcon();
                width = DEFAULT_ICON_HEIGHT;
                height = DEFAULT_ICON_WIDTH;
            } else {
                icon = classInfo.getIcon();
                width = 32;
                height = 32;
            }
            
            Embedded embedded = new Embedded("embeddedimage");
            
            if (icon == null || icon.length == 0) {
                buf = createRectangleIcon(new Color(classInfo.getColor()), width, height);
            } else {                
                buf = icon;
                embedded.setWidth(width, Sizeable.Unit.PIXELS);
                embedded.setHeight(height, Sizeable.Unit.PIXELS);
            }
            
            StreamResource.StreamSource streamSource = new StreamResource.StreamSource() {

                @Override
                public InputStream getStream() {
                    return new ByteArrayInputStream(buf);
                }
            };
            StreamResource streamResource = new StreamResource(streamSource, classInfo.getClassName() + ".png");
            streamResource.setCacheTime(0);
            
            embedded.setSource(streamResource);
            
            return streamResource;

    }
    
    private byte[] createRectangleIcon(Color color, int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(color == null ? DEFAULT_ICON_COLOR : color);
            graphics.fillRect(0, 0, width, height);
                        
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            
            return baos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
}