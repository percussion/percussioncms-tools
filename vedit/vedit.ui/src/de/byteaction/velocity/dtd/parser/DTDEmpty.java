package de.byteaction.velocity.dtd.parser;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Represents the EMPTY keyword in an Element's content spec
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public class DTDEmpty extends DTDItem
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3257570602842927925L;

    public DTDEmpty()
    {
    }

    /** Writes out the keyword "EMPTY" */
    public void write(PrintWriter out) throws IOException
    {
        out.print("EMPTY");
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
        if (!(ob instanceof DTDEmpty)) { return false; }
        return super.equals(ob);
    }
}
