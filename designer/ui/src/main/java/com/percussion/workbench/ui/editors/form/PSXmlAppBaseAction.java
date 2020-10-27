/******************************************************************************
 *
 * [ PSXmlAppBaseAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.E2DesignerResources;
import com.percussion.E2Designer.OSApplication;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.ResourceHelper;
import com.percussion.E2Designer.UIMainFrame;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSXmlApplicationModel;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.actions.PSBaseSelectionListenerAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

/**
 * Abstract base class for legacy XML application actions.
 *
 * @author Andriy Palamarchuk
 */
public abstract class PSXmlAppBaseAction extends PSBaseSelectionListenerAction
{
   /**
    * Creates new action.
    * @param site current site. Can be <code>null</code>. In case the
    * application should be provided through
    * {@link #setApplication(OSApplication)}.
    */
   public PSXmlAppBaseAction(IWorkbenchSite site)
   {
      super("");
      final String id = getId();
      configureAction(id);
      m_site = site;
   }

   /**
    * Configures action for the specified id.
    */
   protected void configureAction(final String id)
   {
      setText(getActionName(id));
      setImageDescriptor(ResourceHelper.getIcon2(getResources(), id));
      setAccelerator(ResourceHelper.getAccelKey2(getResources(), id));
      setToolTipText(ResourceHelper.getToolTipText(getResources(), id));
   }

   /**
    * Loads the application from the server or takes the application provided
    * in {@link #setApplication(PSApplication)}.
    * Application is locked for editing.
    * If no application was provided to the action must be called in SWT event
    * handling thread.
    */
   protected PSApplication loadApplication()
   {
      if (getApplication() != null)
      {
         return getApplication();
      }

      if (!isEnabled())
      {
         return null;
      }
      final IPSReference ref = getSelectedRef();
      if (ref == null)
      {
         return null;
      }
      try
      {
         return (PSApplication) getAppModel().load(ref, true, true);
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
      }
      return null;
   }

   /**
    * Application model.
    */
   protected IPSXmlApplicationModel getAppModel() throws PSModelException
   {
      return (IPSXmlApplicationModel) PSCoreFactory.getInstance().getModel(
            PSObjectTypes.XML_APPLICATION);
   }

   /**
    * Subclasses must provide hardcoded value.
    */
   @Override
   public abstract String getId();
   
   /**
    * Returns action name for the specified id.
    */
   private String getActionName(final String id)
   {
      return ResourceHelper.getWithMnemonic(
            getResources().getString(id), getResources(), id);
   }

   /**
    * Convenience method to access designer resources.
    */
   private static E2DesignerResources getResources()
   {
      return E2Designer.getResources();
   }
   
   /**
    * Reference selected in the tree.
    * @return <code>null</code> if no app reference is selected.
    */
   protected IPSReference getSelectedRef()
   {
      if (!(m_site.getSelectionProvider().getSelection() instanceof
            IStructuredSelection))
      {
         return null;
      }
      final IStructuredSelection selection =
         (IStructuredSelection) m_site.getSelectionProvider().getSelection();
      if (selection.size() != 1)
      {
         return null;
      }

      final Object o = selection.getFirstElement();
      if (!(o instanceof PSUiReference))
      {
         return null;
      }
      
      final PSUiReference node = (PSUiReference) o;
      if (node.getReference() == null || node.isFolder())
      {
         return null;
      }
      
      if (!(node.getObjectType().getPrimaryType().equals(
            PSObjectTypes.XML_APPLICATION)))
      {
         return null;
      }
      return node.getReference();
   }

   /**
    * Application to show dialog for.
    */
   public PSApplication getApplication()
   {
      return m_application;
   }

   /**
    * Application to show dialog for.
    * If the application is not specified the action will retrieve application
    * from current selection.
    */
   public void setApplication(PSApplication application)
   {
      m_application = application;
   }
   
   /**
    * Current main frame.
    * @return Never <code>null</code>.
    */
   protected UIMainFrame getMainFrame()
   {
      return E2Designer.getApp().getMainFrame();
   }

   /**
    * The action site.
    * @return Never <code>null</code>.
    */
   protected IWorkbenchSite getSite()
   {
      return m_site;
   }

   /**
    * Application to show dialog for.
    */
   private PSApplication m_application;

   /**
    * Used to obtain the selection when the action is run. Never
    * <code>null</code> or modified after ctor.
    */
   private final IWorkbenchSite m_site;
}
