import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import * as Constants from './google-map-constants.js';
/**
 * `my-el`
 * my-el
 *
 * @customElement
 * @polymer
 * @demo demo/index.html
 */
class GoogleMapMarker extends PolymerElement {
  static get is() {
    return Constants.googleMapMarker;
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
       * {"url":"", "labelOrigin":}
       */
      url: {
        type: String,
      },
      lat: {
        type: Number,
        value: 2.4573831
      },
      lng: {
        type: Number,
        value: -76.6699746
        
      },
      title: {
        type: String
      },
      /**
       * {"color":"","fontFamily":"","fontSize":"","fontWeight":"","text":""}
       */
      label: {
        type: Object
      }
    };
  }

  draw(map) {
    var icon = {url:'default-map-marker.png', labelOrigin: new google.maps.Point(20, 40)};
    var position = {lat: this.lat, lng: this.lng};

    this.marker = new google.maps.Marker({
      position: position,
      map: map,
      title: this.title,
      icon: icon,
      label: this.label,
    });
    var _this = this;
    this.marker.addListener('click', function(event) {
      _this.dispatchEvent(new CustomEvent('marker-click'));
    });
    this.marker.addListener('dblclick', function(event) {
      _this.dispatchEvent(new CustomEvent('marker-dbl-click'));
    });
    this.marker.addListener('rightclick', function(event) {
      _this.dispatchEvent(new CustomEvent('marker-right-click'));
    });
    /*
    Events: 
    animation_changed, 
    *click, 
    clickable_changed, 
    cursor_changed, 
    *dblclick, 
    drag, 
    dragend, 
    draggable_changed, 
    dragstart, 
    flat_changed, 
    icon_changed, 
    mousedown, 
    mouseout, 
    mouseover, 
    mouseup, 
    position_changed, 
    *rightclick, 
    shape_changed, 
    title_changed, 
    visible_changed, 
    zindex_changed
    */
  }
}

window.customElements.define(GoogleMapMarker.is, GoogleMapMarker);