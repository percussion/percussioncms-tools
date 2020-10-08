package de.byteaction.velocity.vaulttec.ui.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import de.byteaction.velocity.editor.VelocityEditor;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class VelocityTextHover implements ITextHover
{

    private VelocityEditor fEditor;

    public VelocityTextHover(VelocityEditor anEditor)
    {
        fEditor = anEditor;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aTextViewer
     *            DOCUMENT ME!
     * @param aRegion
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getHoverInfo(ITextViewer aTextViewer, IRegion aRegion)
    {
        return fEditor.getDefinitionLine(aRegion);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aTextViewer
     *            DOCUMENT ME!
     * @param anOffset
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public IRegion getHoverRegion(ITextViewer aTextViewer, int anOffset)
    {
        return new Region(anOffset, 0);
    }
}
