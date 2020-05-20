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
import {FlattenedNodesObserver} from '@polymer/polymer/lib/utils/flattened-nodes-observer.js';
/**
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
class InfoWindow extends PolymerElement {
  static get is() {
    return 'info-window';
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
      disableAutoPan: {
        type: Boolean,
        observer: '_disableAutoPanChanged'
      },
      maxWidth: {
        type: Number,
        observer: '_maxWidthChanged'
      },
      pixelOffset: {
        type: Object,
        observer: '_pixelOffsetChanged'
      },
      position: {
        type: Object,
        observer: '_positionChanged'
      },
      zIndex: {
        type: Number,
        observer: '_zIndexChanged'
      }
    };
  }
  /**
   * @return {google.maps.MVCObject} The google.maps.InfoWindow
   */
  getMVCObject() {
    return this.infoWindow;
  }
  added() {
    this.infoWindow = new google.maps.InfoWindow();
    google.maps.event.addListener(this.infoWindow, 'closeclick', () => {
      this.dispatchEvent(new CustomEvent('info-window-close-click'));
    });
    google.maps.event.addListener(this.infoWindow, 'content_changed', () => {
      this.dispatchEvent(new CustomEvent('info-window-content-changed'));
    });
    google.maps.event.addListener(this.infoWindow, 'domready', () => {
      this.dispatchEvent(new CustomEvent('info-window-dom-ready'));
    });
    google.maps.event.addListener(this.infoWindow, 'position_changed', () => {
      this.dispatchEvent(new CustomEvent('info-window-position-changed'));
    });
    google.maps.event.addListener(this.infoWindow, 'zindex_changed', () => {
      this.dispatchEvent(new CustomEvent('info-window-zindex-changed'));
    });
    this._observer = new FlattenedNodesObserver(this, info => {
      this._addedNodes(info.addedNodes);
      this._removedNodes(info.removedNodes);
    });
    this.dispatchEvent(new CustomEvent('info-window-added'));
  }
  _addedNodes(addedNodes) {
    addedNodes.forEach(value => {
      this._contentChanged(value);
    });
  }
  _removedNodes(removedNodes) {
  }
  removed() {
    this.close();
  }
  close() {
    if (this.infoWindow)
      this.infoWindow.close();
  }
  /**
   * 
   * @param {google.maps.Map} map 
   * @param {google.maps.MVCObject} anchor 
   */
  open(map, anchor) {
    if (this.infoWindow) {
      if (map && anchor)
        this.infoWindow.open(map, anchor);
      else if (map)
        this.infoWindow.open(map);
      else if (anchor)
        this.infoWindow.open(anchor);
      else
        this.infoWindow.open();
    } else {
      
    }
  }
  /**
   * 
   * @param {*} content 
   */
  _contentChanged(content) {
    if (this.infoWindow)
      this.infoWindow.setOptions({content: content});
  }
  /**
   * 
   * @param {boolean} newValue 
   * @param {*} oldValue 
   */
  _disableAutoPanChanged(newValue, oldValue) {
    if (this.infoWindow)
      this.infoWindow.setOptions({disableAutoPan: newValue});
  }
  /**
   * 
   * @param {number} newValue 
   * @param {*} oldValue 
   */
  _maxWidthChanged(newValue, oldValue) {
    if (this.infoWindow)
      this.infoWindow.setOptions({maxWidth: newValue});
  }
  /**
   * 
   * @param {object} newValue {width: 0, height: 0}
   * @param {*} oldVlue 
   */
  _pixelOffsetChanged(newValue, oldValue) {
    if (this.infoWindow)
      this.infoWindow.setOptions({pixelOffset: new google.map.Size(newValue.width, newValue.height)});
  }
  /**
   * 
   * @param {object} newValue {lat: 0, lng: 0}
   * @param {*} oldValue 
   */
  _positionChanged(newValue, oldValue) {
    if (this.infoWindow)
      this.infoWindow.setOptions({position: newValue});
  }
  /**
   * 
   * @param {number} newValue 
   * @param {*} oldValue 
   */
  _zIndexChanged(newValue, oldValue) {
    if (this.infoWindow)
      this.infoWindow.setOptions({zIndex: newValue});
  }
}
window.customElements.define(InfoWindow.is, InfoWindow);