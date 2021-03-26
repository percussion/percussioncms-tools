/******************************************************************************
 *
 * [ IPSEditorFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.IPSReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * This class hides the details of how to instantiate an editor for a design
 * object. The underlying editor may be a dialog or an <code>IEditorPart</code>
 * based editor. After this method returns, the implementer will see the 
 * appropriate UI to modify the object associated with the supplied reference.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public interface IPSEditorFactory
{
   /**
    * Retrieves the data for the supplied reference and opens the correct
    * editor.
    * 
    * @param page The page that will contain the editor. Never <code>null</code>.
    * 
    * @param reference Never <code>null</code>. If the object cannot be
    * loaded for any reason, an exception is thrown.
    * 
    * @throws PartInitException If the editor can't be opened for any reason.
    */
   public void openEditor(IWorkbenchPage page, IPSReference reference)
      throws PartInitException;
}
