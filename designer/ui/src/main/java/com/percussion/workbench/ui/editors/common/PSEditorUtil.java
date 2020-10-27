/******************************************************************************
 *
 * [ PSEditorUtil.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.common;

import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.catalogers.PSCatalogDatasources;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A place for some common editor and wizard methods
 */
public class PSEditorUtil
{

   private PSEditorUtil()
   {
      
   }
   
   /**
    * Get the field catalog from the server via the model. 
    * @param forDisplayFormat if <code>true</code> then will retrieve a
    * catalog specifically for Display formats.
    * @return The field catalog, includes hidden fields.  An error dialog is
    * displayed if it cannot be loaded and <code>null</code> is returned.
    */
   public static PSContentEditorFieldCataloger getCEFieldCatalog(
      boolean forDisplayFormat)
   {
      PSContentEditorFieldCataloger cat = null;
      try
      {
         PSCoreFactory factory = PSCoreFactory.getInstance();
         IPSContentTypeModel model = 
            (IPSContentTypeModel)factory.getModel(PSObjectTypes.CONTENT_TYPE);
         cat = model.getCEFieldCatalog(false, forDisplayFormat);
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString(
               "PSSearchEditor.error.catalogingContentEditorFields"),   //$NON-NLS-1$
            PSMessages.getString("common.error.title"), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      return cat;
     
   }
   
   /**
    * Update or add database bindings to a template based on the selected
    * database resource.
    * @param datasource cannot be <code>null</code> or empty.
    * @param template cannot be <code>null</code>.
    */
   public static void updateTemplateDbBindings(String datasource,
      PSUiAssemblyTemplate template)
   {
      if(StringUtils.isBlank(datasource))
         throw new IllegalArgumentException("datasource cannot be null or empty.");
      if(template == null)
         throw new IllegalArgumentException("template cannot be null.");
      List<PSTemplateBinding> bindings =
         new java.util.ArrayList<>(template.getBindings());
      Map<String, String> ds = 
         PSCatalogDatasources.getDataSourceByName(datasource, false);
      if(ds != null)
      {
         modifyTemplateBinding(bindings, "$db.action", "r", false);
         String origin = ds.get(PSCatalogDatasources.ORIGIN);
         modifyTemplateBinding(bindings, "$db.origin", origin, true);
         String resource = ds.get(PSCatalogDatasources.JNDI_DATASOURCE_NAME);
         modifyTemplateBinding(bindings, "$db.resource", resource, true);
         String url = ds.get(PSCatalogDatasources.JDBC_URL);
         String type = StringUtils.isBlank(url) ?
            null : PSJdbcUtils.getDriverFromUrl(url);               
         modifyTemplateBinding(bindings, "$db.drivertype", type, true);
         String database = ds.get(PSCatalogDatasources.DATABASE);
         modifyTemplateBinding(bindings, "$db.database", database, true);
         List<String> tables = getTablesFromSource(template);
         if(tables.size() > 0)
         {
            // Use the first table as the parent
            modifyTemplateBinding(bindings, "$db.parent", tables.get(0), true);
         }
         template.setBindings(bindings);
      }
      
   }
   
   /**
    * Helper method to retrieve a list of tables that exist in the
    * table def source for this template.
    * @param template the assembly template, cannot be <code>null</code>.
    * @return list of tables in source, never <code>null</code> may
    * be empty.
    */
   public static List<String> getTablesFromSource(PSUiAssemblyTemplate template)
   {
      if(template == null)
         throw new IllegalArgumentException("template cannot be null.");
      List<String> tables = new ArrayList<String>();
      String source = template.getTemplate();
      if(StringUtils.isNotBlank(source))
      {
         try
         {
            Document doc = 
               PSXmlDocumentBuilder.createXmlDocument(new StringReader(source), false);
            Element root = doc.getDocumentElement();
            NodeList nl = root.getElementsByTagName("tabledef");
            int len = nl.getLength();
            for(int i = 0 ; i < len; i++)
            {
               Element current = (Element)nl.item(i);
               tables.add(current.getAttribute("name"));
            }            
         }
         catch (Exception e)
         {
           e.printStackTrace();
         }
         
      }
      return tables;
   }
   
   /**
    * Helper method to add or modify a binding to a template.
    * @param bindings assumed not <code>null</code>.
    * @param name the binding variable name, assumed not <code>null</code>
    * or empty.
    * @param expression the binding expression, may be <code>null</code> 
    * or empty. If <code>null</code> or empty  and overwrite is 
    * <code>true</code> then we will remove any existing binding
    * of the supplied name.
    * @param overwrite flag indicating that we can overwrite the expression
    * of the binding if it already exists.
    */
   private static void modifyTemplateBinding(List<PSTemplateBinding> bindings,
      String name,
      String expression, boolean overwrite)
   {
      boolean remove = false;
      if(StringUtils.isBlank(expression))
      {
         remove = true;
      }
      else
      {
        // Make sure the expression is surrounded by quotes.
         if(!expression.startsWith("\""))
            expression = "\"" + expression;
         if(!expression.endsWith("\""))
            expression = expression + "\"";
      }
      PSTemplateBinding existing = null;
      for(PSTemplateBinding binding : bindings)
      {
         if(name.equals(binding.getVariable()))
         {
            existing = binding; 
            break;
         }
      }
      if(existing != null)
      {
         if(overwrite)
         {
            if(remove)
            {
               bindings.remove(existing);
            }
            else
            {
               ((PSTemplateBinding)existing).setExpression(expression);
            }
         }
      }
      else
      {
         PSTemplateBinding newBinding = new PSTemplateBinding();
         newBinding.setVariable(name);
         newBinding.setExpression(expression);
         newBinding.setExecutionOrder(bindings.size() + 1 );
         bindings.add(newBinding);
      }
      
   }

}
