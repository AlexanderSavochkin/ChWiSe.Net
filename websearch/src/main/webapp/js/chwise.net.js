var ChWiSe = ( function() {

    var chwise = {};

    var numResultsOnPage = 10;
    var maximalNumberOfPages = 10;

    var cacheStruct = {
        currentText:null,
        currentStructure:null,
        numViewableResults:null,
        resultsCache: new Array(),
    };

    var sketcher = null;

    function resetCache(queryStruct) {
        cacheStruct.currentText = queryStruct.textQuery;
        cacheStruct.currentStructure = queryStruct.structQuery;
        cacheStruct.resultsCache.length = 0;
    }

    function isCacheValid(queryStruct) {
        return (cacheStruct.currentText == queryStruct.textQuery) && (cacheStruct.currentStructure == queryStruct.structQuery);
    }

    function isPresentInCache(first, total) {
        for (var i = 0; i < total; ++i) {
            if (typeof cacheStruct.resultsCache[first + i] == 'undefined')
                return false;
        }
        return true;
    }

    function getCachedData(first, total) {
        var result = [];
        for (var i = 0; i < total; ++i)
            result.push( cacheStruct.resultsCache[first + i] );
        return result;
    }

    function rememberDataInCache( searchResultList, first ) {
        for (var i = 0; i < searchResultList.length; ++i)
            cacheStruct.resultsCache[first + i] = searchResultList[i];
    }

    function setResultData( searchResultList, firsrIndex ) {
        var dataForTemplate = { responseRecords:searchResultList };
        var resultsHTML = new EJS({url: 'templates/searchresult.ejs'}).render(dataForTemplate);
        $('#results-list').html( resultsHTML );
        $('#results-list').prop("start", firsrIndex + 1);
        //Draw molecular structure
        for (var i = 0; i < searchResultList.length; ++i) {
            var canvasId = "moleculeCanvas" + i.toString();
            var viewerCanvas = new ChemDoodle.ViewerCanvas( canvasId, 200, 200 );
//        var viewerCanvas = new ChemDoodle.ViewerCanvas( canvasId, '100%', '100%' );
            viewerCanvas.specs.bonds_width_2D = .6;
            viewerCanvas.specs.bonds_saturationWidth_2D = .18;
            viewerCanvas.specs.bonds_hashSpacing_2D = 2.5;
            viewerCanvas.specs.atoms_font_size_2D = 10;
            viewerCanvas.specs.atoms_font_families_2D = ["Helvetica", "Arial", "sans-serif"];
            viewerCanvas.specs.atoms_displayTerminalCarbonLabels_2D = true;
            var molFile = searchResultList[i].mdlmol;
            var molecule = ChemDoodle.readMOL(molFile);
            molecule.scaleToAverageBondLength(14.4);
            viewerCanvas.loadMolecule(molecule);
        }
    }

    chwise.doSearch = function( queryStruct, first ) {
        //Clear errors and warnings alerts
        $('#search-problem-notification').empty();

        //Clear previous results
        $('#results-list').empty();

        if ( isCacheValid(queryStruct) && isPresentInCache(first, numResultsOnPage) ) {
            //Get all data from cache
            setResultData( getCachedData(first, numResultsOnPage), first );
        }
        else {
            resetCache( queryStruct );                
            var textQuery = queryStruct.textQuery;
            var structQuery = queryStruct.structQuery;

            var params = {q:textQuery, sq:structQuery};
            if (first != null)
                params.from =  first;
            if (numResultsOnPage != null)
                params.numShow = numResultsOnPage;

            //Setup wait indicator
            var opts = {
                lines: 5, // The number of lines to draw
                length: 10, // The length of each line
                width: 5, // The line thickness
                radius: 15, // The radius of the inner circle
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

            //Request server
            $.ajax({
                type: "GET",
                url: "search",
                data: params,
            })
            .done( getSearchResultsHandler(first, spinner) )
            .fail( getSearchServerFailed(spinner) );

            //Start wait indicator
            var target = document.getElementById('results-list'); //TODO: Use jQuery-style
            spinner.spin(target);
        }
    }

    function onPageChangedHandle(e, oldPage, newPage) {
        var rangeFrom = (newPage - 1) * numResultsOnPage;
        var numResultsToShow = Math.min( numResultsOnPage, cacheStruct.numViewableResults - rangeFrom );            
        chwise.doSearch( {textQuery: cacheStruct.currentText, structQuery: cacheStruct.currentStructure}, rangeFrom, numResultsToShow );
    }

    function getSearchResultsHandler(from, spinnerWaitIndicator) {
        return function( serverResponse ) {
            if (spinnerWaitIndicator != null) { //Stop wait indicator if provided
                spinnerWaitIndicator.stop();
            }
            var result = serverResponse.result;
            var totalResults = serverResponse.total;
            $('#total-results').html( 'Total results: ' + totalResults );
            cacheStruct.numViewableResults = Math.min( numResultsOnPage * maximalNumberOfPages, totalResults );
            if ( $.isArray( result ) ) {
                rememberDataInCache( result, from )
                setResultData( result, from )

                //Calculate number of pages and adjust paginator
                if ( totalResults > numResultsOnPage ) {
                    var numPages = totalResults / numResultsOnPage + (totalResults % numResultsOnPage ? 1 : 0);
                    var currentPage = from / numResultsOnPage;
                    var options = {
                        bootstrapMajorVersion: 3,
                        numberOfPages: 5,
                        currentPage: currentPage + 1,
                        totalPages: numPages,
                        onPageChanged: onPageChangedHandle
                    };
                    $('#paginator').bootstrapPaginator(options);
                }
                else
                //$("paginator").hide();
                ;

            } else {
                //Show alert
                var failure = serverResponse.failure;

                var resultsHTML = new EJS({url: 'templates/searchfailed.ejs'}).render(failure);
                //$('#search-problem-notification').html( resultsHTML );
                $('#search-problem-notification').append( resultsHTML );

            }
        }
    }

    function getSearchServerFailed ( spinnerWaitIndicator ) {
        return function(xmlHttpRequest, statusText, errorThrown) {
            if (spinnerWaitIndicator != null) { //Stop wait indicator if provided
                spinnerWaitIndicator.stop();
            }
            //Show alert
            var alertParam = {
                severity:"error",
                message:"Unknonw error during compound search. Failed to receive response from server.",
            };
            var resultsHTML = new EJS({url: 'templates/searchfailed.ejs'}).render(alertParam);
            $('#search-problem-notification').html( resultsHTML );
         }
     }


    chwise.onFinishEdit = function () {
        var mol = sketcher.getMolecule();
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

        //Request server to convert MOL->SMILES
        $.ajax({
            type: "POST",
            url: "convertmol",
            data: {mol:molFile},
        })
        .done( getMolToSmilesConverted(spinner) )
        .fail( onChemFormatConversionFailed );
    
        //Start wait indicator
        var target = document.getElementById('structEditingArea'); //TODO: Use jQuery-style
        spinner.spin(target);
    }

    function getMolToSmilesConverted(spinnerWaitIndicator) {
        function onMolToSmilesConverted( serverResponse ) {
            if (spinnerWaitIndicator != null) { //Stop wait indicator if provided
                spinnerWaitIndicator.stop();
            }
            $('#smilesEdit').val( serverResponse.smiles );
            $('#structEditor').modal('hide');
        }
        return onMolToSmilesConverted;
    }

    function onChemFormatConversionFailed ( xmlHttpRequest, statusText, errorThrown ) {
        alert("Error in getting MOL->SMILES conversion result from server!");
    }

    chwise.setSketcher = function(sketcher_) {
        sketcher = sketcher_;
    }

    return chwise;

}()); //End ChWiSe module declaration


//TODO: Use jQuery idioms instead of plain JavaScript since jQuery used everywhere
//Initialize sketcher and assign handlers
window.onload = function() {
    //Set up structures editor
    // changes the default JMol color of hydrogen to black so it appears on white backgrounds
    ChemDoodle.ELEMENT['H'].jmolColor = 'black';
    // darkens the default JMol color of sulfur so it appears on white backgrounds
    ChemDoodle.ELEMENT['S'].jmolColor = '#B9A130';
    // initializes the SketcherCanvas
    var sketcher = new ChemDoodle.SketcherCanvas('sketcher', 500, 300, {oneMolecule:false});
    // sets terminal carbon labels to display
    sketcher.specs.atoms_displayTerminalCarbonLabels_2D = true;
    // sets atom labels to be colored by JMol colors, which are easy to recognize
    sketcher.specs.atoms_useJMOLColors = true;
    // enables overlap clear widths, so that some depth is introduced to overlapping bonds
    sketcher.specs.bonds_clearOverlaps_2D = true;
    // sets the shape color to improve contrast when drawing figures
    sketcher.specs.shapes_color = 'c10000';
    // because we do not load any content, we need to repaint the sketcher, otherwise we would just see an empty area with the toolbar
    // however, you can instead use one of the Canvas.load... functions to pre-populate the canvas with content, then you don't need to call repaint
    sketcher.repaint();

    ChWiSe.setSketcher(sketcher);

    
    //Assign action to search button
    var searchButtons = document.getElementsByClassName("chwise-btn-search");
    for (var i = 0; i < searchButtons.length; ++i) { 
        searchButtons[i].onclick = function () {
            ChWiSe.doSearch({textQuery:this.form.textquery.value, structQuery:this.form.smilesEdit.value}, 0);
        }
    }

    //
    var finishEditButtons = document.getElementsByClassName("chwise-btn-finishedit");
    for (var i = 0; i < finishEditButtons.length; ++i) {
        finishEditButtons[i].onclick = ChWiSe.onFinishEdit;
    }

};
