import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {FlattenedNodesObserver} from '@polymer/polymer/lib/utils/flattened-nodes-observer.js';
import * as Constants from './google-map-constants.js';

class GoogleMapPolyline extends PolymerElement {
	static get is() {
		return Constants.googleMapPolyline;
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
			strokeColor: {
				type: String,
				value: '#FF0000',
				observer: '_strokeColorChanged'
			},
			strokeOpacity: {
				type: Number,
				value: 1.0
			},
			strokeWeight: {
				type: Number,
				value: 2
			}
		};
	}
	draw(map) {
		var _this = this;

		this.path = [];
		this.polyline = new google.maps.Polyline({
			map: map,
			strokeColor: this.strokeColor,
			strokeOpacity: this.strokeOpacity,
			strokeWeight: this.strokeWeight
		});
		this.polyline.addListener('click', function(event) {
			_this.dispatchEvent(new CustomEvent('polyline-click'));
		});
		this.polyline.addListener('dblclick', function(event) {
			_this.dispatchEvent(new CustomEvent('polyline-dbl-click'));
		});
		this.polyline.addListener('mouseout', function(event) {
			_this.dispatchEvent(new CustomEvent('polyline-mouse-out'));
		});
		this.polyline.addListener('mouseover', function(event) {
			_this.dispatchEvent(new CustomEvent('polyline-mouse-over'));
		});
		this.polyline.addListener('rightclick', function(event) {
			_this.dispatchEvent(new CustomEvent('polyline-right-click'));
		});		
		this._observer = new FlattenedNodesObserver(this, (info) => {
			_this._processNewCoordinates(info.addedNodes);
			_this._processRemovedCoordinates(info.removedNodes);
		});
	}
	_processNewCoordinates(addedCoordinates) {
		this.path = [];
		
		for (var i = 0; i < addedCoordinates.length; i++) {			
			if (addedCoordinates[i].localName === Constants.googleMapLatLng) {
				console.log('>>> _processNewCoordinate ' + addedCoordinates[i]);
				this.path.push({
					lat: addedCoordinates[i].lat, 
					lng: addedCoordinates[i].lng
				});
			}
		}
		this.polyline.setPath(this.path);
	}
	_processRemovedCoordinates(removedCoordinates) {
		console.log('>>> _processRemovedCoordinates');
	}
	_strokeColorChanged(newValue, oldValue) {
		if (this.polyline !== undefined)
			this.polyline.setOptions({strokeColor: newValue});
	}
}

window.customElements.define(GoogleMapPolyline.is, GoogleMapPolyline);