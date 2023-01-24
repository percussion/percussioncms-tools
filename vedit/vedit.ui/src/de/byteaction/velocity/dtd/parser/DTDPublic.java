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
 * Represents an external Public ID in an entity declaration
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public class DTDPublic extends DTDExternalID
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3257004362877448497L;
    public String pub;

    public DTDPublic()
    {
    }

    /** Writes out a public external ID declaration */
    public void write(PrintWriter out) throws IOException
    {
        out.print("PUBLIC \"");
        out.print(pub);
        out.print("\"");
        if (system != null)
        {
            out.print(" \"");
            out.print(system);
            out.print("\"");
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
        if (!(ob instanceof DTDPublic)) { return false; }
        if (!super.equals(ob)) { return false; }
        DTDPublic other = (DTDPublic) ob;
        if (pub == null)
        {
            if (other.pub != null) { return false; }
        } else
        {
            if (!pub.equals(other.pub)) { return false; }
        }
        return true;
    }

    /** Sets the public identifier */
    public void setPub(String aPub)
    {
        pub = aPub;
    }

    /** Retrieves the public identifier */
    public String getPub()
    {
        return pub;
    }
}
