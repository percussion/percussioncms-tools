package de.byteaction.velocity.ui.editor.xml;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class WholePartitionDamagerRepairer extends DefaultDamagerRepairer
{

    public WholePartitionDamagerRepairer(ITokenScanner scanner)
    {
        super(scanner);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param partition
     *            DOCUMENT ME!
     * @param e
     *            DOCUMENT ME!
     * @param documentPartitioningChanged
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged)
    {
        return partition;
    }
}
