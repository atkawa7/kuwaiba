class Label extends HTMLElement {
    constructor() {
        super();
    }

    static get observedAttributes() {
        return ['text'];
    }

    attributeChangedCallback(name, oldValue, newValue) {
        console.log('[NEOTROPIC-LABEL] ' + this.getAttribute('text'));
        const shadow = this.attachShadow({mode: 'open'});
        
        const label = document.createElement('span');
        label.setAttribute('class', 'label');
        label.textContent = this.getAttribute('text');

        const style = document.createElement('style');
        style.textContent = '.label{background: yellow;}';

        shadow.appendChild(style);
        shadow.appendChild(label);
    }
}
customElements.define('neotropic-label', Label);
