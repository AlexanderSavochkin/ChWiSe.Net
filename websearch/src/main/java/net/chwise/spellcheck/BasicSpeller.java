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

package net.chwise.spellcheck;

import net.chwise.common.document.DocDefinitions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicSpeller implements Speller {

    private final static Logger LOGGER = Logger.getLogger(BasicSpeller.class.getName());

    private Directory spellerDirectory;
    private IndexReader reader;

    public BasicSpeller(Directory spellerDirectory, IndexReader reader) {
        this.spellerDirectory = spellerDirectory;
        this.reader = reader;
    }

    @Override
    public Map<String, String> getCorrections(Query query) throws IOException {
        Set<Term> terms = new HashSet<Term>();
        query.extractTerms(terms);
        Map<String, String> fixes = new HashMap<String, String>();

        for (Iterator<Term> it = terms.iterator(); it.hasNext();) {
            Term term = it.next();
            if (! Arrays.asList(DocDefinitions.getSpellerDictionaryFields()).contains(term.field()) ) {
                it.remove();
            }
        }
        SpellChecker spellChecker = new SpellChecker(spellerDirectory);
        for (Term term : terms) {
            LOGGER.log(Level.INFO, "Searching fixes for term: " + term.text());
            if (reader.totalTermFreq(term) == 0) {
                String[] similarWords = spellChecker.suggestSimilar(term.text(), 1, 0.8f);
                if (similarWords!=null && similarWords.length > 0) {
                    fixes.put(term.text(), similarWords[0]);
                }
                LOGGER.log(Level.INFO, "Corrected: " + similarWords[0]);
            }
        }
        return fixes;
    }
}
