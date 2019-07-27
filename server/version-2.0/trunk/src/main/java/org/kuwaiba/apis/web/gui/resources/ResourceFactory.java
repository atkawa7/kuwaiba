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
import com.vaadin.server.StreamResource;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;

/**
 *  A factory class that builds and/or caches resources (mostly icons and backgrounds).
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ResourceFactory {
    /**
     * Default icon color (used in navigation trees and views). It's a light blue
     */
    public static final Color DEFAULT_ICON_COLOR = new Color(0, 170, 212);
    /**
     * Default icon height (used in navigation trees)
     */
    public static final int DEFAULT_SMALL_ICON_HEIGHT = 12;
    /**
     * Default icon height (used in navigation trees)
     */
    public static final int DEFAULT_SMALL_ICON_WIDTH = 12;
    /**
     * Default icon height (used in views)
     */
    public static final int DEFAULT_ICON_HEIGHT = 24;
    /**
     * Default icon height (used in views)
     */
    public static final int DEFAULT_ICON_WIDTH = 24;
    /**
     * Default small icon (a black 16x16 px square)
     */
    public static final Resource DEFAULT_SMALL_ICON = buildIcon(createRectangleIcon(Color.BLACK, 
            DEFAULT_ICON_WIDTH, DEFAULT_SMALL_ICON_HEIGHT), "DEFAULT_SMALL_ICON.png"); //NOI18N
    /**
     * Default icon (a black 32x32 px square)
     */
    public static final Resource DEFAULT_ICON = buildIcon(createRectangleIcon(Color.BLACK, 
            DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT), "DEFAULT_ICON.png"); //NOI18N
    /**
     * Large icons cache
     */
    private final HashMap<String, Resource> icons;
    /**
     * Small icons cache
     */
    private final HashMap<String, Resource> smallIcons;
    /**
     * Colored icons cache
     */
    private final HashMap<Integer, Resource> coloredIcons;
    /**
     * Singleton
     */
    private static ResourceFactory instance;

    private ResourceFactory() { 
        this.icons = new HashMap<>();
        this.smallIcons = new HashMap<>();
        this.coloredIcons = new HashMap<>();
    }

    public static ResourceFactory getInstance() {
        return instance == null ? instance = new ResourceFactory() : instance;
    }
    
    /**
     * Builds and caches the small icon of a given class. Call isSmallIconCached first to avoid having to provide the whole ClassMetadata object if the icon is already cached
     * @param classMetadata The metadata of the class the small icon will be built for
     * @return The cached resource
     */
    public Resource getSmallIcon(RemoteClassMetadata classMetadata) {
        if (smallIcons.containsKey(classMetadata.getClassName())) 
            return smallIcons.get(classMetadata.getClassName());
        else {
            if (classMetadata.getSmallIcon() != null && classMetadata.getSmallIcon().length != 0) {
                Resource icon = buildIcon(classMetadata.getSmallIcon(), classMetadata.getClassName() + ".png");
                smallIcons.put(classMetadata.getClassName(), icon);
                return icon;
            } else {
                Resource icon = buildIcon(createRectangleIcon(new Color(classMetadata.getColor()), 
                        DEFAULT_SMALL_ICON_WIDTH, DEFAULT_SMALL_ICON_HEIGHT), classMetadata.getClassName() + ".png");
                smallIcons.put(classMetadata.getClassName(), icon);
                return icon;
            }
        }
    }
    
    /**
     * Gets or builds (but doesn't caches) the small icon of the given class name
     * @param className The class name
     * @return The cached resource if it has been previously cached, or a generic black icon otherwise. Call isSmallIcon method first to avoid the latter
     */
    public Resource getSmallIcon(String className) {
        if (smallIcons.containsKey(className))
            return smallIcons.get(className);
        else //Should no happen. Always call isSmallIconCachedFirst to avoid entering here!
            return DEFAULT_SMALL_ICON;
            
    }
    
    /**
     * Builds and caches the small icon of a given class. Call isIconCached first to avoid having to provide the whole ClassMetadata object if the icon is already cached
     * @param classMetadata The metadata of the class the icon will be built for
     * @return The cached resource
     */
    public Resource getIcon(RemoteClassMetadata classMetadata) {
        if (icons.containsKey(classMetadata.getClassName()))
            return icons.get(classMetadata.getClassName());
        else {
            if (classMetadata.getIcon() != null && classMetadata.getSmallIcon().length != 0) {
                Resource icon = buildIcon(classMetadata.getIcon(), classMetadata.getClassName() + "_32.png");
                icons.put(classMetadata.getClassName(), icon);
                return icon;
            } else {
                Resource icon = buildIcon(createRectangleIcon(new Color(classMetadata.getColor()), 
                        DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT), classMetadata.getClassName() + "_32.png");
                icons.put(classMetadata.getClassName(), icon);
                return icon;
            }
        }
    }
    
    /**
     * Creates (or retrieves a cached version) of a squared colored icon
     * @param color The color of the icon
     * @param width The width of the icon
     * @param height The height of the icon
     * @return The icon as a Resource object
     */
    public Resource getColoredIcon(Color color, int width, int height) {
        int rgb = color.getRGB();
        if (coloredIcons.containsKey(rgb))
            return coloredIcons.get(rgb);
        else {
            Resource coloredIcon = buildIcon(createRectangleIcon(color, width, height), "colored_" + rgb + ".png" ); //NOI8N
            coloredIcons.put(rgb, coloredIcon);
            return coloredIcon;
        }
    }
    
    /**
     * Gets or builds (but doesn't caches) the icon of the given class name
     * @param className The class name
     * @return The cached resource if it has been previously cached, or a generic black icon otherwise. Call isIcon method first to avoid the latter
     */
    public Resource getIcon(String className) {
        if (icons.containsKey(className))
            return icons.get(className);
        else //Should no happen. Always call isSmallIconCachedFirst to avoid entering here!
            return DEFAULT_ICON;
            
    }

    /**
     * Tells if the small icon of a given class has been cached
     * @param className The name of the class
     * @return true if the small icon is cached, false otherwise
     */
    public boolean isSmallIconCached(String className) {
        return smallIcons.containsKey(className);
    }
    
    /**
     * Tells if the icon of a given class has been cached
     * @param className The name of the class
     * @return true if the small icon is cached, false otherwise
     */
    public boolean isIconCached(String className) {
        return icons.containsKey(className);
    }
    
    public String getIconUrl(ResourceReference resourceReference) {
        String protocol = Page.getCurrent().getLocation().getScheme();
        String currentUrl = Page.getCurrent().getLocation().getSchemeSpecificPart();
        
        return protocol + ":" + currentUrl + resourceReference.getURL().replaceAll("app://", "");
    }
    
    private static Resource buildIcon(byte[] icon, String fileName) {

            StreamResource.StreamSource source = new StreamResource.StreamSource() {
                @Override
                public InputStream getStream() {
                    InputStream input = new ByteArrayInputStream(icon);
                    return input;
            }
        };
  	StreamResource resource = new StreamResource ( source, fileName);
        resource.setCacheTime(-1);
        resource.setMIMEType("image/png");
        return resource;
    }
    
    /**
     * Creates a colored square icon as a byte array
     * @param color The color to be applied to the icon
     * @param width The icon width
     * @param height The icon height
     * @return The icon as a byte array
     */
    public static byte[] createRectangleIcon(Color color, int width, int height) {
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
    
    /**
     * Creates a colored square icon as a byte array
     * @param color The color to be applied to the icon
     * @param width The icon width
     * @param height The icon height
     * @return The icon as a byte array
     */
    public static byte[] createCircleIcon(Color color, int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(color == null ? DEFAULT_ICON_COLOR : color);
            graphics.fillOval(0, 0, width, height);
                        
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            
            return baos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * Creates a file stream from a byte array, so it can be downloaded. See {@link https://vaadin.com/forum/thread/2864064} .
     * @param fileContents The contents of the file
     * @param fileName The name of the file
     * @return The stream to the file
     */
    public static StreamResource getFileStream(byte[] fileContents, String fileName) {
        StreamResource.StreamSource source = new StreamResource.StreamSource() {
                @Override
                public InputStream getStream() {
                    InputStream input = new ByteArrayInputStream(fileContents);
                    return input;
            }
        };
  	StreamResource resource = new StreamResource ( source, fileName);
        resource.setCacheTime(-1);
        resource.setMIMEType("text/html");
        return resource;
    }
}