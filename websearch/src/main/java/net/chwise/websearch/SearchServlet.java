/**
 Copyright (c) 2013-2015 Alexander Savochkin
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

import net.chwise.common.document.DocDefinitions;
import net.chwise.documents.HighlightedFragmentsRetriever;
import net.chwise.index.ConfigurableDirectorySource;

import net.chwise.websearch.jsonmessages.SearchFailureJSONResponse;
import net.chwise.websearch.jsonmessages.SpellCorrectionsJSONResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.apache.lucene.queryparser.classic.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.openscience.cdk.exception.InvalidSmilesException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.Math;

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
            numShow = Math.min( Integer.parseInt( strNumShow ), 20 );

        int to = from + numShow;

        Integer[] fromTo = {new Integer(from), new Integer(to)};
        LOGGER.log(Level.INFO, "Requested results range: from {0} to {1}", fromTo);

        JSONObject jsonResponse = new JSONObject();
        JSONArray jsonResult = new JSONArray();
        try {
            //Preapre for search
            //Main direcotry
            String directorySourceClassName = getServletConfig().getInitParameter("directorySourceClassName");
            String directorySourceParams = getServletConfig().getInitParameter("directorySourceParams");

            //Speller directory
            boolean isSpellerEnabled = getServletConfig().getInitParameter("spellerEnabled").equals("true");
            String spellerDirectorySourceClassName = getServletConfig().getInitParameter("spellerDirectorySourceClassName");
            String spellerDirectorySourceParams = getServletConfig().getInitParameter("spellerDirectorySourceParams");

            Directory directory = directorySource.getDirectory(directorySourceClassName, directorySourceParams);

            Directory spellerDirectory = null;
            if (isSpellerEnabled) {
                spellerDirectory = directorySource.getDirectory(spellerDirectorySourceClassName, spellerDirectorySourceParams);
            }

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

            to = Math.min(to, hits.length);

            for (int i = from; i < to; ++i) {
                ScoreDoc hit = hits[i];
                Document foundDoc = searcher.doc(hit.doc);

                JSONObject jsonDoc = extractJSON(query, analyzer, highlighter, foundDoc);
                jsonResult.put(jsonDoc);
            }

            jsonResponse.put( "result", jsonResult );
            jsonResponse.put( "total", totalResults );

            //Suggest spell corrections
            //TODO: Parse only textual part (no structure)
            if (isSpellerEnabled) {
                Set<Term> terms = new HashSet<Term>();
                query.extractTerms(terms);
                Map<String, String> fixes = new HashMap<String, String>();

                terms.retainAll( Arrays.asList(DocDefinitions.getSpellerDictionaryFields()) );

                SpellChecker spellChecker = new SpellChecker(spellerDirectory);
                for (Term term : terms) {
                    if (reader.totalTermFreq(term) == 0) {
                        String[] similarWords = spellChecker.suggestSimilar(term.text(), 1, 0.8f);
                        fixes.put(term.text(), similarWords[0]);
                    }
                }

                //Wrap fixes to JSON response
                JSONObject spellCorrectionMessage = SpellCorrectionsJSONResponse.create(fixes);
                jsonResponse.put( "messages",  spellCorrectionMessage );
            }
        }
        catch (ParseException e) {
            JSONObject jsonFailure = SearchFailureJSONResponse.create("info", "We couldn't understand query", "Use quotes for phrase search. Use AND,OR,NOT for boolean search");
            try {
                jsonResponse.put( "messages",  jsonFailure );
            } catch (JSONException e1) {
                e1.printStackTrace();
                throw new RuntimeException(e1);
            }
        }
        catch (RuntimeException e) {
            if (e.getCause() instanceof InvalidSmilesException) {
                JSONObject jsonFailure = SearchFailureJSONResponse.create("info", "We couldn't understand query", "Your structure formula doesn't seem like correct SMILES. Use structure editor for generating correct SMILES structures");
                try {
                    jsonResponse.put( "messages",  jsonFailure );
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    throw new RuntimeException(e1);
                }
            } else {
                e.printStackTrace();
                throw e;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException( "Exception in servlet SearchServlet", e );
        }

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        out.print( jsonResponse );
        out.flush();
    }

    private JSONObject extractJSON(Query query, Analyzer analyzer, HighlightedFragmentsRetriever highlighter, Document foundDoc) throws IOException, InvalidTokenOffsetsException, JSONException {
        JSONObject jsonDoc = new JSONObject();

        String title = foundDoc.getField(TITLE_FIELD_NAME).stringValue();
        String url = "#";
        String textFragment = foundDoc.getField( TEXT_FIELD_NAME ).stringValue();
        String smiles = foundDoc.getField(STRUCTURE_SMILES_FIELD_NAME).stringValue();
        String mdlmol = foundDoc.getField(STRUCTURE_MOL_FIELD_NAME).stringValue();

        IndexableField f = foundDoc.getField(CAS_NO);
        String casno = f == null ? null : f.stringValue();
        f = foundDoc.getField(PUBCHEM_ID);
        String pubChemId = f == null ? null : f.stringValue();
        f = foundDoc.getField(CHEMSPIDER);
        String chemSpiderId = f == null ? null : f.stringValue();
        f = foundDoc.getField(CHEBI);
        String chebi = f == null ? null : f.stringValue();

        JSONArray jsonSynonymsArray = new JSONArray();
        IndexableField[] synonymFields = foundDoc.getFields(SYNONYM_FIELD_NAME);
        for (IndexableField field: synonymFields) {
            String synonym = field.stringValue();
            jsonSynonymsArray.put(synonym);
        }

        //Highlight and fragment text
        String[] documentTextFragments = highlighter.getFragmentsWithHighlightedTerms(analyzer, query,
                TEXT_FIELD_NAME, textFragment, 3, 200);

        String textFragmentsJoined = StringUtils.join(documentTextFragments, " ... ");

        jsonDoc.put( "title", title );
        jsonDoc.put( "textFragment", textFragmentsJoined );
        jsonDoc.put( "url", url );
        jsonDoc.put( "smiles", smiles );
        jsonDoc.put( "mdlmol", mdlmol );
        jsonDoc.put( "synonyms", jsonSynonymsArray );

        JSONObject externalIdsDictionary = new JSONObject();
        if ( casno != null )
            externalIdsDictionary.put("cas", casno);
        if ( pubChemId != null )
            externalIdsDictionary.put("pubchem", pubChemId);
        if ( chemSpiderId != null )
            externalIdsDictionary.put("chemspider",chemSpiderId);
        if ( chebi != null )
            externalIdsDictionary.put("chebi",chebi);

        jsonDoc.put( "externalrefs", externalIdsDictionary );

        return jsonDoc;
    }

    boolean isQuery( String query ) {
        //Check that query contains alphanumeric character(s)
        if ( query.matches(".*\\w.*") )
            return true;
        return false;
    }

}
