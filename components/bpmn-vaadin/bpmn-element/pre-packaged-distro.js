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

/**
 * Includes pre-packaged BPMN viewer/modeler
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
class PrePackagedDistro {
  /**
   * 
   * @param {String} distro external script source
   * viewer distro (without pan and zoom)
   * https://unpkg.com/bpmn-js@6.4.0/dist/bpmn-viewer.production.min.js
   * viewer distro (with pan and zoom)
   * https://unpkg.com/bpmn-js@6.4.0/dist/bpmn-navigated-viewer.production.min.js
   * modeler distro
   * https://unpkg.com/bpmn-js@6.4.0/dist/bpmn-modeler.production.min.js
   */
  constructor(distro) {
    this.distro = distro;
  }
  load() {
    if (!this.distro)
      return;
    
    if (!this.promise) {
      this.promise = new Promise(resolve => {
        this.resolve = resolve;
        
        const scriptDistro = document.createElement('script');
        scriptDistro.src = this.distro;
        scriptDistro.async = true;
        scriptDistro.addEventListener('load', () => {
          this.ready();
        });
        document.body.append(scriptDistro);
      });
    }
    return this.promise;
  }
  ready() {
    if (this.resolve)
      this.resolve();
  }
}
export {PrePackagedDistro};