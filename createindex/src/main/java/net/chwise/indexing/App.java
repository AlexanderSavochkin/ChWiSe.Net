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

import net.chwise.common.document.DocDefinitions;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class App 
{
    private static void printUsage() {
        System.err.println("Usage: java -jar createindex.jar <Path to files> <Path to index> [--infoboxstatistics <Path to infobox statistics>] [--printnameslist <Path to compounds list names>]");
    }

    static void writeInfoboxesStatToFile(String filename, Map<String, Integer> stat) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename))) ) {
            for (Map.Entry<String, Integer> entry : stat.entrySet()) {
                writer.format("%s %d\n", entry.getKey(), entry.getValue() );
            }
        }
    }

    public static void main( String[] args ) throws IOException {
        if (args.length < 2 || args.length > 6) {
            printUsage();
            return;
        }

        String startFrom = args[0];
        String indexPath = args[1];

        String compoundsListFileName = null;
        String infoboxStatisticsFileName = null;
        String spellerIndexPath = null;

        for (int i = 2; i < args.length - 1;) {
            if ("--printnameslist".equals(args[i])) {
                compoundsListFileName = args[i + 1];
                i += 2;
                continue;
            }

            if ("--infoboxstatistics".equals(args[i])) {
                infoboxStatisticsFileName = args[i + 1];
                i += 2;
                continue;
            }

            if ("--speller".equals(args[i])) {
                spellerIndexPath = args[i + 1];
                i += 2;
                continue;
            }
        }


        System.err.println("Starting rimary index creation...");
        Directory directory = new SimpleFSDirectory( new File(indexPath) );
        Analyzer analyzer = DocDefinitions.getAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, analyzer);
        IndexWriter indexWriter = new IndexWriter( directory, config);
        SimpleFileIndexer fileIndexer = new SimpleFileIndexer( indexWriter, compoundsListFileName );
        Files.walkFileTree(Paths.get(startFrom), fileIndexer);
        indexWriter.close();
        if (infoboxStatisticsFileName != null)
            writeInfoboxesStatToFile(args[2], fileIndexer.getInfoboxKeysStatistics());
        System.err.println("Done!");

        if (spellerIndexPath != null) {
            System.err.println("Starting spell checking index...");
            Directory spellerDirectory = new SimpleFSDirectory(new File(spellerIndexPath));

            SpellChecker spell= new SpellChecker(spellerDirectory);
            IndexReader primaryIndexReader = DirectoryReader.open(directory);

            for (String spellField: DocDefinitions.getSpellerDictionaryFields()) {
                Dictionary dict = new LuceneDictionary(primaryIndexReader, spellField);
                spell.indexDictionary(dict, config, true );
            }

            System.err.println("Done!");
        }
    }
}
