/******************************************************************************
 *
 * [ PSRelationshipTypeEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.design.objectstore.PSRelationshipConfig;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a multi-tabbed UI for modifying a relationship type design object.
 * 
 * @author Paul Howard
 * @author Andriy Palamarchuk
 */
public class PSRelationshipTypeEditor extends PSMultiPageEditorBase
{
   @Override
   protected void createPages()
   {
      addGeneralPage();
      addPropertiesPage();
      addCloningPage();
      addEffectsPage();
   }

   /**
    * Adds general page.
    */
   private void addGeneralPage()
   {
      m_generalPage = new PSRelTypeGeneralPage(getContainer(), SWT.NONE, this);
      final int idx = addPage(m_generalPage);
      setPageText(idx, GENERAL_LABEL_KEY);
      registerControl(GENERAL_LABEL_KEY, m_generalPage, null);
   }

   /**
    * Adds page to specify relationship type properties.
    */
   private void addPropertiesPage()
   {
      m_propertiesPage =
            new PSRelTypePropertiesPage(getContainer(), SWT.NONE, this);
      final int idx = addPage(m_propertiesPage);
      setPageText(idx, PROPERTIES_LABEL_KEY);
      registerControl(PROPERTIES_LABEL_KEY, m_propertiesPage, null);
   }

   /**
    * Adds page to specify cloning settings.
    */
   private void addCloningPage()
   {
      m_cloningPage = new PSRelTypeCloningPage(getContainer(), SWT.NONE, this);
      final int idx = addPage(m_cloningPage);
      setPageText(idx, CLONING_LABEL_KEY);
      registerControl(CLONING_LABEL_KEY, m_cloningPage, null);
   }

   /**
    * Adds page to specify effects.
    */
   private void addEffectsPage()
   {
      m_effectsPage = new PSRelTypeEffectsPage(getContainer(), SWT.NONE, this);
      final int idx = addPage(m_effectsPage);
      setPageText(idx, EFFECTS_LABEL_KEY);
      registerControl(EFFECTS_LABEL_KEY, m_effectsPage, null);
   }

   @Override
   public boolean isValidReference(IPSReference ref)
   {
      return ref.getObjectType().getPrimaryType()
            .equals(PSObjectTypes.RELATIONSHIP_TYPE);
   }

   public void loadControlValues(Object designObject)
   {
      final PSRelationshipConfig relType = (PSRelationshipConfig) designObject; 
      m_generalPage.loadControlValues(relType);
      m_propertiesPage.loadControlValues(relType);
      m_cloningPage.loadControlValues(relType);
      m_effectsPage.loadControlValues(relType);
   }

   public void updateDesignerObject(Object designObject,
         @SuppressWarnings("unused") Object control) //$NON-NLS-1$
   {
      final PSRelationshipConfig relType = (PSRelationshipConfig) designObject; 
      m_generalPage.updateRelType(relType);
      m_propertiesPage.updateRelType(relType);
      m_propertiesPage.updateRelType(relType);
      m_cloningPage.updateRelType(relType);
      m_effectsPage.updateRelType(relType);
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSMultiPageEditorBase#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   @Override
   public String getHelpKey(Control control)
   {         
      String[] rawkey = super.getHelpKey(control).split("_"); //$NON-NLS-1$
      String postfix = ms_helpMappings.get(rawkey[1]);
      if (StringUtils.isBlank(postfix))
      {
         postfix = "general"; //$NON-NLS-1$
      }
      return rawkey[0] + '_' + postfix;      
   }  

   /**
    * General tab text
    */
   private static final String GENERAL_LABEL_KEY =
         "PSRelationshipTypeEditor.label.tab.general"; //$NON-NLS-1$

   /**
    * Properties tab text
    */
   private static final String PROPERTIES_LABEL_KEY =
         "PSRelationshipTypeEditor.label.tab.properties"; //$NON-NLS-1$
   
   /**
    * Properties tab text
    */
   private static final String CLONING_LABEL_KEY =
        "PSRelationshipTypeEditor.label.tab.cloning"; //$NON-NLS-1$
   
   /**
    * Effects tab text
    */
   private static final String EFFECTS_LABEL_KEY =
         "PSRelationshipTypeEditor.label.tab.effects"; //$NON-NLS-1$

   /**
    * General page of the relationship type.
    */
   private PSRelTypeGeneralPage m_generalPage; 
   
   /**
    * Allows to edit list of properties.
    */
   private PSRelTypePropertiesPage m_propertiesPage;
   
   /**
    * Cloning settings page.
    */
   private PSRelTypeCloningPage m_cloningPage; 
   
   /**
    * Effects configuration page
    */
   private PSRelTypeEffectsPage m_effectsPage; 
   
   // Help key mappings 
   private static final Map<String, String> ms_helpMappings = 
      new HashMap<String, String>();
   static
   {
      ms_helpMappings.put(
         CLONING_LABEL_KEY, "cloning"); //$NON-NLS-1$
      ms_helpMappings.put(
         EFFECTS_LABEL_KEY, "effects"); //$NON-NLS-1$
      ms_helpMappings.put(
         PROPERTIES_LABEL_KEY, "properties"); //$NON-NLS-1$
      ms_helpMappings.put(
         GENERAL_LABEL_KEY, "general"); //$NON-NLS-1$
   }
}
