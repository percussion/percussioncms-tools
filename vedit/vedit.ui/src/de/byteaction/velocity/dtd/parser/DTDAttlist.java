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
 * Represents an ATTLIST declaration in the DTD. Although attributes are
 * associated with elements, the ATTLIST is here to allow the DTD object to
 * write out the DTD in roughly the original form. Because the ATTLIST may
 * appear somewhere other than immediately after the ELEMENT, this object is
 * used to keep track of where it is.
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public class DTDAttlist implements DTDOutput
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3616724963625873976L;
    /** The name of the element */
    public String    name;
    /** The attlist's attributes */
    public ArrayList attributes;

    public DTDAttlist()
    {
        attributes = new ArrayList();
    }

    public DTDAttlist(String aName)
    {
        name = aName;
        attributes = new ArrayList();
    }

    /** Writes out an ATTLIST declaration */
    public void write(PrintWriter out) throws IOException
    {
        out.print("<!ATTLIST ");
        out.println(name);
        Iterator itr = attributes.iterator();
        while (itr.hasNext())
        {
            out.print("           ");
            DTDAttribute attr = (DTDAttribute) itr.next();
            attr.write(out);
            if (itr.hasNext())
            {
                out.println();
            } else
            {
                out.println(">");
            }
        }
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
        if (!(ob instanceof DTDAttlist)) { return false; }
        DTDAttlist other = (DTDAttlist) ob;
        if ((name == null) && (other.name != null)) { return false; }
        if ((name != null) && !name.equals(other.name)) { return false; }
        return attributes.equals(other.attributes);
    }

    /** Returns the entity name of this attlist */
    public String getName()
    {
        return name;
    }

    /** Sets the entity name of this attlist */
    public void setName(String aName)
    {
        name = aName;
    }

    /** Returns the attributes in this list */
    public DTDAttribute[] getAttribute()
    {
        DTDAttribute[] attrs = new DTDAttribute[attributes.size()];
        attributes.toArray(attrs);
        return attrs;
    }

    /** Sets the list of attributes */
    public void setAttribute(DTDAttribute[] attrs)
    {
        attributes = new ArrayList(attrs.length);
        for (int i = 0; i < attrs.length; i++)
        {
            attributes.add(attrs[i]);
        }
    }

    /** Returns a specific attribute from the list */
    public DTDAttribute getAttribute(int i)
    {
        return (DTDAttribute) attributes.get(i);
    }

    /** Sets a specific attribute in the list */
    public void setAttribute(DTDAttribute attr, int i)
    {
        attributes.set(i, attr);
    }
}
