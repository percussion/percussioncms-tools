/******************************************************************************
 *
 * [ PSCompletionProvider.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.util;

import com.percussion.client.PSCatalogUtils;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.cms.objectstore.client.PSLightWeightField;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionMethod;
import com.percussion.extension.PSExtensionMethodParam;
import com.percussion.extension.PSExtensionRef;
import com.percussion.utils.jexl.PSPredefinedJexlVariableDefs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides fields, methods, variables completions.
 * Is used for velocity editor. 
 *
 * @author Andriy Palamarchuk
 * @author Doug Rand
 */
public class PSCompletionProvider
{
   /**
    * The class log.
    */
   final static Log ms_log = LogFactory.getLog(PSCompletionProvider.class);

   /**
    * Creates new provider. Initializes completion lists. 
    * @throws PSModelException if fields loading failed.
    */
   public PSCompletionProvider() throws PSModelException
   {
      m_fieldCompletions.addAll(ms_predefinedFieldCompletions);
      loadFieldCompletions();
      try
      {
         loadMethodCompletions();
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         ms_log.error("Failed to load field completions", e);
      }
      m_varCompletions = PSPredefinedJexlVariableDefs
            .getPredefinedTemplateVarDefs();
   }

   /**
    * Attaches the completion data to the provided resource for the
    * velocity editor.
    * The data is stored as follows:
    * <ul>
    * <li>{@link #FIELDS_VEDIT_KEY} - List<String[]>
    * - each list element is an object array
    * with either one or two components. The first component is the field name.
    * The second, optional, component is a field description.</li>
    *
    * <li>{@link #VARIABLES_VEDIT_KEY} - List<String[]>
    * - each list element is an object array
    * with either two or three components. The first component is the variable
    * name. The second component is the type of the variable if known, or empty
    * is not known. The third, optional, component is a description.</li>
    * 
    * <li>{@link #METHODS_VEDIT_KEY} - List<Object[]>
    * - each list element is an object array
    * with three required elements. The first is the name of the method.
    * Never empty or <code>null</code>.
    * The second is the return type of the method or "void" if the method
    * doesn't return a value. Never empty or <code>null</code>.
    * The third is a description of the method. Can be empty.
    * This is followed
    * by one or more parameter descriptions. Each parameter description is an
    * object array with three values, the name of the parameter, the type of
    * the parameter and a description of the parameter.
    * The type and description can be empty.
    * </ul>
    * 
    * @param source the resource to attach the completions to.
    * 
    * @throws CoreException when attaching data to the resource fails.
    * 
    */
   public void attachVelocityEditorCompletionData(final IResource source)
         throws CoreException
   {
      source.setSessionProperty(
            new QualifiedName(VEDIT_KEY, FIELDS_VEDIT_KEY),
            m_fieldCompletions);
      source.setSessionProperty(
            new QualifiedName(VEDIT_KEY, METHODS_VEDIT_KEY),
            m_methodCompletions);
      source.setSessionProperty(
            new QualifiedName(VEDIT_KEY, VARIABLES_VEDIT_KEY),
            m_varCompletions);
      source.setSessionProperty(
            new QualifiedName(VEDIT_KEY, MACROS_VEDIT_KEY),
            ms_predefinedMacroCompletions);      
      source.setSessionProperty(
            new QualifiedName(VEDIT_KEY, "hidepreviewpage"),                    //$NON-NLS-1$
            Boolean.TRUE.toString());
   }
   
   /**
    * Loads system, shared, local field completions from model.
    *
    * @throws PSModelException on model error.
    */
   @SuppressWarnings("unchecked")
   private void loadFieldCompletions() throws PSModelException
   {
      // Catalog and set the field information for completion
      final PSContentEditorFieldCataloger cat =
            getContentTypeModel().getCEFieldCatalog(false, false);

      addFieldCompletions(cat.getSystemMap());
      addFieldCompletions(cat.getSharedMap());
      addFieldCompletions(cat.getLocalMap());
   }

   /**
    * Add the field data to the completion data for the current source
    * 
    * @param fielddata the fielddata, may be <code>null</code>
    */
   private void addFieldCompletions(Map<String, PSLightWeightField> fielddata)
   {
      if (fielddata != null)
      {
         for (String field : fielddata.keySet())
         {
            String element[] = new String[2];
            element[0] = "rx:" + field; //$NON-NLS-1$
            PSLightWeightField info = fielddata.get(field);
            element[1] = info.getDataType();
            m_fieldCompletions.add(element);
         }
      }
   }

