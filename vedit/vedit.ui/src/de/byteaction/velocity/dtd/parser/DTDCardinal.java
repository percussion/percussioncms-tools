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

/**
 * Represents the various cardinality values for a DTD item. <bl>
 * <li>NONE indicates no cardinality</li>
 * <li>OPTIONAL indicates an optional value (specified by ?)</li>
 * <li>ZEROMANY indicates zero-to-many values (specified by *)</li>
 * <li>ONEMANY indicates an one-to-many values (specified by +)</li>
 * </bl>
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public class DTDCardinal implements DTDOutput
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3257281418316100665L;
    /** Indicates no cardinality (implies a single object) */
    public static final DTDCardinal NONE     = new DTDCardinal(0, "NONE");
    /** Indicates that an item is optional (zero-to-one) */
    public static final DTDCardinal OPTIONAL = new DTDCardinal(1, "OPTIONAL");
    /** Indicates that there can be zero-to-many occurrances of an item */
    public static final DTDCardinal ZEROMANY = new DTDCardinal(2, "ZEROMANY");
    /** Indicates that there can be one-to-many occurrances of an item */
    public static final DTDCardinal ONEMANY  = new DTDCardinal(3, "ONEMANY");
    public int                      type;
    public String                   name;

    public DTDCardinal(int aType, String aName)
    {
        type = aType;
        name = aName;
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
        if (!(ob instanceof DTDCardinal)) { return false; }
        DTDCardinal other = (DTDCardinal) ob;
        if (other.type == type) { return true; }
        return false;
    }

    /** Writes the notation for this cardinality value */
    public void write(PrintWriter out) throws IOException
    {
        if (this == NONE) { return; }
        if (this == OPTIONAL)
        {
            out.print("?");
        } else if (this == ZEROMANY)
        {
            out.print("*");
        } else if (this == ONEMANY)
        {
            out.print("+");
        }
    }
}
