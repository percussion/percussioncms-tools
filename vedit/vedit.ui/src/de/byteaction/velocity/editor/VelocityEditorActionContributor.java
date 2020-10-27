package de.byteaction.velocity.editor;

import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;

import java.io.File;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Contributes Velocity actions to the desktop's edit menu and registers the
 * according global action handlers.
 */
public class VelocityEditorActionContributor
      extends
         MultiPageEditorActionBarContributor
{

   private IEditorPart activeEditorPart;

   /**
    * Creates a multi-page contributor.
    */
   public VelocityEditorActionContributor() {
      super();
   }

   /**
    * Returns the action registed with the given text editor.
    * 
    * @return IAction or null if editor is null.
    */
   protected IAction getAction(ITextEditor editor, String actionID)
   {
      return ((editor == null) ? null : editor.getAction(actionID));
   }

   public void setActivePage(IEditorPart part)
   {
      if (activeEditorPart == part)
         return;
      activeEditorPart = part;

      IActionBars actionBars = getActionBars();
      if (actionBars != null)
      {
         ITextEditor editor = (part instanceof ITextEditor)
               ? (ITextEditor) part
               : null;
         actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
               getAction(editor, ITextEditorActionConstants.DELETE));
         actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
               getAction(editor, ITextEditorActionConstants.UNDO));
         actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
               getAction(editor, ITextEditorActionConstants.REDO));
         actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
               getAction(editor, ITextEditorActionConstants.CUT));
         actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
               getAction(editor, ITextEditorActionConstants.COPY));
         actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
               getAction(editor, ITextEditorActionConstants.PASTE));
         actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
               getAction(editor, ITextEditorActionConstants.SELECT_ALL));
         actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(),
               getAction(editor, ITextEditorActionConstants.FIND));
         actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(),
               getAction(editor, IDEActionFactory.BOOKMARK.getId()));
         actionBars.updateActionBars();
      }
   }
}
