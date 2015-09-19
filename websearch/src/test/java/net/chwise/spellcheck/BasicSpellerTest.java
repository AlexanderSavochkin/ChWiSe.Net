/**
 * Copyright (c) 2015 Alexander Savochkin
 * Chemical wikipedia search (chwise.net) web-site source code
 * <p>
 * This file is part of ChWiSe.Net infrastructure.
 * <p>
 * ChWiSe.Net infrastructure is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.chwise.spellcheck;

import net.chwise.common.document.DocDefinitions;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class BasicSpellerTest {

    private final String TITLE_FIELD = "title";
    private final String BODY_FIELD = "body";

    private final String MISSPELED_QUERY = "etanol";

    private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, analyzer);

    private Directory createTestDoxIndex() {
        Directory directory = null;
        IndexWriter indexWriter = null;
        try {
            directory = new RAMDirectory();
            indexWriter = new IndexWriter(directory, config);

            //Add doc1
            Document doc1 = new Document();
            doc1.add( new TextField( TITLE_FIELD, "ethanol", Field.Store.YES ) );
            doc1.add( new TextField( BODY_FIELD, "ethanol is alcohol bla-bla", Field.Store.YES ) );

            indexWriter.addDocument(doc1);

            //Add doc2
            Document doc2 = new Document();
            doc2.add( new TextField( TITLE_FIELD, "methane", Field.Store.YES ) );
            doc2.add( new TextField( BODY_FIELD, "methane is a flamable gas", Field.Store.YES ) );

            indexWriter.addDocument(doc2);

        } catch (IOException e) {
            throw new RuntimeException("Exception during creating index.", e);
        } finally {
            if (indexWriter != null)
                try {
                    indexWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException("Exception during closing index.", e);
                }
        }
        return directory;
    }

    @Test
    public void testGetCorrections() throws Exception {
        Directory docIndexDirectory = createTestDoxIndex();
        IndexReader indexReader = IndexReader.open(docIndexDirectory);

        //Create speller index
        Directory spellerDirectory = new RAMDirectory();

        SpellChecker spell = new SpellChecker(spellerDirectory);
        IndexReader primaryIndexReader = DirectoryReader.open(docIndexDirectory);

        for (String spellField : new String[]{"title"}) {
            Dictionary dict = new LuceneDictionary(primaryIndexReader, spellField);
            spell.indexDictionary(dict, config, true);
        }


        //Test speller index
        Query q = new QueryParser(Version.LUCENE_43, TITLE_FIELD, analyzer).parse(MISSPELED_QUERY);
        Speller speller = new BasicSpeller(spellerDirectory, indexReader);
        Map<String,String> spellerResults = speller.getCorrections(q);

        //One correction expected
        assert(spellerResults.size() == 1);

        //It should be ethanol
        assert (spellerResults.containsKey(MISSPELED_QUERY));

        String correctedWord = spellerResults.get(MISSPELED_QUERY);
        assert (correctedWord.equals("ethanol"));
    }
}