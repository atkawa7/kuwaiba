import '@polymer/polymer/polymer-legacy.js';

import '@polymer/paper-item/paper-item.js';
import '@polymer/paper-ripple/paper-ripple.js';
import '@cwmr/paper-autocomplete/paper-autocomplete.js';
import { Polymer } from '@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '@polymer/polymer/lib/utils/html-tag.js';
/**
@license
Copyright (c) 2015 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/
/*
  This example shows how to implement your own autocomplete with custom template and search function.
*/
Polymer({
  _template: html`
    <!-- When using custom template and or custom query function, it is important to also set the \`textProperty\`
     and \`valueProperty properties\`. -->
    <paper-autocomplete label="Select Account" id="input-local" no-label-float="true" source="[[accounts]]" text-property="accountNumber" value-property="id">
        <template slot="autocomplete-custom-template">
          <!-- NOTICE: Due to shadow-dom, custom template styles need to be included with the template in Polymer 2.x -->
          <style>
            :host {
              display: block;
            }
            paper-item.account-item {
              padding: 8px 16px;
            }
            .company-name {
              color: #333;
            }
            .account-number, .email {
              margin-top: 4px;
              color: #999;
            }
            .email {
              font-size: small;
            }
          </style>
          <paper-item class="account-item" role="option" aria-selected="false">
            <div>
              <div class="company-name">[[item.companyName]]</div>
              <div class="account-number">[[item.accountNumber]]</div>
              <div class="email">[[item.email]]</div>
            </div>
            <paper-ripple></paper-ripple>
          </paper-item>
        </template>
    </paper-autocomplete>
`,

  is: "account-autocomplete",

  properties: {
  },

  listeners: {
    'autocomplete-selected': '_onOptionSelected'
  },

  ready: function () {
    var autocomplete = this.$$('paper-autocomplete');
    autocomplete.addEventListener('autocomplete-selected', this.onSelect);

    // Override default queryFn with our custom. This is a needed requirement to implement the custom template
    autocomplete.queryFn = this._queryFn;
  },

  _onOptionSelected: function (event) {
    var paperToast = document.querySelector('paper-ripple');
    var selected = event.detail.text;
    paperToast.text = 'Selected: ' + selected;
    paperToast.show();
  },

  // This custom queryFn will filter results searching in both companyName and accountNumber, then it will return the
  // whole data object so it can be accessed in the custom template
  _queryFn: function (datasource, query) {
    return datasource.filter(function (item) {
      return (
        item.companyName.toLowerCase().indexOf(query) != -1 || item.accountNumber.toLowerCase().indexOf(query) != -1
      );
    });
  }
});