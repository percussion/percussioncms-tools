package de.byteaction.velocity.dtd.parser;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Represents a comment in the DTD
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public class DTDComment implements DTDOutput
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3905521601133885237L;
    /** The comment text */
    public String text;

    public DTDComment()
    {
    }

    public DTDComment(String theText)
    {
        text = theText;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String toString()
    {
        return text;
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
        out.print("<!--");
        out.print(text);
        out.println("-->");
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
        if (!(ob instanceof DTDComment)) { return false; }
        DTDComment other = (DTDComment) ob;
        if ((text == null) && (other.text != null)) { return false; }
        if ((text != null) && !text.equals(other.text)) { return false; }
        return true;
    }

    /** Sets the comment text */
    public void setText(String theText)
    {
        text = theText;
    }

    /** Returns the comment text */
    public String getText()
    {
        return text;
    }
}
