/*
 * Copyright 2017 johnyortega.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaadin.tapio.googlemaps.client.overlays;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.maps.client.overlays.Polygon;

/**
 *
 * @author johnyortega
 */
public class GoogleGwtMapPolygon extends Polygon {
    
    protected GoogleGwtMapPolygon() {}
    
    public final static GoogleGwtMapPolygon extNewInstance(GoogleGwtMapPolygonOptions options) {
        return createJso(options).cast();
    }
    
    private final static native JavaScriptObject createJso(GoogleGwtMapPolygonOptions options) /*-{
      return new $wnd.google.maps.Polygon(options);
    }-*/;
    
    public final native void setDraggable(boolean isDraggable) /*-{
        this.draggable = isDraggable;
    }-*/;
}
