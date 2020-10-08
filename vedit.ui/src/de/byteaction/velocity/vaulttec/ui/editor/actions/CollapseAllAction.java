package de.byteaction.velocity.vaulttec.ui.editor.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;
import de.byteaction.velocity.vaulttec.ui.VelocityPluginImages;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class CollapseAllAction extends Action
{

    private AbstractTreeViewer fViewer;

    public CollapseAllAction(AbstractTreeViewer aViewer)
    {
        fViewer = aViewer;
        setText(VelocityPlugin.getMessage("VelocityEditor.CollapseAllAction.label"));
        setToolTipText(VelocityPlugin.getMessage("VelocityEditor.CollapseAllAction.tooltip"));
        VelocityPluginImages.setLocalImageDescriptors(this, "collapseall.gif");
    }

    /**
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run()
    {
        fViewer.collapseAll();
    }
}
