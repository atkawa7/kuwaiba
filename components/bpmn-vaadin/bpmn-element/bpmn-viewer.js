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
import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {PrePackagedDistro} from './pre-packaged-distro.js';
/**
 * `bpmn-viewer`
 * @element bpmn-viewer
 * Bpmn viewer based on bpmn-js
 * @customElement
 * @polymer
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
class BpmnViewer extends PolymerElement {
  static get is() {
    return 'bpmn-viewer';
  }
  static get template() {
    return html`
      <style>
        :host {
          display: block;
        }
        .fullSize {
          width: 100%;
          height: 100%;
          padding: 0;
          margin: 0;
        }
      </style>
      <div class="fullSize" id="[[containerId]]"></div>
    `;
  }
  static get properties() {
    return {
      /**
       * @attribute containerId
       * @type {string}
       * @default 'canavas'
       */
      containerId: {
        type: String,
        value: 'canvas'
      },
      /**
       * @attribute viewerDistro
       * @type {string}
       */
      viewerDistro: {
        type: String
      },
      /**
       * @attribute diagramUrl
       * @type {string}
       */
      diagramUrl: {
        type: String
      }
    }
  }
  /**
   * @override
   */
  ready() {
    super.ready();
    new PrePackagedDistro(this.viewerDistro).load().then(() => {this._initBpmnViewer()});
  }
  /**
   * @private
   */
  _initBpmnViewer() {
    var viewer = new BpmnJS({
      container: this.shadowRoot.getElementById(this.containerId)
    });
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4) {
        viewer.importXML(xhr.response, function(err) {
          if (err) {
            return console.error('could not import BPMN 2.0 diagram', err);
          }
        });
      }
    };
    xhr.open('GET', this.diagramUrl, true);
    xhr.send();
  }
}
window.customElements.define(BpmnViewer.is, BpmnViewer);