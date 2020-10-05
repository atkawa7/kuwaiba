import { PolymerElement, html } from '@polymer/polymer/polymer-element.js';

import 'ace-builds/src-noconflict/ace.js';
import 'ace-builds/src-noconflict/ext-language_tools.js';
import 'ace-builds/src-noconflict/theme-dreamweaver.js'
import 'ace-builds/src-noconflict/theme-chaos.js'
import 'ace-builds/src-noconflict/theme-eclipse.js'
import 'ace-builds/src-noconflict/theme-textmate.js'
import 'ace-builds/src-noconflict/mode-java.js'
import 'ace-builds/src-noconflict/mode-groovy.js'
import 'ace-builds/src-noconflict/mode-html.js'
import 'ace-builds/src-noconflict/worker-html'
import 'ace-builds/src-noconflict/worker-javascript.js'
//import 'ace-builds/webpack-resolver'


class AceEditor extends PolymerElement {
  static get template() {
    return html`
      <style>
        :host {
          display: block;
        }
    
      </style>
      <div id="editor" 
      style="width:[[width]];height:[[height]];min-height:50px;max-width:[[maxWidth]];
      max-height:[[maxHeight]];border:1px solid gray"></div>
    `;
  }

  static get is() { 
      return 'ace-editor'; 
  }

  static get properties() {
    return {
      width: {
            type: String,
            value: '100%'
        },
      height: {
            type: String,
            value: '100%'
        },
      maxWidth: {
            type: String,
            value: '100%'
        },
      maxHeight: {
            type: String,
            value: '100%'
        },
      editor: {
            type: Object,
            value: null
            },
      mode: {
        type: String,
        notify: true,
        observer: 'modeChanged',
      },
      theme: {
        type: String,
        value: 'ace/theme/textmate',
        observer: 'themeChanged',
      },
     
      value: {
        type: String,
        notify: true,
        observer: 'valueChanged',
      },
      readOnly: {
        type: Boolean,
        value: false,
        observer: 'readOnlyChanged',
      },     
      minlines: {
        type: Number,
        value: 15,
      },
      maxlines: {
        type: Number
      },
      enableLiveAutocompletion: {
        type: Boolean,
        value: false,
      },
      wrap: {
        type: Boolean,
        value: false,
        observer: 'wrapChanged',
      },
      fontSize: {
        type: String,
        value: '14px',
        observer: 'fontSizeChanged',
      },
      baseUrl: {
        type: String,
        value: '../../ace-builds/src-min-noconflict/',
      }
    };
  }

  constructor() {
    super();
  }
 
  connectedCallback() {
    super.connectedCallback();
    this.editor = ace.edit(this.$.editor);
    this.fireAceEditorLoaded();
    this.initAceEditor();
  }

  initAceEditor() {

    let aceStyles = '#ace_editor\\.css';
    const lightStyle = this.getRootNode().querySelector(aceStyles) || document.querySelector(aceStyles);
    this.shadowRoot.appendChild(lightStyle.cloneNode(true));
//    this.editor.$blockScrolling = Infinity;
    ace.config.set('basePath', this.baseUrl);
    ace.config.set('modePath', this.baseUrl);
    ace.config.set('themePath', this.baseUrl);
    ace.config.set('workerPath', this.baseUrl);
    this.editor.setOptions({
        minLines: this.minlines,
        maxLines: this.maxlines,
        enableBasicAutocompletion: true,
        enableLiveAutocompletion: this.enableLiveAutocompletion      
    });
    var _this = this;
    this.editor.on('change', () => { 
        _this.value = _this.editorValue;
        _this.fireEditorValueChanged();
    });
    
    this.themeChanged();
    this.readOnlyChanged();
    this.wrapChanged();
    this.modeChanged();
    this.fontSizeChanged();
    
    // set inner tag content if no value was set
    var innerContent = this.innerHTML.trim();
    if (!this.value) {
      this.value = innerContent;
    } else {
      this.valueChanged();
    }
  }

  fontSizeChanged() {
    if (this.editor) {
        this.$.editor.style.fontSize = this.fontSize;
    }
  }

  modeChanged() {
    if (this.editor) 
        this.editor.getSession().setMode(this.mode);
  }

  themeChanged() {
    if (this.editor) {
        this.editor.setTheme(this.theme);
    }
  }

  valueChanged() {
     console.debug("valueChanged - ", this.value)
    if (this.editor) {
        if (this.editorValue != this.value) {
          this.editorValue = this.value;
          this.editor.clearSelection();
          this.editor.resize();
        }
    }
  }

  readOnlyChanged() {
    if (this.editor) {
        this.editor.setReadOnly(this.readOnly);
    }
  }

  wrapChanged() {
    if (this.editor) {    
       this.editor.getSession().setUseWrapMode(this.wrap);
    }
  }

  get editorValue() {
    return this.editor.getValue();
  }

  set editorValue(value) {
    if (value) {  
        this._value = value;
        this.editor.setValue(value);
    }
  }
  
  //This method dispatches a custom event when the editor is loaded
  fireAceEditorLoaded() {
      this.dispatchEvent(new CustomEvent('ace-editor-loaded', { detail: {kicked: true}}));
  }
  
  fireEditorValueChanged() {
    this.dispatchEvent(new CustomEvent('editor-value-changed', {detail: {kicked: true}}));
  }
}

window.customElements.define(AceEditor.is, AceEditor);