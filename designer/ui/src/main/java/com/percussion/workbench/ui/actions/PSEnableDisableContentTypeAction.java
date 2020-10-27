/******************************************************************************
 *
 * [ PSEnableDisableContentTypeAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import java.util.Collection;

/**
 * Standard action for enabling and disabling the content editor application.
 */
final class PSEnableDisableContentTypeAction
      extends PSBaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId()
         + ".EnableDisableContentTypeAction"; //$NON-NLS-1$

   /**
    * Creates a new action.
    * 
    * @param provider Supplied to super ctor. Never <code>null</code>.
    */
   public PSEnableDisableContentTypeAction(ISelectionProvider provider) 
   {
      super(PSMessages.getString("PSEnableDisableContentTypeAction.label.enable"),
            provider); //$NON-NLS-1$
      setId(PSEnableDisableContentTypeAction.ID);
   }

   @Override
   protected boolean updateSelection(IStructuredSelection selection)
   {
      PSUiReference node = (PSUiReference) selection.getFirstElement();
      if (node == null)
         return false;
      IPSReference ref = node.getReference();
      if (ref == null)
         return false;
      try
      {
         IPSReference appRef = getApplicationsReference(ref);
         if (appRef == null)
         {
            return false;
         }
         IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               PSObjectTypes.XML_APPLICATION);
         PSApplication editorApp = (PSApplication) model.load(appRef, false,
               false);
         if (editorApp == null)
            return false;
         if(editorApp.isEnabled())
         {
            setText(PSMessages.getString(
                  "PSEnableDisableContentTypeAction.label.disable")); //$NON-NLS-1$
            setToolTipText(PSMessages.getString(
                  "PSEnableDisableContentTypeAction.tooltiptext.disablesapplication")); //$NON-NLS-1$
         }
         else
         {
            setText(PSMessages.getString(
                  "PSEnableDisableContentTypeAction.label.enable")); //$NON-NLS-1$
            setToolTipText(PSMessages.getString(
                  "PSEnableDisableContentTypeAction.tooltiptext.enablesapplication")); //$NON-NLS-1$
         }
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.getDefault().log(getText() + PSMessages.getString(
               "PSEnableDisableContentTypeAction.error.actionfailed"),e); //$NON-NLS-1$
      }
      return super.updateSelection(selection);
   }

   /**
    * Enables or disables the application.
    */
   public void run()
   {
      try
      {
         if (!isEnabled())
            return;
         ISelection sel = getStructuredSelection();

         IStructuredSelection ssel = (IStructuredSelection) sel;
         PSUiReference node = (PSUiReference) ssel.getFirstElement();
         IPSReference ref = node.getReference();
         IPSReference appRef = getApplicationsReference(ref);
         if (appRef == null)
         {
            String title = getText() + PSMessages.getString(
                  "PSEnableDisableContentTypeAction.error.actionfailed"); //$NON-NLS-1$
            String message = PSMessages.getString(
                  "PSEnableDisableContentTypeAction.error.couldnotfindapplication"); //$NON-NLS-1$
            PSWorkbenchPlugin.displayWarning(
                  "Enable or disable editor action",title, message,null); //$NON-NLS-1$
            return;
         }
         IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               PSObjectTypes.XML_APPLICATION);
         PSApplication editorApp = (PSApplication) model.load(appRef, true,
               false);
         if (editorApp != null)
         {
            editorApp.setEnabled(!editorApp.isEnabled());
            model.save(appRef, true);
         }
      }
      catch (Exception e)
      {
         String title = getText() + PSMessages.getString(
               "PSEnableDisableContentTypeAction.error.actionfailed"); //$NON-NLS-1$
         String message = PSMessages.getString(
               "PSEnableDisableContentTypeAction.error.failedto") //$NON-NLS-1$
               + getText() 
               + PSMessages.getString(
               "PSEnableDisableContentTypeAction.error.contenteditor"); //$NON-NLS-1$ 
         PSWorkbenchPlugin.handleException(
               "Enable or disable editor action", title, //$NON-NLS-1$
               message, e);
      }
   }

   /**
    * Returns the Application reference corresponding to the supplied content
    * type reference.
    * 
    * @param cTypeRef Content type reference.
    * @return Application reference or <code>null</code>, if not found.
    * @throws PSModelException in case of an error.
    */
   private IPSReference getApplicationsReference(IPSReference cTypeRef)
         throws PSModelException
   {
      IPSReference appRef = null;
      if (cTypeRef == null
            || !(cTypeRef.getObjectType().getPrimaryType().equals(PSObjectTypes.CONTENT_TYPE)))
         return null;
      String ctypeAppName = "psx_ce" + cTypeRef.getName(); //$NON-NLS-1$
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(
            PSObjectTypes.XML_APPLICATION);
      Collection<IPSReference> refs = model.catalog();
      for (IPSReference ref : refs)
      {
         if (ref.getName().equalsIgnoreCase(ctypeAppName))
         {
            appRef = ref;
            break;
         }
      }
      return appRef;
   }
}
