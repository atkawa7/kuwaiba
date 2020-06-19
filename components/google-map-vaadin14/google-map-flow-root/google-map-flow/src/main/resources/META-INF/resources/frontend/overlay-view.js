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
 * 
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
      <slot></slot>
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
    this._observer = new FlattenedNodesObserver(this, info => {
      info.addedNodes.forEach(value => {
        this._div = value;
      });
      info.removedNodes.forEach(value => {
        this._div = value;
      });
    });

    MyOverlay.prototype = new google.maps.OverlayView();

    function MyOverlay(bounds, map) {
      this._bounds = bounds;
      this._map = map;
      this._div = null;
      this.setMap(map)
    }
    MyOverlay.prototype.onAdd = function() {
      var div = document.createElement('div');
      div.style.boderStyle = 'none';
      div.style.borderWidth = '0px';
      div.style.overflow = 'visible';
      div.style.position = 'absolute';
      div.style.background = 'blue';
      div.style.opacity = 0.7;

      this._div = div;

      this.getPanes().overlayLayer.appendChild(div);
    }
    MyOverlay.prototype.draw = function() {
      var overlayProjection = this.getProjection();
      
      var sw = overlayProjection.fromLatLngToDivPixel(this._bounds.getSouthWest());
      var ne = overlayProjection.fromLatLngToDivPixel(this._bounds.getNorthEast());

      var div = this._div;
      div.style.left = sw.x + 'px';
      div.style.top = ne.y + 'px';
      div.style.width = (ne.x - sw.x) + 'px';
      div.style.height = (sw.y - ne.y) + 'px';
    }
    MyOverlay.prototype.onRemove = function() {
      this._div.parentNode.removeChild(this._div);
      this._div = null;
    }

    this.overlay = new MyOverlay(
      new google.maps.LatLngBounds(
        {lat: this.bounds.south, lng: this.bounds.west}, 
        {lat: this.bounds.north, lng: this.bounds.east}
      ), 
      map
    );
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