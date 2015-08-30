// defines the namespace
window.ChWiSe = {  // top level namespace is declared on the window
  Models: {},
  Collections: {},
  Views: {},
  Router: {},
  Constants: {
    numResultsToFetch: 10,
    triggerPoint: 100  // 100px from the bottom
  },
  Utils: {}
};

ChWiSe.Models.SearchResultCompound = Backbone.Model.extend({
  defaults: {
    rendered: false
  }
});

ChWiSe.Models.ServerMessage = Backbone.Model.extend({
});

ChWiSe.Views.SearchResultCompound = Backbone.View.extend({

  tagName: "li",

  initialize: function() {
    this.template = _.template($('#compoundresult').html());
  },

  render: function() {
    this.$el.html(this.template( {responseRecord: this.model.attributes} ));
    return this;
  }
});

ChWiSe.Views.ServerMessage = Backbone.View.extend({
  //tagName: "",

  initialize: function() {
    this.template = _.template( $('#servermessage').html() );
  },

  render: function() {
    this.$el.html(this.template( {serverMessage: this.model.attributes} ));
    return this;
  }

});

ChWiSe.Models.SearchResults = Backbone.Collection.extend({

  urlRoot: 'search',

  url: function() {
    // send the url along with the serialized query params
    var result = this.urlRoot;
    var encodedURIfragment = $.param( this.params );
    result += ('?' +  encodedURIfragment);
    return result;
  },

  initialize: function(models, options) {
    options || (options = {});
    this.params = {};
    if (options.query) {
        this.params.q = options.query;
    };
    if (options.structureQuery) {
      this.params.sq = options.structureQuery;
    }
    this.currentResultNumber = 0;
  },

  parse: function(response) {
    //Process error/warnings/messages
    if ( _.isObject(response.messages) ) {
        this.serverMessage = new ChWiSe.Models.ServerMessage(response.messages); //For now we expect the only one msg
	this.trigger("servermessage");
    }

    if ( _.isObject(response.result) ) {
      var result = response.result;
      for (var i = 0; i < result.length; ++i) {
        result[i].resultNumber = ++this.currentResultNumber; 
      }
      return result;
    }
    return [];
  }
});

