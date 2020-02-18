import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import '@polymer/iron-pages/iron-pages.js';
import '@vaadin/vaadin-tabs/vaadin-tabs.js';
import '@vaadin/vaadin-tabs/vaadin-tab.js';
import '@vaadin/vaadin-ordered-layout/vaadin-vertical-layout.js';
import '@vaadin/vaadin-split-layout/vaadin-split-layout.js';
import './google-map-constants.js';
import './google-map.js';
import './google-map-marker.js';
import './google-map-polyline.js';
/**
 * `my-element`
 * my-element
 *
 * @customElement
 * @polymer
 * @demo demo/index.html
 */
class GoogleMapDemo extends PolymerElement {
  static get template() {
    return html`
      <style>
        :host {
          display: block;
        }
        label {
          width: 100%;
          padding: 5px;
        }
      </style>
      <vaadin-split-layout style="height: 100%;">
        <div style="width: 70%;">
          <google-map id="map" api-key=[[apiKey]] map-type-id="hybrid">
            <google-map-marker id="marker" lat="2.4574702" lng="-76.6349535" title="Marker" label='{"color":"#305F72", "text":"Marker"}'></google-map-marker>
            <google-map-polyline id="polyline" editable draggable path='[{"lat":2.4574702, "lng":-76.6349535}, {"lat":2.3512629, "lng":-76.6915093}, {"lat":2.260897, "lng":-76.7449569}, {"lat":2.1185563, "lng":-76.9974436}, {"lat":2.0693058, "lng":-77.0552842}]'></google-map-polyline>
          </google-map>
        </div>
        <div style="width: 30%;">
          <vaadin-tabs selected="{{page}}">
              <vaadin-tab>Map Events</vaadin-tab>
              <vaadin-tab>Marker Events</vaadin-tab>
              <vaadin-tab>Polyline Events</vaadin-tab>
          </vaadin-tabs>

          <iron-pages selected="[[page]]">
            <vaadin-vertical-layout>
              <label id="lblMapClick">map-click (Go To Pasto)</label>
              <label id="lblMapDblClick">map-dbl-click</label>
              <label id="lblMapRightClick">map-right-click (New Marker)</label>
              <label id="lblMapCenterChanged">map-center-changed</label>
              <label id="lblMapMouseMove">map-mouse-move</label>
              <label id="lblMapMouseOut">map-mouse-out</label>
              <label id="lblMapMouseOver">map-mouse-over</label>
              <label id="lblMapZoomChanged">map-zoom-changed</label>
            </vaadin-vertical-layout>
            <vaadin-vertical-layout>
              <label id="lblMarkerClick">marker-click (Move To Silvia)</label>
              <label id="lblMarkerDblClick">marker-dbl-click (Remove Marker)</label>
              <label id="lblMarkerDragEnd">marker-drag-end</label>
              <label id="lblMarkerDragStart">marker-drag-start</label>
              <label id="lblMarkerMouseOut">marker-mouse-out</label>
              <label id="lblMarkerMouseOver">marker-mouse-over</label>
              <label id="lblMarkerRightClick">marker-right-click</label>
            </vaadin-vertical-layout>
            <vaadin-vertical-layout>
              <label id="lblPolylineClick">polyline-click</label>
              <label id="lblPolylineDblClick">polyline-dbl-click (Remove Polyline)</label>
              <label id="lblPolylineMouseOut">polyline-mouse-out</label>
              <label id="lblPolylineMouseOver">polyline-mouse-over</label>
              <label id="lblPolylineRightClick">polyline-right-click</label>
              <label id="lblPolylinePathChanged">polyline-path-changed</label>
            </vaadin-vertical-layout>
          </iron-pages>
        </div>
      </vaadin-split-layout>
    `;
  }
  static get properties() {
    return {
      page: {
        type: String
      },
      apiKey: {
        type: String,
        value: ''
      }
    };
  }
  static get is() {
      return 'google-map-demo';
  }
  ready() {
    super.ready();

    const map = this.$.map;
    this._setMapEventListeners(map);
    
    const marker = this.$.marker;
    this._setMarkerEventListeners(marker);
    
    const polyline = this.$.polyline;
    this._setPolylineEventListeners(polyline);
  }

  _updateLabelBackground(aLabel) {
    aLabel.style.background = '#F2F4F9';
    const wait = ms => new Promise(resolve => setTimeout(resolve, ms));
    wait(1000).then(() => aLabel.style.background = 'transparent');                
  }

