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

import java.util.Vector;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordPatternRule;
import org.eclipse.jface.text.rules.WordRule;
import de.byteaction.velocity.vaulttec.ui.IColorConstants;
import de.byteaction.velocity.vaulttec.ui.VelocityColorProvider;
import de.byteaction.velocity.vaulttec.ui.editor.text.DirectiveDetector;
import de.byteaction.velocity.vaulttec.ui.editor.text.IdentifierDetector;
import de.byteaction.velocity.vaulttec.ui.editor.text.WhitespaceDetector;
import de.byteaction.velocity.vaulttec.ui.model.Directive;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class HTMLTagScanner extends RuleBasedScanner
{

    private boolean  isElement;
    private boolean  isEndElement;
    private IToken   fDefaultToken;
    private IToken   fElementToken;
    private IToken   fEndElementToken;
    private WordRule fElementRule;

    public HTMLTagScanner(VelocityColorProvider manager)
    {
        fDefaultToken = manager.getToken(IColorConstants.HTML_ATTRIBUTE);
        fElementToken = manager.getToken(IColorConstants.HTML_TAG);
        fEndElementToken = manager.getToken(IColorConstants.HTML_ENDTAG);
        IToken string = manager.getToken(IColorConstants.HTML_String);
        Vector rules = new Vector();
        setDefaultReturnToken(fDefaultToken);
        fElementRule = new WordRule(new HTMLElementDetector(), fDefaultToken);
        Token token = (Token) manager.getToken(IColorConstants.DIRECTIVE);
        WordRule wordRule = new WordRule(new DirectiveDetector(), token);
        token = (Token) manager.getToken(IColorConstants.DIRECTIVE);
        // System directives
        String[] directives = Directive.DIRECTIVES;
        for (int i = directives.length - 1; i >= 0; i--)
        {
            wordRule.addWord(directives[i], token);
        }
        rules.add(wordRule);
        rules.add(new PatternRule("\"", "\"", string, '\0', true));
        rules.add(new PatternRule("'", "'", string, '\0', true));
        rules.add(new WhitespaceRule(new WhitespaceDetector()));
        token = (Token) manager.getToken(IColorConstants.REFERENCE);
        rules.add(new PatternRule("$!{", "}", token, (char) 0, true));
        rules.add(new PatternRule("${", "}", token, (char) 0, true));
        // Add pattern rule for shorthand references
        token = (Token) manager.getToken(IColorConstants.REFERENCE);
        rules.add(new WordPatternRule(new IdentifierDetector(), "$!", null, token));
        rules.add(new WordPatternRule(new IdentifierDetector(), "$", null, token));
        IRule[] result = new IRule[rules.size()];
        rules.copyInto(result);
        setRules(result);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public IToken nextToken()
    {
        super.fTokenOffset = super.fOffset;
        super.fColumn = -1;
        IToken token;
        if (super.fRules != null)
        {
            for (int i = 0; i < super.fRules.length; i++)
            {
                token = super.fRules[i].evaluate(this);
                if (!token.isUndefined()) { return token; }
            }
        }
        token = fElementRule.evaluate(this);
        if (!token.isUndefined())
        {
            if (isEndElement)
            {
                isEndElement = false;
                return fEndElementToken;
            }
            if (isElement)
            {
                isElement = false;
                return fElementToken;
            } else
            {
                return fDefaultToken;
            }
        }
        if (read() == -1)
        {
            return Token.EOF;
        } else
        {
            return super.fDefaultReturnToken;
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param document
     *            DOCUMENT ME!
     * @param offset
     *            DOCUMENT ME!
     * @param length
     *            DOCUMENT ME!
     */
    public void setRange(IDocument document, int offset, int length)
    {
        super.setRange(document, offset, length);
        isElement = false;
        isEndElement = false;
        try
        {
            if (document.getChar(offset) == '<')
            {
                if ((length > 0) && (document.getChar(offset + 1) == '/'))
                {
                    isEndElement = true;
                } else
                {
                    isElement = true;
                }
            }
        }
        catch (BadLocationException badlocationexception)
        {
        }
    }

    class HTMLElementDetector implements IWordDetector
    {

        HTMLElementDetector()
        {
        }

        public boolean isWordStart(char c)
        {
            return XMLChar.isNameStart(c);
        }

        public boolean isWordPart(char c)
        {
            return XMLChar.isName(c);
        }
    }
}
