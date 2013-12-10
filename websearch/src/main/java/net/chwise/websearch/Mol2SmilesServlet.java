/**
 Copyright (c) 2013 Alexander Savochkin
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

package net.chwise.websearch;

import net.chwise.common.conversion.ToSmilesConverter;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Mol2SmilesServlet extends HttpServlet {

    private final static Logger LOGGER = Logger.getLogger(SearchServlet.class.getName());

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String mol = req.getParameter("mol");
        ToSmilesConverter converter = new ToSmilesConverter();
        String smiles = converter.convert(mol);
        JSONObject jsonDoc = new JSONObject();

        LOGGER.log(Level.INFO, "Molfile to convert: {0}", mol );

        try {
            jsonDoc.put("smiles", smiles );
        } catch (JSONException e) {
            throw new RuntimeException("Exception in Mol2SmilesServlet", e);
        }

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print(jsonDoc);
        out.flush();
    }

}