   /**
    * Loads method completion from model.
    * @throws Exception upon an error.
    */
   private void loadMethodCompletions() throws Exception
   {
      // Catalog the JEXL and Velocity tools
      final List<PSExtensionDef> defs = PSCatalogUtils.catalogExtensions(
            "com.percussion.extension.IPSJexlExpression");                      //$NON-NLS-1$
      final List<Object[]> mcompletions = new ArrayList<Object[]>();
      for (PSExtensionDef def : defs)
      {
         PSExtensionRef ref = def.getRef();
         String name = ref.getExtensionName();
         String context = ref.getContext();
         String startname;
         if (context.endsWith("user/")) //$NON-NLS-1$
            startname = "$user"; //$NON-NLS-1$
         else if (context.endsWith("system/")) //$NON-NLS-1$
            startname = "$rx"; //$NON-NLS-1$
         else
            startname = "$tools"; //$NON-NLS-1$
         name = startname + "." + name + "."; //$NON-NLS-1$ //$NON-NLS-2$
         Iterator<PSExtensionMethod> miter = def.getMethods();

         while (miter.hasNext())
         {
            List<Object> md = new ArrayList<Object>();
            PSExtensionMethod method = miter.next();
            md.add(name + method.getName());
            md.add(method.getReturnType());
            md.add(method.getDescription());
            Iterator<PSExtensionMethodParam> piter = method.getParameters();
            while (piter.hasNext())
            {
               PSExtensionMethodParam param = piter.next();
               String parts[] = new String[3];
               parts[0] = param.getName();
               parts[1] = param.getType();
               parts[2] = param.getDescription();
               md.add(parts);
            }
            mcompletions.add(md.toArray());
         }
      }

      m_methodCompletions.addAll(mcompletions); 
   }

   /**
    * Convenience method to access a content type model.
    * @return the content type model. Never <code>null</code>.
    * @throws PSModelException if model loading fails or content type model
    * can't be found.
    */
   private IPSContentTypeModel getContentTypeModel() throws PSModelException
   {
      return (IPSContentTypeModel)
            PSCoreFactory.getInstance().getModel(PSObjectTypes.CONTENT_TYPE);
   }

   /**
    * Methods completions.
    * Initialized in constructor.
    * Never changed, never <code>null</code> after initialization.
    * @see #attachVelocityEditorCompletionData(IResource)
    */
   public List<Object[]> getMethodCompletions()
   {
      return m_methodCompletions;
   }

   /**
    * Variables completions.
    * Initialized in constructor.
    * Never changed, never <code>null</code> after initialization.
    * @see #attachVelocityEditorCompletionData(IResource)
    */
   public List<String[]> getVarCompletions()
   {
      return m_varCompletions;
   }

   /**
    * Key used to pass data to velocity editor.
    */
   static final String VEDIT_KEY = "vedit";         //$NON-NLS-1$

   /**
    * Subkey for fields completion data.
    * @see #attachVelocityEditorCompletionData(IResource)
    */
   static final String FIELDS_VEDIT_KEY = "fields"; //$NON-NLS-1$

   /**
    * Subkey for methods completion data.
    * @see #attachVelocityEditorCompletionData(IResource)
    */
   static final String METHODS_VEDIT_KEY = "methods"; //$NON-NLS-1$
   
   /**
    * Subkey for variables completion data.
    * @see #attachVelocityEditorCompletionData(IResource)
    */
   static final String VARIABLES_VEDIT_KEY = "variables"; //$NON-NLS-1$

   /**
    * Subkey for variables completion data.
    * @see #attachVelocityEditorCompletionData(IResource)
    */
   static final String MACROS_VEDIT_KEY = "macros";
   
   /**
    * Field completions for defined fields. Never <code>null</code> or empty.
    */
   private static Collection<String[]> ms_predefinedFieldCompletions;
   
   /**
    * Completion data for predefined macros available to users during
    * typing
    */
   private static List<String> ms_predefinedMacroCompletions =
      new ArrayList<String>();

