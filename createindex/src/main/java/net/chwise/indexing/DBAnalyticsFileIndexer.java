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

/*
This file processor put compound articles to Lucene index along with storeing information
in Postgres database table for future analysis
*/

package net.chwise.indexing;

import org.apache.lucene.index.IndexWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DBAnalyticsFileIndexer extends FileProcessor {

    Connection connention;
    PreparedStatement insertCompoundStatement = null;
    PreparedStatement insertInfoboxStatement = null;
    PreparedStatement setStrippedWikiTextStatement = null;
    boolean ownConnection;

    public DBAnalyticsFileIndexer(IndexWriter indexWriter, Connection connection, boolean ownConnection) {
        super(indexWriter);
        this.connention = connection;
        this.ownConnection = ownConnection;
    }

    private void init() throws SQLException {
        //Clear old id list
        Statement initializeStatemnent = connention.createStatement();
        initializeStatemnent.executeUpdate("TRUNCATE compoundspages");
        initializeStatemnent.executeUpdate("TRUNCATE compoundattributes");

        insertCompoundStatement = connention.prepareStatement("INSERT INTO compoundspages (pageid, has_chembox, has_smiles, smiles, indexed, exception) VALUES (?,?,?,?,?,?)");
        insertInfoboxStatement = connention.prepareStatement("INSERT INTO compoundattributes(pageid, attribute, value) VALUES (?,?,?)");
        setStrippedWikiTextStatement = connention.prepareStatement("UPDATE wikipages SET =? WHERE pageid=?");
    }

    private void putCompoundDataToDB(WikiArticle wikiArticle, boolean putToIndex, String exceptionReport) throws Exception {
        setStrippedWikiTextStatement.setString(1, wikiArticle.text);
        setStrippedWikiTextStatement.setString(2, wikiArticle.pageId);
        setStrippedWikiTextStatement.execute();

        insertCompoundStatement.setString(1, wikiArticle.pageId);
        insertCompoundStatement.setString(2, "1"); //has_chembox
        insertCompoundStatement.setString(3, "1"); //has_smiles
        insertCompoundStatement.setString(4, wikiArticle.smiles); //has_smiles
        insertCompoundStatement.setString(5, putToIndex ? "1" : null );
        insertCompoundStatement.setString(6, exceptionReport);
        insertCompoundStatement.execute();

        for (String key : wikiArticle.infoboxFields.keySet()) {
            insertInfoboxStatement.setString(1, wikiArticle.pageId);
            insertInfoboxStatement.setString(2, key);
            insertInfoboxStatement.setString(3, wikiArticle.infoboxFields.get(key));
            insertCompoundStatement.addBatch();
        }
        insertInfoboxStatement.execute();
    }

    @Override
    void processDocument(WikiArticle wikiArticle) throws Exception {
        boolean putToIndex = false;
        String exceptionReport = null;
        try {
            indexWriter.addDocument(wikiArticle.getLuceneDocument());
            putToIndex = true;
        }
        catch(Exception e) {
            putToIndex = false;
            exceptionReport = e.toString();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println(exceptionReport);
            e.printStackTrace(pw);
            exceptionReport += sw.toString();
        }
        putCompoundDataToDB(wikiArticle, putToIndex, exceptionReport);
    }

    @Override
    void finishProcessing() throws Exception {
        if (ownConnection) {
            connention.close();
        }
    }
}
