/*
 * Created on 16.09.2003
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.byteaction.velocity.vaulttec.ui.editor.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class MultiLineRuleWithEOF extends MultiLineRule
{

    public MultiLineRuleWithEOF(String startSequence, String endSequence, IToken token)
    {
        super(startSequence, endSequence, token);
    }

    protected boolean endSequenceDetected(ICharacterScanner scanner)
    {
        boolean result = super.endSequenceDetected(scanner);
        if (!result)
        {
            if (scanner.read() == ICharacterScanner.EOF)
            {
                result = true;
            } else
            {
                scanner.unread();
            }
        }
        return result;
    }
}
