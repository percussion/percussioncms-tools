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

package de.byteaction.velocity.scanner;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class XMLTagDetector implements IWordDetector
{

    /**
     * DOCUMENT ME!
     * 
     * @param aChar
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean isWordStart(char aChar)
    {
        return (aChar == '<') || (aChar == '>');
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aChar
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean isWordPart(char aChar)
    {
        return Character.isLetterOrDigit(aChar) || (aChar == '-') || (aChar == '<') || (aChar == '_') || (aChar == '>') || (aChar == '/');
    }
}
