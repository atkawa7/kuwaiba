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
import {getBpmnDiagram} from './util.js';
import '@polymer/polymer/lib/elements/dom-repeat.js';
/**
 * `bpmn-modeler`
 * @element bpmn-modeler
 * Bpmn modeler based on bpmn-js
 * @customElement
 * @polymer
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
class BpmnModeler extends PolymerElement {
  static get is() {
    return 'bpmn-modeler';
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
      <template is="dom-repeat" items="[[modelerStyles]]">
        <link rel="stylesheet" href="{{item}}">
      </template>
      <div class="fullSize" id="[[containerId]]"></div>
    `;
  }
  static get properties() {
    return {
      /**
       * @attribute containerId
       * @type {string}
       * @default 'canvas'
       */
      containerId: {
        type: String,
        value: 'canvas'
      },
      /**
       * [
       * 'https://unpkg.com/bpmn-js@6.4.0/dist/assets/diagram-js.css',
       * 'https://unpkg.com/bpmn-js@6.4.0/dist/assets/bpmn-font/css/bpmn.css'
       * ]
       * @attribute modelerStyles
       * @type {array}
       */
      modelerStyles: {
        type: Array
      },
      /**
       * https://unpkg.com/bpmn-js@6.4.0/dist/bpmn-modeler.production.min.js
       * @attribute modelerDistro
       * @type {string}
       */
      modelerDistro: {
        type: String
      },
      /**
       * @attribute diagramUrl
       * @type {string}
       */
      diagramUrl: {
        type: String
      },
      /**
       * @attribute djsPalette
       * @type {boolean}
       */
      djsPalette: {
        type: Boolean
      },
      /**
       * @attribute djsContextPad
       * @type {boolean}
       */
      djsContextPad: {
        type: Boolean
      }
    };
  }
  /**
   * @override
   */
  ready() {
    super.ready();
    new PrePackagedDistro(this.modelerDistro).load().then(() => {this._initBpmnModeler()});
  }
  /**
   * @private
   */
  _initBpmnModeler() {
    var _this = this;
    var emptyModules = {};
    if (!this.djsContextPad)
      emptyModules.contextPadProvider = ['value', null];
    this.bpmnModeler = new BpmnJS({
      container: this.shadowRoot.getElementById(this.containerId),
      additionalModules: [
        emptyModules
      ]
    });
    if (!this.djsPalette) {
      var palette = this.shadowRoot.querySelector('.djs-palette');
      if (palette)
        palette.style.display = 'none';
    }
    if (this.diagramUrl) {
      var xhr = new XMLHttpRequest();
      xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
          _this.bpmnModeler.importXML(xhr.response, function(err) {
            if (err)
              return console.error('could not import BPMN 2.0 diagram', err);
          });
        }
      }
      xhr.open('GET', this.diagramUrl, true);
      xhr.send();
    } else {
      this.bpmnModeler.importXML(getBpmnDiagram(), function(err) {
        if (err)
          return console.error('could not import BPMN 2.0 diagram', err);
      });
    }
  }
  exportDiagram() {
    var _this = this;
    this.bpmnModeler.saveXML({ format: true }, function(err, xml) {
      if (err)
        return console.error('could not save BPMN 2.0 diagram', err);
      _this.xml = xml;
    });
  }
}
window.customElements.define(BpmnModeler.is, BpmnModeler);
