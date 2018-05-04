/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.icons;

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
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfo;

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
    
    static private ClassIcon instance;
    private final TopComponent topComponent;
    
    private final Map<String, Resource> icons = new HashMap();
    private final Map<String, Resource> smallIcons = new HashMap();
    
    private ClassIcon(TopComponent topComponent) {
        this.topComponent = topComponent;
    }
    
    public static ClassIcon newInstance(TopComponent topComponent) {
        
        if (instance == null) {
            
            if (topComponent == null)
                return null;
            
            instance = new ClassIcon(topComponent);
        }
        return instance;
    }
    
    public Resource getSmallIcon(String className) {
        if (smallIcons.containsKey(className))
            return smallIcons.get(className);
        else {
            Resource resource = addIcon(className, true);
            
            if (resource != null)
                smallIcons.put(className, resource);
            return resource;
        }
    }
    
    public Resource getIcon(String className) {
        if (icons.containsKey(className))
            return icons.get(className);
        else {
            Resource resource = addIcon(className, false);
            if (resource != null)
                icons.put(className, resource);
            return resource;
        }
    }    
    
    public String getIconUrl(ResourceReference resourceReference) {
        String protocol = Page.getCurrent().getLocation().getScheme();
        Page.getCurrent().getLocation().getAuthority();
        Page.getCurrent().getLocation().getPath();
        String currentUrl = Page.getCurrent().getLocation().getSchemeSpecificPart();
        
        return protocol + ":" + currentUrl + resourceReference.getURL().replaceAll("app://", "");
    }
    
    private Resource addIcon(String className, boolean small) {
        try {
            ClassInfo classInfo = topComponent.getWsBean().getClass(
                    className,
                    Page.getCurrent().getWebBrowser().getAddress(),
                    topComponent.getApplicationSession().getSessionId());
            
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
        } catch (ServerSideException ex) {
            return null;
        }
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