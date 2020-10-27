/******************************************************************************
 *
 * [ PSMultiPageSelectionProvider.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorPart;

public class PSMultiPageSelectionProvider implements ISelectionProvider
{

   /**
    * Creates a selection provider for the given multi-page editor.
    * 
    * @param multiPageEditor the multi-page editor
    */
   public PSMultiPageSelectionProvider(PSMultiPageEditorBase multiPageEditor)
   {
      Assert.isNotNull(multiPageEditor);
      this.m_multiPageEditor = multiPageEditor;
   }

   /*
    * (non-Javadoc) Method declared on <code>ISelectionProvider</code>.
    */
   public void addSelectionChangedListener(ISelectionChangedListener listener)
   {
      m_listeners.add(listener);
   }

   /**
    * Notifies all registered selection changed listeners that the editor's
    * selection has changed. Only listeners registered at the time this method
    * is called are notified.
    * 
    * @param event the selection changed event
    */
   public void fireSelectionChanged(final SelectionChangedEvent event)
   {
      Object[] listeners = this.m_listeners.getListeners();
      for (int i = 0; i < listeners.length; ++i)
      {
         final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
         Platform.run(new SafeRunnable()
         {
            public void run()
            {
               l.selectionChanged(event);
            }
         });
      }
   }

   /**
    * Returns the multi-page editor.
    */
   public PSMultiPageEditorBase getMultiPageEditor()
   {
      return m_multiPageEditor;
   }

   /*
    * (non-Javadoc) Method declared on <code>ISelectionProvider</code>.
    */
   public ISelection getSelection()
   {
      IEditorPart activeEditor = m_multiPageEditor.getActiveEditor();
      if (activeEditor != null)
      {
         ISelectionProvider selectionProvider = activeEditor.getSite()
            .getSelectionProvider();
         if (selectionProvider != null)
            return selectionProvider.getSelection();
      }
      return null;
   }

   /*
    * (non-JavaDoc) Method declaed on <code>ISelectionProvider</code>.
    */
   public void removeSelectionChangedListener(ISelectionChangedListener listener)
   {
      m_listeners.remove(listener);
   }

   /*
    * (non-Javadoc) Method declared on <code>ISelectionProvider</code>.
    */
   public void setSelection(ISelection selection) {
       IEditorPart activeEditor = m_multiPageEditor.getActiveEditor();
       if (activeEditor != null) {
           ISelectionProvider selectionProvider = activeEditor.getSite()
                   .getSelectionProvider();
           if (selectionProvider != null)
               selectionProvider.setSelection(selection);
       }
   }
   
   /**
    * Registered selection changed listeners (element type:
    * <code>ISelectionChangedListener</code>).
    */
   private ListenerList m_listeners = new ListenerList();

   /**
    * The multi-page editor.
    */
   private PSMultiPageEditorBase m_multiPageEditor;

}
