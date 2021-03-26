package de.byteaction.velocity.dtd.parser;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Represents a named item in the DTD
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public class DTDName extends DTDItem
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3257844398334425139L;
    public String value;

    public DTDName()
    {
    }

    public DTDName(String aValue)
    {
        value = aValue;
    }

    /** Writes out the value of this name */
    public void write(PrintWriter out) throws IOException
    {
        out.print(value);
        cardinal.write(out);
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
        if (!(ob instanceof DTDName)) { return false; }
        if (!super.equals(ob)) { return false; }
        DTDName other = (DTDName) ob;
        if (value == null)
        {
            if (other.value != null) { return false; }
        } else
        {
            if (!value.equals(other.value)) { return false; }
        }
        return true;
    }

    /** Sets the name value */
    public void setValue(String aValue)
    {
        value = aValue;
    }

    /** Retrieves the name value */
    public String getValue()
    {
        return value;
    }
}
