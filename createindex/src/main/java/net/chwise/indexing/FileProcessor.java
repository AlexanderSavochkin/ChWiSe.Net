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

import net.chwise.dataextraction.SimpleFieldToFieldProcessor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

public abstract class FileProcessor extends SimpleFileVisitor<Path> {
    IndexWriter indexWriter;
    DocumentFromWikitextExtractor documentFromWikitextExtractor;PrintWriter allCompoundNamesWriter = null;

    public FileProcessor(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
        documentFromWikitextExtractor = new DocumentFromWikitextExtractor();
    }

    public void close() {
        if (allCompoundNamesWriter != null)
            allCompoundNamesWriter.close();
    }

    @Override
    public FileVisitResult visitFile(
            Path aFile, BasicFileAttributes aAttrs
    )  {
        System.err.println("Processing file: " + aFile.toString() );
        try {
            WikiArticle wikiArticle = readFile( aFile );
            processDocument(wikiArticle);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return FileVisitResult.CONTINUE;
    }

    abstract void processDocument(WikiArticle wikiArticle) throws Exception;

    @Override
    public FileVisitResult preVisitDirectory(
            Path aDir, BasicFileAttributes aAttrs
    ) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    WikiArticle readFile( Path path ) throws IOException, LinkTargetException, EngineException {
        SimpleFieldToFieldProcessor simpleFieldToFieldProcessor = new SimpleFieldToFieldProcessor();
        String pathStr = path.toString();
        try (BufferedReader br = new BufferedReader(new FileReader( pathStr )))  {
            //First line is compound name
            String title = br.readLine();

            if (allCompoundNamesWriter != null)
                allCompoundNamesWriter.println(title);

            //Second line is smiles
            String smiles = br.readLine();

            //Rest of the file is our content, or "text" field
            StringBuffer sb = new StringBuffer();
            String line = br.readLine();
            while (line != null) {
                sb.append( line );
                sb.append('\n');
                line = br.readLine();
            }
            String wikitext = sb.toString();
            return documentFromWikitextExtractor.getArticle(simpleFieldToFieldProcessor, pathStr, title, smiles, wikitext);
        }
    }
}
