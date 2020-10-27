/******************************************************************************
 *
 * [ PSTemplateSitesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.controls.PSSitesControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows user to select sites for the template.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateSitesPage extends Composite
{
   public PSTemplateSitesPage(Composite parent, int style, PSEditorBase editor)
   {
      super(parent, style);
      setLayout(new FormLayout());
      
      m_sitesControl = createSitesControl(editor);
   }

   /**
    * Creates and initializes site selection UI.
    */
   private PSSitesControl createSitesControl(PSEditorBase editor)
   {
      final PSSitesControl sitesControl = new PSSitesControl(this, SWT.NONE);

      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      formData.bottom = new FormAttachment(100, 0);
      sitesControl.setLayoutData(formData);
      
      editor.registerControl("PSSitesControl.label.selectedSites",
            sitesControl.getSelectionControl(), null);
      return sitesControl;
   }

   /**
    * Initializes control with template data.
    */
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      m_sitesControl.loadControlValues(template);
   }

   /**
    * Updates template with the UI data.
    * @param template template to update.
    */
   public void updateTemplate(final PSUiAssemblyTemplate template)
   {
      m_sitesControl.updateTemplate(template);
   }

   /**
    * Manages the char sets dropdown.
    */
   private PSSitesControl m_sitesControl;
   
   @SuppressWarnings("unused")
   private List<IPSReference> m_siteRefs = new ArrayList<IPSReference>();
}
