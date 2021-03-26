/******************************************************************************
 *
 * [ PSActionMenuEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a multi-tabbed UI for modifying an action menu design object.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSActionMenuEditor extends PSMultiPageEditorBase
{

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSMultiPageEditorBase#
    * createPages()
    */
   @Override
   protected void createPages()
   {
      createGeneralPage(getPageCount());
      createUsagePage(getPageCount());
      if(m_isDynamicMenu || m_isMenuEntry)
         createCommandPage(getPageCount());
      createVisibilityPage(getPageCount());      
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * isValidReference(com.percussion.client.IPSReference)
    */
   @Override
   public boolean isValidReference(IPSReference ref)
   {
      return ref.getObjectType().getPrimaryType().equals(
            PSObjectTypes.UI_ACTION_MENU);
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * loadControlValues()
    */
   public void loadControlValues(Object designObject)
   {
      if(m_isMenuEntry)
      {
         m_generalComposite.loadControlValues(designObject);
      }
      else
      {
         m_generalComposite.loadControlValues(designObject);
      }
      m_usageComposite.loadControlValues(designObject);
      m_visibilityComposite.loadControlValues(designObject);
      if(m_commandComposite != null)
      {
         m_commandComposite.loadControlValues(designObject);
      }
      
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      m_generalComposite.updateDesignerObject(designObject, control);
      m_usageComposite.updateDesignerObject(designObject, control);
      m_visibilityComposite.updateDesignerObject(designObject, control);
      if(m_hasCommandPage)
         m_commandComposite.updateDesignerObject(designObject, control);
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSMultiPageEditorBase#
    * init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
    */
   @Override
   public void init(IEditorSite site, IEditorInput input) 
      throws PartInitException
   {
      super.init(site, input);
      PSAction action = (PSAction)m_data;
      m_isMenuEntry = action.isMenuItem();
      m_isDynamicMenu = action.isDynamicMenu();
   }
   
   /**
    * Create the composite for the general tab
    * @param page the page index
    */
   private void createGeneralPage(int page)
   {
      
      if(m_isMenuEntry)
      {
         m_generalComposite = 
            new PSActionMenuEntryGeneralComposite(
               getContainer(), SWT.NONE, this, page);
         
      }
      else
      {
         m_generalComposite = 
            new PSActionMenuGeneralComposite(
               getContainer(), SWT.NONE, this, page);
      }
      addPage((Control)m_generalComposite);
      setPageText(page, 
         "PSActionMenuEditor.tab.general.label"); //$NON-NLS-1$
   }
   
   /**
    * Create the composite for the usage tab
    * @param page the page index
    */
   private void createUsagePage(int page)
   {
      m_usageComposite = new PSActionMenuUsageComposite(
         getContainer(), SWT.NONE, this, page);
      addPage(m_usageComposite);
      setPageText(page, 
         "PSActionMenuEditor.tab.usage.label"); //$NON-NLS-1$
   }
   
   /**
    * Create the composite for the command tab 
    * @param page the page index
    */
   protected void createCommandPage(int page)
   {
      m_commandComposite = new PSActionMenuCommandComposite(
         getContainer(), SWT.NONE, this, page);
      addPage(m_commandComposite);
      setPageText(page, 
         "PSActionMenuEditor.tab.command.label"); //$NON-NLS-1$
      m_hasCommandPage = true;
   }
   
   /**
    * Toggles the command tab by removing or adding the appropriate 
    * pages.
    * @param add a flag that operation should be an "add" operation
    * if the flag is set to <code>true</code>
    */
   protected void toggleCommandPage(boolean add)
   {
      if((!add && !m_hasCommandPage) || (add && m_hasCommandPage))
         return;
      if(add)
      {
         removePage(2);
         createCommandPage(getPageCount());
         createVisibilityPage(getPageCount());
         m_visibilityComposite.loadControlValues(m_data);
      }
      else
      {
         removePage(2);
         m_hasCommandPage = false;
      }
   }    
   
   /**
    * Create the composite for the visibility tab
    * @param page the page index
    */
   private void createVisibilityPage(int page)
   {
      m_visibilityComposite = new PSActionMenuVisibilityComposite(
         getContainer(), SWT.NONE, this, page);
      addPage(m_visibilityComposite);
      setPageText(page, 
         "PSActionMenuEditor.tab.visibility.label"); //$NON-NLS-1$
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSMultiPageEditorBase#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   @Override
   public String getHelpKey(Control control)
   {         
      String[] rawkey = super.getHelpKey(control).split("_");
      String postfix = ms_helpMappings.get(rawkey[1]);
      if(StringUtils.isBlank(postfix))
         postfix = "general";
      return rawkey[0] + "_" + postfix;
      
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * getHelpHintKey(com.percussion.workbench.ui.util.PSControlInfo)
    */
   @Override
   protected String getHelpHintKey(PSControlInfo controlInfo)
   {
      if(m_helpHintKeyHelper == null)
      {
         m_helpHintKeyHelper = new PSHelpHintKeyHelper(new String[]
         {
            "PSNameLabelDesc.label.label", "",
            "PSNameLabelDesc.label.description", "",
            "PSMenuActionGeneralCommonComposite.accelerator.label", "accelerator",
            "PSMenuActionGeneralCommonComposite.mnemonic.label", "mnemonic",
            "PSMenuActionGeneralCommonComposite.tooltip.label", "tooltip",
            "PSMenuActionGeneralCommonComposite.iconPath.label", "icon_path",
            "PSMenuActionGeneralCommonComposite.sortrank.label", "sort_rank",
            "PSActionMenuCommandComposite.commandGroup.label", "command_group",
            "PSActionMenuCommandComposite.url.label", "url",
            "PSActionMenuCommandComposite.parameters.label", "",
            "PSUrlParamTableComposite.col.name.label", "name",
            "PSUrlParamTableComposite.col.value.label", "value"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Flag indicating that the underlying designer object is a menu entry.
    * Set in {@link #init(IEditorSite, IEditorInput)}.
    */
   private boolean m_isMenuEntry;
   
   /**
    * Flag indicating that the underlying designer object is a dynamic menu.
    * Set in {@link #init(IEditorSite, IEditorInput)}.
    */
   private boolean m_isDynamicMenu;
   
   /**
    * A flag indicating that the command page is present.
    */
   private boolean m_hasCommandPage;
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;
   
   
   // Page composites
   private IPSDesignerObjectUpdater m_generalComposite;
   private PSActionMenuUsageComposite m_usageComposite;
   private PSActionMenuCommandComposite m_commandComposite;
   private PSActionMenuVisibilityComposite m_visibilityComposite;
   
   //Help key mappings 
   private static final Map<String, String> ms_helpMappings = 
      new HashMap<String, String>();
   static
   {
      ms_helpMappings.put(
         "PSActionMenuEditor.tab.general.label", "general");
      ms_helpMappings.put(
         "PSActionMenuEditor.tab.usage.label", "usage");
      ms_helpMappings.put(
         "PSActionMenuEditor.tab.command.label", "command");
      ms_helpMappings.put(
         "PSActionMenuEditor.tab.visibility.label", "visibility");
   }
   
   
   
   
   
   

}
