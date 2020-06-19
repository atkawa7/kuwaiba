/** 
@license
Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.

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

/**
 * `<google-map-rectangle>` Rectangle overlay.
 * @customElement
 * @polymer
 * 
 */
class GoogleMapRectangle extends PolymerElement {
  static get is() {
    return 'google-map-rectangle';
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
      clickable: {
        type: Boolean,
        value: true,
        observer: '_clickableChanged'
      },
      draggable: {
        type: Boolean,
        value: false,
        observer: '_draggableChanged'
      },
      editable: {
        type: Boolean,
        value: false,
        observer: '_editableChanged'
      },
      fillColor: {
        type: String,
        value: '#FF0000',
        observer: '_fillColorChanged'
      },
      fillOpacity: {
        type: Number,
        value: 0.35,
        observer: '_fillOpacityChanged'
      },
      strokeColor: {
        type: String,
        value: '#FF0000',
        observer: '_strokeColorChanged'
      },
      strokeOpacity: {
        type: Number,
        value: 0.8,
        observer: '_strokeOpacityChanged'
      },
      strokePosition: {
        type: String,
        observer: '_strokePositionChanged'
      },
      strokeWeight: {
        type: Number,
        value: 2,
        observer: '_strokeWeightChanged'
      },
      visible: {
        type: Boolean,
        value: true,
        observer: '_visibleChanged'
      },
      zIndex: {
        type: Number,
        observer: '_zIndexChanged'
      }
    };
  }
  /**
   * @return {google.maps.MVCObject} The google.maps.Rectangle
   */
  getMVCObject() {
    return this.rectangle;
  }
  /**
   * @param {google.maps.Map} map 
   */
  added(map) {
    this.rectangle = new google.maps.Rectangle({
      bounds: this.bounds,
      clickable: this.clickable,
      draggable: this.draggable,
      editable: this.editable,
      fillColor: this.fillColor,
      fillOpacity: this.fillOpacity,
      strokeColor: this.strokeColor,
      strokeOpacity: this.strokeOpacity,
      strokeWeight: this.strokeWeight,
      map: map,
      visible: this.visible
    });
    if (this.strokePosition)
      this._strokePositionChanged(this.strokePosition);
    if (this.zIndex)
      this._zIndexChanged(this.zIndex);
    
    this.rectangle.addListener('bounds_changed', event => {
      this.bounds = this.rectangle.getBounds().toJSON();
      this.dispatchEvent(new CustomEvent('rectangle-bounds-changed'));
    });
    this.rectangle.addListener('click', event => {
      this.dispatchEvent(new CustomEvent('rectangle-click', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}
      ));
    });
    this.rectangle.addListener('dblclick', event => {
      this.dispatchEvent(new CustomEvent('rectangle-dbl-click', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}
      ));
    });
    this.rectangle.addListener('drag', event => {
      this.dispatchEvent(new CustomEvent('rectangle-drag', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}
      ));
    });
    this.rectangle.addListener('dragend', event => {
      this.dispatchEvent(new CustomEvent('rectangle-drag-end', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}
      ));
    });
    this.rectangle.addListener('dragstart', event => {
      this.dispatchEvent(new CustomEvent('rectangle-drag-start', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}
      ));
    });
    this.rectangle.addListener('mousedown', event => {
      this.dispatchEvent(new CustomEvent('rectangle-mouse-down', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}
      ));
    });
    this.rectangle.addListener('mousemove', event => {
      this.dispatchEvent(new CustomEvent('rectangle-mouse-move', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}
      ));
    });
    this.rectangle.addListener('mouseout', event => {
      this.dispatchEvent(new CustomEvent('rectangle-mouse-out', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}
      ));
    });
    this.rectangle.addListener('mouseover', event => {
      this.dispatchEvent(new CustomEvent('rectangle-mouse-over', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}
      ));
    });
    this.rectangle.addListener('mouseup', event => {
      this.dispatchEvent(new CustomEvent('rectangle-mouse-up', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}
      ));
    });
    this.rectangle.addListener('rightclick', event => {
      this.dispatchEvent(new CustomEvent('rectangle-right-click', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}
      ));
    });
  }

  removed() {
    if (this.rectangle)
      this.rectangle.setMap(null);
  }

  _boundsChanged(newValue, oldValue) {
    if (this.rectangle)
      this.rectangle.setOptions({bounds: newValue});
  }

  _clickableChanged(newValue, oldValue) {
    if (this.rectangle)
      this.rectangle.setOptions({clickable: newValue});
  }

  _draggableChanged(newValue, oldValue) {
    if (this.rectangle)
      this.rectangle.setOptions({draggable: newValue});
  }

  _editableChanged(newValue, oldValue) {
    if (this.rectangle)
      this.rectangle.setOptions({editable: newValue});
  }

  _fillColorChanged(newValue, oldValue) {
    if (this.rectangle)
      this.rectangle.setOptions({fillColor: newValue});
  }

  _fillOpacityChanged(newValue, oldValue) {
    if (this.rectangle)
      this.rectangle.setOptions({fillOpacity: newValue});
  }

  _strokeColorChanged(newValue, oldValue) {
    if (this.rectangle)
      this.rectangle.setOptions({strokeColor: newValue});
  }

  _strokeOpacityChanged(newValue, oldValue) {
    if (this.rectangle)
      this.rectangle.setOptions({strokeOpacity: newValue});
  }

  _strokePositionChanged(newValue, oldValue) {
    if (this.rectangle) {
      switch(newValue) {
        case 'center':
          this.rectangle.setOptions({strokePosition: google.maps.StrokePosition.CENTER});
        break;
        case 'inside':
          this.rectangle.setOptions({strokePosition: google.maps.StrokePosition.INSIDE});
        break;
        case 'outside':
          this.rectangle.setOptions({strokePosition: google.maps.StrokePosition.OUTSIDE});
        break;
      }
    }
  }

  _strokeWeightChanged(newValue, oldValue) {
    if (this.rectangle)
      this.rectangle.setOptions({strokeWeight: newValue});
  }

  _visibleChanged(newValue, oldValue) {
    if (this.rectangle)
      this.rectangle.setOptions({visible: newValue});
  }

  _zIndexChanged(newValue, oldValue) {
    if (this.rectangle)
      this.rectangle.setOptions({zIndex: newValue});
  }
}

window.customElements.define(GoogleMapRectangle.is, GoogleMapRectangle);