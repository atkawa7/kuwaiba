import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import '@polymer/iron-pages/iron-pages.js';
import '@vaadin/vaadin-tabs/vaadin-tabs.js';
import '@vaadin/vaadin-tabs/vaadin-tab.js';
import '@vaadin/vaadin-ordered-layout/vaadin-vertical-layout.js';
import '@vaadin/vaadin-split-layout/vaadin-split-layout.js';
import './google-map.js';
import './google-map-marker.js';
import {GoogleMapMarker} from './google-map-marker.js';
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
          <google-map id="map" api-key=[[apiKey]] lib-drawing>
            <google-map-marker id="marker" title="Marker" label='{"color":"#305F72", "text":"Marker"}'></google-map-marker>
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
              <label id="lblMapClick">map-click</label>
              <label id="lblMapDblClick">map-dbl-click</label>
              <label id="lblMapRightClick">map-right-click (New Marker)</label>
              <label id="lblMapCenterChanged">map-center-changed</label>
              <label id="lblMapMouseMove">map-mouse-move</label>
              <label id="lblMapMouseOut">map-mouse-out</label>
              <label id="lblMapMouseOver">map-mouse-over</label>
              <label id="lblMapZoomChanged">map-zoom-changed</label>
            </vaadin-vertical-layout>
            <vaadin-vertical-layout>
              <label id="lblMarkerClick">marker-click</label>
              <label id="lblMarkerDblClick">marker-dbl-click</label>
              <label id="lblMarkerRightClick">marker-right-click</label>
            </vaadin-vertical-layout>
            <page><h3>Page 3</h3>Good Morning</page>
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
        type: String
      }
    };
  }
  static get is() {
      return 'google-map-demo';
  }
  ready() {
    super.ready();
    const map = this.$.map;
    const marker = this.$.marker;
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
      map.appendChild(googleMapMarker);
      _this._setMarkerEventListeners(googleMapMarker);

      _this._updateLabelBackground(_this.$.lblMapRightClick);
    });
    this._setMarkerEventListeners(marker);
  }
  _updateLabelBackground(aLabel) {
    aLabel.style.background = '#F2F4F9';
    const wait = ms => new Promise(resolve => setTimeout(resolve, ms));
    wait(1000).then(() => aLabel.style.background = 'transparent');                
  }
  _setMarkerEventListeners(marker) {
    var _this = this;
    marker.addEventListener('marker-click', function(event) {
      _this._updateLabelBackground(_this.$.lblMarkerClick);
    });
    marker.addEventListener('marker-dbl-click', function(event) {
      _this._updateLabelBackground(_this.$.lblMarkerDblClick);
    });
    marker.addEventListener('marker-right-click', function(event) {
      _this._updateLabelBackground(_this.$.lblMarkerRightClick);
    });
  }
}

window.customElements.define(GoogleMapDemo.is, GoogleMapDemo);