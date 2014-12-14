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

package net.chwise.documents;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.*;

import java.io.IOException;

//See
//http://hrycan.com/2009/10/25/lucene-highlighter-howto/
// and
//http://code.google.com/p/hrycan-blog/source/browse/trunk/lucene-highlight/src/com/hrycan/search/HighlighterUtil.java


public class HighlightedFragmentsRetriever {


    public String[] getFragmentsWithHighlightedTerms(Analyzer analyzer, Query query,
                        String fieldName, String fieldContents, int fragmentNumber, int fragmentSize) throws IOException, InvalidTokenOffsetsException {

                TokenStream stream = TokenSources.getTokenStream(fieldName, fieldContents, analyzer);
                QueryScorer scorer = new QueryScorer(query, fieldName);
                Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, fragmentSize);
                
                Highlighter highlighter = new Highlighter(scorer);
                highlighter.setTextFragmenter(fragmenter);
                highlighter.setMaxDocCharsToAnalyze(Integer.MAX_VALUE);
                        
                String[] fragments = highlighter.getBestFragments(stream, fieldContents, fragmentNumber);

                if (fragments.length == 0) {
                    //Return starting piece of fieldContents fragment
                    fragments = new String[1];
                    fragments[0] = fieldContents.substr(0, fragmentSize);
                }
                        
                return fragments;
        }

}
