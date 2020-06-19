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
/**
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
class GoogleMapPolygon extends PolymerElement {
  static get is() {
    return 'google-map-polygon';
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
      geodesic: {
        type: Boolean,
        value: false,
        observer: '_geodesicChanged'
      },
      paths: {
        type: Array,
        observer: '_pathsChanged'
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
   * @return {google.maps.MVCObject} The google.maps.Polygon
   */
  getMVCObject() {
    return this.polygon;
  }
  added(map) {
    this.polygon = new google.maps.Polygon({
      clickable: this.clickable,
      draggable: this.draggable,
      editable: this.editable,
      fillColor: this.fillColor,
      fillOpacity: this.fillOpacity,
      geodesic: this.geodesic,
      strokeColor: this.strokeColor,
      strokeOpacity: this.strokeOpacity,
      strokeWeight: this.strokeWeight,
      visible: this.visible,
    });
    this.polygon.setMap(map);
    this._setPolygonPaths(this.paths);
    this.polygon.addListener('click', event => {
      this.dispatchEvent(new CustomEvent('polygon-click'));
    });
    this.polygon.addListener('dblclick', event => {
      this.dispatchEvent(new CustomEvent('polygon-dbl-click'));
    });
    this.polygon.addListener('drag', event => {
      this.dispatchEvent(new CustomEvent('polygon-drag'));
    });
    this.polygon.addListener('dragend', event => {
      this.dispatchEvent(new CustomEvent('polygon-drag-end'));
    });
    this.polygon.addListener('dragstart', event => {
      this.dispatchEvent(new CustomEvent('polygon-drag-start'));
    });
    this.polygon.addListener('mousedown', event => {
      this.dispatchEvent(new CustomEvent('polygon-mouse-down'));
    });
    this.polygon.addListener('mousemove', event => {
      this.dispatchEvent(new CustomEvent('polygon-mouse-move'));
    });
    this.polygon.addListener('mouseout', event => {
      this.dispatchEvent(new CustomEvent('polygon-mouse-out'));
    });
    this.polygon.addListener('mouseover', event => {
      this.dispatchEvent(new CustomEvent('polygon-mouse-over'));
    });
    this.polygon.addListener('mouseup', event => {
      this.dispatchEvent(new CustomEvent('polygon-mouse-up'));
    });
    this.polygon.addListener('rightclick', event => {
      this.dispatchEvent(new CustomEvent('polygon-right-click'));
    });
  }
  _setPolygonPaths(paths) {
    this.polygon.setPaths(paths);
    
    google.maps.event.addListener(this.polygon.getPaths(), 'insert_at', index => {
      this._updatePaths();
    });
    google.maps.event.addListener(this.polygon.getPaths(), 'remove_at', index => {
      this._updatePaths();
    });
    google.maps.event.addListener(this.polygon.getPaths(), 'set_at', index => {
      this._updatePaths();
    });
    this.polygon.getPaths().forEach(path => {
      google.maps.event.addListener(path, 'insert_at', index => {
        this._updatePaths();
      });
      google.maps.event.addListener(path, 'remove_at', index => {
        this._updatePaths();
      });
      google.maps.event.addListener(path, 'set_at', index => {
        this._updatePaths();
      });
    });
  }
  _clickableChanged(newValue, oldValue) {
    if (this.polygon) {
      this.polygon.setOptions({
        clickable: newValue
      });
    }
  }
  _draggableChanged(newValue, oldValue) {
    if (this.polygon)
      this.polygon.setDraggable(newValue);
  }
  _editableChanged(newValue, oldValue) {
    if (this.polygon)
      this.polygon.setEditable(newValue);
  }
  _fillColorChanged(newValue, oldValue) {
    if (this.polygon) {
      this.polygon.setOptions({
        fillColor: newValue
      });
    }
  }
  _fillOpacityChanged(newValue, oldValue) {
    if (this.polygon) {
      this.polygon.setOptions({
        fillOpacity: newValue
      });
    }
  }
  _geodesicChanged(newValue, oldValue) {
    if (this.polygon) {
      this.polygon.setOptions({
        geodesic: newValue
      });
    }
  }
  _pathsChanged(newValue, oldValue) {
    if (this.polygon) {
      var paths = GoogleMapPolygon.getPathsArray(this.polygon.getPaths());
      if (JSON.stringify(paths) !== JSON.stringify(newValue))
        this._setPolygonPaths(newValue);
    }
  }
  _updatePaths() {
    this.paths = GoogleMapPolygon.getPathsArray(this.polygon.getPaths());
    this.dispatchEvent(new CustomEvent('polygon-paths-changed'));
  }
  static getPathsArray(paths) {
    var pathsArray = [];
    paths.forEach(path => {
      var pathArray = [];
      path.forEach(coordinate => {
        pathArray.push(coordinate.toJSON());
      });
      pathsArray.push(pathArray);
    });
    return pathsArray;
  }
  _strokeColorChanged(newValue, oldValue) {
    if (this.polygon) {
      this.polygon.setOptions({
        strokeColor: newValue
      });
    }
  }
  _strokeOpacityChanged(newValue, oldValue) {
    if (this.polygon) {
      this.polygon.setOptions({
        strokeOpacity: newValue
      });
    }
  }
  _strokeWeightChanged(newValue, oldValue) {
    if (this.polygon) {
      this.polygon.setOptions({
        strokeWeight: newValue
      });
    }
  }
  _visibleChanged(newValue, oldValue) {
    if (this.polygon)
      this.polygon.setVisible(newValue);
  }
  _zIndexChanged(newValue, oldValue) {
    if (this.polygon) {
      this.polygon.setOptions({
        zIndex: newValue
      });
    }
  }
  removed() {
    if (this.polygon)
      this.polygon.setMap(null);
  }
}
window.customElements.define(GoogleMapPolygon.is, GoogleMapPolygon);

export { GoogleMapPolygon };