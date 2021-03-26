package de.byteaction.velocity.vaulttec.ui.editor.actions;

import java.util.ResourceBundle;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import de.byteaction.velocity.editor.VelocityEditor;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class GotoDefinitionAction extends TextEditorAction
{

    public GotoDefinitionAction(ResourceBundle aBundle, String aPrefix, ITextEditor anEditor)
    {
        super(aBundle, aPrefix, anEditor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run()
    {
        VelocityEditor editor = (VelocityEditor) getTextEditor();
        ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
        if (!selection.isEmpty())
        {
            editor.gotoDefinition(new Region(selection.getOffset(), selection.getLength()));
        }
    }
}
