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

package de.byteaction.velocity.ui.editor.xml;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.Token;
import de.byteaction.velocity.editor.EditorsUtil;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class IgnoreCasePatternRule extends PatternRule
{

    protected char[]  fStartUpper;
    protected char[]  fEndUpper;
    protected boolean fSkipString;

    public IgnoreCasePatternRule(String startSequence, String endSequence, IToken token, char escapeCharacter, boolean breaksOnEOL, boolean skipString)
    {
        super(startSequence.toLowerCase(), endSequence.toLowerCase(), token, escapeCharacter, breaksOnEOL);
        fStartUpper = startSequence.toUpperCase().toCharArray();
        fEndUpper = endSequence.toUpperCase().toCharArray();
        fSkipString = skipString;
    }

    protected IToken doEvaluate(ICharacterScanner scanner, boolean resume)
    {
        if (resume)
        {
            if (endSequenceDetected(scanner)) { return super.fToken; }
        } else
        {
            int c = scanner.read();
            if (((c == super.fStartSequence[0]) || (c == fStartUpper[0])) && sequenceDetected(scanner, super.fStartSequence, fStartUpper, false) && endSequenceDetected(scanner)) { return super.fToken; }
        }
        scanner.unread();
        return Token.UNDEFINED;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param scanner
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public IToken evaluate(ICharacterScanner scanner)
    {
        return evaluate(scanner, false);
    }

    protected boolean endSequenceDetected(ICharacterScanner scanner)
    {
        char[][] delimiters = scanner.getLegalLineDelimiters();
        int c;
        while ((c = scanner.read()) != -1)
        {
            if (c == super.fEscapeCharacter)
            {
                scanner.read();
            } else if (fSkipString && ((c == 34) || (c == 39)))
            {
                EditorsUtil.skipString(c, scanner);
            } else if ((super.fEndSequence.length > 0) && ((c == super.fEndSequence[0]) || (c == fEndUpper[0])))
            {
                if (sequenceDetected(scanner, super.fEndSequence, fEndUpper, true)) { return true; }
            } else if (super.fBreaksOnEOL)
            {
                for (int i = 0; i < delimiters.length; i++)
                {
                    if ((c == delimiters[i][0]) && sequenceDetected(scanner, delimiters[i], false)) { return true; }
                }
            }
        }
        scanner.unread();
        return true;
    }

    protected boolean sequenceDetected(ICharacterScanner scanner, char[] lower, char[] upper, boolean eofAllowed)
    {
        for (int i = 1; i < lower.length; i++)
        {
            int c = scanner.read();
            if ((c == -1) && eofAllowed) { return true; }
            if ((c != lower[i]) && (c != upper[i]))
            {
                scanner.unread();
                for (int j = i - 1; j > 0; j--)
                {
                    scanner.unread();
                }
                return false;
            }
        }
        return true;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param scanner
     *            DOCUMENT ME!
     * @param resume
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public IToken evaluate(ICharacterScanner scanner, boolean resume)
    {
        if (super.fColumn == -1) { return doEvaluate(scanner, resume); }
        int c = scanner.read();
        scanner.unread();
        if ((c == super.fStartSequence[0]) || (c == fStartUpper[0]))
        {
            return (super.fColumn != scanner.getColumn()) ? Token.UNDEFINED : doEvaluate(scanner, resume);
        } else
        {
            return Token.UNDEFINED;
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public IToken getSuccessToken()
    {
        return super.fToken;
    }
}
