/******************************************************************************
 *
 * [ PSNewRxResourceAction.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * This action is somewhat generic in that it can configure itself to create any
 * Rx design object. It chooses the object by looking at the supplied selection
 * and walking up the tree until it finds a home node. The type of the home node
 * determines the type presented by this action.
 *
 * @author paulhoward
 */
public class PSNewRxResourceAction extends PSBaseSelectionListenerAction
{
   private final IWorkbenchWindow m_window;

   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId()
         + ".NewRxResourceAction"; //$NON-NLS-1$

   /**
    * Ctor
    * @param provider Passed to super ctor.
    */
   public PSNewRxResourceAction(ISelectionProvider provider, IWorkbenchWindow win)
   {
      super("TobeReplaced", provider); //$NON-NLS-1$
      m_window = win;
      initWizardRegistrations(this);
   }

   /**
    * @inheritDoc
    * Gets the selection using the window supplied in the ctor. If this is an
    * <code>IStructuredSelection</code> with 1 entry, the branch is traversed
    * upwards until a node that is a home node is found. The object type for
    * this home node is used as the type of this action.
    */
   @Override
   public void run()
   {
      ms_logger.debug("PSNewRxResourceAction ran: " + isEnabled());
      if (!isEnabled())
         return;
      final IStructuredSelection ss = getStructuredSelection();
      assert(ss != null);
      
      final WizardInfo info = getWizardInfo(m_wizardType);
      final Exception[] exc = new Exception[1];
      
      // a home node is registered for a type, but there is no registered
      // wizard, should never happen
      if (info == null)
         return;
      
      final WizardDialog[] dialog = new WizardDialog[1];
      IWorkbench wb = PlatformUI.getWorkbench();
      IProgressService ps = wb.getProgressService();
      try
      {
         ps.runInUI(m_window, new IRunnableWithProgress() 
         {
            @SuppressWarnings("unused")
            public void run(IProgressMonitor pm) 
            {
               try
               {
                  INewWizard wizard = info.getWizard();
                  wizard.init(m_window.getWorkbench(), ss);
                  dialog[0] = new WizardDialog(null, wizard);
                  dialog[0].create();
//                  dialog[0].getShell().setSize(
//                          Math.max(SIZING_WIZARD_WIDTH, dialog[0].getShell().getSize().x),
//                          SIZING_WIZARD_HEIGHT);
                  
                  Rectangle r = dialog[0].getShell().getBounds();
                  Point p = centerRectangle(
                        dialog[0].getShell().getDisplay().getClientArea(), r);
                  dialog[0].getShell().setBounds(p.x, p.y, r.width, r.height);         
                  
                  //fixme assign help
//                  PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
//                          IIDEHelpContextIds.NEW_PROJECT_WIZARD);
               }
               catch (CoreException e)
               {
                  exc[0] = e;
               }
            }
         }, null);
      }
      catch (InvocationTargetException e)
      {
         exc[0] = e;
      }
      catch (InterruptedException e)
      {
         //should never happen as we don't allow cancel
         exc[0] = e;
      }
      if (exc[0] != null)
      {
         String title = PSMessages
               .getString("PSNewRxResourceAction.error.wizardCreate.title");
         String msg = PSMessages
               .getString("PSNewRxResourceAction.error.wizardCreate.message");
         PSWorkbenchPlugin.handleException("Wizard creation", title, msg,
               exc[0]);
         return;
      }
      
      // Open wizard.
      dialog[0].open();
   }

   /**
    * Calculates the point where the <code>toCenter</code> rectangle would be
    * centered within the <code>field</code> rectangle. If the toCenter
    * rectangle is bigger in either the x or y directions, that direction will
    * be set to 0.
    * 
    * @param field Assumed not <code>null</code>.
    * @param toCenter Assumed not <code>null</code>.
    * 
    * @return The x,y coordinate where the upper left hand corner of the 2nd
    * rectangle should be placed to center it.
    */
   private Point centerRectangle(Rectangle field, Rectangle toCenter)
   {
      int xResult = 0;
      int yResult = 0;
      if (toCenter.width < field.width)
         xResult = (field.width - toCenter.width) / 2;
      if (toCenter.height < field.height)
         yResult = (field.height - toCenter.height) / 2;
      return new Point(xResult, yResult);
   }
   
