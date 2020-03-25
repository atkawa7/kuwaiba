/* 
 * Copyright 2019 Vaadin Visitor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {PolymerElement,html} from '@polymer/polymer/polymer-element.js';
import '@polymer/paper-input/paper-input.js';

class HelloWorld extends PolymerElement {

    static get template() {
        return html`
            <div>
                <paper-input id="inputId" value="{{userInput}}"></paper-input>
                <button id="helloButton" on-click="sayHello">Say hello</button>
                <div id="greeting">[[greeting]]</div>
            </div>`;
    }

    static get is() {
          return 'hello-world';
    }
}

customElements.define(HelloWorld.is, HelloWorld);




