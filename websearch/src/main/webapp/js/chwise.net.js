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

ChWiSe.Models.SearchResultCompoundModel = Backbone.Model.extend({
  defaults: {
    rendered: false
  }
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

ChWiSe.Models.SearchResults = Backbone.Collection.extend({

  urlRoot: 'search',

  url: function() {
    // send the url along with the serialized query params
    var result = this.urlRoot; 
    var firstParam = true;
    if (this.query) {
      firstParam = false;
      result += ("?" + "q=" + this.query);      
    }
    if (this.structureQuery) {
      result += firstParam ? "?" : "&";
      result += "sq=" + this.structureQuery;
    }
    return result;
  },

  initialize: function(models, options) {
    options || (options = {});
    if (options.query) {
        this.query = options.query;
    };
    if (options.structureQuery) {
      this.structureQuery = options.structureQuery;
    }
  },

  parse: function(response) {
console.log("Parse:")
console.log( response );
    return response.result;
  }
});

ChWiSe.Views.SearchResults = Backbone.View.extend({

  model: ChWiSe.Models.SearchResults,

  el: $("#topview"),

  initialize: function() {        
    this.listenTo(this.model, 'add', this.renderNewEntries);
    this.listenTo(this.model, 'reset', this.clear);
    this.resultlist = $("#results-list");

    this.render();

    //Set up structures editor
    // changes the default JMol color of hydrogen to black so it appears on white backgrounds
    ChemDoodle.ELEMENT['H'].jmolColor = 'black';
    // darkens the default JMol color of sulfur so it appears on white backgrounds
    ChemDoodle.ELEMENT['S'].jmolColor = '#B9A130';
    // initializes the SketcherCanvas
    this.sketcher = new ChemDoodle.SketcherCanvas('sketcher', 500, 300, {oneMolecule:false});
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
    'scroll': 'checkScroll',
    'click #search-button': 'clickSearch'      
  },

  checkScroll: function () {
    if( !this.isLoading && this.el.scrollTop + this.el.clientHeight + ChWiSe.Constants.triggerPoint > this.el.scrollHeight ) {
      this.twittermodel.page += 1; // Load next page
      this.loadResults();
    }
  },

  clickSearch: function() {
    var q = $("#textquery").val();
    var sq = $("#smilesEdit").val();
console.log('Clicksearch! q=' + q + ", sq = " + sq);
console.log(this)
    ChWiSe.router.navigate("search?q=" + q + "&sq=" + sq, true); 
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
console.log("render new entry: uid = " + canvasId)        

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
    console.log('search route with query string: ' + querystring);
    //Parse params
    var params = ChWiSe.Utils.parseQueryString( querystring );
    if (ChWiSe.Models.searchResults) {
      ChWiSe.Models.searchResults.reset();
      ChWiSe.Views.view.render();
    }
    //ChWiSe.Models.searchResults = new ChWiSe.Models.SearchResults([], {query: params.q, structureQuery: params.sq});
    if (params.q) {
      ChWiSe.Models.searchResults.query = params.q;
    }
    if (params.sq) {
      ChWiSe.Models.searchResults.structureQuery = params.sq;    
    }
console.log("New model")        
console.log(ChWiSe.Models.searchResults)    
    ChWiSe.Models.searchResults.fetch({success: function(){
console.log("On success fetch result:")
console.log(ChWiSe.Models.searchResults); // => 2 (collection have been populated)
    }});   
  },

  detail: function( ) {

  }

});


$(document).ready(function () {
  ChWiSe.router = new ChWiSe.Router();
  Backbone.history.start();
  ChWiSe.Models.searchResults = new ChWiSe.Models.SearchResults();
  ChWiSe.Views.view = new ChWiSe.Views.SearchResults({
    model:ChWiSe.Models.searchResults, 
    el: $("#topview"),
    resultlist: $("#results-list")
  });
});
