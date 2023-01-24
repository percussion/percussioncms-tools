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
