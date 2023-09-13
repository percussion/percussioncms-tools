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
 * Represents a Notation defined in a DTD
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public class DTDNotation implements DTDOutput
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3617575989887054131L;
    public String        name;
    public DTDExternalID externalID;

    public DTDNotation()
    {
    }

    public DTDNotation(String aName)
    {
        name = aName;
    }

    /** Writes out a declaration for this notation */
    public void write(PrintWriter out) throws IOException
    {
        out.print("<!NOTATION ");
        out.print(name);
        out.print(" ");
        externalID.write(out);
        out.println(">");
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
        if (!(ob instanceof DTDNotation)) { return false; }
        DTDNotation other = (DTDNotation) ob;
        if (name == null)
        {
            if (other.name != null) { return false; }
        } else
        {
            if (!name.equals(other.name)) { return false; }
        }
        if (externalID == null)
        {
            if (other.externalID != null) { return false; }
        } else
        {
            if (!externalID.equals(other.externalID)) { return false; }
        }
        return true;
    }

    /** Sets the notation name */
    public void setName(String aName)
    {
        name = aName;
    }

    /** Retrieves the notation name */
    public String getName()
    {
        return name;
    }

    /** Sets the external ID */
    public void setExternalID(DTDExternalID theExternalID)
    {
        externalID = theExternalID;
    }

    /** Retrieves the external ID */
    public DTDExternalID getExternalID()
    {
        return externalID;
    }
}
