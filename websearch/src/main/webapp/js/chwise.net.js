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
  url: '/search'
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

  fetch: function() { 
    this.model.fetch({ add: true, remove: false, merge: false, data: {
      q: this.model.textQuery,
      sq: this.model.structQuery,
      from: this.model.length, 
      numShow: ChWiSe.Constants.numResultsToFetch } 
    });
  },

  render: function() {
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
    console.log('Clicksearch!');
  },

  renderNewEntries: function() {   
    for (var i = 0; i < this.model.length; ++i) {
      var itemModel = this.model.at(i);        
      if ( !itemModel.attributes.rendered ) {
        itemModel.set('rendered', true);
        var compoundView = new ChWiSe.Views.SearchResultCompound({model: itemModel});
        //var t = compoundView.render().el;
        var t = compoundView.render().$el.html();
        this.resultlist.append( t );
        //Initialize ChemDoodle canvas

        var canvasId = "moleculeCanvas" + itemModel.id;

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
    ChWiSe.Models.searchResults = new ChWiSe.Models.SearchResults({query: query});
    ChWiSe.Models.searchResults.fetch();   
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

  //Mock-up logic
  _.each( simulatedData.result, function(x) { ChWiSe.Models.searchResults.add(x);} );
});


//-----------------------------------------
var simulatedData = {
   "total":172,
   "result":[
      {
         "id" : "cdv",
         "title":"Indole",
         "mdlmol":"\n  CDK     0131151745\n\n  9 10  0  0  0  0  0  0  0  0999 V2000\n    0.0000    1.5000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.4266    1.9635    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    1.2990    2.2500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.4266   -0.4635    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    1.2990   -0.7500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -2.3083    0.7500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    2.5981    1.5000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    2.5981   -0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n  2  1  4  0  0  0  0 \n  3  1  4  0  0  0  0 \n  4  1  4  0  0  0  0 \n  5  2  4  0  0  0  0 \n  6  2  4  0  0  0  0 \n  3  7  4  0  0  0  0 \n  4  8  4  0  0  0  0 \n  7  5  4  0  0  0  0 \n  9  6  4  0  0  0  0 \n  8  9  4  0  0  0  0 \nM  END",
         "synonyms":[
            "Indole",
            "Indole",
            "2,3-Benzopyrrole, ketole,<br />1-benzazole"
         ],
         "externalrefs":{
            "cas":"120-72-9",
            "chebi":"16881",
            "pubchem":"798"
         },
         "smiles":"C12=C(C=CN2)C=CC=C1",
         "textFragment":"**<B>Indole<\/B>** is an aromatic heterocyclic organic compound. It has a bicyclic\nstructure, consisting of a six-membered benzene ring fused to a five-membered\nnitrogen-containing pyrrole ring. <B>Indole<\/B> is a common component of fragrances\nand the precursor to many pharmaceuticals. Compounds that contain an <B>indole<\/B>\nring are called indoles. The amino acid tryptophan is an <B>indole<\/B> derivative and\nthe precursor of the neurotransmitter serotonin.\n\n1. General properties and occurrence\n------------------------------------\n\n<B>Indole<\/B> is a solid at room temperature. <B>Indole<\/B> can be produced by bacteria",
         "url":"#"
      },
      {
         "id" : "bl1",        
         "title":"Indole-5,6-quinone",
         "mdlmol":"\n  CDK     0131151755\n\n 11 12  0  0  0  0  0  0  0  0999 V2000\n    0.0000    1.5000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.4266    1.9635    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.4266   -0.4635    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n   -2.0367    3.3338    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -2.3083    0.7500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -3.5285    3.4906    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -3.8000    0.9068    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -4.4102    2.2771    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -4.1386    4.8610    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n   -5.9019    2.4339    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n  2  1  4  0  0  0  0 \n  3  1  4  0  0  0  0 \n  4  2  4  0  0  0  0 \n  3  5  2  0  0  0  0 \n  3  6  4  0  0  0  0 \n  6  4  4  0  0  0  0 \n  5  7  1  0  0  0  0 \n  8  6  2  0  0  0  0 \n  7  9  1  0  0  0  0 \n 10  7  2  0  0  0  0 \n  9  8  1  0  0  0  0 \n 11  9  2  0  0  0  0 \nM  END",
         "synonyms":[
            "Indole-5,6-quinone",
            "1H-indole-5,6-dione"
         ],
         "externalrefs":{
            "pubchem":"440728"
         },
         "smiles":"C1=CNC2=CC(=O)C(=O)C=C21",
         "textFragment":"**<B>Indole<\/B>-5,6-quinone** is an indolequinone, a chemical compound found in the\noxidative browning reaction of fruits like bananas where it is mediated by the\ntyrosinase type polyphenol oxidase from tyrosine and catecholamines leading to\nthe formation of catechol melanin.\n\n1. References\n-------------",
         "url":"#"
      },
      {
         "id" : "tvt",        
         "title":"Indole-3-carbinol",
         "mdlmol":"\n  CDK     0131151743\n\n 11 12  0  0  0  0  0  0  0  0999 V2000\n    1.0567    3.8735    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n    1.2135    2.3817    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    1.5000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.4266    1.9635    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    1.1147   -1.0037    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.4266   -0.4635    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -2.3083    0.7500    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    0.8028   -2.4709    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.7385   -1.9307    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.6237   -2.9344    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n  2  1  1  0  0  0  0 \n  3  2  1  0  0  0  0 \n  4  3  1  0  0  0  0 \n  5  3  1  0  0  0  0 \n  6  4  4  0  0  0  0 \n  7  4  4  0  0  0  0 \n  5  8  1  0  0  0  0 \n  9  6  4  0  0  0  0 \n  7 10  4  0  0  0  0 \n  8  7  1  0  0  0  0 \n 11  9  4  0  0  0  0 \n 10 11  4  0  0  0  0 \nM  END",
         "synonyms":[
            "1''H''-Indol-3-ylmethanol",
            "Indole-3-carbinol; 3-Indolylcarbinol; 1''H''-Indole-3-methanol; 3-Hydroxymethylindole; 3-Indolemethanol; Indole-3-methanol; I3C"
         ],
         "externalrefs":{
            "cas":"700-06-1",
            "chebi":"24814",
            "pubchem":"3712"
         },
         "smiles":"OCc2c1ccccc1nc2",
         "textFragment":"**<B>Indole<\/B>-3-carbinol** (C9H9NO) is produced by the breakdown of the\nglucosinolate glucobrassicin, which can be found at relatively high levels in\ncruciferous vegetables such as broccoli, cabbage, cauliflower, brussels sprouts\n, collard greens and kale. I3C is also available in a dietary supplement.\n<B>Indole<\/B>-3-carbinol is the subject of on-going Biomedical research into its\npossible anticarcinogenic, antioxidant, and anti-atherogenic effects. Research\non <B>indole<\/B>-3-carbinol has been conducted primarily using laboratory animals and\ncultured cells. Limited and inconclusive human studies have been",
         "url":"#"
      }
   ]
};
