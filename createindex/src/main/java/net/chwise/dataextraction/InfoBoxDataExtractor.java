/**
 Copyright (c) 2014 Alexander Savochkin
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

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.utils.StringUtils;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.parser.nodes.*;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.util.*;


/**
 * FSM-like logic for extracting information from chembox. Based on Sweble.Org wiki-parser
 */
public class InfoBoxDataExtractor extends
        AstVisitor<WtNode> {

    private final static List<String> INTERESTING_INFOBOXES = Arrays.asList(new String[]{"chembox", "drugbox"});

    private static enum ProcessingState {
        Outside,
        InsideUnknownTemplate,
        InsideUnknownTemplateNodeList,
        InsideChemboxTemplate,
        InsideChemboxNodeList,
        InsideChemBoxKey,
        InsideChemBoxValue,
        InsideChemboxNodeListTemplateArgument
    }

    private ProcessingState currentState = ProcessingState.Outside;
    private String currentKey;
    private String currentValue;
    private Map<String, String> extractedProperties = null;

    private StringBuilder currentValueStringBuilder = new StringBuilder();

    @Override
    protected boolean before(WtNode node)
    {
        // This method is called by go() before visitation starts
        extractedProperties = new HashMap<String, String>();
        return super.before(node);
    }

    @Override
    protected Object after(WtNode node, Object result)
    {
        // This method is called by go() after visitation has finished
        // The return value will be passed to go() which passes it to the caller
        return extractedProperties;
    }


    public void visit(WtText text)
    {
        if (currentState == ProcessingState.InsideChemBoxKey)
            currentKey = text.getContent().trim();
        else if (currentState == ProcessingState.InsideChemBoxValue) {
            currentValueStringBuilder.append(' ');
            currentValueStringBuilder.append(text.getContent().trim());
            currentValueStringBuilder.append(' ');
        } else if (currentState == ProcessingState.InsideUnknownTemplateNodeList) {
            String value = text.getContent().trim();
            if (INTERESTING_INFOBOXES.contains(value)) {
                currentState = ProcessingState.InsideChemboxTemplate;
            }
        }
    }

    public void visit(WtTemplate n)
    {
        if ( currentState == ProcessingState.Outside) {
            //Save previous state
            ProcessingState savedState = currentState;
            currentState = ProcessingState.InsideUnknownTemplate;
            iterate(n);
            //Restore previous state
            currentState = savedState;
        } else if (currentState == ProcessingState.InsideChemBoxValue) { //Infobox subsection
            //Skip current key-value pair and process subsection in recurent way
            currentState = ProcessingState.InsideChemboxTemplate;
            iterate(n);
            currentState = ProcessingState.InsideChemboxTemplate;
        }
    }

    public void visit(WtTemplateArgument n)
    {
        if ( currentState == ProcessingState.InsideChemboxNodeList ) {
            //Save previous state
            currentState = ProcessingState.InsideChemboxNodeListTemplateArgument;
            iterate(n);
            //Restore previous state
            currentState = ProcessingState.InsideChemboxNodeList;
        } else if ( currentState == ProcessingState.InsideChemBoxValue) {
            iterate(n);
        }
    }

    public void visit(WtNodeList n)
    {

        if ( currentState == ProcessingState.InsideUnknownTemplate) {
            //Save previous state
            ProcessingState savedState = currentState;
            currentState = ProcessingState.InsideUnknownTemplateNodeList;
            iterate(n);
            //Restore previous state
            if (currentState != ProcessingState.InsideChemboxTemplate)
                currentState = savedState;
        } else if ( currentState == ProcessingState.InsideChemboxTemplate) {
            //Save previous state
            ProcessingState savedState = currentState;
            currentState = ProcessingState.InsideChemboxNodeList;
            iterate(n);
            //Restore previous state
            currentState = savedState;
        } else if (currentState == ProcessingState.InsideChemboxNodeListTemplateArgument) {
            currentState = ProcessingState.InsideChemBoxKey;
            iterate(n);
        } else if ( currentState == ProcessingState.InsideChemBoxKey ) {
            currentState = ProcessingState.InsideChemBoxValue;
            iterate(n);
            String value = currentValueStringBuilder.toString().trim();
            extractedProperties.put(currentKey, value);
            currentValueStringBuilder.setLength(0); //Reset stringBuildr
            currentKey = "";
            currentState = ProcessingState.InsideChemboxNodeListTemplateArgument;

        }
        iterate(n);
    }

    public void visit(WtInternalLink link)
    {
        if (currentState == ProcessingState.InsideChemBoxValue) {
            currentValueStringBuilder.append(' ');
            currentValueStringBuilder.append(link.getPostfix());
            currentValueStringBuilder.append(' ');
            currentValueStringBuilder.append(link.getPrefix());
            if (!link.hasTitle()) {
                iterate(link.getTarget());
            } else {
                iterate(link.getTitle());
            }
        }
    }

    public void visit(WtTagExtension n)
    {
    }

    public void visit(WtSection s)
    {
//        iterate(s.getHeading());
//        iterate(s.getBody());
    }

}
