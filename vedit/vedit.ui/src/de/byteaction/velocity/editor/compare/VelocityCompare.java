/*
 * Created on 30.12.2004
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package de.byteaction.velocity.editor.compare;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;

/**
 * @author akmal
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class VelocityCompare
{

    public static int compare(final VelocityInput left, final VelocityInput right, String error)
    {
        Shell shell = new Shell();
        CompareConfiguration cc = new CompareConfiguration();
        CompareEditorInput input = new CompareEditorInput(cc) {

            protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                // VelocityInput left = new VelocityInput("left",
                // "these\nare\nthe\ncontents");
                // VelocityInput right = new VelocityInput("right",
                // "theasdse\nare\nthe\ncontednts");
                return new DiffNode(left, right);
            }
        };
        input.setTitle("Veloeclipse Compare editor");
        // CompareUI.openCompareEditor(input);
        return openCompareDialog(input, error);
    }

    public static int openCompareDialog(CompareEditorInput input, String error)
    {
        VelocityPlugin plugin = VelocityPlugin.getDefault();
        if (plugin != null) return plugin.openCompareDialog(input, error);
        return 0;
    }
}
