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
import {html, PolymerElement} from '@polymer/polymer/polymer-element.js'
/**
 * `bpmn-edge`
 * @element bpmn-edge
 * @customElement
 * @polymer
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
class BpmnEdge extends PolymerElement {
  static get is() {
    return 'bpmn-edge';
  }
  static get templates() {
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

    };
  }
  /**
   * Bpmn edge added
   * @param {BpmnJS} bpmnModeler
   */
  added(bpmnModeler) {
    console.log('added edge');
  }
  /**
   * Bpmn edge removed
   * @param {BpmnJS} bpmnModeler
   */
  removed(bpmnModeler) {
    console.log('removed edge');
  }
}
window.customElements.define(BpmnEdge.is, BpmnEdge);