package de.byteaction.velocity.dtd.parser;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class DTDParseException extends java.io.IOException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3834313920977843510L;
    public String uriID = "";
    public int    lineNumber;
    public int    column;

    public DTDParseException()
    {
        lineNumber = -1;
        column = -1;
    }

    public DTDParseException(String message)
    {
        super(message);
        lineNumber = -1;
        column = -1;
    }

    public DTDParseException(String message, int line, int col)
    {
        super("At line " + line + ", column " + col + ": " + message);
        lineNumber = line;
        column = col;
    }

    public DTDParseException(String id, String message, int line, int col)
    {
        super((((null != id) && (id.length() > 0)) ? ("URI " + id + " at ") : "At ") + "line " + line + ", column " + col + ": " + message);
        if (null != id)
        {
            uriID = id;
        }
        lineNumber = line;
        column = col;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getId()
    {
        return (uriID);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getColumn()
    {
        return column;
    }
}
