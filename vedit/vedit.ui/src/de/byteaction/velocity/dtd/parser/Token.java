package de.byteaction.velocity.dtd.parser;

/**
 * Token returned by the lexical scanner
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
class Token
{

    public TokenType type;
    public String    value;

    public Token(TokenType aType)
    {
        type = aType;
        value = null;
    }

    public Token(TokenType aType, String aValue)
    {
        type = aType;
        value = aValue;
    }
}
