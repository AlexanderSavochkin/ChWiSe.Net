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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicSpeller implements Speller {

    private final static Logger LOGGER = Logger.getLogger(BasicSpeller.class.getName());

    private Directory spellerDirectory;
    private IndexReader reader;

    public BasicSpeller(Directory spellerDirectory, IndexReader reader) {
        this.spellerDirectory = spellerDirectory;
        this.reader = reader;
    }

    @Override
    public Map<String, Correction[]> getCorrections(String stringQuery, Query query) throws IOException {
        Set<Term> terms = new HashSet<Term>();
        query.extractTerms(terms);
        Map<String, Correction[]> fixes = new HashMap<String, Correction[]>();

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
                String[] similarWords = spellChecker.suggestSimilar(term.text(), 3, 0.8f);
                Correction[] corrections = new Correction[similarWords.length];
                for (int i = 0; i < corrections.length; ++i) {
                    String pattern = "(\\s|^)" + term.text() + "(\\s|$)";
                    Pattern compiledPattern = Pattern.compile(pattern,  Pattern.CASE_INSENSITIVE);
                    Matcher matcher = compiledPattern.matcher(stringQuery);
                    String correctedQuery = matcher.replaceAll(similarWords[i]);
                    corrections[i] = new Correction(term.text(), similarWords[i], correctedQuery);
                }
                if (similarWords!=null && similarWords.length > 0) {
                    fixes.put(term.text(), corrections);
                    LOGGER.log(Level.INFO, "Corrected: " + similarWords[0]);
                }
            }
        }
        return fixes;
    }
}