  _setMapEventListeners(map) {
    const _this = this;

    map.addEventListener('map-center-changed', function(event) {
      _this._updateLabelBackground(_this.$.lblMapCenterChanged);
    });
    map.addEventListener('map-mouse-move', function(event) {      
      _this._updateLabelBackground(_this.$.lblMapMouseMove);
    });
    map.addEventListener('map-mouse-out', function(event) {
      _this._updateLabelBackground(_this.$.lblMapMouseOut);
    });
    map.addEventListener('map-mouse-over', function(event) { 
      _this._updateLabelBackground(_this.$.lblMapMouseOver);
    });
    map.addEventListener('map-zoom-changed', function(event) {
      _this._updateLabelBackground(_this.$.lblMapZoomChanged);
    });
    map.addEventListener('map-click', function(event) {
      _this._updateLabelBackground(_this.$.lblMapClick);
      map.lat = 1.2135252;
      map.lng = -77.3122422;
      map.zoom = 13;
    });
    map.addEventListener('map-dbl-click', function(event) {
      _this._updateLabelBackground(_this.$.lblMapDblClick);
    });
    map.addEventListener('map-right-click', function(event) {
      var googleMapMarker = document.createElement('google-map-marker');
      googleMapMarker.lat = event.detail.lat;
      googleMapMarker.lng = event.detail.lng;
      googleMapMarker.label = JSON.parse('{"color":"#305F72", "text":"New Marker"}');
      googleMapMarker.title = 'New Marker';
      googleMapMarker.icon = JSON.parse('{"url":"star.png", "labelOrigin":{"x": 20, "y": 40}}');
      googleMapMarker._draggable = true;
      map.appendChild(googleMapMarker);
      _this._setMarkerEventListeners(googleMapMarker);
      _this._updateLabelBackground(_this.$.lblMapRightClick);
    });    
  }

  _setMarkerEventListeners(marker) {
    var _this = this;
    marker.addEventListener('marker-mouse-over', function(event) {
      _this.page = 1;
      _this._updateLabelBackground(_this.$.lblMarkerMouseOver);
    });
    marker.addEventListener('marker-mouse-out', function(event) {
      _this.page = 0;
      _this._updateLabelBackground(_this.$.lblMarkerMouseOut);
    });
    marker.addEventListener('marker-click', function(event) {
      marker.lat = 2.6116145;
      marker.lng = -76.3862953;
      _this._updateLabelBackground(_this.$.lblMarkerClick);
    });
    marker.addEventListener('marker-dbl-click', function(event) {
      _this._updateLabelBackground(_this.$.lblMarkerDblClick);
      _this.$.map.removeChild(marker);
    });
    marker.addEventListener('marker-drag-end', function(event) {
      _this._updateLabelBackground(_this.$.lblMarkerDragEnd);
    });
    marker.addEventListener('marker-drag-start', function(event) {
      _this._updateLabelBackground(_this.$.lblMarkerDragStart);
    });
    marker.addEventListener('marker-right-click', function(event) {
      _this._updateLabelBackground(_this.$.lblMarkerRightClick);
    });
  }

  _setPolylineEventListeners(polyline) {
    var _this = this;
    polyline.addEventListener('polyline-mouse-over', function(event) {
      _this.page = 2;
      _this._updateLabelBackground(_this.$.lblPolylineMouseOver);
    });
    polyline.addEventListener('polyline-mouse-out', function(event) {
      _this.page = 0;
      _this._updateLabelBackground(_this.$.lblPolylineMouseOut);
    });
    polyline.addEventListener('polyline-click', function(event) {
      _this._updateLabelBackground(_this.$.lblPolylineClick);
    });
    polyline.addEventListener('polyline-dbl-click', function(event) {
      _this._updateLabelBackground(_this.$.lblPolylineDblClick);
      _this.$.map.removeChild(polyline);
    });
    polyline.addEventListener('polyline-right-click', function(event) {
      _this._updateLabelBackground(_this.$.lblPolylineRightClick);
    });
    polyline.addEventListener('polyline-path-changed', function(event) {
      _this._updateLabelBackground(_this.$.lblPolylinePathChanged);
    });
  }
}

window.customElements.define(GoogleMapDemo.is, GoogleMapDemo);