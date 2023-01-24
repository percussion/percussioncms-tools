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
 * Represents an external ID in an entity declaration
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public abstract class DTDExternalID implements DTDOutput
{

    public String system;

    public DTDExternalID()
    {
    }

    /** Writes out a declaration for this external ID */
    public abstract void write(PrintWriter out) throws IOException;

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
        if (!(ob instanceof DTDExternalID)) { return false; }
        DTDExternalID other = (DTDExternalID) ob;
        if (system == null)
        {
            if (other.system != null) { return false; }
        } else
        {
            if (!system.equals(other.system)) { return false; }
        }
        return true;
    }

    /** Sets the system ID */
    public void setSystem(String aSystem)
    {
        system = aSystem;
    }

    /** Retrieves the system ID */
    public String getSystem()
    {
        return system;
    }
}
