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

package de.byteaction.velocity.vaulttec.ui.editor.text;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordPatternRule;
import de.byteaction.velocity.vaulttec.ui.IColorConstants;
import de.byteaction.velocity.vaulttec.ui.VelocityColorProvider;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class VelocityStringScanner extends RuleBasedScanner
{

    public VelocityStringScanner(VelocityColorProvider manager)
    {
        List rules = new ArrayList();
        // Add generic whitespace rule
        rules.add(new WhitespaceRule(new WhitespaceDetector()));
        // Add pattern rule for formal references
        Token token = (Token) manager.getToken(IColorConstants.STRING_REFERENCE);
        rules.add(new PatternRule("$!{", "}", token, (char) 0, true));
        rules.add(new PatternRule("${", "}", token, (char) 0, true));
        // Add pattern rule for shorthand references
        token = (Token) manager.getToken(IColorConstants.STRING_REFERENCE);
        rules.add(new WordPatternRule(new IdentifierDetector(), "$!", null, token));
        rules.add(new WordPatternRule(new IdentifierDetector(), "$", null, token));
        IRule[] result = new IRule[rules.size()];
        rules.toArray(result);
        setRules(result);
        setDefaultReturnToken(manager.getToken(IColorConstants.STRING));
    }

    private class WhitespaceDetector implements IWhitespaceDetector
    {

        public boolean isWhitespace(char aChar)
        {
            return ((aChar == ' ') || (aChar == '\t'));
        }
    }
}