   /**
    * Walks the plugin registry for all wizards whose id begins with
    * "com.percussion.workbench.ui" and builds a local registry mapping them to
    * an object type. This is only done once.
    */
   private static synchronized void initWizardRegistrations(
         PSNewRxResourceAction action)
   {
      if (ms_wizardInfoMap != null)
         return;
      ms_wizardInfoMap = new HashMap<PSObjectType, WizardInfo>();
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      IExtensionPoint point = registry
            .getExtensionPoint("org.eclipse.ui.newWizards");
      if (point != null)
      {
         //an extension per plugin that has wizards
         IExtension[] extensions = point.getExtensions();
         boolean found = false;
         IConfigurationElement[] cfgs = null;
         for (int i = 0; i < extensions.length; i++)
         {
            cfgs = extensions[i].getConfigurationElements();
            if (cfgs.length<1)
                continue;
            String id = cfgs[0].getAttribute("id");
            if (StringUtils.isBlank(id)
                  || !id.startsWith("com.percussion.workbench.ui"))
            {
               continue;
            }
            found = true;
            break;
         }
         if (!found)
            return;
         
         for (int j=0; j < cfgs.length; j++)
         {
            if (!cfgs[j].getName().equals("wizard"))
               continue;
            PSObjectType[] types = getWizardObjectType(cfgs[j].getAttribute("id"));
            WizardInfo info = action.new WizardInfo(cfgs[j]);
            for (PSObjectType t : types)
            {
               ms_wizardInfoMap.put(t, info);
            }
         }
      }      
   }
   