ChWiSe.Views.SearchResults = Backbone.View.extend({

  model: ChWiSe.Models.SearchResults,

  el: $("#topview"),

  initialize: function() {        
    this.listenTo(this.model, 'add', this.renderNewEntries);
    this.listenTo(this.model, 'reset', this.clear);
    this.listenTo(this.model, 'servermessage', this.renderMessage);

    this.resultlist = $("#results-list");

    this.render();

    //Set up structures editor
    // changes the default JMol color of hydrogen to black so it appears on white backgrounds
    ChemDoodle.ELEMENT['H'].jmolColor = 'black';
    // darkens the default JMol color of sulfur so it appears on white backgrounds
    ChemDoodle.ELEMENT['S'].jmolColor = '#B9A130';
    // initializes the SketcherCanvas
    this.sketcher = new ChemDoodle.SketcherCanvas('sketcher', 500, 300, {useServices:false, oneMolecule:false, includeToolbar:true, includeQuery:false});
    // sets terminal carbon labels to display
    this.sketcher.specs.atoms_displayTerminalCarbonLabels_2D = true;
    // sets atom labels to be colored by JMol colors, which are easy to recognize
    this.sketcher.specs.atoms_useJMOLColors = true;
    // enables overlap clear widths, so that some depth is introduced to overlapping bonds
    this.sketcher.specs.bonds_clearOverlaps_2D = true;
    // sets the shape color to improve contrast when drawing figures
    this.sketcher.specs.shapes_color = 'c10000';
    // because we do not load any content, we need to repaint the sketcher, otherwise we would just see an empty area with the toolbar
    // however, you can instead use one of the Canvas.load... functions to pre-populate the canvas with content, then you don't need to call repaint
    this.sketcher.repaint();

    var self = this;
    this.infiniScroll = new Backbone.InfiniScroll(this.model, {
      success: function() {
        self.infiniScroll.enableFetch();
      },
      untilAttr: "resultNumber",
      param: "from"
    });

  },

  //
  clearMessage: function() {
    $('#server-messages-area').empty();
  },

  //Renders server message
  renderMessage: function() {
    if ( _.isObject( this.model.serverMessage ) ) {
      var messageView = new ChWiSe.Views.ServerMessage({model:this.model.serverMessage });
      $('#server-messages-area').append( messageView.render().el );       
    }
  },

  render: function() {
    this.resultlist.empty();
    for (var i = 0; i < this.model.length; ++i) {
      var itemModel = this.model.at(i);
      var itemView = new ChWiSe.Views.SearchResultCompound({model: itemModel});
      this.resultlist.append( itemView.render().el  );
    }
    //return this;
  },

  clear: function() {
    this.resultlist.html(); //Clean
  },

  events: {
    'click #search-button': 'clickSearch',
    'click .chwise-btn-finishedit': 'finishEdit'
  },

  clickSearch: function() {
    var params = {
	    q:$("#textquery").val().replace(/%([^\d].)/, "%25$1"),
	    sq:$("#structurequery").val().replace(/%([^\d].)/, "%25$1")
    };
    var encodedURIfragment = $.param( params );
    ChWiSe.router.navigate("search?" + encodedURIfragment, {trigger: true} ); 
  },

  finishEdit: function() {
    var mol = this.sketcher.getMolecule();
    var molFile = ChemDoodle.writeMOL(mol);

    //Setup waiting indicator
    var opts = {
        lines: 5, // The number of lines to draw
        length: 20, // The length of each line
        width: 10, // The line thickness
        radius: 30, // The radius of the inner circle
        corners: 1, // Corner roundness (0..1)
        rotate: 0, // The rotation offset
        direction: 1, // 1: clockwise, -1: counterclockwise
        color: '#000', // #rgb or #rrggbb or array of colors
        speed: 1, // Rounds per second
        trail: 60, // Afterglow percentage
        shadow: false, // Whether to render a shadow
        hwaccel: false, // Whether to use hardware acceleration
        className: 'spinner', // The CSS class to assign to the spinner
        zIndex: 2e9, // The z-index (defaults to 2000000000)
        top: '50%', // Top position relative to parent
        left: '50%' // Left position relative to parent
    };

    var spinner = new Spinner(opts);
    //Start wait indicator
    var target = document.getElementById('structEditingArea'); //TODO: Use jQuery-style
    spinner.spin(target);

    //Request server to convert MOL->SMILES
    $.ajax({
        type: "POST",
        url: "convertmol",
        data: {mol:molFile},
    })
    .done( function( serverResponse ) {
      spinner.stop(); //Stop wait indicator if provided
      $('#structurequery').val( serverResponse.smiles );
      $('#structEditor').modal('hide');
    })        // .done( getMolToSmilesConverted(spinner) )
    .fail( function( xmlHttpRequest, statusText, errorThrown ) {  //Conversion error handler
      alert("Error in getting MOL->SMILES conversion result from server!");
    });
  },

  renderNewEntries: function() {
    for (var i = 0; i < this.model.length; ++i) {
      var itemModel = this.model.at(i);        
      if ( !itemModel.attributes.rendered ) {
        itemModel.set('rendered', true);
        itemModel.set('uid', itemModel.cid );
        var compoundView = new ChWiSe.Views.SearchResultCompound({model: itemModel});
        //var t = compoundView.render().el;
        var t = compoundView.render().$el.html();
        this.resultlist.append( t );
        //Initialize ChemDoodle canvas

        var canvasId = "moleculeCanvas" + itemModel.get('uid');

        var viewerCanvas = new ChemDoodle.ViewerCanvas( canvasId, 200, 200 );
        viewerCanvas.specs.bonds_width_2D = .6;
        viewerCanvas.specs.bonds_saturationWidth_2D = .18;
        viewerCanvas.specs.bonds_hashSpacing_2D = 2.5;
        viewerCanvas.specs.atoms_font_size_2D = 10;
        viewerCanvas.specs.atoms_font_families_2D = ["Helvetica", "Arial", "sans-serif"];
        viewerCanvas.specs.atoms_displayTerminalCarbonLabels_2D = true;

        var molFile = itemModel.attributes.mdlmol;
        var molecule = ChemDoodle.readMOL(molFile);
        molecule.scaleToAverageBondLength(14.4);
        viewerCanvas.loadMolecule(molecule);
      }           
    }            
  }
});

