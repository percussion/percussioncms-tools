/*
 * Copyright 1999-2021 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var newRules = new Array();
var hideNonStrictAction = null;

function writeButton(what) {
  if (document.styleSheets && (document.all || document.getElementById)
    && document.styleSheets[0] != null)
  {
    document.write("<p><input type=button value=\"Hide non-strict " + what
      + "\" id=toggler accesskey=s onclick=\"toggleNonStrict('"
      + what + "')\"></p>");
  }
}

function toggleNonStrict(what) {
  var sheet = document.styleSheets[0];
  var toggler = null;

  if (document.all) {
    toggler = document.all.toggler;

    if (toggler != null) {
      if (toggler.value == "Hide non-strict " + what) {
        sheet.addRule(".transitional", "display:none");
        newRules.push(sheet.rules.length - 1);

        sheet.addRule(".transitional", "speak:none");
        newRules.push(sheet.rules.length - 1);

        sheet.addRule(".frameset", "display:none");
        newRules.push(sheet.rules.length - 1);
      
        sheet.addRule(".frameset", "speak:none");
        newRules.push(sheet.rules.length - 1);
      
        if (hideNonStrictAction != null) {
          hideNonStrictAction(true);
        }
        toggler.value = "Show non-strict " + what;

      } else {
        while (newRules.length > 0) {
          sheet.removeRule(newRules.pop());
        }
        if (hideNonStrictAction != null) {
          hideNonStrictAction(false);
        }
        toggler.value = "Hide non-strict " + what;
      }
    }

  } else {
    toggler = document.getElementById('toggler');

    if (toggler != null) {
      if (toggler.value == "Hide non-strict " + what) {
        newRules.push(sheet.insertRule(".transitional {display:none}",sheet.cssRules.length));
        newRules.push(sheet.insertRule(".transitional {speak:none}",sheet.cssRules.length));
        newRules.push(sheet.insertRule(".frameset {display:none}",sheet.cssRules.length));
        newRules.push(sheet.insertRule(".frameset {speak:none}",sheet.cssRules.length));

        if (hideNonStrictAction != null) {
          hideNonStrictAction(true);
        }
        toggler.value = "Show non-strict " + what;
      } else {
        while (newRules.length > 0) {
          sheet.deleteRule(newRules.pop());
        }
        if (hideNonStrictAction != null) {
          hideNonStrictAction(false);
        }
        toggler.value = "Hide non-strict " + what;
      }
    }
  }
}