   /**
    * Looks up the object types that can be created by a wizard whose fully
    * qualified id is supplied.
    * 
    * @param id The id of the wizard, as found in 'id' attribute of the
    * newWizards extension. Never <code>null</code> or empty.
    * 
    * @return The matching type.
    * 
    * @throws UnsupportedOperationException If a match is not found.
    */
   private static PSObjectType[] getWizardObjectType(String id)
   {
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSSlotWizard"))
      {
         return new PSObjectType[] 
         {
            PSObjectTypeFactory.getType(PSObjectTypes.SLOT)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSXmlApplicationWizard"))
      {
         return new PSObjectType[] 
         {
            PSObjectTypeFactory.getType(PSObjectTypes.XML_APPLICATION, 
                  PSObjectTypes.XmlApplicationSubTypes.USER)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSKeywordWizard"))
      {
         return new PSObjectType[] 
         {
            PSObjectTypeFactory.getType(PSObjectTypes.KEYWORD)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSContentTypeWizard"))
      {
         return new PSObjectType[] 
         {
            PSObjectTypeFactory.getType(PSObjectTypes.CONTENT_TYPE)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSSharedDefFileWizard"))
      {
         return new PSObjectType[] 
         {
            PSObjectTypeFactory.getType(PSObjectTypes.SHARED_FIELDS)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSLocaleWizard"))
      {
         return new PSObjectType[] 
         {
            PSObjectTypeFactory.getType(PSObjectTypes.LOCALE)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSItemFilterWizard"))
      {
         return new PSObjectType[] 
         {
            PSObjectTypeFactory.getType(PSObjectTypes.ITEM_FILTER)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSViewWizard"))
      {
         return new PSObjectType[] 
         {
               PSObjectTypeFactory.getType(PSObjectTypes.UI_VIEW, 
                     PSObjectTypes.SearchSubTypes.CUSTOM),
               PSObjectTypeFactory.getType(PSObjectTypes.UI_VIEW, 
                     PSObjectTypes.SearchSubTypes.STANDARD),
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSSearchWizard"))
      {
         return new PSObjectType[] 
         {
               PSObjectTypeFactory.getType(PSObjectTypes.UI_SEARCH, 
                     PSObjectTypes.SearchSubTypes.CUSTOM),
               PSObjectTypeFactory.getType(PSObjectTypes.UI_SEARCH, 
                     PSObjectTypes.SearchSubTypes.STANDARD),
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSCommunityWizard"))
      {
         return new PSObjectType[] 
         {
            PSObjectTypeFactory.getType(PSObjectTypes.COMMUNITY)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSDisplayFormatWizard"))
      {
         return new PSObjectType[] 
         {
            PSObjectTypeFactory.getType(PSObjectTypes.UI_DISPLAY_FORMAT)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSActionMenuWizard"))
      {
         return new PSObjectType[] 
         {
               PSObjectTypeFactory.getType(PSObjectTypes.UI_ACTION_MENU,
                     PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_USER),
               PSObjectTypeFactory.getType(PSObjectTypes.UI_ACTION_MENU,
                     PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_USER)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSActionMenuEntryWizard"))
      {
         return new PSObjectType[] 
         {
               PSObjectTypeFactory.getType(PSObjectTypes.UI_ACTION_MENU,
                     PSObjectTypes.UiActionMenuSubTypes.MENU_ENTRY_USER)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSTemplateWizard"))
      {
         return new PSObjectType[] 
         {
               PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                     PSObjectTypes.TemplateSubTypes.GLOBAL),
               PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                     PSObjectTypes.TemplateSubTypes.LOCAL),
               PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                     PSObjectTypes.TemplateSubTypes.SHARED),
         };
      }
      if (id.equalsIgnoreCase(
      "com.percussion.workbench.ui.editors.wizards.PSRelationshipTypeWizard"))
      {
         return new PSObjectType[] 
         {
            PSObjectTypeFactory.getType(PSObjectTypes.RELATIONSHIP_TYPE)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSVariantWizard"))
      {
         return new PSObjectType[] 
         {
               PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                     PSObjectTypes.TemplateSubTypes.VARIANT)
         };
      }
      if (id.equalsIgnoreCase(
            "com.percussion.workbench.ui.editors.wizards.PSExtensionRegistrationWizard"))
      {
         return new PSObjectType[] 
         {
            PSObjectTypeFactory.getType(PSObjectTypes.EXTENSION)
         };
      }
      
      throw new UnsupportedOperationException("Unknown wizard id: " + id);
   }

   /**
    * If the selection is the proper type and has the appropriate content, it
    * is walked looking for a home node. If one is found, it is returned. 
    * 
    * @param sel Assumed not <code>null</code>.
    * 
    * @return A valid node or <code>null</code> if not found.
    */
   private PSObjectType findHomeNode(ISelection sel)
   {
      if (!(sel instanceof IStructuredSelection))
         return null;
      IStructuredSelection ss = (IStructuredSelection) sel;
      if (ss.size() != 1
            || (ss.size() == 1 
                  && !(ss.getFirstElement() instanceof PSUiReference)))
      {
         return null;
      }
      
      PSUiReference node = (PSUiReference) ss.getFirstElement();
      PSUiReference testNode = node;
      do
      {
         /*
          * This property can be specified in the declarative hierarchy view
          * definition as a NodeProperty. It's value should be the serialized
          * form of a PSObjectType for the wizard short-cut you want to appear.
          */
         Object prop = testNode.getProperty("wizardShortCutType");
         if (prop != null && !StringUtils.isBlank(prop.toString()))
         {
            return new PSObjectType(prop.toString());
         }
         if (testNode.getHomeObjectType() != null)
            return testNode.getHomeObjectType();
         testNode = testNode.getParentNode();
      }
      while (testNode != null);
      return null;
   }
   
   /**
    * @inheritDoc
    * The selection must contain 1 item and it must be a {@link PSUiReference}
    * node that is a home node, or has an ancestor that is a home node to be
    * enabled.
    */
   @Override
   protected boolean updateSelection(IStructuredSelection sel)
   {
      //as documented by super class
      if (!super.updateSelection(sel))
         return false;
      
      if (sel == null)
         return false;
      
      m_wizardType = findHomeNode(sel);
      boolean found = m_wizardType != null;
      if (found)
      {
         WizardInfo info = getWizardInfo(m_wizardType);
         if (info != null)
         {
            setText(info.getLabel());
            String desc = info.getDescription();
            setDescription(desc);
            setToolTipText(desc);
            setImageDescriptor(info.getImageDescriptor());
         }
         else
            //home registered, but no wizard registered for that type
            found = false;
      }
      return found;
   }

   /**
    * Gets the info for the wizard used to create a new instance of the object
    * identified by the home object type of the supplied node.
    * 
    * @param wizardType The type of object to be created. Assumed not
    * <code>null</code>.
    * 
    * @return May be <code>null</code> if match not found.
    */
   private static WizardInfo getWizardInfo(PSObjectType wizardType)
   {
      if (ms_wizardInfoMap == null)
      {
         throw new IllegalStateException("Can't be called until wizard info initialized.");
      }
      
      return ms_wizardInfoMap.get(wizardType);
   }

   /**
    * A little class that knows how to extract information from a 'new wizard'
    * registry entry.
    * 
    * @author paulhoward
    */
   private class WizardInfo
   {
      /**
       * Create a new one, guaranteeing that the params are never
       * <code>null</code>.
       * 
       * @param cfg The descriptor from the plugin registry for a new wizard.
       * Never <code>null</code>.
       */
      public WizardInfo(IConfigurationElement cfg)
      {
         if (null == cfg)
         {
            throw new IllegalArgumentException("cfg cannot be null");  
         }
         m_cfg = cfg;
      }

      /**
       * Gets the description for this wizard registry entry.
       * @return The description, or "" if not found. Never <code>null</code>.
       */
      public String getDescription()
      {
         IConfigurationElement[] desc = m_cfg.getChildren("description");
         if (desc == null || desc.length == 0)
            return "";
         String d = desc[0].getValue();
         if (d == null)
            d = "";
         return d;
      }
      
      /**
       * Returns the descriptor for the icon specified in the configuration
       * supplied in the ctor. If one is not registered or it is not found, an
       * unknown icon image is returned.
       * 
       * @return Never <code>null</code>.
       * 
       * @throws IllegalStateException If no icon could be found.
       */
      public ImageDescriptor getImageDescriptor()
      {
         ImageDescriptor d = PSUiUtils.getImageDescriptor(m_cfg.getAttribute("icon"));
         if (d == null)
         {
            throw new IllegalStateException("Couldn't find unknown image icon.");
         }
         return d;
      }
      
      /**
       * Creates an instance of the class used to create some new design object.
       * 
       * @return Never <code>null</code>.
       *
       * @throws CoreException If there is no class name or it can't be 
       * instantiated.
       */
      public INewWizard getWizard()
         throws CoreException
      {
         return (INewWizard) m_cfg.createExecutableExtension("class");
      }
      
      /**
       * The text to use as the label of the menu option.
       * 
       * @return Never <code>null</code> or empty.
       * 
       * @throws IllegalStateException If there is no label.
       */
      public String getLabel()
      {
         String label = m_cfg.getAttribute("name");
         if (StringUtils.isBlank(label))
         {
            throw new IllegalStateException("Missing label for wizard: " + 
                  m_cfg.getAttribute("id"));
         }
         return label;
      }
      
      /**
       * Never <code>null</code> or empty after ctor.
       */
      private final IConfigurationElement m_cfg;
   }
   
   /**
    * In addition to the base class, there must be a valid type in the selection.
    */
   @Override
   public boolean isEnabled()
   {
      return m_wizardType != null && super.isEnabled();
   }
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static Logger ms_logger = LogManager.getLogger(PSNewRxResourceAction.class);
   
   /**
    * The wizard object type found when the selection was updated. Never
    * <code>null</code> if the action is enabled. If this action is run, a 
    * wizard that knows how to create a design object of this type will be
    * instantiated.
    */
   private PSObjectType m_wizardType;

   /**
    * Contains all wizard registrations mapped to the object types they can
    * create. Initialized in
    * {@link #initWizardRegistrations(PSNewRxResourceAction)}, then never
    * <code>null</code> or modified.
    */
   private static Map<PSObjectType, WizardInfo> ms_wizardInfoMap;
}
