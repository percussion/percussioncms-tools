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

import de.byteaction.velocity.vaulttec.ui.editor.text.VelocityTextGuesser.Type;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class XMLElementGuesser
{

    private int    tagoffset = -1;
    private String fText;
    private Type    fType;

    public XMLElementGuesser(IDocument aDocument, int anOffset, boolean aGuessEnd)
    {
        guessWord(aDocument, anOffset, aGuessEnd);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Type getType()
    {
        return fType;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getTagOffset()
    {
        return tagoffset;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getText()
    {
        return fText;
    }

    private static final boolean isWordPart(char aChar)
    {
        return Character.isLetterOrDigit(aChar) || (aChar == '-') || (aChar == '_');
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aDocument
     *            DOCUMENT ME!
     * @param anOffset
     *            DOCUMENT ME!
     * @param aGuessEnd
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    private final String guessWord(IDocument aDocument, int anOffset, boolean aGuessEnd)
    {
        try
        {
            // Guess start position
            int start = anOffset;
            while ((start >= 1) && isWordPart(aDocument.getChar(start - 1)))
            {
                start--;
            }
            // Guess end position
            int end = anOffset;
            if (aGuessEnd)
            {
                int len = aDocument.getLength() - 1;
                while ((end < len) && isWordPart(aDocument.getChar(end)))
                {
                    end++;
                }
            }
            fText = aDocument.get(start, (end - start));
            if (start >= 1)
            {
                // Directive or shorthand reference
                char c1 = aDocument.getChar(start - 1);
                char c0 = 0;
                try
                {
                    c0 = aDocument.getChar(start - 2);
                }
                catch (Exception e)
                {
                    // TODO: handle exception
                }
                if (c1 == '<')
                {
                    fType = Type.TAG_DIRECTIVE;
                    tagoffset = start - 1;
                } else if ((c1 == '/') && (c0 == '<'))
                {
                    fType = Type.TAG_CLOSE;
                    tagoffset = start - 2;
                } else
                {
                    fType = Type.TYPE_INVALID;
                }
            }
        }
        catch (BadLocationException e)
        {
        }
        return fText;
    }
}
