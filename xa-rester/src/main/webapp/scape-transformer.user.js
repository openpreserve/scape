/*
$Id: $

SCAPE Transformer - (c) 2010 THE SCAPE Consortium

A greasemonkey user script which lets you send external links to a service
that may be able to transform the content into a new format you like more.

This program is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation; either version 2 of the License, or (at your
option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
59 Temple Place, Suite 330, Boston, MA 02111-1307 USA


CHANGELOG
	Version 0.1
	- initial release, ...

BUGS
	---

TODO
	---


*/

// ==UserScript==
// @name          SCAPE Transformer
// @namespace     http://www.scape-project.eu/transformer
// @description   Integrates the SCAPE transformation services into your browser.
// @include       *
// @exclude       
// @version       0.1
// @require       http://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.min.js
// @require       http://cherne.net/brian/resources/jquery.hoverIntent.minified.js
// ==/UserScript==
// Add jQuery
(function() {

function annotate() {
	if (window.annotateLinksDone) return;
	appendStyles();
	$("a").each( function() {
		var anno = $("<a class=\"annlink-span\" target=\"_new\" href=\"http://localhost:8080/scape/tools/test2?fmt=png&src="+$(this).get(0).href+"\">[SCAPE]</a>");
		$(anno).hide();
		$(anno).insertAfter($(this));
		
		$(this).hoverIntent( function() {
			$(anno).fadeIn('fast');
		}, function() {
			$(anno).delay(2000).fadeOut('slow');
		});
		
	});
	window.annotateLinksDone = true;
};

// -------------------- Stylesheet: display for print media only --------------------
function appendStyles() {
	var styleElement = document.createElement('style');
	if (styleElement) {
		styleElement.type = 'text/css';
		// styleElement.media = 'print';
		styleElement.id = 'annotatelinkstyles';
		document.getElementsByTagName('head')[0].appendChild(styleElement);
		var sty = document.styleSheets[document.styleSheets.length - 1];
		sty.insertRule(".annlink-inline { }", 0);
		sty.insertRule(".annlink-span { display: none; position: absolute; }", 1);
		//sty.insertRule(".annlink-inline:hover .annlink-span { display: block; }", 2);
		//sty.insertRule("@media screen { .:hover { display: block; }}", 3);
	}
}


// this fixes a double injection problem with GM 3.x
$().ready(function() {
	annotate();
});

// Menu Command:
if (GM_registerMenuCommand && typeof(GM_registerMenuCommand) == 'function') {
	GM_registerMenuCommand("Transform Links", annotate, "l");
}

// end user script
})();

