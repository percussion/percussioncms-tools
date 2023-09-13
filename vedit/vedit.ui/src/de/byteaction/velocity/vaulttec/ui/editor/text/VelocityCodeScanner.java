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
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordPatternRule;
import org.eclipse.jface.text.rules.WordRule;
import de.byteaction.velocity.vaulttec.ui.IColorConstants;
import de.byteaction.velocity.vaulttec.ui.VelocityColorProvider;
import de.byteaction.velocity.vaulttec.ui.editor.VelocityEditorEnvironment;
import de.byteaction.velocity.vaulttec.ui.model.Directive;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class VelocityCodeScanner extends RuleBasedScanner
{

    public VelocityCodeScanner(VelocityColorProvider manager)
    {
        List rules = new ArrayList();
        // Add generic whitespace rule
        rules.add(new WhitespaceRule(new WhitespaceDetector()));
        // Add word rule for directives
        Token token = (Token) manager.getToken(IColorConstants.DIRECTIVE);
        WordRule wordRule = new WordRule(new DirectiveDetector(), token);
        token = (Token) manager.getToken(IColorConstants.DIRECTIVE);
        // System directives
        String[] directives = Directive.DIRECTIVES;
        for (int i = directives.length - 1; i >= 0; i--)
        {
            wordRule.addWord(directives[i], token);
        }
        // User directives
        Iterator userDirectives = VelocityEditorEnvironment.getParser().getUserDirectives().iterator();
        while (userDirectives.hasNext())
        {
            wordRule.addWord((String) userDirectives.next(), token);
        }
        rules.add(wordRule);
        // Add pattern rule for formal references
        token = (Token) manager.getToken(IColorConstants.REFERENCE);
        rules.add(new PatternRule("$!{", "}", token, (char) 0, true));
        rules.add(new PatternRule("${", "}", token, (char) 0, true));
        // Add pattern rule for shorthand references
        token = (Token) manager.getToken(IColorConstants.REFERENCE);
        rules.add(new WordPatternRule(new IdentifierDetector(), "$!", null, token));
        rules.add(new WordPatternRule(new IdentifierDetector(), "$", null, token));
        // token = new Token(new
        // TextAttribute(aProvider.getColor(IColorConstants.TAG), null,
        // SWT.BOLD));
        // rules.add(new TagRule(token));
        // token = new Token(new
        // TextAttribute(aProvider.getColor(IColorConstants.COMMENT)));
        IRule[] result = new IRule[rules.size()];
        rules.toArray(result);
        setRules(result);
    }
}
