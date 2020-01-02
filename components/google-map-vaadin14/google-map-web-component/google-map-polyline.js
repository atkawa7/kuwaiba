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
			draggable: {
				type: Boolean,
				value: false,
				observer: '_draggableChanged'
			},
			editable:{
				type: Boolean,
				value: false,
				observer: '_editableChanged'
			},
			/**
			 * All CSS3 colors (except for extended named colors)
			 */
			strokeColor: {
				type: String,
				value: '#FF0000',
				observer: '_strokeColorChanged'
			},
			/**
			 * Between 0.0 and 1.0
			 */
			strokeOpacity: {
				type: Number,
				value: 1.0,
				observer: '_strokeOpacityChanged'
			},
			/**
			 * In pixels
			 */
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
			path: {
				type: Array,
				observer: '_pathChanged'
			}
		};
	}
	draw(map) {
		var _this = this;
		this.polyline = new google.maps.Polyline({
			draggable: this.draggable,
			editable: this.editable,
			map: map,
			strokeColor: this.strokeColor,
			strokeOpacity: this.strokeOpacity,
			strokeWeight: this.strokeWeight,
			visible: this.visible
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
		this._setPolylinePath(this.path);
		/*
		this._observer = new FlattenedNodesObserver(this, (info) => {
			_this._processNewCoordinates(info.addedNodes);
			_this._processRemovedCoordinates(info.removedNodes);
		});
		*/
	}
	_setPolylinePath(path) {
		if (path.length >= 2) {
			var _this = this;
			this.polyline.setPath(path);
			google.maps.event.addListener(this.polyline.getPath(), 'insert_at', function(index) {
				_this._updatePath();
			});
			google.maps.event.addListener(this.polyline.getPath(), 'remove_at', function(index, removed) {
				_this._updatePath();
			});
			google.maps.event.addListener(this.polyline.getPath(), 'set_at', function(index, previous) {
				_this._updatePath();
			});
		}
	}
	_updatePath() {
		var path = [];
		this.polyline.getPath().forEach(function(coordinate, index) {
			path.push({lat: coordinate.lat(), lng: coordinate.lng()});
		});
		this.path = path;
		this.dispatchEvent(new CustomEvent('polyline-path-changed'));
	}
	remove() {
		if (this.polyline !== undefined)
			this.polyline.setMap(null);
	}
	/*
	_processNewCoordinates(addedCoordinates) {
		var _this = this;
		var path = [];
		for (var i = 0; i < addedCoordinates.length; i++) {			
			if (addedCoordinates[i].localName === Constants.googleMapLatLng) {
				console.log('>>> _processNewCoordinate lat ' + addedCoordinates[i].lat + ' lng ' + addedCoordinates[i].lng);
				path.push({
					lat: addedCoordinates[i].lat, 
					lng: addedCoordinates[i].lng
				});
			}
		}
		this.polyline.setPath(path);
		google.maps.event.addListener(this.polyline.getPath(), 'insert_at', function(index) {
			console.log('>>> polyline-path-insert-at ' + index);
			var j = 0;
			for (var i = 0; i < _this.childNodes.length; i++) {
				if (_this.childNodes[i].localName === Constants.googleMapLatLng)
					console.log('>>> childNodes ' + ++j);
			}
		});
		google.maps.event.addListener(this.polyline.getPath(), 'remove_at', function(index, removed) {
			console.log('>>> polyline-path-remove-at index ' + index + ' removed ' + removed);
			var j = 0;
			for (var i = 0; i < _this.childNodes.length; i++) {
				if (_this.childNodes[i].localName === Constants.googleMapLatLng)
					console.log('>>> childNodes ' + ++j);
			}
		});
		google.maps.event.addListener(this.polyline.getPath(), 'set_at', function(index, previous) {
			console.log('>>> polyline-path-set-at index ' + index + ' previous lat ' + previous.lat() + ' lng ' + previous.lng());
		});
	}
	_processRemovedCoordinates(removedCoordinates) {
		console.log('>>> _processRemovedCoordinates');
	}
	*/
	_draggableChanged(newValue, oldValue) {
		if (this.polyline !== undefined && 
			this.polyline.getDraggable() !== newValue) {
			this.polyline.setDraggable(newValue);
		}
	}
	_editableChanged(newValue, oldValue) {
		if (this.polyline !== undefined && 
			this.polyline.getEditable() !== newValue) {
			this.polyline.setEditable(newValue);
		}
	}
	_strokeColorChanged(newValue, oldValue) {
		if (this.polyline !== undefined)
			this.polyline.setOptions({strokeColor: newValue});
	}
	_strokeOpacityChanged(newValue, oldValue) {
		if (this.polyline !== undefined)
			this.polyline.setOptions({strokeOpacity: newValue});
	}
	_strokeWeightChanged(newValue, oldValue) {
		if (this.polyline !== undefined)
			this.polyline.setOptions({strokeWeight: newValue});
	}
	_visibleChanged(newValue, oldValue) {
		if (this.polyline !== undefined && 
			this.polyline.getVisible() != newValue) {
			this.polyline.setVisible(newValue);
		}
	}
	_pathChanged(newValue, oldValue) {		
		if (this.polyline !== undefined && 
			this.polyline.getPath() !== undefined) { 
			if (this.polyline.getPath().getLength() === this.path.length) {
				var flag = false;
				for (var i = 0; i < this.path.length; i++) {
					if (this.polyline.getPath().getAt(i).lat() != this.path[i].lat ||  
						this.polyline.getPath().getAt(i).lng() != this.path[i].lng) {
						flag = true;
						break;
					}
				}
				if (flag) {
					this._setPolylinePath(newValue);
				}
			} else {
				this._setPolylinePath(newValue);
			}
		}
	}
}

window.customElements.define(GoogleMapPolyline.is, GoogleMapPolyline);