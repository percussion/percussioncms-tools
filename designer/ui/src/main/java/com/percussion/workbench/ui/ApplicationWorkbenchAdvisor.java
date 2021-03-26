/******************************************************************************
 *
 * [ ApplicationWorkbenchAdvisor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.IPSReference;
import com.percussion.client.PSMultiOperationException;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.model.PSFileEditorTracker;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Required by eclipse framework. We provide the Rhythmyx perspective and 
 * check for unsaved externally edited files.
 * 
 * @author paulhoward
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor 
{
   @Override
   public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
         IWorkbenchWindowConfigurer configurer)
   {
      return new ApplicationWorkbenchWindowAdvisor(configurer);
   }

   
   /* (non-Javadoc)
    * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)
    */
   @Override
   public void initialize(IWorkbenchConfigurer configurer)
   {
      super.initialize(configurer);
      configurer.setSaveAndRestore(true);
   }

   @Override
   public void preStartup()
   {
      super.preStartup();
      PSWorkbenchPlugin.getDefault().uiStart();
   }


   /**
    * Returns our default id, the Rhythmyx perspective.
    */
   @Override
   public String getInitialWindowPerspectiveId()
   {
      return PERSPECTIVE_ID;
   }

   /**
    * Used to provide a label for an <code>IPSReference</code> object of the
    * following form:
    * <pre>
    *    baseFilename [fully qualified path w/ name]
    * </pre>
    *
    * @author paulhoward
    */
   private class RefLabelProvider extends LabelProvider
   {
      //see base class method for details
      @SuppressWarnings("unchecked") // passed in element
      @Override
      public String getText(Object element)
      {
         PSPair<IPSReference, IFile> p = (PSPair<IPSReference, IFile>) element;
         return MessageFormat.format("{0} [{1}]", p.getFirst().getName(),
               p.getSecond().getFullPath());
      }
   }
   
   /**
    * We override this method to check for unsaved, externally edited files and
    * ask the user if they want to save them (just like the check for unsaved
    * editors.)
    */
   @SuppressWarnings("unchecked") //PSPair List returned from control
   @Override
   public boolean preShutdown()
   {
      boolean result = super.preShutdown();
      if (!result)
         return false;
      
      if (!PSFileEditorTracker.isInitialized())
      {
         // this prevents PSFileEditorTracker to get loaded in getInstance()
         return true;
      }
      List<PSPair<IPSReference, IFile>> data = PSFileEditorTracker
            .getInstance().getRegisteredReferences(false);
      if (data.isEmpty())
         return true;
      IStructuredContentProvider provider = new IStructuredContentProvider()
      {
         //see base class method for details
         public Object[] getElements(Object inputElement)
         {
            Collection c = (Collection) inputElement;
            return c.toArray();
         }

         //see base class method for details
         public void dispose()
         {}

         //see base class method for details
         @SuppressWarnings("unused")
         public void inputChanged(Viewer viewer, Object oldInput,
               Object newInput)
         {}
      };
      
      String msg = PSMessages
         .getString("ApplicationWorkbenchAdvisor.saveExternalResources.message");
      ListSelectionDialog dlg = new ListSelectionDialog(PSUiUtils.getShell(),
            data, provider, new RefLabelProvider(), msg);
   
      dlg.setInitialElementSelections(data);
      String title = PSMessages
         .getString("ApplicationWorkbenchAdvisor.saveExternalResources.title");
      dlg.setTitle(title);
      int queryResult = dlg.open();
      if (queryResult == Window.CANCEL)
         return false;
      
      Object[] chosen = dlg.getResult();
      List<Exception> errors = new ArrayList<Exception>(); 
      for (Object o : chosen)
      {
         PSPair<IPSReference, IFile> p = (PSPair<IPSReference, IFile>) o;
         try
         {
            PSFileEditorTracker.getInstance().save(p.getFirst(), true);
         }
         catch (Exception e)
         {
            errors.add(e);
         }
      }
      if (!errors.isEmpty())
      {
         PSUiUtils.handleExceptionSync("Save during shutdown", null, null,
               new PSMultiOperationException(errors.toArray()));
      }
      return true;
   }

   /**
    * The identifier for the default perspective.
    */
   private static final String PERSPECTIVE_ID = 
      "com.percussion.workbench.ui.RhythmyxPerspective";
}
