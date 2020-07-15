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
      this._map = map;
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
      var overlayProjection = this.getProjection();
      
      var sw = overlayProjection.fromLatLngToDivPixel(this._bounds.getSouthWest());
      var ne = overlayProjection.fromLatLngToDivPixel(this._bounds.getNorthEast());

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

        this.overlay = new MyOverlay(
          new google.maps.LatLngBounds(
            {lat: this.bounds.south, lng: this.bounds.west}, 
            {lat: this.bounds.north, lng: this.bounds.east}
          ), 
          map
        );
      });
      info.removedNodes.forEach(value => {
      });
    });
  }
  
  fromLatLngToDivPixel(lat, lng) {
    if (this.projection) {
      var pixel = this.projection.fromLatLngToDivPixel(new google.maps.LatLng({lat: lat, lng: lng}));
      return {x: pixel.x, y: pixel.y};
    }
    return null;
  }

  removed() {
    if (this.overlay)
      this.overlay.setMap(null);
  }

  _boundsChanged(newValue, oldValue) {
    if (this.overlay) {
      this.overlay._bounds = new google.maps.LatLngBounds(
        {lat: newValue.south, lng: newValue.west},
        {lat: newValue.north, lng: newValue.east}
      );
    }
  }
}

window.customElements.define(OverlayView.is, OverlayView);