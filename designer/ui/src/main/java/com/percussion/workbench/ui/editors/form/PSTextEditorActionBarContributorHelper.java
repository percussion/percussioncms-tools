/******************************************************************************
 *
 * [ PSTextEditorActionBarContributorHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;

import java.util.Map;

/**
 * Helps to contribute actions for embedded text editors.
 *
 * @author Andriy Palamarchuk
 */
public class PSTextEditorActionBarContributorHelper
{
   /**
    * Initializes the contributor.
    * @param bars
    */
   public void init(IActionBars bars)
   {
      m_actionBars = bars;
      m_textEditorActionBars = new SubActionBars(bars);
      m_textEditorContributor.init(m_textEditorActionBars);
   }

   /**
    * Releases UI resources.
    */
   public void dispose()
   {
      m_textEditorContributor.dispose();
      m_textEditorActionBars.dispose();
   }

   /**
    * Performs actual contribution.
    * @param textEditor if <code>null</code> this method deactivates the
    * contributions, otherwise enables them.
    */
   public void activateActionBars(final TextEditor textEditor)
   {
      m_textEditorContributor.setActiveEditor(textEditor);
      if (textEditor != null) {
         m_textEditorActionBars.activate();
         final Map handlers = m_textEditorActionBars.getGlobalActionHandlers();
         if (handlers != null)
         {
            for (final Object id : handlers.keySet())
            {
               m_actionBars.setGlobalActionHandler((String) id,
                     (IAction) handlers.get(id));
            }
         }
      }
      else
      {
         m_textEditorActionBars.deactivate();
      }
   }

   /**
    * Default text editor global action contributor.
    * Is used to provide default text editor actions for the text editors
    * shown on the pages of this multipage editor. 
    * The idea is taken from
    * <code>org.eclipse.pde.internal.ui.editor.PDEFormEditorContributor</code>.
    */
   private final TextEditorActionContributor m_textEditorContributor =
         new TextEditorActionContributor();

   /**
    * Dummy {@link IActionBars} implementation used to extract global text
    * editor actions.
    */
   private SubActionBars m_textEditorActionBars;

   /**
    * Stores action bars provided in the {@link #init(IActionBars)} method.
    * Never <code>null</code> after that.
    */
   private IActionBars m_actionBars;
}
