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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;

/**
 * Represents an Entity defined in a DTD
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public class DTDEntity implements DTDOutput
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3258131375264380465L;
    public String        name;
    public boolean       isParsed;
    public String        value;
    public DTDExternalID externalID;
    public String        ndata;
    public Object        defaultLocation;

    public DTDEntity()
    {
    }

    public DTDEntity(String aName)
    {
        name = aName;
    }

    public DTDEntity(String aName, Object aDefaultLocation)
    {
        name = aName;
        defaultLocation = aDefaultLocation;
    }

    /** Writes out an entity declaration for this entity */
    public void write(PrintWriter out) throws IOException
    {
        out.print("<!ENTITY ");
        if (isParsed)
        {
            out.print(" % ");
        }
        out.print(name);
        if (value != null)
        {
            char quoteChar = '"';
            if (value.indexOf(quoteChar) >= 0)
            {
                quoteChar = '\'';
            }
            out.print(quoteChar);
            out.print(value);
            out.print(quoteChar);
        } else
        {
            externalID.write(out);
            if (ndata != null)
            {
                out.print(" NDATA ");
                out.print(ndata);
            }
        }
        out.println(">");
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getExternalId()
    {
        return (externalID.system);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    public Reader getReader() throws IOException
    {
        // MAW Ver 1.19 - Added check for externalID == null
        if (externalID == null) { return null; }
        Reader rd = getReader(externalID.system);
        return rd;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param entityName
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Reader getReader(String entityName)
    {
        try
        {
            if (defaultLocation != null)
            {
                if (defaultLocation instanceof File)
                {
                    File loc = (File) defaultLocation;
                    BufferedReader in = new BufferedReader(new FileReader(new File(loc, entityName)));
                    return in;
                } else if (defaultLocation instanceof URL)
                {
                    // MAW Version 1.17
                    // Changed to construct new URL based on default
                    // location plus the entity name just like is done
                    // with the File-based name. This allows parsing of
                    // a URL-based DTD file that references other files either
                    // relatively or absolutely.
                    URL url = new URL((URL) defaultLocation, entityName);
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    return in;
                }
            }
            BufferedReader in = new BufferedReader(new FileReader(entityName));
            return in;
        }
        catch (Exception ignore)
        {
        }
        try
        {
            URL url = new URL(entityName);
            InputStream inStream = url.openStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
            return in;
        }
        catch (Exception ignore)
        {
        }
        return null;
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
        if (!(ob instanceof DTDEntity)) { return false; }
        DTDEntity other = (DTDEntity) ob;
        if (name == null)
        {
            if (other.name != null) { return false; }
        } else
        {
            if (!name.equals(other.name)) { return false; }
        }
        if (isParsed != other.isParsed) { return false; }
        if (value == null)
        {
            if (other.value != null) { return false; }
        } else
        {
            if (!value.equals(other.value)) { return false; }
        }
        if (externalID == null)
        {
            if (other.externalID != null) { return false; }
        } else
        {
            if (!externalID.equals(other.externalID)) { return false; }
        }
        if (ndata == null)
        {
            if (other.ndata != null) { return false; }
        } else
        {
            if (!ndata.equals(other.ndata)) { return false; }
        }
        return true;
    }

    /** Sets the name of this entity */
    public void setName(String aName)
    {
        name = aName;
    }

    /** Returns the name of this entity */
    public String getName()
    {
        return name;
    }

    /** Sets the isParsed flag */
    public void setIsParsed(boolean flag)
    {
        isParsed = flag;
    }

    /** Returns the isParsed flag */
    public boolean isParsed()
    {
        return isParsed;
    }

    /** Sets the entity value */
    public void setValue(String aValue)
    {
        value = aValue;
    }

    /** Returns the entity value */
    public String getValue()
    {
        return value;
    }

    /** Sets the external ID for the entity */
    public void setExternalID(DTDExternalID anExternalID)
    {
        externalID = anExternalID;
    }

    /** Returns the external ID for the entity */
    public DTDExternalID getExternalID()
    {
        return externalID;
    }

    /** Sets the entity ndata */
    public void setNdata(String anNdata)
    {
        ndata = anNdata;
    }

    /** Returns the entity ndata */
    public String getNdata()
    {
        return ndata;
    }
}