ChWiSe.Utils.parseQueryString = function(queryString) {
    var params = {};
    if(queryString){
        _.each(
            _.map(decodeURI(queryString).split(/&/g),function(el,i){
                var aux = el.split('='), o = {};
                if(aux.length >= 1){
                    var val = undefined;
                    if(aux.length == 2)
                        val = aux[1];
                    o[aux[0]] = val;
                }
                return o;
            }),
            function(o){
                _.extend(params,o);
            }
        );
    }
    return params;
}


ChWiSe.Router = Backbone.Router.extend({

  routes: { // sets the routes
    "" : "showWelcomePage", // http://tutorial.com
    "search?*queryString" : "search",
    "deatils/:compound_id" : "details"
  },

  // the same as we did for click events, we now define function for each route
  index: function() { 
    console.log('index route');
  },

  search: function( querystring ) {
    //Parse params
    var querystring = querystring.replace(/\+/g, '%20');
    var params = ChWiSe.Utils.parseQueryString( querystring );
    if (ChWiSe.Models.searchResults) {
      ChWiSe.Models.searchResults.reset();
      ChWiSe.Views.view.render();
    }

    ChWiSe.Models.searchResults.params = params;
    ChWiSe.Models.searchResults.serverMessage = null;
    ChWiSe.Views.view.clearMessage();

    if (params.q) {
      $("#textquery").val(params.q);
    } else {
      $("#textquery").val("");     
    }

    if (params.sq) {
      $("#structurequery").val( params.sq );
    } else {
      $("#structurequery").val("");
    }

    ChWiSe.Models.currentResultNumber = 0;

    //Setup waiting indicator
    var opts = {
        lines: 5, // The number of lines to draw
        length: 20, // The length of each line
        width: 10, // The line thickness
        radius: 30, // The radius of the inner circle
        corners: 1, // Corner roundness (0..1)
        rotate: 0, // The rotation offset
        direction: 1, // 1: clockwise, -1: counterclockwise
        color: '#000', // #rgb or #rrggbb or array of colors
        speed: 1, // Rounds per second
        trail: 60, // Afterglow percentage
        shadow: false, // Whether to render a shadow
        hwaccel: false, // Whether to use hardware acceleration
        className: 'spinner', // The CSS class to assign to the spinner
        zIndex: 2e9, // The z-index (defaults to 2000000000)
        top: '50%', // Top position relative to parent
        left: '50%' // Left position relative to parent
    };

    var spinner = new Spinner(opts);
    //Start wait indicator
    var target = document.getElementById('results-list'); //TODO: Use jQuery-style
    spinner.spin(target);    
    ChWiSe.Models.searchResults.fetch({success: function() {
      spinner.stop();
    }});

    ChWiSe.Views.view.infiniScroll.resetScroll();  
  },

  detail: function( ) {

  }

});


$(document).ready(function () {
  ChWiSe.router = new ChWiSe.Router();
  ChWiSe.Models.searchResults = new ChWiSe.Models.SearchResults();
  ChWiSe.Views.view = new ChWiSe.Views.SearchResults({
    model:ChWiSe.Models.searchResults, 
    el: $("#topview"),
    resultlist: $("#results-list")
  });
  Backbone.history.start();
});
