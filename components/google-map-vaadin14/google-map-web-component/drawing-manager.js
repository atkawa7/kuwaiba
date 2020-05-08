import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {GoogleMapPolygon} from './google-map-polygon.js';

class DrawingManager extends PolymerElement {
  static get is() {
    return 'drawing-manager';
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
       * circle, marker, polygon, polyline, rectangle
       */
      drawingMode: {
        type: String,
        observer: '_drawingModeChanged'
      },
      drawingControl: {
        type: Boolean,
        value: false,
        observer: '_drawingControlChanged'
      }
    };
  }
  added(map) {
    this.drawingManager = new google.maps.drawing.DrawingManager({
      drawingMode: this.drawingMode,
      drawingControl: this.drawingControl
    });
    this.drawingManager.setMap(map);
    this.drawingManager.addListener('markercomplete', marker => {
      marker.setMap(null);
      this.dispatchEvent(new CustomEvent('marker-complete', 
        {
          detail: {
            lat: marker.getPosition().lat(), 
            lng: marker.getPosition().lng()
          }
        }
      ));
    });
    this.drawingManager.addListener('polylinecomplete', polyline => {
      polyline.setMap(null);
      var path = [];
      polyline.getPath().forEach(coordinate => {
        path.push({
          lat: coordinate.lat(),
          lng: coordinate.lng()
        });
      });

      this.dispatchEvent(new CustomEvent('polyline-complete', 
        {
          detail: {
            path: path
          }
        }
      ));
    });
    this.drawingManager.addListener('polygoncomplete', polygon => {
      polygon.setMap(null);
      this.dispatchEvent(new CustomEvent('polygon-complete', 
        {
          detail: {
            paths: GoogleMapPolygon.getPathsArray(polygon.getPaths())
          }
        }
      ));
    });
  }
  removed() {
    if (this.drawingManager)
      this.drawingManager.setMap(null);
  }
  _drawingModeChanged(newValue, oldValue) {
    if (this.drawingManager)
      this.drawingManager.setOptions({drawingMode: newValue});
  }
  _drawingControlChanged(newValue, oldValue) {
    if (this.drawingManager)
      this.drawingManager.setOptions({drawingControl: newValue});
  }
}
window.customElements.define(DrawingManager.is, DrawingManager);