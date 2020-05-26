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
import { FlattenedNodesObserver } from '@polymer/polymer/lib/utils/flattened-nodes-observer.js';
import {MapApi} from './google-map-api.js';
import * as Constants from './google-map-constants.js';
/**
 * `google-map`
 * &lt;google-map&gt; is a web component displays a map using Maps JavaScript API
 *
 * @customElement
 * @polymer
 * @demo demo/index.html
 * @extends PolymerElement
 * @summary Custom Element for Google Map
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
class GoogleMap extends PolymerElement {
  
  static get is() {
    return Constants.googleMap;
  }
    
  static get template() {
    return html`
      <style>
        /* Always set the map height explicitly to define the size of the div
        * element that contains the map. */
        .fullSize {          
          height: 100%;
          width: 100%;
          padding: 0;
          margin: 0;
        }
      </style>
      <slot></slot>
      <!--Container for the map-->
      <div class="fullSize" id="[[divId]]"></div>
    `;
  }

  static get properties() {
    return {
      /**
       * @attribute divId
       * @type {string}
       * @default 'map'
       */
      divId: {
        type: String,
        value: 'map'
      },
      /**
       * The string contains your application API key. See https://developers.google.com/maps/documentation/javascript/get-api-key
       */
      apiKey: {
        type: String,
        value: ''
      },
      /**
       * Specifies a client Id
       */
      clientId: String,
      /**
       * The name of the additional library or libraries to load
       * drawing,geometry,places,visualization
       */
      libraries: String,
      /**
       * The initial map center latitude coordinate ranges between [-90, 90] degrees 
       */
      lat: {
        type: Number,
        value: 2.4573831,
        observer: '_latChanged'
      },
      /**
       * The initial map center longitude coordinate ranges between [-180, 180] degress
       */
      lng: {
        type: Number,
        value: -76.6699746,
        observer: '_lngChanged'
      },
      /**
       * The initial map zoom level, specify the value as an integer
       */
      zoom: {
        type: Number,
        value: 10,
        observer: '_zoomChanged'
      },
      /**
       * hybrid, roadmap, satellite, terrain
       */
      mapTypeId: {
        type: String,
        value: 'roadmap',
        observer: '_mapTypeIdChanged'
      },
      disableDefaultUi: {
        type: Boolean,
        value: false,
        observer: '_disableDefaultUiChanged'
      },
      zoomControl: {
        type: Boolean,
        observer: '_zoomControlChanged'
      },
      mapTypeControl: {
        type: Boolean,
        observer: '_mapTypeControlChanged'
      },
      scaleControl: {
        type: Boolean,
        observer: '_scaleControlChanged'
      },
      streetViewControl: {
        type: Boolean,
        observer: '_streetViewControlChanged'
      },
      rotateControl: {
        type: Boolean,
        observer: '_rotateControlChanged'
      },
      fullscreenControl: {
        type: Boolean,
        observer: '_fullscreenControlChanged'
      },
      /**
       * @attribute styles
       * @type {array}
       */
      styles: {
        type: Array,
        observer: '_stylesChanged'
      }
    };
  }
  /**
   * @return {google.maps.MVCObject} The google.maps.Map
   */
  getMVCObject() {
    return this.map;
  }
  /*
  constructor() {
    super();
  }
  */
  /*
  connectedCallback() {
    super.connectedCallback();
  }
  */
  disconnectedCallback() {
    super.disconnectedCallback();
  }
  
  /**
   * Display a map
   * @return {void}
   */
  initMap() {    
    /**
     * Map instance which define a single map on a page
     * @type google.map.Map
     */
    this.map = new google.maps.Map(this.shadowRoot.getElementById(this.divId), {
      center: {lat: this.lat, lng: this.lng},
      zoom: this.zoom,
      disableDefaultUI: this.disableDefaultUi
    });
    this.map.setMapTypeId(this.mapTypeId);
    this._stylesChanged(this.styles);
    /*
    var drawingManager = new google.maps.drawing.DrawingManager({
      drawingMode: 'marker'
    });
    drawingManager.setMap(this.map);
    */
    /*
    Events:
    bounds_changed, 
    *center_changed, 
    *click, 
    *dblclick, 
    drag, 
    dragend, 
    dragstart, 
    heading_changed, 
    idle, 
    maptypeid_changed, 
    *mousemove, 
    *mouseout, 
    *mouseover, 
    projection_changed, 
    *rightclick, 
    tilesloaded, 
    tilt_changed, 
    *zoom_changed
    */
    var _this = this;
    this.map.addListener('center_changed', function() {
      _this.lat = _this.map.getCenter().lat();
      _this.lng = _this.map.getCenter().lng();
      _this.dispatchEvent(new CustomEvent('map-center-changed'));
    });
    this.map.addListener('mousemove', event => {
      this.dispatchEvent(new CustomEvent('map-mouse-move', 
        {
          detail: {
            lat: event.latLng.lat(), 
            lng: event.latLng.lng()
          }
        }
      ));
    });
    this.map.addListener('mouseout', function(event) {
      _this.dispatchEvent(new CustomEvent('map-mouse-out'));
    });
    this.map.addListener('mouseover', function(event) {
      _this.dispatchEvent(new CustomEvent('map-mouse-over'));
    });
    this.map.addListener('zoom_changed', function() {
      _this.zoom = _this.map.getZoom();
      _this.dispatchEvent(new CustomEvent('map-zoom-changed'));
    });
    this.map.addListener('click', function(event) {
      var lat = event.latLng.lat();
      var lng = event.latLng.lng();
      _this.dispatchEvent(new CustomEvent('map-click', 
        {detail: {lat: lat, lng: lng}}));
    });
    this.map.addListener('dblclick', function(event) {
      _this.dispatchEvent(new CustomEvent('map-dbl-click'));
    });
    this.map.addListener('rightclick', function(event) {
      var lat = event.latLng.lat();
      var lng = event.latLng.lng();
      _this.dispatchEvent(new CustomEvent('map-right-click', 
        {detail: {lat: lat, lng: lng}}));
    });
    var _this = this;
    this._observer = new FlattenedNodesObserver(this, (info) => {      
      _this._processAddedNodes(info.addedNodes);
      _this._processRemovedNodes(info.removedNodes);
    });
  }
  /**
   * Loads the Maps JavaScript API
   * when the api is ready, it will call this.initMap()
   * @override
   * @return {void}
   */
  ready() {
    super.ready();    

    const mapApi = new MapApi(this.apiKey, this.clientId, this.libraries);
    mapApi.load().then(() => {this.initMap()});
  }
       
  _processAddedNodes(addedNodes) {
    addedNodes.forEach(value => {
      if (value.added)
        value.added(this.map);
    });
  }

  _processRemovedNodes(removedNodes) {
    removedNodes.forEach(value => { 
      if (value.removed)
        value.removed();
    });
  }

  _latChanged(newValue, oldValue) {
    if (this.map !== undefined && this.map.getCenter() !== undefined && 
      this.map.getCenter().lat() !== newValue) {
      this.map.setCenter({lat: newValue, lng: this.map.getCenter().lng()});
    }
  }

  _lngChanged(newValue, oldValue) {
    if (this.map !== undefined && this.map.getCenter() !== undefined && 
      this.map.getCenter().lng() !== newValue) {
      this.map.setCenter({lat: this.map.getCenter().lat(), lng: newValue});
    }
  }

  _zoomChanged(newValue, oldValue) {
    if (this.map !== undefined && 
      this.map.getZoom() !== newValue) {
      this.map.setZoom(newValue);
    }
  }

  _mapTypeIdChanged(newValue, oldValue) {
    if (this.map !== undefined && 
      this.map.getMapTypeId() !== newValue) {
      this.map.setMapTypeId(newValue);
    }
  }
  _disableDefaultUiChanged(newValue) {
    if (this.map)
      this.map.setOptions({disableDefaultUI : newValue});      
  }
  _zoomControlChanged(newValue) {
    if (this.map)
      this.map.setOptions({zoomControl : newValue});
  }
  _mapTypeControlChanged(newValue) {
    if (this.map)
      this.map.setOptions({mapTypeControl : newValue});
  }
  _scaleControlChanged(newValue) {
    if (this.map)
      this.map.setOptions({scaleControl : newValue});
  }
  _streetViewControlChanged(newValue) {
    if (this.map)
      this.map.setOptions({streetViewControl : newValue});
  }
  _rotateControlChanged(newValue) {
    if (this.map)
      this.map.setOptions({rotateControl : newValue});
  }
  _fullscreenControlChanged(newValue) {
    if (this.map)
      this.map.setOptions({fullscreenControl : newValue});
  }
  _stylesChanged(newValue) {
    if (this.map)
      this.map.setOptions({styles: newValue});
  }
}

window.customElements.define(GoogleMap.is, GoogleMap);
