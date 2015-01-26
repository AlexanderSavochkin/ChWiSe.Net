/**
 Copyright (c) 2015 Alexander Savochkin
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

package net.chwise.dataextraction;

import net.chwise.common.document.DocDefinitions;
import net.chwise.indexing.FileIndexer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

import static net.chwise.test.TestData.*;

public class InfoBoxDataExtractorTest {


    private boolean testArticleAndValue(String article, String[][] infoboxValues)
            throws EngineException, LinkTargetException {
        WikiConfig config = DefaultConfigEnWp.generate();

        // Instantiate a compiler for wiki pages
        WtEngineImpl engine = new WtEngineImpl(config);

        // Retrieve a page
        PageTitle pageTitle = PageTitle.make(config, "manzanate");

        PageId pageId = new PageId(pageTitle, -1);

        // Compile the retrieved page
        EngProcessedPage cp = engine.postprocess(pageId, article, null);

        InfoBoxDataExtractor p = new InfoBoxDataExtractor();
        Map<String, String> infoBoxFields = (Map<String, String>) p.go(cp.getPage());

        for (String[] expectedInfoboxKeyValue : infoboxValues) {
            String key = expectedInfoboxKeyValue[0];
            String val = expectedInfoboxKeyValue[1];
            if( !infoBoxFields.containsKey(key) )
                return false;
            String actualValue = infoBoxFields.get(key);
            if( !val.equals(actualValue) )
                return false;
        }
        return true;
    }

    @Test
    public void testManzanateChemBoxExtraction() throws LinkTargetException, EngineException {
        assertTrue( testArticleAndValue(MANZANATE_ARTICLE, manzanateExpectedInfoboxValues) );
    }

    @Test
    public void testNedazoinChemBoxExtraction() throws LinkTargetException, EngineException {
        assertTrue( testArticleAndValue(NELDAZOSIN_ARTICLE, nedazosinExpectedInfoboxValues) );
    }

}