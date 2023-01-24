/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.byteaction.velocity.dtd.parser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents an enumeration of attribute values
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public class DTDEnumeration implements DTDOutput
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3906085646337325109L;
    protected ArrayList items;

    /** Creates a new enumeration */
    public DTDEnumeration()
    {
        items = new ArrayList();
    }

    /** Adds a new value to the list of values */
    public void add(String item)
    {
        items.add(item);
    }

    /** Removes a value from the list of values */
    public void remove(String item)
    {
        items.remove(item);
    }

    /** Returns the values as an array */
    public String[] getItems()
    {
        String[] retval = new String[items.size()];
        items.toArray(retval);
        return retval;
    }

    /** Returns the values as a ArrayList (not a clone!) */
    public ArrayList getItemsVec()
    {
        return items;
    }

    /** Writes out a declaration for this enumeration */
    public void write(PrintWriter out) throws IOException
    {
        out.print("( ");
        Iterator e = getItemsVec().iterator();
        boolean isFirst = true;
        while (e.hasNext())
        {
            if (!isFirst)
            {
                out.print(" | ");
            }
            isFirst = false;
            out.print(e.next());
        }
        out.print(")");
    }

    /**
     * DOCUMENT ME!
     * 
     * @param ob
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean equals(Object ob)
    {
        if (ob == this) { return true; }
        if (!(ob instanceof DTDEnumeration)) { return false; }
        DTDEnumeration other = (DTDEnumeration) ob;
        return items.equals(other.items);
    }

    /** Returns the items in the enumeration */
    public String[] getItem()
    {
        return getItems();
    }

    /** Sets the items in the enumeration */
    public void setItem(String[] newItems)
    {
        items = new ArrayList(newItems.length);
        for (int i = 0; i < newItems.length; i++)
        {
            items.add(newItems[i]);
        }
    }

    /** Stores an item in the enumeration */
    public void setItem(String item, int i)
    {
        items.set(i, item);
    }

    /** Retrieves an item from the enumeration */
    public String getItem(int i)
    {
        return (String) items.get(i);
    }
}
