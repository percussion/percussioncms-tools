/******************************************************************************
 *
 * [ PSTdGeneratorDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.editors.dialog;

import com.percussion.client.catalogers.PSCatalogDatasources;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateBinding;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.common.PSEditorUtil;
import com.percussion.workbench.ui.editors.common.PSTdBuilderComposite;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog used to regenerate the table definition xml based on selected
 * tables.
 * @author erikserating
 *
 */
public class PSTdGeneratorDialog extends PSDialog implements IPSUiConstants
{

   /**
    * @param parentShell
    */
   public PSTdGeneratorDialog(Shell parentShell, 
      PSUiAssemblyTemplate template)
   {
      super(parentShell);
      m_template = template;
      
   }

   /* (non-Javadoc)
    * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {
      // FIXME Auto-generated method stub
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new FormLayout());
      m_comp = new PSTdBuilderComposite(container);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      formData.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      m_comp.setLayoutData(formData);
      setControlValues();
      return container;
   }

   /* (non-Javadoc)
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @Override
   protected void okPressed()
   {
      try
      {
         m_source = m_comp.generateSchemaXml();
         m_datasource = m_comp.getResourceCombo().getItem(
            m_comp.getResourceCombo().getSelectionIndex());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      super.okPressed();
   }
   
   
   
   private void setControlValues()
   {
      String datasource = getDatasourceFromBindings(m_template);
      if(StringUtils.isBlank(datasource))
         return;
      List<String> tables = PSEditorUtil.getTablesFromSource(m_template);
      m_comp.setValues(datasource, tables, doesSourceAllowSchemaChanges());
   }
   
   /**
    * Determine from the source if allow schema changes is set.
    * @return <code>true</code> if sourcea allow schema changes.
    */
   private boolean doesSourceAllowSchemaChanges()
   {
      String source = m_template.getTemplate();
      if(StringUtils.isNotBlank(source))
      {
         try
         {
            Document doc = 
               PSXmlDocumentBuilder.createXmlDocument(new StringReader(source), false);
            Element root = doc.getDocumentElement();
            NodeList nl = root.getElementsByTagName("tabledef"); //$NON-NLS-1$
            int len = nl.getLength();
            if(len > 0)
            {
               Element current = (Element)nl.item(0);
               return "y".equals(current.getAttribute("allowSchemaChanges")); //$NON-NLS-1$ //$NON-NLS-2$
            }            
         }
         catch (Exception e)
         {
           e.printStackTrace();
         }
         
      }
      return false;
   }   
   
   /**
    * Figures out the selected data source from the existing bindings.
    * @param template cannot be <code>null</code>.
    * @return the datasource name if found or <code>null</code> if not.
    */
   public static String getDatasourceFromBindings(PSUiAssemblyTemplate template)
   {
      if(template == null)
         throw new IllegalArgumentException("template cannot be null.");
      Map<String, String> bindings = new HashMap<String, String>();
      for(IPSTemplateBinding binding : template.getBindings())
      {
         if(StringUtils.isNotBlank(binding.getVariable()))
            bindings.put(binding.getVariable(), binding.getExpression());
      }
      String resource = StringUtils.strip(bindings.get("$db.resource"), "\""); //$NON-NLS-1$
      if(StringUtils.isBlank(resource))
         return null;
      String db = StringUtils.strip(StringUtils.defaultString(bindings.get(
            "$db.database")), "\""); //$NON-NLS-1$
      List<Map<String, String>> sources = 
         PSCatalogDatasources.getCatalogAllInfo(null, false, true);
      for(Map<String, String> source : sources)
      {
         if(resource.equals(
               source.get(PSCatalogDatasources.JNDI_DATASOURCE_NAME)) &&
            db.equals(StringUtils.defaultString(
               source.get(PSCatalogDatasources.DATABASE))))
         return source.get(PSCatalogDatasources.DATASOURCE_NAME);  
      }
      return null;
   }
   
   /**
    * Returns the generated database schema if it was generated.
    * @return the source or <code>null</code> if not generated.
    */
   public String getSource()
   {
      return m_source;
   }
   
   /**
    * Returns the selected datasource
    * @return may be <code>null</code>.
    */
   public String getDatasource()
   {
      return m_datasource;
   }
   
   // Overriden to set the title.
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(PSMessages.getString("PSTdGeneratorDialog.title")); //$NON-NLS-1$
   }
   
   /**
    * Initialized in {@link #createDialogArea(Composite)}
    */
   private PSTdBuilderComposite m_comp;
   
   /** 
    * Holds the value of the generated xml source. Initialized in
    * {@link #okPressed()}. May be <code>null</code> or empty.
    */
   private String m_source;
   
   /**
    * Holds the selected datasource. Initialized in
    * {@link #okPressed()}. May be <code>null</code> or empty.
    */
   private String m_datasource;
   
   /**'
    * The assembly template, initialized in ctor, never
    * <code>null</code> after that.
    */
   private PSUiAssemblyTemplate m_template;

  

}
