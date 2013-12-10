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

/**
 * Created with IntelliJ IDEA.
 * User: savochkin
 * Date: 9/9/13
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDirectoryInitializer {
    public Directory getDirectory();
    public void setParameters(String params);
}