   static
   {
      final List <String[]> predefinedFieldCompletions = new ArrayList<String[]>();
       
      predefinedFieldCompletions.add(new String[]
      {
            "nav:axis",                                                         //$NON-NLS-1$
            "java.lang.String",                                                 //$NON-NLS-1$
            "The axis of the managed navigation proxy node. "
            + "One of the following: "                                          //$NON-NLS-1$
            + "\"ANCESTOR\" - a grandparent, etc. of the self navon, "          //$NON-NLS-1$
            + "\"PARENT\" - the immediate parent of the self navon, "           //$NON-NLS-1$
            + "\"DESCENDANT\" - a child, grandchild, etc. of the self navon, "  //$NON-NLS-1$
            + "\"SELF\", "                                                      //$NON-NLS-1$
            + "\"NONE\" - no other category applies, "                          //$NON-NLS-1$
            + "\"SIBLING\" - the navon shares a parent with the self navon"});  //$NON-NLS-1$
      predefinedFieldCompletions.add(new String[]
      {"nav:url", "java.lang.String", "The landing page url"});                 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      predefinedFieldCompletions.add(new String[]
      {"nav:landingPage", "javax.jcr.Node", "The landing page item"});          //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      predefinedFieldCompletions.add(new String[]
      {"nav:leaf", "java.lang.Boolean",                                         //$NON-NLS-1$ //$NON-NLS-2$
            "Is this node a leaf node, i.e. has no children"});                 //$NON-NLS-1$
      predefinedFieldCompletions.add(new String[]
      {"nav:submenu", "javax.jcr.NodeIterator",                                 //$NON-NLS-1$ //$NON-NLS-2$
            "Contains all the navon children"});                                //$NON-NLS-1$
      predefinedFieldCompletions.add(new String[]
      {"nav:image", "javax.jcr.NodeIterator",                                   //$NON-NLS-1$ //$NON-NLS-2$
            "Contains all the image children"});                                //$NON-NLS-1$
      predefinedFieldCompletions.add(new String[]
      {"nav:selectedImage", "javax.jcr.Node",                                   //$NON-NLS-1$ //$NON-NLS-2$
            "The nav image selected by the selector"});                         //$NON-NLS-1$
      predefinedFieldCompletions.add(new String[]
      {"jcr:isCheckedOut", "java.lang.Boolean",                                 //$NON-NLS-1$ //$NON-NLS-2$
            "true if the item is checked out"});                                //$NON-NLS-1$

      ms_predefinedFieldCompletions =
            Collections.unmodifiableCollection(predefinedFieldCompletions);
   }

   static {
      ms_predefinedMacroCompletions.add("#displayfield(\"fieldname\")");
      ms_predefinedMacroCompletions.add("#field(\"fieldname\")");
      ms_predefinedMacroCompletions.add("#datefield(\"fieldname\" \"fieldformat\")");
      ms_predefinedMacroCompletions.add("#field_if_set(\"before\" \"field\" \"after\")");
      ms_predefinedMacroCompletions.add("#datefield_if_set(\"before\" \"field\" \"format\" \"after\")");
      ms_predefinedMacroCompletions.add("#slot_simple(\"slotname\")");
      ms_predefinedMacroCompletions.add("#slot_wrapped(\"slotname\" \"startslottext\" \"endslottext\")");
      ms_predefinedMacroCompletions.add("#slot(\"slotname\" \"header\" \"before\" \"after\" \"footer\" \"params\")");
      ms_predefinedMacroCompletions.add("#node_slot(\"node\" \"slotname\" \"header\" \"before\" \"after\" \"footer\" \"params\")");
      ms_predefinedMacroCompletions.add("#inner()");
      ms_predefinedMacroCompletions.add("#children(\"childname\" \"templatename\" \"header\" \"before\" \"after\" \"footer\")");
   }

   /**
    * Fields completions.
    * Initialized in constructor, never changed after that.
    * @see #attachVelocityEditorCompletionData(IResource)
    */
   private final List<String[]> m_fieldCompletions = new ArrayList<String[]>();

   /**
    * @see #getVarCompletions()
    */
   private final List<String[]> m_varCompletions;

   /**
    * @see #getMethodCompletions()
    */
   private final List<Object[]> m_methodCompletions = new ArrayList<Object[]>();
}
