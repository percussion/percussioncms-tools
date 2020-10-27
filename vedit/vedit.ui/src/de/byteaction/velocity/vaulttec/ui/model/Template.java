package de.byteaction.velocity.vaulttec.ui.model;

import java.util.Iterator;
import java.util.Vector;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class Template extends AbstractTreeNode implements IBlock
{

    private String fName;
    private Vector fDirectives = new Vector();

    public Template(String aName)
    {
        super(null, -1, -1);
        fName = aName;
    }

    /**
     * @see de.byteaction.velocity.vaulttec.ui.model.IBlock#addDirective(de.byteaction.velocity.vaulttec.ui.model.Directive)
     */
    public void addDirective(Directive aDirective)
    {
        fDirectives.add(aDirective);
    }

    /**
     * @see de.byteaction.velocity.vaulttec.ui.model.ITreeNode#getName()
     */
    public String getName()
    {
        return fName;
    }

    /**
     * @see de.byteaction.velocity.vaulttec.ui.model.ITreeNode#hasChildren()
     */
    public boolean hasChildren()
    {
        return !fDirectives.isEmpty();
    }

    /**
     * @see de.byteaction.velocity.vaulttec.ui.model.ITreeNode#getChildren()
     */
    public Object[] getChildren()
    {
        return fDirectives.toArray();
    }

    /**
     * @see de.byteaction.velocity.vaulttec.ui.model.ITreeNode#accept(de.byteaction.velocity.vaulttec.ui.model.ITreeVisitor)
     */
    public boolean accept(ITreeVisitor aVisitor)
    {
        boolean more = true;
        // Visit all directives of this template
        Iterator iter = fDirectives.iterator();
        while (more && iter.hasNext())
        {
            more = ((ITreeNode) iter.next()).accept(aVisitor);
        }
        // Finally visit this template
        if (more)
        {
            more = aVisitor.visit(this);
        }
        return more;
    }

    /**
     * @see de.byteaction.velocity.vaulttec.ui.model.ITreeNodeInfo#getUniqueID()
     */
    public String getUniqueID()
    {
        return getName();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String toString()
    {
        return getUniqueID() + " [" + getStartLine() + ":" + getEndLine() + "] with directive(s) " + fDirectives;
    }
}
