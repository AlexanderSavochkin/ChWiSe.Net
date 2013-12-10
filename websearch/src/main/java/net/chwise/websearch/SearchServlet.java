/**
 Copyright (c) 2013 Alexander Savochkin
 Chemical wikipedia search (chwise.net) web-site source code

 This file is part of ChWiSe.Net infrastructure.

 ChWiSe.Net infrastructure is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.chwise.websearch;

import net.chwise.documents.HighlightedFragmentsRetriever;
import net.chwise.index.ConfigurableDirectorySource;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.chwise.common.document.DocDefinitions.*;

public class SearchServlet extends HttpServlet {

    private final static Logger LOGGER = Logger.getLogger(SearchServlet.class.getName());

    private ConfigurableDirectorySource directorySource = new ConfigurableDirectorySource();


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String queryText = req.getParameter("q");
        if ( queryText == null )
            queryText = "";
        String[] smilesQueriesString = req.getParameterValues("sq");

        //Join text query with structures query
        StringBuffer sb = new StringBuffer( );
        boolean nonEmptyQuery = isQuery( queryText );
        if ( nonEmptyQuery )
            sb.append( queryText );

        if (smilesQueriesString != null) {
            for (String structSmiles: smilesQueriesString) {

                if (!isQuery( structSmiles ) )
                    continue;

                String escapedSmiles = QueryParser.escape(structSmiles);

                if (nonEmptyQuery) {
                    sb.append(" AND ");
                }

                sb.append(" smiles:");
                sb.append(escapedSmiles);
                nonEmptyQuery = true;
            }
        }

        String joinedTextChemicalQuery = sb.toString();

        LOGGER.log(Level.INFO, "Query: {0}", joinedTextChemicalQuery );

        int from = 0;
        int numShow = 10;

        String strFrom = req.getParameter("from");
        String strNumShow = req.getParameter("numShow");

        if ( strFrom != null )
            from = Integer.parseInt( strFrom );

        if ( strNumShow != null )
            numShow = java.lang.Math.min( Integer.parseInt( strNumShow ), 20 );

        int to = from + numShow;

        JSONObject jsonResponse = new JSONObject();
        JSONArray jsonResult = new JSONArray();
        try {
            //Preapre for search
            String directorySourceClassName = getServletConfig().getInitParameter("directorySourceClassName");
            String directorySourceParams = getServletConfig().getInitParameter("directorySourceParams");
            Directory directory = directorySource.getDirectory(directorySourceClassName, directorySourceParams);

            IndexReader reader = null;

            reader = IndexReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);

            //Perform query
            Query query = null;
            Analyzer analyzer = getAnalyzer();
            query = new MultiFieldQueryParser(Version.LUCENE_43, getTextFields(), analyzer, getFieldWeights() )
                    .parse(joinedTextChemicalQuery);

            TopScoreDocCollector collector = TopScoreDocCollector.create( to, true); //TODO: use from, to
            searcher.search(query, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            int totalResults = collector.getTotalHits();

            LOGGER.log(Level.INFO, "Found {0} documents", hits.length );

            //Wrap results into json object
            HighlightedFragmentsRetriever highlighter = new HighlightedFragmentsRetriever();

            for (int i = from; i < to; ++i) {
                ScoreDoc hit = hits[i];
                Document foundDoc = searcher.doc(hit.doc);

                JSONObject jsonDoc = new JSONObject();

                String title = foundDoc.getField(TITLE_FIELD_NAME).stringValue();
                String url = "#";
                String textFragment = foundDoc.getField( TEXT_FIELD_NAME ).stringValue();
                String smiles = foundDoc.getField(STRUCTURE_SMILES_FIELD_NAME).stringValue();
                String mdlmol = foundDoc.getField(STRUCTURE_MOL_FIELD_NAME).stringValue();


                //Highlight and fragment text
                String[] documentTextFragments = highlighter.getFragmentsWithHighlightedTerms(analyzer, query,
                        TEXT_FIELD_NAME, textFragment, 3, 200);

                String textFragmentsJoined = StringUtils.join(documentTextFragments, " ... ");

                jsonDoc.put( "title", title );
                jsonDoc.put( "textFragment", textFragmentsJoined );
                jsonDoc.put( "url", url );
                jsonDoc.put( "smiles", smiles );
                jsonDoc.put( "mdlmol", mdlmol );

                jsonResult.put(jsonDoc);
            }

            jsonResponse.put( "result", jsonResult );
            jsonResponse.put( "total", totalResults );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException( "Exception in servlet SearchServlet", e );
        }

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        out.print( jsonResponse );
        out.flush();
    }

    boolean isQuery( String query ) {
        //Check that query contains alphanumeric character(s)
        if ( query.matches(".*\\w.*") )
            return true;
        return false;
    }

}
