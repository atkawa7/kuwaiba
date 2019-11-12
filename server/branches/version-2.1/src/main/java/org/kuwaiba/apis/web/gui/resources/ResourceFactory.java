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

package org.kuwaiba.apis.web.gui.resources;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;

/**
 * A factory class that builds and/or caches resources (mostly icons and backgrounds).
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ResourceFactory {
    /**
     * Default icon width (used in views)
     */
    public static final int DEFAULT_ICON_WIDTH = 24;
    /**
     * Default icon height (used in views)
     */
    public static final int DEFAULT_ICON_HEIGHT = 24;
    /**
     * Default icon width (used in navigation trees)
     */
    private static final int DEFAULT_SMALL_ICON_WIDTH = 12;
    /**
     * Default icon height (used in navigation trees)
     */
    private static final int DEFAULT_SMALL_ICON_HEIGHT = 12;
    
    private static ResourceFactory instance;
    /**
     * Large icons cache
     */
    private final HashMap<String, StreamResource> icons;
    /**
     * Small icons cache
     */
    private final HashMap<String, StreamResource> smallIcons;
    /**
     * Default large icons cache
     */
    private final HashMap<Integer, StreamResource> defaultIcons;
    /**
     * Default small icons cache
     */
    private final HashMap<Integer, StreamResource> defaultSmallIcons;
    
    private ResourceFactory() {
        icons = new HashMap();
        smallIcons = new HashMap();
        defaultIcons = new HashMap();
        defaultSmallIcons = new HashMap();
    }
    
    public static ResourceFactory getInstance() {
        return instance == null ? instance = new ResourceFactory() : instance;
    }
    
    /**
     * Builds and caches an icon of a given class. 
     * @param className the class name of the icon will be built for
     * @param webserviceBean backend bean
     * @return The cached resource
     */
    public StreamResource getClassIcon(String className, WebserviceBean webserviceBean) {
        if (className == null || webserviceBean == null)
            return null;
//        if (icons.containsKey(className))
//            return icons.get(className);
//        else {
            try {
                RemoteSession remoteSession = UI.getCurrent().getSession().getAttribute(RemoteSession.class);
                RemoteClassMetadata remoteClass = webserviceBean.getClass(className, remoteSession.getIpAddress(), remoteSession.getSessionId());
                byte[] classIcon = remoteClass.getIcon();
                if (classIcon != null && classIcon.length > 0) {
                    StreamResource icon = buildIcon(className + ".png", remoteClass.getIcon());
                    VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
                    icons.put(className, icon);
                    return icon;
                } else {
                    int color = remoteClass.getColor();
                    if (defaultIcons.containsKey(color))
                        return defaultIcons.get(color);
                    else {
                        StreamResource icon = buildIcon(remoteClass.getColor() + ".png", getIcon(new Color(remoteClass.getColor()), DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT));
                        VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
                        defaultIcons.put(remoteClass.getColor(), icon);
                        return icon;                        
                    }
                }

            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage());
            }
//        }
        return null;
    }
    
    /**
     * Gets or builds (but doesn't caches) the small icon of the given class name
     * @param className The class name
     * @param webserviceBean backend bean
     * @return The cached resource if it has been previously cached, or a generic black icon otherwise
     */
    public StreamResource getClassSmallIcon(String className, WebserviceBean webserviceBean) {
        if (className == null || webserviceBean == null)
            return null;
//        if (smallIcons.containsKey(className))
//            return smallIcons.get(className);
//        else {
            try {
                RemoteSession remoteSession = UI.getCurrent().getSession().getAttribute(RemoteSession.class);
                RemoteClassMetadata remoteClass = webserviceBean.getClass(className, remoteSession.getIpAddress(), remoteSession.getSessionId());
                byte[] classIcon = remoteClass.getSmallIcon();
                if (classIcon != null && classIcon.length > 0) {
                    StreamResource icon = buildIcon("small" + className + ".png", remoteClass.getIcon());
                    VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
                    smallIcons.put(className, icon);
                    return icon;
                } else {
                    int color = remoteClass.getColor();
                    if (defaultSmallIcons.containsKey(color))
                        return defaultSmallIcons.get(color);
                    else {
                        StreamResource icon = buildIcon(remoteClass.getColor() + ".png", getIcon(new Color(remoteClass.getColor()), DEFAULT_SMALL_ICON_WIDTH, DEFAULT_SMALL_ICON_HEIGHT));
                        VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
                        defaultSmallIcons.put(remoteClass.getColor(), icon);
                        return icon;                        
                    }
                }

            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage());
            }
//        }
        return null;
    }
    /**
     * Builds an icon resource
     * @param name the name of the resource
     * @param icon the icon as byte array
     * @return An icon resource which is not registry
     */
    private StreamResource buildIcon(String name, byte[] icon) {
        return new StreamResource(name, new InputStreamFactory() {
            @Override
            public InputStream createInputStream() {
                return new ByteArrayInputStream(icon);
            }
        });                                
    }
    /**
     * Creates (or retrieves a cached version) of a squared colored icon
     * @param color The color of the icon
     * @param width The width of the icon
     * @param height The height of the icon
     * @return The icon as a byte array
     */
    private byte[] getIcon(Color color, int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(color);
            graphics.fillRect(0, 0, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
}
