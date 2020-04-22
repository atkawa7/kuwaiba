/**
@license
Copyright 2020 Neotropic SAS <contact@neotropic.co>.

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
import {PolymerElement, html} from '@polymer/polymer/polymer-element.js';
import '@cwmr/paper-autocomplete/paper-autocomplete.js';
import '@polymer/paper-icon-button/paper-icon-button.js'
/**
 * `autocomplete-search`
 * @element autocomplete-search
 * @customElement
 * @polymer
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
class AutocompleteSearch extends PolymerElement {
  static get template() {
    return html`
      <style>
        :host {
          display: block;
        }
      </style>
      <paper-autocomplete id="[[slotId]]" label="[[label]]" source="[[source]]">
        <paper-icon-button slot="[[slotId]]" icon="[[icon]]"></paper-icon-button>
      </paper-autocomplete>
    `;
  }
  static get is() {
    return 'autocomplete-search';
  }
}
customElements.define(AutocompleteSearch.is, AutocompleteSearch);