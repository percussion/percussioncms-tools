package de.byteaction.velocity.scanner;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.WhitespaceRule;
import de.byteaction.velocity.vaulttec.ui.editor.text.WhitespaceDetector;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class HTMLScanner extends RuleBasedScanner
{

    public HTMLScanner()
    {
        List rules = new ArrayList();
        rules.add(new WhitespaceRule(new WhitespaceDetector()));
        IRule[] result = new IRule[rules.size()];
        rules.toArray(result);
        setRules(result);
    }
}
