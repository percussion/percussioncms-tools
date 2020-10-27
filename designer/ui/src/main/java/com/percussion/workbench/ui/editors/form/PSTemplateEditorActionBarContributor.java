/******************************************************************************
 *
 * [ PSTemplateEditorActionBarContributor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Publishes source editor actions.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateEditorActionBarContributor
      extends EditorActionBarContributor
{
   @Override
   public void init(IActionBars bars, IWorkbenchPage page)
   {
      super.init(bars, page);
      m_partListener = new IPartListener()
      {
         public void partActivated(
               @SuppressWarnings("unused") IWorkbenchPart part)
         { /* no op */ }

         public void partBroughtToTop(
               @SuppressWarnings("unused") IWorkbenchPart part)
         { /* no op */ }

         /**
          * Clears the local copy of the active editor when it is closed.
          */
         public void partClosed(IWorkbenchPart part)
         {
            if (m_activeEditor == part)
               m_activeEditor = null;
         }

         public void partDeactivated(
               @SuppressWarnings("unused") IWorkbenchPart part)
         { /* no op */ }

         public void partOpened(
               @SuppressWarnings("unused") IWorkbenchPart part)
         { /* no op */ }
         
      };
      page.addPartListener(m_partListener);
      m_editorHelper.init(bars);
   }

   @Override
   public void dispose()
   {
      m_editorHelper.dispose();
      getPage().removePartListener(m_partListener);
      super.dispose();
   }

   /**
    * Connects to the editor returned by {@link #getActiveEditor()}.
    */
   private void connectToEditor()
   {
      if (getTabFolder() != null)
      {
         getTabFolder().addSelectionListener(m_editorFolderSelectionListener);
         doContributeToMenu();
      }
   }

   /**
    * Disconnects from the editor returned by {@link #getActiveEditor()}.
    */
   private void disconnectFromEditor()
   {
      if (getTabFolder() != null)
         getTabFolder().removeSelectionListener(m_editorFolderSelectionListener);
   }

   /**
    * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
    */
   @Override
   public void contributeToMenu(IMenuManager menuManager)
   {
      m_menuManager = menuManager;
      doContributeToMenu();
   }

   /**
    * Editor tab folder.
    * 
    * @return May be <code>null</code>. One case is if the active editor is
    * <code>null</code>.
    */
   private CTabFolder getTabFolder()
   {
      if (getActiveEditor() == null)
         return null;
      return getActiveEditor().getTabFolder();
   }

   /**
    * Method which actually contributes to the menu.
    * The editor menu is dynamic because it depends on the current page, so
    * the menu needs to be regenerated any time editor or editor page is changed.  
    */
   private void doContributeToMenu()
   {
      clearGlobalActionHandlers();
      clearBindingsContributions();

      applyTextEditorContributions();
      applyBindingsContributions();

      updateMenu();
   }

   /**
    * Reset global action handlers, so they can be provided by this contributor.
    *
    */
   private void clearGlobalActionHandlers()
   {
      getActionBars().clearGlobalActionHandlers();
      getActionBars().updateActionBars();
   }
   
   private void applyTextEditorContributions()
   {
      if (getActiveEditor() == null)
      {
         return;
      }
      final IEditorPart editor = getActiveEditor().getVisibleEditor();
      final ITextEditor textEditor = editor == null
            ? null
            : (ITextEditor) editor.getAdapter(ITextEditor.class);  
      m_editorHelper.activateActionBars(textEditor);
   }

   /**
    * Adds contributions for bindings UI. 
    */
   private void applyBindingsContributions()
   {
      if (getActiveEditor() == null
            || getBindingsPage() == null
            || !getBindingsPage().isVisible()
            || getActionBars() == null)
      {
         return;
      }
      
      m_copyBindingsAction = new Action()
            {
               @Override
               public void run()
               {
                  getBindingsPage().copyBindingsToClipboard();
               }
            };
      m_copyBindingsAction.setEnabled(false);
      getActionBars().setGlobalActionHandler(
            ActionFactory.COPY.getId(), m_copyBindingsAction);

      m_pasteBindingsAction = new Action()
            {
               @Override
               public void run()
               {
                  getBindingsPage().pasteBindingsFromClipboard();
               }
            };
      m_pasteBindingsAction.setEnabled(false);
      getActionBars().setGlobalActionHandler(
            ActionFactory.PASTE.getId(), m_pasteBindingsAction);
      
      if (m_menuManager != null)
      {
         final IMenuManager editMenuManager = m_menuManager.findMenuUsingPath(
               IWorkbenchActionConstants.M_EDIT);
         if (editMenuManager != null)
         {
            editMenuManager.addMenuListener(m_bindingsMenuListener);
         }
      }
      getActionBars().updateActionBars();
   }

   /**
    * Removes bindings UI contributions.
    */
   private void clearBindingsContributions()
   {
      m_copyBindingsAction = null;
      m_pasteBindingsAction = null;
      if (m_menuManager != null)
      {
         final IMenuManager editMenuManager = m_menuManager.findMenuUsingPath(
               IWorkbenchActionConstants.M_EDIT);
         if (editMenuManager != null)
         {
            editMenuManager.removeMenuListener(m_bindingsMenuListener);
         }
      }
   }

   private void updateMenu()
   {
      getMenuManager().update(true);
   }

   /**
    * The editor bindings page.
    *
    * @return can be <code>null</code>.
    * @see PSTemplateEditor#getBindingsPage()
    */
   private PSTemplateBindingsPage getBindingsPage()
   {
      if (getActiveEditor() == null)
         return null;
      return getActiveEditor().getBindingsPage();
   }

   /**
    * The editor to contribute to.
    * 
    * @return May be <code>null</code> if the editor was closed in a certain
    * way.
    */
   private PSTemplateEditor getActiveEditor()
   {
      return m_activeEditor;
   }

   @Override
   public void setActiveEditor(IEditorPart targetEditor)
   {
      assert targetEditor instanceof PSTemplateEditor;
      if (getActiveEditor() != null)
      {
         disconnectFromEditor();
      }
      m_activeEditor = (PSTemplateEditor) targetEditor;
      connectToEditor();
   }

   /**
    * Menu manager to contribute to.
    */
   public IMenuManager getMenuManager()
   {
      return m_menuManager;
   }
   
   /**
    * Listener tracking editor page change. 
    */
   private final SelectionAdapter m_editorFolderSelectionListener =
      new SelectionAdapter()
      {
         @Override
         public void widgetSelected(
               @SuppressWarnings("unused")final SelectionEvent e)
         {
            doContributeToMenu();
         }
      };

   /**
    * @see #getMenuManager()
    */
   private IMenuManager m_menuManager;

   /**
    * Contributes actions for embedded source editor.
    */
   private final PSTextEditorActionBarContributorHelper m_editorHelper =
         new PSTextEditorActionBarContributorHelper();

   /**
    * Current active editor.
    */
   private PSTemplateEditor m_activeEditor;
   
   /**
    * Action to copy bindings. Is not <code>null</code> only when actions are
    * initialized.
    */
   private Action m_copyBindingsAction;

   /**
    * Action to paste bindings. Is not <code>null</code> only when actions are
    * initialized.
    */
   private Action m_pasteBindingsAction;

   /**
    * This class is created during initialization, then never modified. It is
    * used to track when our registered active listener is closed.
    */
   private IPartListener m_partListener;
   
   /**
    * Disables, enables {@link #m_copyBindingsAction},
    * {@link #m_pasteBindingsAction} before the global menu is displayed.
    * Should be added to Edit menu manager. 
    */
   private final IMenuListener m_bindingsMenuListener =
         new IMenuListener()
   {

      // see interface
      public void menuAboutToShow(
            @SuppressWarnings("unused") IMenuManager manager)
      {
         if (getActiveEditor() == null
               || getBindingsPage() == null)
         {
            if (m_copyBindingsAction != null)
            {
               m_copyBindingsAction.setEnabled(false);
            }
            if (m_pasteBindingsAction != null)
            {
               m_copyBindingsAction.setEnabled(false);
            }
         }

         if (m_copyBindingsAction != null)
         {
            m_copyBindingsAction.setEnabled(
                  getBindingsPage().hasValidBindingsSelection());
         }
         if (m_pasteBindingsAction != null)
         {
            m_pasteBindingsAction.setEnabled(
                  getBindingsPage().clipboardContainsBindings());
         }
      }
   };
}
