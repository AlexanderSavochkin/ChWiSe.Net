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

package net.chwise.indexing;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class SimpleFileIndexer extends FileProcessor {

    Map<String, Integer> infoboxKeysCout = new HashMap<String, Integer>();
    boolean calculateStatistics = true;
    String infoboxStatisticsFileName = null;
    PrintWriter allCompoundNamesWriter = null;

    SimpleFileIndexer(IndexWriter indexWriter,
                      boolean calculateStatistics,
                      String infoboxStatisticsFileName,
                      String compoundNamesFileName) throws FileNotFoundException {
        super(indexWriter);
        this.indexWriter = indexWriter;
        this.infoboxStatisticsFileName = infoboxStatisticsFileName;
        this.calculateStatistics = calculateStatistics;
        documentFromWikitextExtractor = new DocumentFromWikitextExtractor();
        allCompoundNamesWriter = new PrintWriter(compoundNamesFileName);
    }


    @Override
    void processDocument(WikiArticle wikiArticle) throws Exception {
        indexWriter.addDocument(wikiArticle.getLuceneDocument());

        if (allCompoundNamesWriter != null)
            allCompoundNamesWriter.println(wikiArticle.title);

        //Update statistics if required
        if (calculateStatistics) {
            for (String key : wikiArticle.infoboxFields.keySet()) {
                int count = infoboxKeysCout.containsKey(key) ? infoboxKeysCout.get(key) : 0;
                infoboxKeysCout.put(key, count + 1);
            }
        }
    }

    @Override
    void finishProcessing() throws Exception {
        if (allCompoundNamesWriter != null) {
            allCompoundNamesWriter.close();
        }
        if (calculateStatistics && infoboxStatisticsFileName != null) {

        }
    }

    public Map<String, Integer> getInfoboxKeysStatistics() {
        return infoboxKeysCout;
    }
}


