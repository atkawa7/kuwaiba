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
import { BpmnLibrary } from './bpmn-library';
/**
 * `bpmn-element`
 * bpmn-element is a BPMN 2.0 viewer and modeler
 *
 * @customElement
 * @polymer
 * @demo demo/index.html
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
class BpmnElement extends PolymerElement {
  static get template() {
    return html`
      <style>
        :host {
          display: block;
        }
      </style>
      <div id="canvas" style="width: 100%; height: 100%; padding: 0; margin: 0;"></div>
      <!--<h2>Hello [[prop1]]!</h2>-->
    `;
  }
  static get properties() {
    return {
      includes: {
        type: Object
      },
      prop1: {
        type: String,
        value: 'bpmn-element',
      }
    };
  }
  ready() {
    super.ready();
    new BpmnLibrary(this.includes).load().then(() => {this.initBpmn()});
  }
  initBpmn() {
    this.diagramUrl = './start-diagram.bpmn';
    this.bpmnJS = new BpmnJS({
      container: this.$.canvas
    });
    
    var a = this.shadowRoot.querySelector('.djs-palette');
    if (a) {
     a.style.display = 'none';
    }
    console.log(a);

    var xhr = new XMLHttpRequest();

    xhr.open('GET', this.diagramUrl);
    var _this = this;
    xhr.onload = function() {
        if (xhr.status === 200) {
          _this.openDiagram(xhr.responseText);
        }
        else {
            alert('Request failed.  Returned status of ' + xhr.status);
        }
    };
    xhr.send();
  }
  openDiagram(bpmnXML) {
    var _this = this;
    this.bpmnJS.importXML(bpmnXML, function(err) {

    if (err) {
      return console.error('could not import BPMN 2.0 diagram', err);
    }
    var canvas = _this.bpmnJS.get('canvas');
    var dragging = _this.bpmnJS.get('dragging');
    var selection = _this.bpmnJS.get('selection');
    var mouse =  _this.bpmnJS.get('mouse');
    var palette = _this.bpmnJS.get('palette');
    var modeling = _this.bpmnJS.get('modeling');
     var elementFactory = _this.bpmnJS.get('elementFactory');
     var bpmnFactory = _this.bpmnJS.get('bpmnFactory');
     var elementRegistry = _this.bpmnJS.get('elementRegistry');
     var modeling = _this.bpmnJS.get('modeling');
     var startEventShape = elementRegistry.get('StartEvent_0wms1y6');

     var task = modeling.appendShape(startEventShape, { type: 'bpmn:Task' });
     var exclusiveGateway = modeling.appendShape(task, { type: 'bpmn:ExclusiveGateway' });
     var userTask = modeling.appendShape(exclusiveGateway, { type: 'bpmn:UserTask' });
     modeling.appendShape(userTask, { type: 'bpmn:EndEvent' });
     

     //modeling.createShape('bpmn:Task');
     //var boundary = elementRegistry.get('Boundary');
     //console.log(task.parent.type);
     //var newLane = modeling.addLane(boundary, 'bottom');

      // given
      var rootElement = canvas.getRootElement(),
          rootGfx = canvas.getGraphics(rootElement);

      //triggerPaletteEntry('create.subprocess-expanded');
      var entry = palette.getEntries()['create.exclusive-gateway'];

      if (entry && entry.action && entry.action.click) {
        //entry.action.click(mouse.getLastMoveEvent());
        entry.action.click(_this.createMoveEvent(100, 100));
      }

      // when
      //dragging.hover({ element: rootElement, gfx: rootGfx });

      //dragging.move(canvasEvent({ x: 100, y: 100 }));

      // when
      dragging.end();
      console.log(selection.get()[0]);
      //modeling.appendShape(startEventShape, selection.get()[0]);
      modeling.createConnection(startEventShape, selection.get()[0], {
        type: 'bpmn:SequenceFlow'
      }, startEventShape.parent);
      
      selection.deselect(selection.get()[0]);


    });
  }

  createMoveEvent(x, y) {
    var event = document.createEvent('MouseEvent');
  
    var screenX = x,
        screenY = y,
        clientX = x,
        clientY = y;
  
    if (event.initMouseEvent) {
      event.initMouseEvent(
        'mousemove',
        true,
        true,
        window,
        0,
        screenX,
        screenY,
        clientX,
        clientY,
        false,
        false,
        false,
        false,
        0,
        null
      );
    }
  
    return event;
  }
}
/*
businessObject
contextPad
modeling
elementFactory
elementRegistry

bpmn:StartEvent
bpmn:SubProcess
bpmn:ParallelGateway
*/

window.customElements.define('bpmn-element', BpmnElement);
