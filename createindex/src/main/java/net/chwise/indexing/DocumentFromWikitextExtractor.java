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

package net.chwise.indexing;

import net.chwise.dataextraction.InfoBoxDataExtractor;
import net.chwise.dataextraction.InfoboxDataProcessor;
import net.chwise.dataextraction.SimpleFieldToFieldProcessor;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.util.Map;

public class DocumentFromWikitextExtractor {

    // Set-up a simple wiki configuration
    WikiConfig config = DefaultConfigEnWp.generate();
    final int wrapCol = 80;
    // Instantiate a compiler for wiki pages
    WtEngineImpl engine = new WtEngineImpl(config);
    //InfoboxDataProcessor infoboxDataProcessor = new SimpleFieldToFieldProcessor();


    public DocumentFromWikitextExtractor() {}

    public WikiArticle getArticle(InfoboxDataProcessor infoboxDataProcessor,
                                  String pathStr,
                                  String strPageId,
                                  String title,
                                  String smiles,
                                  String wikitext) throws LinkTargetException, EngineException {
        // Retrieve a page
        PageTitle pageTitle = PageTitle.make(config, pathStr);

        PageId pageId = new PageId(pageTitle, -1);

        // Compile the retrieved page
        EngProcessedPage cp = engine.postprocess(pageId, wikitext, null);

        //Check chembox in the begining

        TextConverter markupStripConverter = new TextConverter(config, wrapCol);
        String text = (String) markupStripConverter.go(cp.getPage());

        InfoBoxDataExtractor infoBoxDataExtractor = new InfoBoxDataExtractor();
        Map<String, String> infoboxFields = (Map<String, String>) infoBoxDataExtractor.go(cp.getPage());

        WikiArticle wikiArticle = new WikiArticle(strPageId, title, text, smiles, infoboxFields, infoboxDataProcessor);
        return wikiArticle;
    }
}
