/*jslint indent: 3, undef:true, nomen: true, eqeq: true, sloppy: true, strict: false */
/*global $, JST, Backbone, window */

var StippleApp = {
    init: function () {
        window.App = new StippleApp.Rounters.App();
//    new StippleApp.Rounters.PhotoSets();

        Backbone.history.start();
    },

    // The class
    Models: {}

};
