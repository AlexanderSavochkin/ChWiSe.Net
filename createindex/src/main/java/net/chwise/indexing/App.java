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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

    public static void main( String[] args ) throws Exception {
        if (args.length < 2 || args.length > 6) {
            printUsage();
            return;
        }

        String startFrom = args[0];
        String indexPath = args[1];

        String compoundsListFileName = null;
        String infoboxStatisticsFileName = null;
        String spellerIndexPath = args[2];//null;

        boolean dumpStatisiticsToFile = false;
        Connection sqlConnection = null;
        FileProcessor fileProcessor = null;


        try {
            Directory directory = new SimpleFSDirectory(new File(indexPath));
            Analyzer analyzer = DocDefinitions.getAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, analyzer);
            IndexWriter indexWriter = new IndexWriter(directory, config);
            for (int i = 3; i < args.length - 1;) {
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

                if ("--analytics".equals(args[i]) && fileProcessor == null) {
                    if ("file".equals(args[i + 1]) ) {
                        dumpStatisiticsToFile = true;
                        i += 2;
                        continue;
                    }
                    else if ("db".equals(args[i + 1]) ) {
                        sqlConnection = DriverManager.getConnection("jdbc:postgresql://10.0.2.2/chwise", "postgres", "pspGs123");
                        sqlConnection.setAutoCommit(false);
                        fileProcessor = new DBAnalyticsFileIndexer(indexWriter, sqlConnection, true);
                        i += 2;
                        continue;
                    }
                    //Wrong parameters
                    printUsage();
                    System.exit(1);
                }
                //Wrong parameters
                printUsage();
                System.exit(1);
            }
            if (fileProcessor == null) {
                fileProcessor = new SimpleFileIndexer(indexWriter, dumpStatisiticsToFile, infoboxStatisticsFileName, compoundsListFileName );
            }
            System.err.println("Starting primary index creation...");

            Files.walkFileTree(Paths.get(startFrom), fileProcessor);
            indexWriter.close();

            System.err.println("Done!");

            if (spellerIndexPath != null) {
                System.err.println("Starting spell checking index...");
                Directory spellerDirectory = new SimpleFSDirectory(new File(spellerIndexPath));

                SpellChecker spell = new SpellChecker(spellerDirectory);
                IndexReader primaryIndexReader = DirectoryReader.open(directory);

                for (String spellField : DocDefinitions.getSpellerDictionaryFields()) {
                    Dictionary dict = new LuceneDictionary(primaryIndexReader, spellField);
                    spell.indexDictionary(dict, config, true);
                }

                System.err.println("Done!");
            }
        }
        finally {
            fileProcessor.finishProcessing();
        }
    }
}