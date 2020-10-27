package de.byteaction.velocity.dtd.parser;

/**
 * Enumerated value representing the type of a token
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
class TokenType
{

    public int    value;
    public String name;

    public TokenType(int aValue, String aName)
    {
        value = aValue;
        name = aName;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param o
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (!(o instanceof TokenType)) { return false; }
        TokenType other = (TokenType) o;
        if (other.value == value) { return true; }
        return false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int hashCode()
    {
        return name.hashCode();
    }
}
