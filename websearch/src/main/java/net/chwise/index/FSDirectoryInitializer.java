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

package net.chwise.index;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: savochkin
 * Date: 9/9/13
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class FSDirectoryInitializer implements IDirectoryInitializer {

    String indexPath = null;

    @Override
    public Directory getDirectory() {
        Directory directory;
        try {
            directory = new SimpleFSDirectory( new File(indexPath) );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
        return directory;
    }

    @Override
    public void setParameters(String params) {
        //To change body of implemented methods use File | Settings | File Templates.
        indexPath = params;
    }
}
