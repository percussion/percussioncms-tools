package de.byteaction.velocity.dtd.parser;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Represents the possible values for an attribute declaration
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public class DTDDecl implements DTDOutput
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3834306250149280056L;
    public static final DTDDecl FIXED    = new DTDDecl(0, "FIXED");
    public static final DTDDecl REQUIRED = new DTDDecl(1, "REQUIRED");
    public static final DTDDecl IMPLIED  = new DTDDecl(2, "IMPLIED");
    public static final DTDDecl VALUE    = new DTDDecl(3, "VALUE");
    public int                  type;
    public String               name;

    public DTDDecl(int aType, String aName)
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
        if (!(ob instanceof DTDDecl)) { return false; }
        DTDDecl other = (DTDDecl) ob;
        if (other.type == type) { return true; }
        return false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param out
     *            DOCUMENT ME!
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    public void write(PrintWriter out) throws IOException
    {
        if (this == FIXED)
        {
            out.print(" #FIXED");
        } else if (this == REQUIRED)
        {
            out.print(" #REQUIRED");
        } else if (this == IMPLIED)
        {
            out.print(" #IMPLIED");
        }
        // Don't do anything for value since there is no associated DTD keyword
    }
}
