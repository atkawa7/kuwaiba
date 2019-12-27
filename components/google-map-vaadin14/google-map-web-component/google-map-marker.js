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
       * {"url":"marker.png", "labelOrigin":{"x":20, "y":40}}
       */
      icon: {
        type: Object,
        value: {url: "marker.png", labelOrigin: {x: 20, y: 40}},
        observer: '_iconChanged'
      },
      lat: {
        type: Number,
        value: 2.4573831,
        observer: '_latChanged'
      },
      lng: {
        type: Number,
        value: -76.6699746,
        observer: '_lngChanged'        
      },
      title: {
        type: String,
        observer: '_titleChanged'
      },
      /**
       * {"color":"","fontFamily":"","fontSize":"","fontWeight":"","text":""}
       */
      label: {
        type: Object,
        observer: '_labelChanged'
      },
      /** 
       * Note:
       * For some reason cannot name the property as draggable
       * that is the reason behind the underscore
       */
      _draggable: {
        type: Boolean,
        value: false,
        observer: '__draggableChanged'
      },
      visible: {
        type: Boolean,
        value: true,
        observer: '_visibleChanged'
      }
    };
  }
  _getIcon() {
    var icon = {
      url: this.icon.url, 
      labelOrigin: new google.maps.Point(this.icon.labelOrigin.x, this.icon.labelOrigin.y)
    };
    return icon;
  }
  draw(map) {
    ////var icon = {url:'default-map-marker.png', labelOrigin: new google.maps.Point(20, 40)};
    var position = {lat: this.lat, lng: this.lng};

    this.marker = new google.maps.Marker({
      position: position,
      map: map,
      title: this.title,
      icon: this._getIcon(),
      label: this.label,
      draggable: this._draggable,
      visible: this.visible
    });
    var _this = this;
    this.marker.addListener('click', function(event) {
      _this.dispatchEvent(new CustomEvent('marker-click'));
    });
    this.marker.addListener('dblclick', function(event) {
      _this.dispatchEvent(new CustomEvent('marker-dbl-click'));
    });
    this.marker.addListener('dragend', function(event) {
      _this.dispatchEvent(new CustomEvent('marker-drag-end', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}));
    });
    this.marker.addListener('dragstart', function(event) {
      _this.dispatchEvent(new CustomEvent('marker-drag-start', 
        {detail: {lat: event.latLng.lat(), lng: event.latLng.lng()}}));
    });
    this.marker.addListener('mouseout', function(event) {
      _this.dispatchEvent(new CustomEvent('marker-mouse-out'));
    });
    this.marker.addListener('mouseover', function(event) {
      _this.dispatchEvent(new CustomEvent('marker-mouse-over'));
    });
    this.marker.addListener('position_changed', function() {
      _this.lat = _this.marker.getPosition().lat();
      _this.lng = _this.marker.getPosition().lng();
      _this.dispatchEvent(new CustomEvent('marker-position-changed'));
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

  remove() {
    if (this.marker !== undefined)
      this.marker.setMap(null);
  }

  _iconChanged(newValue, oldValue) {
    if (this.marker !== undefined) {
      var icon = this._getIcon(newValue);
      if (this.marker.getIcon() !== icon) {
        this.marker.setIcon(icon);
      }
    }
  }

  _latChanged(newValue, oldValue) {
    if (this.marker !== undefined &&
      this.marker.getPosition().lat() !== newValue) {
      this.marker.setPosition({lat: newValue, lng: this.marker.getPosition().lng()});
    }
  }

  _lngChanged(newValue, oldValue) {
    if (this.marker !== undefined && 
      this.marker.getPosition().lng() !== newValue) {
      this.marker.setPosition({lat: this.marker.getPosition().lat(), lng: newValue});
    }
  }

  _titleChanged(newValue, oldValue) {
    if (this.marker !== undefined && 
      this.marker.getTitle() !== newValue) {
        this.marker.setTitle(newValue);
      }
  }

  _labelChanged(newValue, oldValue) {
    if (this.marker !== undefined && 
      this.marker.getLabel() !== newValue) {
        this.marker.setLabel(newValue);
    }
  }

  __draggableChanged(newValue, oldValue) {
    if (this.marker !== undefined && 
      this.marker.getDraggable() !== newValue) {
        this.marker.setDraggable(newValue);
    }
  }

  _visibleChanged(newValue, oldValue) {
    if (this.marker !== undefined && 
      this.marker.getVisible() !== newValue) {
        this.marker.setVisible(newValue);
    }
  }
}

window.customElements.define(GoogleMapMarker.is, GoogleMapMarker);