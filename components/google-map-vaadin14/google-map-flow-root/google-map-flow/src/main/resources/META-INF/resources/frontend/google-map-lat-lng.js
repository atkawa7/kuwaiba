import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import * as Constants from './google-map-constants.js';

class GoogleMapLatLng extends PolymerElement {
	static get is() {
		return Constants.googleMapLatLng;
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
			lat: {
				type: Number,
				value: 2.4573831
			},
			lng: {
				type: Number,
				value: -76.6699746
			}
		};
	}
}

window.customElements.define(GoogleMapLatLng.is, GoogleMapLatLng);