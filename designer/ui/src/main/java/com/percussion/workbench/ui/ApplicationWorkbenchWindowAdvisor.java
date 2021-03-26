/******************************************************************************
 *
 * [ ApplicationWorkbenchWindowAdvisor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.IPSCoreListener;
import com.percussion.client.PSConnectionInfo;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSLogonStateChangedEvent;
import com.percussion.client.PSLogonStateChangedEvent.LogonStateEvents;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import com.percussion.workbench.ui.help.PSHelpManager;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.views.PSProblemsView;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor
      implements IPSCoreListener {

   public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
      super(configurer);
      registerConnectionUpdates();
   }

   /**
    * Registers listener for server connection changes.
    */
   private void registerConnectionUpdates()
   {
      PSCoreFactory.getInstance().addListener(this);
      final LogonStateEvents eventType =
            PSCoreFactory.getInstance().isConnected()
                  ? LogonStateEvents.LOGON
                  : LogonStateEvents.LOGOFF;
      logonStateChanged(new PSLogonStateChangedEvent(eventType));
   }

   /* 
    * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#
    * createActionBarAdvisor(org.eclipse.ui.application.IActionBarConfigurer)
    */
   @Override
   public ActionBarAdvisor createActionBarAdvisor(
         IActionBarConfigurer configurer) 
   {
      return new ApplicationActionBarAdvisor(configurer);
   }

   //see base class method for details
   @Override
   public void createWindowContents(Shell shell)
   {
      super.createWindowContents(shell);
      shell.setImage(PSUiUtils.getImage("icons/main16.gif")); //$NON-NLS-1$
   }

   /* 
    * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
    */
   @Override
   public void preWindowOpen() 
   {
      IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
      configurer.setInitialSize(new Point(1000, 700));
      configurer.setShowCoolBar(true);
      configurer.setShowStatusLine(true);
      configurer.setShowProgressIndicator(true);
      // Pre-load the help mapping keys in a separate thread
      PSHelpManager.preloadKeyMappings();
   }

   /* 
    * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowOpen()
    */
   @Override
   public void postWindowOpen()
   {
      IWorkbenchWindow window = getWindowConfigurer().getWindow();
      for(IWorkbenchPage page : window.getPages())
      {
         page.addPartListener(new IPartListener()
            {

               /* 
                * @see org.eclipse.ui.IPartListener#partActivated(
                * org.eclipse.ui.IWorkbenchPart)
                */
               public void partActivated(IWorkbenchPart part)
               {
                  // Force the activated editor to run validation
                  // so that the problems view is in sync with
                  // the active editor.
                  if(part instanceof PSEditorBase)
                  {
                     PSEditorBase editor = (PSEditorBase)part;
                     editor.setStatusLineMessage(editor.isReadOnly() ? 
                        PSMessages.getString(
                           "ApplicationWorkbenchWindowAdvisor.readOnly.message") : //$NON-NLS-1$
                           null);
                     editor.runValidation(true);
                  }
                  else if(part instanceof IEditorPart)
                  {
                     IViewPart view = PSUiUtils.findView(PSProblemsView.ID);
                     if(view != null)
                     {
                        ((PSProblemsView)view).displayProblems(null);
                     }
                     
                  }
                  
               }

               /* 
                * @see org.eclipse.ui.IPartListener#partBroughtToTop(
                * org.eclipse.ui.IWorkbenchPart)
                */
               public void partBroughtToTop(
                  @SuppressWarnings("unused") IWorkbenchPart part) //$NON-NLS-1$
               {
                  // no-op                  
               }

               /* 
                * @see org.eclipse.ui.IPartListener#partClosed(
                * org.eclipse.ui.IWorkbenchPart)
                */
               public void partClosed(IWorkbenchPart part)
               {
                  
                  if(part instanceof PSEditorBase)
                  {
                     IWorkbenchPage wpage = part.getSite().getPage();
                     int count = wpage.getEditorReferences().length;
                     if(count == 0)
                     {
                        IViewPart view = PSUiUtils.findView(PSProblemsView.ID);
                        if(view != null)
                        {
                           ((PSProblemsView)view).displayProblems(null);
                        }
                     }
                  }
               }

               /* 
                * @see org.eclipse.ui.IPartListener#partDeactivated(
                * org.eclipse.ui.IWorkbenchPart)
                */
               public void partDeactivated(
                  @SuppressWarnings("unused") IWorkbenchPart part) //$NON-NLS-1$
               {
                  if(part instanceof PSEditorBase)
                  {
                     PSEditorBase editor = (PSEditorBase)part;
                     editor.setStatusLineMessage(null);
                  }           
               }

               /* 
                * @see org.eclipse.ui.IPartListener#partOpened(
                * org.eclipse.ui.IWorkbenchPart)
                */
               public void partOpened(
                  @SuppressWarnings("unused") IWorkbenchPart part) //$NON-NLS-1$
               {
                  // no-op                  
               }
            
            });
      }
   }

   public void logonStateChanged(PSLogonStateChangedEvent event)
   {
      switch (event.getEventType())
      {
         case LOGON:
            getDisplay().asyncExec(new Runnable()
            {
               public void run()
               {
                  
                  final PSConnectionInfo connectionInfo = getConnectionInfo();
                  if (connectionInfo == null)
                  {
                     // happens when user closes the workbench during opening
                     return;
                  }
                  getWindowConfigurer().setTitle(PSMessages.getString(
                        "ApplicationWorkbenchWindowAdvisor.title.loggedIn",     //$NON-NLS-1$
                        connectionInfo.getServer(),
                        Integer.toString(connectionInfo.getPort())));
               }
            });
            break;
         case LOGOFF:
            getDisplay().asyncExec(new Runnable()
            {
               public void run()
               {
                  getWindowConfigurer().setTitle(PSMessages.getString(
                        "ApplicationWorkbenchWindowAdvisor.title.loggedOff")); //$NON-NLS-1$
               }
            });
            break;
         default:
            throw new AssertionError("Unknown type " + event.getEventType()); //$NON-NLS-1$
      }
   }

   /**
    * Current workbench display.
    * Never <code>null</code>.
    */
   private Display getDisplay()
   {
      return getWindowConfigurer().getWorkbenchConfigurer().getWorkbench().getDisplay();
   }

   /**
    * Convenience method to access connection info.
    */
   private PSConnectionInfo getConnectionInfo()
   {
      return PSCoreFactory.getInstance().getConnectionInfo();
   }
}
