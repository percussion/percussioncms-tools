package de.byteaction.velocity.dtd.parser;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Represents the #PCDATA keyword in an Element's content spec
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public class DTDPCData extends DTDItem
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3907211541982950452L;

    public DTDPCData()
    {
    }

    /** Writes out the #PCDATA keyword */
    public void write(PrintWriter out) throws IOException
    {
        out.print("#PCDATA");
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
        if (!(ob instanceof DTDPCData)) { return false; }
        return super.equals(ob);
    }
}
