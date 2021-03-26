package de.byteaction.velocity.dtd.parser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

/**
 * Represents a choice of items. A choice in a DTD looks like (option1 | option2 |
 * option3)
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public class DTDChoice extends DTDContainer
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3545234739045611831L;

    public DTDChoice()
    {
    }

    /** Writes out the possible choices to a PrintWriter */
    public void write(PrintWriter out) throws IOException
    {
        out.print("(");
        Iterator e = getItemsVec().iterator();
        boolean isFirst = true;
        while (e.hasNext())
        {
            if (!isFirst)
            {
                out.print(" | ");
            }
            isFirst = false;
            DTDItem item = (DTDItem) e.next();
            item.write(out);
        }
        out.print(")");
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
        if (!(ob instanceof DTDChoice)) { return false; }
        return super.equals(ob);
    }
}
