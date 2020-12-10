/** 
@license
Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/
import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {FlattenedNodesObserver} from '@polymer/polymer/lib/utils/flattened-nodes-observer.js'

/**
 * `<overlay-view>`
 * @customElement
 * @polymer
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
class OverlayView extends PolymerElement {
  static get is() {
    return 'overlay-view';
  }

  static get template() {
    return html`
      <style>
        :host {
          display: block;
        }
      </style>
    `;
  }

  static get properties() {
    return {
      /**
       * Rectangle bounds
       * @type {{east: number, north: number, south: number, west: number}}
       */
      bounds: {
        type: Object,
        observer: '_boundsChanged'
      },
      /**
       * Bounds of the current viewport.
       */
      mapBounds: {
        type: Boolean,
        value: false
      }
    };
  }
  /**
   * @return {google.maps.OverlayView} The google.maps.OverlayView
   */
  getMVCObject() {
    return this.overlay;
  }
  /**
   * @param {google.maps.Map} map 
   */
  added(map) {
    MyOverlay.prototype = new google.maps.OverlayView();

    function MyOverlay(bounds, map) {
      this._bounds = bounds;
      this._div = null;
      this.setMap(map);
    }
    var _this = this;

    MyOverlay.prototype.onAdd = function() {
      this._div = _this._div;
      this.getPanes().overlayMouseTarget.appendChild(this._div);
    }

    MyOverlay.prototype.draw = function() {
      _this.projection = this.getProjection();
      if (_this.mapBounds) {
        var sw = _this.projection.fromLatLngToDivPixel(this.getMap().getBounds().getSouthWest());
        var ne = _this.projection.fromLatLngToDivPixel(this.getMap().getBounds().getNorthEast());
      } else {
        var sw = _this.projection.fromLatLngToDivPixel(this._bounds.getSouthWest());
        var ne = _this.projection.fromLatLngToDivPixel(this._bounds.getNorthEast());
      }
      var div = this._div;
      div.style.position = 'absolute';
      div.style.left = sw.x + 'px';
      div.style.top = ne.y + 'px';
      var width = ne.x - sw.x;
      var height = sw.y - ne.y;
      div.style.width = width + 'px';
      div.style.height = height + 'px';    

      _this.dispatchEvent(new CustomEvent('overlay-view-width-changed', 
        {
          detail: {
            width: width
          }
        }
      ));         
    }
    
    MyOverlay.prototype.onRemove = function() {
      this._div.parentNode.removeChild(this._div);
      this._div = null;
    }

    this._observer = new FlattenedNodesObserver(this, info => {
      info.addedNodes.forEach(value => {
        this._div = value;
        if (this.mapBounds) {
          this.overlay = new MyOverlay(null, map);
        } else {
          this.overlay = new MyOverlay(
            new google.maps.LatLngBounds(
              {lat: this.bounds.south, lng: this.bounds.west}, 
              {lat: this.bounds.north, lng: this.bounds.east}
            ), 
            map
          );
        }
      });
      info.removedNodes.forEach(value => {
      });
    });
  }
  /**
   * Gets the pixel coordinates of the given geographical location.
   * @param {number} lat - Latitude.
   * @param {number} lng - Longitude.
   * @returns {Object} Pixel coordinates {x: 0.0, y: 0.0}.
   */
  fromLatLngToDivPixel(lat, lng) {
    if (this.projection) {
      var pixel = this.projection.fromLatLngToDivPixel(new google.maps.LatLng({lat: lat, lng: lng}));
      return {x: pixel.x, y: pixel.y};
    }
    return null;
  }
  /**
   * Gets the geographical coordinates from pixel coordinates.
   * @param {number} x - Pixel coordinate x.
   * @param {number} y - Pixel coordinate y.
   * @returns {Object} Geographical coordinates {lat: 0.0, lng: 0.0}.
   */
  fromDivPixelToLatLng(x, y) {
    if (this.projection) {
      var latLng = this.projection.fromDivPixelToLatLng(new google.maps.Point(x, y));
      return {lat: latLng.lat(), lng: latLng.lng()}
    }
    return null;
  }
  /**
   * Gets the pixel coordinates of the given geographical location.
   * @param {Array} geoCoordinates - Geographical coordinates Array [{lat: 0.0, lng: 0.0}, ..].
   * @returns {Array} Pixel coordinates Array [{x: 0.0, y: 0.0}, ..].
   */
  projectionFromLatLngToDivPixel(geoCoordinates) {
    if (this.projection && geoCoordinates) {
      var pixelCoordinates = [];
      geoCoordinates.forEach(geoCoordinate => {
        var pixelCoordinate = this.projection.fromLatLngToDivPixel(new google.maps.LatLng({lat: geoCoordinate.lat, lng: geoCoordinate.lng}));
        pixelCoordinates.push({x: pixelCoordinate.x, y: pixelCoordinate.y});
      });
      return pixelCoordinates;
    }
    return null;
  }
  /**
   * Gets the geographical coordinates from pixel coordinates.
   * @param {Array} pixelCoordinates - Pixel Array [{x: 0.0, y: 0.0}, ..].
   * @returns {Array} Geographical coordinates Array [{lat: 0.0, lng: 0.0}, ..].
   */
  projectionFromDivPixelToLatLng(pixelCoordinates) {
    if (this.projection && pixelCoordinates) {
      var geoCoordinates = [];
      pixelCoordinates.forEach(pixelCoordinate => {
        var geoCoordinate = this.projection.fromDivPixelToLatLng(new google.maps.Point(pixelCoordinate.x, pixelCoordinate.y));
        geoCoordinates.push({lat: geoCoordinate.lat(), lng: geoCoordinate.lng()});
      });
      return geoCoordinates;
    }
    return null;
  }
  /**
   * Gets the pixel coordinates of the given geographical location.
   * @param {Array} geoCoordinates - Geographical coordinates Object {"id": [{lat: 0.0, lng: 0.0}, ..] ..}.
   * @returns {Array} Pixel coordinates Object {"id": [{x: 0.0, y: 0.0}, ..] ..}.
   */
  projectionMapFromLatLngToDivPixel(geoCoordinates) {
    if (this.projection && geoCoordinates) {
      var pixelCoordinates = {};
      for (const key in geoCoordinates) {
        pixelCoordinates[key] = this.projectionFromLatLngToDivPixel(geoCoordinates[key]);
      }
      return pixelCoordinates;
    }
    return null;
  }
  /**
   * Gets the geographical coordinates from pixel coordinates.
   * @param {Array} pixelCoordinates - Pixel coordinates Object {"id": [{x: 0.0, y: 0.0}, ..], ..}.
   * @returns {Array} Geographical coordinates Object {"id": [{lat: 0.0, lng: 0.0}, ..], ..}.
   */
  projectionMapFromDivPixelToLatLng(pixelCoordinates) {
    if (this.projection && pixelCoordinates) {
      var geoCoordinates = {};
      for (const key in pixelCoordinates) {
        geoCoordinates[key] = this.projectionFromDivPixelToLatLng(pixelCoordinates[key]);
      }
      return geoCoordinates;
    }
    return null;
  }

  removed() {
    if (this.overlay)
      this.overlay.setMap(null);
  }

  _boundsChanged(newValue) {
    if (this.overlay) {
      this.overlay._bounds = new google.maps.LatLngBounds(
        {lat: newValue.south, lng: newValue.west},
        {lat: newValue.north, lng: newValue.east}
      );
    }
  }
}

window.customElements.define(OverlayView.is, OverlayView);