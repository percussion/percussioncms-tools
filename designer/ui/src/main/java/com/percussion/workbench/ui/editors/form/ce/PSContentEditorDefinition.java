/******************************************************************************
 *
 * [ PSContentEditorDefinition.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.E2Designer.E2Designer;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSUserConfig;
import com.percussion.client.catalogers.PSSqlCataloger;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSFieldDefinition;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.design.objectstore.*;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Utility class to get the information required for editing content editor
 * resource.
 **/
public class PSContentEditorDefinition
{
   /**
    * Gets table locator used in system table definition. The table location is
    * same right now for all table sets in container locator of system table
    * definition. So takes the table location of first table set for system
    * table location.
    *
    * @return the table locator, never <code>null</code>
    * @throws Exception 
    */
   public static PSTableLocator getSystemTableLocator() throws Exception
   {
      PSTableLocator sysTableLocator = null;

      PSContentEditorSystemDef definition = getSystemDef();
      PSContainerLocator sysContainerLocator = definition.getContainerLocator();

      //get table set from system container locator
      Iterator sysTableIter = sysContainerLocator.getTableSets();
      while(sysTableIter.hasNext())
      {
         PSTableSet set = (PSTableSet)sysTableIter.next();
         sysTableLocator = set.getTableLocation();
         break;
      }

      return sysTableLocator;
   }


   /**
    * Gets the list of minimum required system columns for content editor
    * table used for <code>type</code> of fieldset.
    *
    * @return list of system column names, never <code>null</code> and empty.
    */
   public static List getSystemColumns(int type)
   {
      checkFieldSetType(type);

      if(type == PSFieldSet.TYPE_COMPLEX_CHILD)
         return ms_sysComplexChildColumns;
      else
         return ms_sysColumns;
   }

   /**
    * Gets the sort column name of child table used in content editor.
    *
    * @return the name, never <code>null</code> or empty.
    */
   public static String getChildSortColumn()
   {
      return IPSConstants.CHILD_SORT_KEY;
   }

   /**
    * Gets content editor controls and caches the controls in different sets
    * based on control type(dimension). All controls of 'table' dimension are
    * used for complex childs, 'array' dimension are used for simple child
    * fields and 'single' dimension are used for fields in parent or complex
    * childs or multiproperty simple childs.
    *
    * @param bCatalog if <code>true</code> forces to catalog from server,
    * otherwise gets from cache.
    *
    * @return A valid List of zero-or-more PSControlMeta objects
    * @throws PSModelException 
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public static List getContentEditorControls(boolean bCatalog) throws PSModelException
   {
      if(ms_controlList == null || ms_controlList.isEmpty() || bCatalog)
      {
         IPSContentTypeModel model = (IPSContentTypeModel) ms_factory
               .getModel(PSObjectTypes.CONTENT_TYPE);
         ms_controlList = model.getControls();
         ms_fieldControls = new ArrayList<>();
         ms_simpleChildFieldControls = new ArrayList<>();
         ms_complexChildControls = new ArrayList<>();

         Iterator iter = ms_controlList.iterator();
         while(iter.hasNext())
         {
            PSControlMeta control = (PSControlMeta)iter.next();
            if(control.getDimension().equals(PSControlMeta.TABLE_DIMENSION))
               ms_complexChildControls.add(control);
            else if(control.getDimension().equals(PSControlMeta.ARRAY_DIMENSION))
               ms_simpleChildFieldControls.add(control);
            else
               ms_fieldControls.add(control);
         }
      }

      return ms_controlList;
   }

   /**
    * Gets list of controls that can be used to map fields in parent, complex
    * child and multiproperty simple child fieldsets.
    *
    * @return the list of controls, never <code>null</code> may be empty if not
    * found.
    * @throws PSModelException 
    */
   public static List getFieldControls() throws PSModelException
   {
      if(ms_fieldControls == null)
      {
         getContentEditorControls(false);
      }

      return ms_fieldControls;
   }

   /**
    * Gets list of controls that can be used to map fields in simple child
    * fieldsets.
    *
    * @return the list of controls, never <code>null</code> may be empty if not
    * found.
    * @throws PSModelException 
    */
   public static List getSimpleChildFieldControls() throws PSModelException
   {
      if(ms_simpleChildFieldControls == null)
      {
         getContentEditorControls(false);
      }

      return ms_simpleChildFieldControls;
   }

   /**
    * Gets list of controls that can be used to map complex childs in parent
    * mapper.
    *
    * @return the list of controls, never <code>null</code> may be empty if not
    * found.
    * @throws PSModelException 
    */
   public static List getComplexChildControls() throws PSModelException
   {
      if(ms_complexChildControls == null)
      {
         getContentEditorControls(false);
      }

      return ms_complexChildControls;
   }

   /**
    * Gets control type from control name. Check is case sensitive.
    *
    * @param controlName the name of control, may not be <code>null</code> or
    * empty.
    *
    * @return the control type, may be <code>null</code> if it can not find the
    * type for passed in control name.
    * @throws PSModelException 
    *
    * @throws IllegalArgumentException if <code>controlName</code> is invalid.
    */
   public static String getControlType(String controlName) throws PSModelException
   {
      PSControlMeta control = getControl(controlName);
      if(control != null)
         return control.getDimension();
      else
         return null;
   }

   /**
    * Gets control meta object from control name. Check is case sensitive.
    *
    * @param controlName the name of control, may not be <code>null</code> or
    * empty.
    *
    * @return the control meta, may be <code>null</code> if it can not find the
    * control for passed in control name.
    * @throws PSModelException 
    *
    * @throws IllegalArgumentException if <code>controlName</code> is invalid.
    */
   public static PSControlMeta getControl(String controlName) throws PSModelException
   {
      if(controlName == null || controlName.trim().length() == 0)
         throw new IllegalArgumentException(
            "controlName can not be null or empty"); //$NON-NLS-1$

      PSControlMeta controlMeta = null;

      Iterator controls = getContentEditorControls(false).iterator();
      while(controls.hasNext())
      {
         PSControlMeta control = (PSControlMeta)controls.next();
         if(control.getName().equals(controlName))
         {
            controlMeta = control;
            break;
         }
      }

      return controlMeta;
   }


   /**
    * Converts the passed in name to proper case(the first letter of each word
    * is converted to uppercase), appends a colon(:), it replaces undeerscores
    * with blanks, and returns.
    * <p>
    * For example, if the name is 'first name' the returned string will be
    * 'First Name:'.
    *
    * @param name the name to be converted, may not be <code>null</code>, but
    * can be empty
    *
    * @return the converted name, never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>name</code> is <code>null</code>
    */
   public static String convertToProper(String name)
   {
      if(name == null )
         throw new IllegalArgumentException("Name can not be null"); //$NON-NLS-1$

      String newName = new String();

      if(name.trim().length() == 0)
         return newName;
      if(name.indexOf('_') != -1)
         name.replace('_' , ' ');

      StringTokenizer tokens = new StringTokenizer(name);
      int count = tokens.countTokens();
      for (int i = 0; i < count; i++)
      {
         String token = tokens.nextToken();
         String oldFirstChar = token.substring(0,1);
         String newFirstChar = oldFirstChar.toUpperCase();

         String str = newFirstChar + token.substring(1, token.length());
         if( (count - i ) > 1)
            newName += str + " "; //$NON-NLS-1$
         else
            newName += str;
      }
      if(!newName.endsWith(":")) //$NON-NLS-1$
         newName += ":"; //$NON-NLS-1$

      return newName;
   }

   /**
    * Finds whether the passed in column name is system column for the table
    * corresponding to the passed in type of the fieldset. Check is case
    * insensitive.
    *
    * @param colName the column name to check, may not be <code>null</code> or
    * empty.
    * @param type the type of fieldset, must be one of valid types of
    * <code>PSFieldSet</code>
    *
    * @return <code>true</code> if it is system column, otherwise
    * <code>false</code>
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public static boolean isSystemColumn(String colName, int type)
   {
      if(colName == null || colName.trim().length() == 0)
         throw new IllegalArgumentException("colName can not be null or empty"); //$NON-NLS-1$

      checkFieldSetType(type);

      boolean isSysColumn = false;
      if( (type == PSFieldSet.TYPE_COMPLEX_CHILD ||
         type == PSFieldSet.TYPE_SIMPLE_CHILD)  &&
         colName.equalsIgnoreCase(getChildSortColumn()) )
      {
         isSysColumn = true;
      }
      else
      {
         List sysColumns = getSystemColumns(type);

         Iterator columns = sysColumns.iterator();
         while(columns.hasNext())
         {
            String name = (String)columns.next();
            if(name.equalsIgnoreCase(colName))
            {
               isSysColumn = true;
               break;
            }
         }
      }
      return isSysColumn;
   }
   
   /**
    * Checks whether all values in <code>list2</code> are present in
    * <code>list1</code>. Assumes the objects in lists are Strings and check
    * is case insensitive.
    * <br>
    * If <code>list2</code> is empty it always returns <code>true</code>. If
    * <code>list1</code> is empty and <code>list2</code> is not empty it always
    * returns <code>false</code>
    *
    * @param list1 the list in which to check, may not be <code>null</code>,
    * may be empty.
    * @param list2 the list which to check, may not be <code>null</code>,
    * may be empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @return <code>true</code> if list1 contains all values of list2 otherwise
    * <code>false</code>
    */
   public static boolean containsAll(List list1, List list2)
   {
      if(list1 == null)
         throw new IllegalArgumentException("list1 can not be null"); //$NON-NLS-1$

      if(list2 == null)
         throw new IllegalArgumentException("list2 can not be null"); //$NON-NLS-1$

      boolean contains = true;

      Iterator list2Values = list2.iterator();

      while(list2Values.hasNext())
      {
         String list2Value = (String)list2Values.next();

         boolean exists = false;
         Iterator list1Values = list1.iterator();
         while(list1Values.hasNext())
         {
            String list1Value = (String)list1Values.next();
            if(list1Value.equalsIgnoreCase(list2Value))
            {
               exists = true;
               break;
            }
         }

         if(!exists)
         {
            contains = false;
            break;
         }
      }

      return contains;
   }

   /**
    * Guesses the default UI set for a field or child mapping. The label and 
    * error label are proper case of supplied field reference name. 
    * <br>
    * Lists the conditions for guessing the default control name.
    * <table border=1>
    * <tr><th>MappingType</th><th>FieldsetType</th><th>DefaultControl</th></tr>
    * <tr><td>field</td><td>SimpleChild</td><td>sys_CheckBoxGroup</td></tr>
    * <tr><td>field</td><td>All Types other than SimpleChild</td><td>sys_EditBox
    * </td></tr>
    * <tr><td>child</td><td>ignored</td><td>sys_Table</td></tr>
    * </table>
    * The default control is used if it is found in the list of available 
    * controls for the specified conditions, otherwise it uses the first control
    * in the list. If it didn't find any controls, then the control is not set
    * in the UI set.
    * 
    * @param fieldRefName the fieldref of mapping, may not be <code>null</code> 
    * or empty.
    *
    * @return the default UI set, never <code>null</code>
    * @throws PSModelException 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public static PSUISet getDefaultUISet(String fieldRefName, int editorType)
         throws PSModelException
   {
      if(fieldRefName == null || fieldRefName.trim().length() == 0)
         throw new IllegalArgumentException(
            "fieldRefName can not be null or empty"); //$NON-NLS-1$

      if(!isValidEditorType(editorType))
         throw new IllegalArgumentException(
         "invalid editorType"); //$NON-NLS-1$
      List controls = null;
      String defControlName = ms_defFieldControlName;   
      switch(editorType)
      {
         case LOCALDEF_PARENT_EDITOR:
            defControlName = ms_defFieldControlName;
            controls = getControls(ALL_CONTROLS);
            break;
         case LOCALDEF_CHILD_EDITOR:
            defControlName = ms_defFieldControlName;
            controls = getControls(SINGLE_DIM_CONTROLS);
            break;
         case SYSTEMDEF_EDITOR:
            defControlName = ms_defFieldControlName;
            controls = getControls(SINGLE_DIM_CONTROLS);
            break;
         case SHAREDDEF_EDITOR:
            defControlName = ms_defFieldControlName;
            controls = getControls(SINGLE_DIM_CONTROLS | ARRAY_DIM_CONTROLS);
            break;
         case PARENT_CHILD_FIELD:
            defControlName = ms_defComplexChildControlName;
            controls = getControls(TABLE_DIM_CONTROLS);
            break;
      }
      PSUISet uiSet = new PSUISet();

      PSDisplayText label = new PSDisplayText(convertToProper(fieldRefName));
      uiSet.setLabel(label);
      uiSet.setErrorLabel(label);
      
      if(controls != null && !controls.isEmpty())
      {
         boolean found = false;
         Iterator iter = controls.iterator();
         while(iter.hasNext())
         {
            if(iter.next().toString().equals(defControlName))
            {
               found = true;
               break;
            }
         }   
         if(!found)
            defControlName = controls.get(0).toString();
         PSControlRef ref = new PSControlRef( defControlName );
         ref.setId(getUniqueId());
         uiSet.setControl( ref );
      }

      return uiSet;
   }

   /**
    * Keeps a persistent counter that is incremented and returned each time
    * the method is called. Since an int is used, it will eventually roll over,
    * but that shouldn't be a problem in our lifetime.
    *
    * @returns a unique number within the universe of the E2 server.
    */
   public static synchronized int getUniqueId()
   {
      PSUserConfig config = PSCoreFactory.getInstance().getUserConfig();
      int nextId = config.getInteger( "UtilNextUniqueId", 10000 );
      config.setInteger( "UtilNextUniqueId", nextId + 1 );
      return nextId;
   }
      
   /**
    * Creates new field or field set based on the type of field and sets.
    *
    * @param fieldName the name to be set to field reference,
    * may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>fieldName</code> is invalid.
    */
   public static PSField getDefaultField(String fieldName, int editorType)
   {
      if (fieldName == null || fieldName.length() == 0)
         throw new IllegalArgumentException(
               "fieldName can not be null or empty"); //$NON-NLS-1$
      PSField field = null;
      if (editorType == SHAREDDEF_EDITOR)
      {
         field = new PSField(PSField.TYPE_SHARED, fieldName, null);
      }
      else if (editorType == LOCALDEF_PARENT_EDITOR
            || editorType == LOCALDEF_CHILD_EDITOR)
      {
         field = new PSField(PSField.TYPE_LOCAL, fieldName, null);
      }
      field.setDataType(PSUIField.DEFAULT_DATATYPE);
      field.setDataFormat(PSUIField
            .getDefaultFormat(PSUIField.DEFAULT_DATATYPE));
      try
      {
         field.setOccurrenceDimension(PSField.OCCURRENCE_DIMENSION_OPTIONAL,
               null);
      }
      catch (PSSystemValidationException e)
      {
         // should not come here as it is setting default
      }
      return field;
   }

   /**
    * Creates new field or field set based on the type of field and sets.
    *
    * @param fieldSetName the name to be set to field reference,
    * may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>fieldName</code> is invalid.
    */
   public static PSFieldSet getDefaultFieldSet(String fieldSetName)
   {
      if(fieldSetName == null || fieldSetName.length() == 0)
         throw new IllegalArgumentException(
            "fieldSetName can not be null or empty"); //$NON-NLS-1$
      
      PSFieldSet fieldSet = new PSFieldSet(fieldSetName);
      fieldSet.setType(PSFieldSet.TYPE_COMPLEX_CHILD);
      return fieldSet;
   }

   /**
    * Gets default UI set for the system field. Gets the UI set from the system
    * definition if it is defined for this field, otherwise gets default UI set
    * for any field. Please see {@link #getDefaultUISet(String, int)} also. The
    * control in the set is object of <code>OSControlRef</code>
    * 
    * @param field the system field, may not be <code>null</code>.
    * @return the default UI set, never <code>null</code>
    * @throws Exception
    * @throws IllegalArgumentException if any param is invalid.
    */
   public static PSUISet getDefaultUISetForSystemField(PSField field) 
      throws Exception
   {
      if (field == null)
         throw new IllegalArgumentException("field can not be null"); //$NON-NLS-1$

      PSUISet uiSet = null;

      PSContentEditorSystemDef sysDefinition = getSystemDef();
      if(sysDefinition != null)
      {
         PSUIDefinition definition =  sysDefinition.getUIDefinition();
         if(definition != null)
         {
            PSDisplayMapping mapping = definition.getMapping(
               field.getSubmitName());
            if(mapping != null)
            {
               uiSet = (PSUISet) mapping.getUISet().clone();
               if (uiSet.getControl() != null)
               {
//                  OSControlRef osControl =
//                     new OSControlRef(uiSet.getControl(), depMgr );
//                  uiSet.setControl( osControl );
               }
            }
         }
      }

      if(uiSet == null)
      {
//         uiSet = getDefaultUISet(field.getSubmitName(), TYPE_FIELD,
//            field.getFieldSetType(), depMgr);
      }

      return uiSet;
   }

   /**
    * Gets default UI set for the shared field. Gets the UI set from the shared
    * definition if it is defined for this field, otherwise gets default UI set
    * for any field. Please see {@link #getDefaultUISetForField} also. The
    * control in the set is object of <code>OSControlRef</code>
    *
    * @param field the shared field, may not be <code>null</code>.
    * @param depMgr maintains the dependency references for control refs,
    * not <code>null</code>.
    *
    * @return the default UI set, never <code>null</code>
    * @throws IllegalArgumentException if any param is invalid.
    */
//   public static PSUISet getDefaultUISetForSharedField(PSSharedField field)
//   {
//      if (field == null)
//         throw new IllegalArgumentException("field can not be null");
//      if (null == depMgr)
//         throw new IllegalArgumentException("dependency mgr may not be null");
//
//      PSUISet uiSet;
//
//      PSSharedFieldGroup group = field.getGroup();
//      PSUIDefinition definition =  group.getUIDefinition();
//      PSDisplayMapping mapping = definition.getMapping(field.getSubmitName());
//      if (mapping == null)
//      {
//         // could be a simple child - get the field set and look for that
//         PSFieldSet fieldSet = group.getFieldSet();
//         if (fieldSet.getType() == PSFieldSet.TYPE_SIMPLE_CHILD && 
//            fieldSet.get(field.getSubmitName()) != null)
//         {
//            mapping = definition.getMapping(fieldSet.getName());
//         }
//      }
//      
//      if(mapping != null)
//      {
//         uiSet = (PSUISet) mapping.getUISet().clone();
//         if (uiSet.getControl() != null)
//         {
//            OSControlRef osControl =
//               new OSControlRef(uiSet.getControl(), depMgr );
//            uiSet.setControl( osControl );
//         }
//      }
//      else
//      {
//         uiSet = getDefaultUISet(field.getSubmitName(), TYPE_FIELD,
//            field.getFieldSetType(), depMgr);
//      }
//      return uiSet;
//   }

   /**
    * Checks the supplied type with one of allowed types of
    * <code>PSFieldSet</code>.
    *
    * @param type the type of fieldset, must be one of valid types of
    * <code>PSFieldSet</code>
    *
    * @throws IllegalArgumentException if it is not one of allowed types of
    * <code>PSFieldSet</code>.
    */
   public static void checkFieldSetType(int type)
   {
      if(!PSFieldSet.isValidType(type))
      {
         throw new IllegalArgumentException(
            "type is invalid, it must be one of following values" + //$NON-NLS-1$
            PSFieldSet.TYPE_PARENT + "," + //$NON-NLS-1$
            PSFieldSet.TYPE_SIMPLE_CHILD + "," + //$NON-NLS-1$
            PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD + "," + //$NON-NLS-1$
            PSFieldSet.TYPE_COMPLEX_CHILD);
      }
   }   

   /**
    * Get the system def object may be <code>null</code>.
    * @throws Exception In case of error.
    */
   public static PSContentEditorSystemDef getSystemDef() throws Exception
   {
      IPSCmsModel model = ms_factory
            .getModel(PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG);
      List<IPSReference> sysref = (List<IPSReference>) model.catalog(false);
      if (sysref.isEmpty())
         return null;
      PSContentEditorSystemDef systemDef = (PSContentEditorSystemDef) model
            .load(sysref.get(0), false, false);
      return systemDef;
   }

   /**
    * Get the shared def object.
    * 
    * @return the cataloged shared definition object, never <code>null</code>
    * may be empty which means may not have any shaed groups.
    * 
    * @throws Exception In case of error.
    */
   public static PSContentEditorSharedDef getSharedDef() throws Exception
   {
      IPSCmsModel model = ms_factory.getModel(PSObjectTypes.SHARED_FIELDS);
      List<IPSReference> shref = (List<IPSReference>) model.catalog(false);
      PSCollection col = new PSCollection(PSSharedFieldGroup.class);
      if (!shref.isEmpty())
      {
         for (IPSReference ref : shref)
         {
            Iterator iter = ((PSContentEditorSharedDef) model.load(ref, false,
               false)).getFieldGroups();
            while (iter.hasNext())
            {
               col.add(iter.next());
            }
         }
      }
      PSContentEditorSharedDef sharedDef = new PSContentEditorSharedDef(col);
      PSContentEditorMapper.addSharedFieldMeta(sharedDef);     
      return sharedDef;
   }


   /**
    * Determines the system field excludes, shared group includes and shared
    * field excludes from this mapper's fieldset and UI definition and updates
    * those. This should be called for a mapper which is merged with system and
    * shared definition.
    * 
    * @param mapper The mapper to process. Never <code>null</code>.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public static void updateExcludes(PSContentEditorMapper mapper)
      throws Exception
   {
      if (null == mapper)
      {
         throw new IllegalArgumentException("mapper cannot be null");   //$NON-NLS-1$
      }
      PSContentEditorSystemDef sysDef = getSystemDef();
      PSContentEditorSharedDef sharedDef = getSharedDef();
      
      PSFieldSet fieldset = mapper.getFieldSet();
      PSUIDefinition uiDef = mapper.getUIDefinition();

      //get system fields that are not used
      ArrayList sysFieldExcludes = new ArrayList();
      sysFieldExcludes.addAll( PSContentEditorMapper.getFieldExcludes(
         sysDef.getFieldSet(), uiDef, fieldset, PSField.TYPE_SYSTEM) );

      //get shared fields that are not used
      Iterator fieldGroups = sharedDef.getFieldGroups();
      ArrayList sharedGroupIncludes = new ArrayList();
      ArrayList sharedFieldExcludes = new ArrayList();
      while(fieldGroups.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup)fieldGroups.next();
         PSFieldSet groupFieldSet = group.getFieldSet();
         String fieldRef = groupFieldSet.getName();

         //Get the fields that are not used by this content editor mappings.
         List fieldExcludes = PSContentEditorMapper.getFieldExcludes(
               groupFieldSet, uiDef, fieldset, PSField.TYPE_SHARED);

         /* Check whether any mapping is referring to this fieldset. If it
          * refers include that group. (satisfies for simple and complex child
          * fieldsets if they are included).
          */
         if(uiDef.getDisplayMapper().getMapping(fieldRef) != null)
         {
            Object object = fieldset.get(fieldRef);
            if(object instanceof PSFieldSet &&
               ((PSFieldSet)object).isSharedFieldSet())
            {
               sharedGroupIncludes.add( group.getName() );
               sharedFieldExcludes.addAll( fieldExcludes );
            }
         }
         /* In case of multiproperty simple child fieldset, we have mappings
          * only for fields, so if all fields of fieldset are not excluded, then
          * we include that group.
          */
         else if( !fieldExcludes.containsAll( new PSCollection(
            groupFieldSet.getNames() ) ) )
         {
            sharedGroupIncludes.add( group.getName() );
            sharedFieldExcludes.addAll( fieldExcludes );
         }
      }

      mapper.setSystemFieldExcludes(sysFieldExcludes);
      mapper.setSharedFieldIncludes(sharedGroupIncludes);
      mapper.setSharedFieldExcludes(sharedFieldExcludes);
   }

   
   
   /**
    * Checks whether the passed in name is a shared field or field set name.
    * Check is case sensitive.
    *
    * @param name the name to check, may not be <code>null</code> or
    * empty.
    *
    * @return <code>true</code> if the name is shared field or field set name,
    * otherwise <code>false</code>.
    * @throws Exception 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public static boolean isSharedFieldOrFieldSet(String name) throws Exception
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name can not be null or empty"); //$NON-NLS-1$

      boolean isShared = false;
      
      PSContentEditorSharedDef sharedDef = getSharedDef();
      if(sharedDef == null)
         return false;
      Iterator iter = sharedDef.getFieldGroups();
      while(iter.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup)iter.next();
         PSFieldSet set = group.getFieldSet();

         if( name.equals(set.getName()) ||
             isFieldinFieldSet( set, name) )
         {
            isShared = true;
            break;
         }
      }
      return isShared;
   }

   /**
    * Checks whether the passed in name is a system field name. Check is case
    * sensitive.
    *
    * @param name the name to check, may not be <code>null</code> or
    * empty.
    *
    * @return <code>true</code> if the name is system field name,
    * otherwise <code>false</code>.
    * @throws Exception 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public static boolean isSystemField(String name) throws Exception
   {
      PSContentEditorSystemDef systemDef = getSystemDef();
      //This should not happen. We will not come to this far
      //if systemDef is null.
      if(systemDef == null)
         throw new Exception("System def is null"); //$NON-NLS-1$
      return isFieldinFieldSet(getSystemDef().getFieldSet(),
         name);
   }

   /**
    * Checks whether the passed in name is a shared field name. Check is case
    * sensitive.
    *
    * @param name the name to check, may not be <code>null</code> or
    * empty.
    *
    * @return <code>true</code> if the name is shared field name,
    * otherwise <code>false</code>.
    * @throws Exception 
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public static boolean isSharedField(String name) throws Exception
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name can not be null or empty"); //$NON-NLS-1$

      boolean isShared = false;

      PSContentEditorSharedDef sharedDef = getSharedDef();
      if(sharedDef == null)
         return false;
      Iterator iter = sharedDef.getFieldGroups();
      while(iter.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup)iter.next();
         PSFieldSet set = group.getFieldSet();

         if( isFieldinFieldSet( set, name) )
         {
            isShared = true;
            break;
         }
      }
      return isShared;
   }

   /**
    * Checks whether the passed in name is a name of field in the passed in
    * field set. The check is case sensitive.
    *
    * @param set the field set to check, may not be <code>null</code>
    * @param name the name to check, may not be <code>null</code> or empty.
    *
    * @return <code>true</code> if the name is a field name in the passed in
    * field set, otherwise <code>false</code>.
    *
    * @throw IllegalArgumentException if any param is invalid.
    */
   private static boolean isFieldinFieldSet(PSFieldSet set, String name)
   {
      if(set == null)
         throw new IllegalArgumentException("set can not be null"); //$NON-NLS-1$

      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name can not be null or empty"); //$NON-NLS-1$

      boolean isField = false;

      Object field = set.get(name);
      if(field instanceof PSField)
         isField = true;
         
      return isField;
   }

   /**
    * Checks whether the passed in field set name is a shared complex child
    * field set name.
    * The check is based on field set name and is case sensitive.
    *
    * @param name the name to check, may not be <code>null</code> or
    * empty.
    *
    * @return <code>true</code> if the field set is shared complex child,
    * otherwise <code>false</code>.
    * @throws Exception 
    *
    * @throws IllegalArgumentException if set is <code>null</code>
    */
   public static boolean isSharedComplexChildFieldSet(String name) throws Exception
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name can not be null"); //$NON-NLS-1$

      boolean isSharedChild = false;

      PSContentEditorSharedDef sharedDef = getSharedDef();
      if(sharedDef == null)
         return false;
      Iterator iter = sharedDef.getFieldGroups();
      while(iter.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup)iter.next();
         PSFieldSet set = group.getFieldSet();

         if( set.getName().equals(name) &&
            set.getType() == PSFieldSet.TYPE_COMPLEX_CHILD )
         {
            isSharedChild = true;
            break;
         }
      }
      return isSharedChild;
   }

   /**
    * Gets the shared complex child fieldset with the specified name.
    *
    * @param name of the fieldset to get, may not be <code>null</code> or empty.
    *
    * @return the shared fieldset if found, otherwise <code>null</code>
    * @throws Exception 
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public static PSFieldSet getSharedComplexChildFieldSet(String name) throws Exception
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name can not be null or empty"); //$NON-NLS-1$

      PSFieldSet sharedSet = null;
      PSContentEditorSharedDef sharedDef = getSharedDef();
      if(sharedDef != null)
      {
         Iterator iter = sharedDef.getFieldGroups();
         while(iter.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup)iter.next();
            PSFieldSet set = group.getFieldSet();

            if( set.getName().equals(name) &&
               set.getType() == PSFieldSet.TYPE_COMPLEX_CHILD )
            {
               sharedSet = set;
               break;
            }
         }
      }

      return sharedSet;
   }

   /**
    * 
    */
   /**
    * Gets the shared ieldset with the specified name.
    *
    * @param name of the fieldset to get, may not be <code>null</code> or empty.
    *
    * @return the shared fieldset if found, otherwise <code>null</code>
    * @throws Exception 
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public static PSFieldSet getSharedFieldSet(String name) throws Exception
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name can not be null or empty"); //$NON-NLS-1$

      PSFieldSet sharedSet = null;
      PSContentEditorSharedDef sharedDef = getSharedDef();
      if(sharedDef != null)
      {
         Iterator iter = sharedDef.getFieldGroups();
         while(iter.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup)iter.next();
            PSFieldSet set = group.getFieldSet();

            if( set.getName().equals(name))
            {
               sharedSet = set;
               break;
            }
         }
      }

      return sharedSet;
   }

   
   
   /**
    * Convenient method to get the the controls for the supplied editor type.
    * 
    * @throws PSModelException
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public static List<PSControlMeta> getControls(int option)
         throws PSModelException
   {
      List<PSControlMeta> controls = new ArrayList();
      if((option & ALL_CONTROLS) != 0)
      {
         controls.addAll(getContentEditorControls(false));
      }
      else
      {
         if((option & SINGLE_DIM_CONTROLS) != 0)
         {
            controls.addAll(getFieldControls());
         }
         if((option & ARRAY_DIM_CONTROLS) != 0)
         {
            controls.addAll(getSimpleChildFieldControls());
         }
         if((option & TABLE_DIM_CONTROLS) != 0)
         {
            controls.addAll(getComplexChildControls());
         }
      }
      return controls;
   }
   
   /**
    * Convenient method to get the names of the controls for the supplied editor
    * type.
    * 
    * @param options One or more of the <code>xxx_DIM_CONTROLS</code> flags
    * OR'd together, or the {@link #ALL_CONTROLS} value.
    * 
    * @return String[] of control names. Never <code>null</code>, may be empty.
    * @throws PSModelException
    */
   public static String[] getControlNames(int options) throws PSModelException
   {
      List<String> cn = new ArrayList<String>();
      for (PSControlMeta cm : getControls(options))
         cn.add(cm.getName());
      Collections.sort(cn);
      return cn.toArray(new String[cn.size()]);
   }
   
   /**
    * Checks whether supplied integer corresponds to a valid editor type or not.
    * 
    * @return <code>true</code> if supplied editor type is valid, otherwise
    *         <code>false</code>.
    */
   public static boolean isValidEditorType(int editorType)
   {
      boolean ret = false;
      if (editorType == LOCALDEF_PARENT_EDITOR || editorType == LOCALDEF_CHILD_EDITOR
            || editorType == SYSTEMDEF_EDITOR || editorType == SHAREDDEF_EDITOR
            || editorType == PARENT_CHILD_FIELD)
         ret = true;
      return ret;
   }

   /**
    * Does the supplied type specify a local field editor type?
    * 
    * @param editorType The type to check, one of the <code>xxx_EDITOR</code>
    * constants.
    * 
    * @return <code>true</code> if it is a local parent or local child type,
    * <code>false</code> otherwise.
    */
   public static boolean isLocalEditorType(int editorType)
   {
      return editorType == LOCALDEF_PARENT_EDITOR || 
         editorType == LOCALDEF_CHILD_EDITOR;
   }
   /**
    * Returns the index of the supplied stirng in the supplied array.
    * @param input Array of strings from which the index of the item
    * needs to be found. If <code>null</code> empty returns -1. If it
    * has duplicate items, the index of the first found item is returned.
    * @param item String whose index needs to be found. If <code>null</code>
    * or empty returns -1.
    * @return int index of the supplied string or -1 if not found or
    * supplied array is null of supplied string is null
    */
   public static int getItemIndex(String[] input, String item)
   {
      if(input != null && input.length > 0  && !StringUtils.isBlank(item))
      {
         for(int i=0;i<input.length; i++)
         {
            if(item.equals(input[i]))
               return i;
         }
      }
      return -1;
   }

   /**
    * Returns the list of PSFieldDefinition objects created from the supplied
    * PSItemDefinition
    * 
    * @return List of PSFieldDefinition objects, may be empty, but never
    *         <code>null</code>.
    * @throws Exception
    * @throws PSValidationException
    */
   public static List<PSFieldDefinition> getMergedFieldDefinitions(
         PSItemDefinition itemDef) throws PSValidationException, Exception
   {
      PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
            .getContentEditor().getPipe();
      PSContentEditorMapper mapper = pipe.getMapper();
      // Get the systemdef and get the shareddef and then get the merged mapper.
      mapper = mapper.getMergedMapper(getSystemDef(), getSharedDef(), false);
      mapper.validateSharedFieldDuplication(getSharedDef(), mapper
            .getSharedFieldIncludes());
      PSUIDefinition def = mapper.getUIDefinition();
      PSDisplayMapper dmapper = def.getDisplayMapper();
      PSFieldSet fieldSet = mapper.getFieldSet();
      return getFieldDefinitions(dmapper, fieldSet);
   }

   public static List<PSFieldDefinition> getFieldDefinitions(
      PSDisplayMapper dmapper, PSFieldSet fieldSet)
   {
      List<PSFieldDefinition> fieldDefs = new ArrayList<PSFieldDefinition>();
      Iterator mappings = dmapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldName = mapping.getFieldRef();
         Object o = fieldSet.get(fieldName);
         if (o instanceof PSField)
         {
            fieldDefs.add(new PSFieldDefinition((PSField) o, mapping));
            continue;
         }
         /*
          * If the field reference is not found in this fieldset, then check
          * whether it is multiproperty simple child field
          */
         if (o == null)
         {
            o = fieldSet.getChildField(fieldName,
                  PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
         }

         /*
          * If it is a field, then add it.
          */
         if (o instanceof PSField)
         {
            fieldDefs.add(new PSFieldDefinition((PSField) o, mapping));
            continue;
         }

         /*
          * If field reference is field set, then it might be simplechild or
          * complexchild. In case of simple child, we have to show the mapping
          * in parent mapper only, so get the field reference from it's mapper
          * and get the field.
          */
         if (o instanceof PSFieldSet)
         {
            PSFieldSet childFs = (PSFieldSet) o;
            if (childFs.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
            {
               PSDisplayMapper childMapper = mapping.getDisplayMapper();
               Iterator childMappings = childMapper.iterator();
               while (childMappings.hasNext())
               {
                  PSDisplayMapping childMapping = (PSDisplayMapping) childMappings
                        .next();
                  String chfieldName = childMapping.getFieldRef();
                  o = childFs.get(chfieldName);
                  if (o != null)
                  {
                     childMapping.setUISet(mapping.getUISet());
                     fieldDefs.add(new PSFieldDefinition((PSField) o, childMapping));
                  }
               }
            }
            else
            {
               fieldDefs.add(new PSFieldDefinition((PSFieldSet) o, mapping));
               continue;
            }
         }

      }
      return fieldDefs;
   }

   /**
    * Returns the list of PSFieldDefinition objects created from the supplied
    * PSItemDefinition
    * 
    * @return List of PSFieldDefinition objects, may be empty, but never
    *         <code>null</code>.
    */
   public static List<PSFieldDefinition> getChildFieldDefinitions(
         PSItemDefinition itemDef, String fieldSetName) 
   {
      List<PSFieldDefinition> fieldDefs = new ArrayList<PSFieldDefinition>();
      PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
      .getContentEditor().getPipe();
      PSFieldSet fieldSet = pipe.getMapper().getFieldSet();
      PSDisplayMapper displayMapper = pipe.getMapper().
         getUIDefinition().getDisplayMapper();
      Iterator mappings = displayMapper.iterator();
      while(mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         PSDisplayMapper mapper = mapping.getDisplayMapper();
         if(mapper != null && mapper.getFieldSetRef().equals(fieldSetName))
         {
            PSFieldSet chSet = (PSFieldSet) fieldSet.get(fieldSetName);
            if(chSet != null && chSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
            {
               fieldDefs = getFieldDefinitions(mapper,chSet);
            }
         }
      }
      return fieldDefs;
   }

   /**
    * Returns the list of PSFieldDefinition objects from systemdef.
    * 
    * @return List of PSFieldDefinition objects, may be empty, but never
    *         <code>null</code>.
    * @throws Exception
    * @throws PSValidationException
    */
   public static List<PSFieldDefinition> getSystemFieldDefinitions()
         throws PSValidationException, Exception
   {
      PSDisplayMapper dmapper = getSystemDef().getUIDefinition()
            .getDisplayMapper();
      PSFieldSet fieldSet = getSystemDef().getFieldSet();
      return getFieldDefinitions(dmapper,fieldSet);
   }

   /**
    * Checks fields and field sets in the supplied set and all contained field
    * sets recursively and returns the one whose name matches the supplied name,
    * case-insensitive. Since field names are unique within a content editor,
    * the search stops once one is found.
    * 
    * @param name The name of the field or fieldset to retrieve. A
    *           case-insensitive compare is performed. If <code>null</code> or
    *           empty, <code>null</code> is returned.
    * 
    * @return The object of <code>PSField</code> or <code>PSFieldSet</code>,
    *         that has a name equal to <code>name</code> (case insensitive).
    *         <code>null</code> if a match isn't found.
    * 
    */
   public static Object getFieldOrSetByName(PSFieldSet set, String name)
   {
      if (StringUtils.isBlank(name))
      {
         return null;
      }
      Object match = null;
      Iterator fields = set.getEveryField();
      while(fields.hasNext() && null == match)
      {
         Object o = fields.next();
         if (o instanceof PSFieldSet)
         {
            PSFieldSet cSet = (PSFieldSet)o;
            if (cSet.getName().equalsIgnoreCase(name))
               match = cSet;
            else
               match = getFieldOrSetByName(cSet,name);
         }
         else if (o instanceof PSField)
         {
            PSField f = (PSField)o;
            if (f.getSubmitName().equalsIgnoreCase(name))
               match = f;
         }
      }
      return match;
   }

   /**
    * Checks for validity of the field name. Checks that field name:
    * <ol>
    * <li>Is not empty</li>
    * <li>Has no embedded whitespace</li>
    * <li>Has only Alphanummeric and "_" (underscore) characters</li> 
    * <li>Does not have first char in lower case and second char in upper case.</li>
    * <li>Is not a reserved word as defined in local static method .</li>
    * </ol>
    * 
    * @param fieldName the field name to be checked for validity, may be
    *           <code>null</code>
    * @return String appropriate error message if the field name is
    * invalid or <code>null</code> if valid. 
    */
   public static String validateFieldName(String fieldName)
   {
      String origFieldName = new String(fieldName);
      
      if (StringUtils.isEmpty(fieldName))
      {
         return FIELD_NAME_EMPTY;
      }

      if(!fieldName.equals(StringUtils.deleteWhitespace(fieldName)))
      {
         return FIELD_NAME_NOWHITESPACE;
      }
      
      // Field name should start with a letter.
      // (This test must be done before alphanumeric test (below)
      // in case there is a leading underscore!)
      if(!StringUtils.isAlpha(fieldName.charAt(0) + "")) //$NON-NLS-1$
      {
         return FIELD_NAME_INVALID_FIRSTCHAR;
      }
      
      //The field should be a alpha numeric with underscores.
      fieldName = StringUtils.replace(fieldName,"_",""); //$NON-NLS-1$ //$NON-NLS-2$
      if(!StringUtils.isAlphanumeric(fieldName))
      {
         return FIELD_NAME_INVALID_CHARS;
      }
      
      // Bizarre special test case for Hibernate. 
      // A field must not start with a lower case letter followed by an 
      // upper case letter. It interferes with deriving the inferred property
      // name from an accessor name when the property name is all upper case.
      // (http://opensource.atlassian.com/projects/hibernate/browse/HB-1338)
      // Also, use copy of the string for test because you have removed all the
      // underscore characters ("_") above.
      if ((origFieldName.length() >= 2) && 
           Character.isLowerCase(origFieldName.charAt(0)) && 
           Character.isUpperCase(origFieldName.charAt(1)) )
      {
         return FIELD_NAME_INVALID_FIRST_TWO_CHARS;
      }
      

      if(isReservedWord(origFieldName))
      {
         return FIELD_NAME_RESERVED_WORD;
      }
  
      return null;
   }

   
   /**
    * Walks the supplied shared fields, grouping each field with its display
    * mapping and adding it to a set, which is then returned.
    * 
    * @param group The set to process. Never <code>null</code>.
    * 
    * @return May be empty, never <code>null</code>.
    */
   public static List<PSFieldDefinition> getSharedFieldDefinitions(
      PSSharedFieldGroup group)
   {
      if (null == group)
      {
         throw new IllegalArgumentException("group cannot be null");   //$NON-NLS-1$
      }
      List<PSFieldDefinition> fieldDefs = new ArrayList<PSFieldDefinition>();
      PSDisplayMapper dmapper = group.getUIDefinition()
            .getDisplayMapper();
      PSFieldSet fieldSet = group.getFieldSet();
      Iterator mappings = dmapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldName = mapping.getFieldRef();
         Object o = fieldSet.get(fieldName);
         if (o instanceof PSField)
         {
            fieldDefs.add(new PSFieldDefinition((PSField) o, mapping));
            continue;
         }
         if (o == null)
         {
            if (fieldSet.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
            {
               PSDisplayMapper childMapper = mapping.getDisplayMapper();
               Iterator childMappings = childMapper.iterator();
               while (childMappings.hasNext())
               {
                  PSDisplayMapping childMapping = (PSDisplayMapping) childMappings
                        .next();
                  fieldName = childMapping.getFieldRef();
                  o = fieldSet.get(fieldName);
                  if (o != null)
                  {
                     childMapping.setUISet(mapping.getUISet());
                     fieldDefs.add(new PSFieldDefinition((PSField) o, childMapping));
                  }
               }
            }
         }
      }
      return fieldDefs;
   }

   /**
    * The server requires the id of parent mapper as '0' and all child display
    * mappers should have unique ids among the display mappers of this content
    * editor resource. By default the id of the mapper is loaded from xml and
    * it is set. The child mappers created through UI will have id as '0' which
    * should be set with unique id before saving the application. So this method
    * sets Unique IDs to all child display mappers which don't have id set.
    */
   public static void setUniqueIdsToMappers(PSDisplayMapper mapper)
   {
      int id = 1;
      Iterator mappings = mapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         if (mapping.getDisplayMapper() != null)
         {
            mapping.getDisplayMapper().setId(id++);
         }
      }
   }

   /**
    * Demerges item definition system fields.
    * @param itemDef item def to demerge fields for. Not <code>null</code>.
    * @throws Exception in case of error.
    */
   public static void demergeFields(final PSItemDefinition itemDef)
         throws Exception
   {
      if (itemDef == null)
      {
         throw new IllegalArgumentException("itemDef should not be equal null");
      }
      final PSContentEditorPipe pipe =
            (PSContentEditorPipe) itemDef.getContentEditor().getPipe();
      PSContentEditorMapper mapper = pipe.getMapper();
      setUniqueIdsToMappers(mapper.getUIDefinition().getDisplayMapper());
      pipe.setMapper(
            mapper.getDemergedMapper(getSystemDef(), getSharedDef(), false));
   }
   
   /**
    * Merges item definition system fields. Notifies user with pop-up dialog
    * if finds invalid controls.
    * @param itemDef item def to merge fields for. Not <code>null</code>.
    * @throws Exception in case of error.
    */
   public static void mergeFields(final PSItemDefinition itemDef)
         throws Exception
   {
      if (itemDef == null)
      {
         throw new IllegalArgumentException("itemDef should not be equal null");
      }
      PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
            .getContentEditor().getPipe();      
      PSContentEditorMapper mapper = pipe.getMapper();
      //Use a copy of System and Shared Defs here
      PSContentEditorSystemDef sysDefCopy = new PSContentEditorSystemDef(
            getSystemDef().toXml());
      PSContentEditorSharedDef shDefCopy = new PSContentEditorSharedDef(
            getSharedDef().toXml());
      PSContentEditorMapper.addSharedFieldMeta(shDefCopy);
      mapper = mapper.getMergedMapper(sysDefCopy, shDefCopy, false);
      mapper.validateSharedFieldDuplication(
            shDefCopy, mapper.getSharedFieldIncludes());
      pipe.setMapper(mapper);
      final List<String> fields = fixDisplayMapperControlMeta(
            mapper.getUIDefinition().getDisplayMapper());
      if (!fields.isEmpty())
      {
         final String msg = PSMessages.getString(
               "PSContentEditorDefinition.error.invalidcontrolmeta.message",
               fields.toString()); 
         final String title = PSMessages.getString(
               "PSContentEditorDefinition.error.invalidcontrols.title"); 
         MessageDialog.openInformation(
               PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
               title, msg);
      }      
   }   
     
   
   /**
    * Sets the inline link properties to <code>true</code> for
    * {@link #EDITLIVE_CONTROL} and {@link #WEP_CONTROL} and <code>false</code>
    * for other controls.
    * 
    * @param field Must not be <code>null</code>.
    * @param ctrlName If blank or not one of {@link #EDITLIVE_CONTROL} or
    *           {@link #WEP_CONTROL} controls then control properties are set to
    *           <code>false</code> otherwiese they are set to
    *           <code>true</code>.
    */
   public static void setDefaultInlineLinkProperties(PSField field,
         String ctrlName)
   {
      if (field == null)
         throw new IllegalArgumentException("field must not be null");

      ctrlName = StringUtils.defaultString(ctrlName);

      if (ms_wysiwygControls.contains(ctrlName))
      {
         field.setCleanupBrokenInlineLinks(true);
         field.setMayHaveInlineLinks(true);
      }
      else
      {
         field.setCleanupBrokenInlineLinks(false);
         field.setMayHaveInlineLinks(false);
      }
   }
   
   /**
    * If we are switching between Edit Live and EWebEditPro controls the certain
    * predefined control parameters need to be preserved.
    * 
    * @param oldRef The old control ref, must not be <code>null</code>.
    * @param newRef The new control ref, must not be <code>null</code>.
    */
   public static void preserveControlProperties(PSControlRef oldRef,
         PSControlRef newRef)
   {
      if (oldRef == null)
         throw new IllegalArgumentException("oldRef must not be null");
      if (newRef == null)
         throw new IllegalArgumentException("newRef must not be null");
      
      if (ms_wysiwygControls.contains(oldRef.getName())
            && ms_wysiwygControls.contains(newRef.getName()))
      {
         PSCollection nparams = new PSCollection(PSControlParameter.class);
         Iterator oiter = oldRef.getParameters();
         while(oiter.hasNext())
         {
            PSControlParameter param = (PSControlParameter) oiter.next();
            if (ms_preserveList.contains(param.getName()))
            {
               nparams.add(param);
            }
         }
         if(!nparams.isEmpty())
            newRef.setParameters(nparams);
      }
   }
   
   /**
    * Sets the maxLength parameter on the supplied control ref, if the control
    * meta corresponding to the control ref exists and has a parameter with the
    * name maxlength. If the dataformat is not numeric but control has a
    * maxlength param, then removes the maxlength param.
    * 
    * 
    * @param ctrlRef The control ref for which the max length needs to be set.
    *           Must not be <code>null</code>.
    * @param dataFormat The data format of the field if blank or not a numeric
    *           value then removes the maxlength param from control ref.
    */
   public static void setMaxLengthParam(PSControlRef ctrlRef, String dataFormat)
   {
      if (ctrlRef == null)
         throw new IllegalArgumentException("ctrlRef must not be null");

      if(StringUtils.isBlank(dataFormat) || !StringUtils.isNumeric(dataFormat))
      {
         PSCollection col = new PSCollection(PSParam.class);
         Iterator iter = ctrlRef.getParameters();
         while(iter.hasNext())
         {
            PSParam param = (PSParam) iter.next();
            if(!param.getName().equals(CONTROL_PARAM_MAXLENGTH))
               col.add(param);
         }
         ctrlRef.setParameters(col);
         return;
      }
      
      PSControlMeta meta = null;
      try
      {
         meta = getControl(ctrlRef.getName());
      }
      catch (PSModelException e)
      {
         // this should not happen as we might have catalogged control meta
         // several times.
         PSWorkbenchPlugin.handleException("Max Lengh Param Setting",
               "PSContentEditorDefinition.error.maxlength.title",
               "PSContentEditorDefinition.error.maxlength.message", e);
      }
      if(meta == null)
         return;
      
      List params = meta.getParams();
      boolean found = false;
      for (int i = 0; i < params.size(); i++)
      {
         PSControlParameter par = (PSControlParameter) params.get(i);
         if (par.getName().equals(CONTROL_PARAM_MAXLENGTH))
         {
            found = true;
            break;
         }
      }
      if (!found)
         return;
      
      boolean maxlSet = false;
      Iterator iter = ctrlRef.getParameters();
      PSCollection col = new PSCollection(PSParam.class);
      CollectionUtils.addAll(col, iter);
      // Check whether maxlength parameter exists in the control parameters
      // already. If exists reset its value other wise add a new maxlength
      // parameter
      for (Object obj : col)
      {
         PSParam param = (PSParam) obj;
         if (param.getName().equals(CONTROL_PARAM_MAXLENGTH))
         {
            param.setValue(new PSTextLiteral(dataFormat));
            maxlSet = true;
            break;
         }
      }
      if (!maxlSet)
         col.add(new PSParam(CONTROL_PARAM_MAXLENGTH, new PSTextLiteral(
               dataFormat)));
      ctrlRef.setParameters(col);
   }
   
   /**
    * Checks the source of the supplied field to see if field validations in the
    * local def are allowed, and displays an error message if they are not.
    * 
    * @param field The field to check, may not be <code>null</code>.
    * 
    * @return <code>true</code> if they are allowed, <code>false</code> if
    *         not or if the source field cannot be located.
    */
   public static boolean checkFieldValidationAllowed(PSField field)
   {
      if (field == null)
         throw new IllegalArgumentException("field may not be null"); //$NON-NLS-1$

      try
      {
         // local fields are fine
         if (field.getType() == PSField.TYPE_LOCAL)
            return true;
         
         PSOverrideValidator validator = PSOverrideValidator.getValidator(
            field);      
         return validator.validate();
      }
      catch (Exception e)
      {
         PSUiUtils.handleExceptionSync("Checking field validation overrides",  //$NON-NLS-1$
            null, null, e);
         return false;
      }
   }
   /**
    * Checks whether the supplied field name is a reserved word.
    * @param fieldName Name of the field to check.
    * @return <code>true</code>, if supplied field name is a reserved
    * word otherwise <code>false</code>.
    */ 
   public static boolean isReservedWord(String fieldName)
   {
      List<String> rwList = getReservedWords();
      for(String rw : rwList)
      {
         if(rw.equalsIgnoreCase(fieldName))
            return true;
      }
      return false;
   }

   /**
    * Gets the resrved words list. Reads the reserved words from
    * ReservedWords.properties file and builds the list with the keys
    * from the properties file.
    */
   public static List<String> getReservedWords()
   {
      if(ms_reservedWords == null)
      {
         InputStream is = E2Designer.class.getResourceAsStream("ReservedWords.properties"); //$NON-NLS-1$
         Properties props = new Properties();
         try
         {
            props.load(is);
         }
         catch (IOException e)
         {
            //As we bundle this file with the supllied jars this should not happen.
            //Incase happens
            PSWorkbenchPlugin
                  .handleException(
                        "Reserved Words", //$NON-NLS-1$
                        PSMessages.getString("PSContentEditorDefinition.error.reservedwordsmissing.title"), //$NON-NLS-1$
                        PSMessages.getString("PSContentEditorDefinition.error.reservedwordsmissing.message"), //$NON-NLS-1$
                        e);
            return new ArrayList<String>();
         }
         ms_reservedWords = new ArrayList<String>();
         Enumeration keys = props.propertyNames();
         while(keys.hasMoreElements())
         {
            ms_reservedWords.add((String)keys.nextElement());
         }
      }
      return ms_reservedWords;
   }
   
   /**
    * Walks through all mappings both parent and child under the supplied
    * display mapper fixes the display mapping. Returns the list of field names
    * that have been fixed.
    * 
    * @param dmapper The display mapper object for which the controls needs to
    *           be fixed.
    * @return List of field names that have been fixed. May be empty if no
    *         controls have been fixed but never <code>null</code>
    * @throws PSModelException If fails to get the controls catalog.
    */
   public static List<String> fixDisplayMapperControlMeta(PSDisplayMapper dmapper)
         throws PSModelException
   {
      List<String> ctrls = new ArrayList<>();
      for (Object mapp : dmapper) {
         if (mapp instanceof PSDisplayMapper) {
            for (Object o : (PSDisplayMapper) mapp) {
               PSDisplayMapping chmapp = (PSDisplayMapping) o;
               if (fixDisplayMappingControlMeta(chmapp)) {
                  ctrls.add(chmapp.getFieldRef());
               }
            }
         } else {
            if (fixDisplayMappingControlMeta((PSDisplayMapping) mapp)) {
               ctrls.add(((PSDisplayMapping) mapp).getFieldRef());
            }
         }
      }
      return ctrls;
   }
   
   /**
    * Checks the supplied display mappings control is valid or not. If not valid
    * the control will be replaced with default control. If the control itself is not set
    * on the mapping(for system and shared fields that are not overridden in local def)
    * then no checking is performed.
    * @param mapp The displaymapping that needs to be fixed.
    * @return <code>true</code>, If control is fixed otherwise <code>false</code>
    * @throws PSModelException If fails to get the controls catalog.
    */
   private static boolean fixDisplayMappingControlMeta(
         PSDisplayMapping mapp) throws PSModelException
   {
      PSUISet uiSet = mapp.getUISet();
      PSControlRef ref = uiSet.getControl();
      if (ref != null)
      {
         PSControlMeta meta = getControl(ref.getName());
         if (meta == null)
         {
            uiSet.setControl(new PSControlRef(ms_defFieldControlName));
            uiSet.setChoices(null);
            return true;
         }
      }
      return false;
   }

   /**
    * Updates the supplied field and display mapping with the supplied database
    * column. Updates the field with the default data type and format and
    * updates the mapping with appropriate control.
    * 
    * @param dbMapping The backend column that needs to be associated with the
    *           field.
    * @param field The field object for which the data type and format needs
    *           to be set with that of the backend column.
    * @param mapping The display mapping object for which the control needs to
    *           be set based on type of the backend column.
    * @return <code>null</code> if succeeds or error message.
    * @throws PSAuthorizationException {@see PSSqlCataloger#getWalker()} 
    * @throws PSAuthenticationFailedException {@see PSSqlCataloger#getWalker()}
    * @throws PSServerException {@see PSSqlCataloger#getWalker()}
    * @throws IOException {@see PSSqlCataloger#getWalker()}
    */
   public static String updateFieldWithBackendColumn(PSBackEndColumn dbMapping,
         PSField field, PSDisplayMapping mapping)
         throws PSAuthorizationException, PSAuthenticationFailedException,
         PSServerException, IOException
   {
      if (dbMapping == null)
         throw new IllegalArgumentException("dbMapping can not be null");
      if(field == null)
         throw new IllegalArgumentException("field can not be null");
      if(mapping == null)
         throw new IllegalArgumentException("mapping can not be null");
      
      String colName = dbMapping.getColumn();
      PSSqlCataloger cat = new PSSqlCataloger(null, dbMapping.getTable()
            .getAlias());
      PSDesignerConnection conn = PSCoreFactory.getInstance()
            .getDesignerConnection();
      cat.setConnectionInfo(conn);
      PSCatalogResultsWalker column = cat.getWalker();
      
      // set locator, so that the (un-used) column will be (re)used. 
      field.setLocator(dbMapping);
      
      // right now add only the column name, type and jdbc type
      String scType = "";
      String size = "";
      while (column.nextResultObject("Column"))
      {
         String name = column.getResultData("name");
         if (!name.equalsIgnoreCase(colName))
            continue;
         if (name != null)
         {
            scType = column.getResultData("jdbcDataType");
            size = column.getResultData("size");
            break;
         }
      }
      //This should not happen incase happens send the error back.
      if (StringUtils.isEmpty(scType))
         return PSMessages.getString(
               "PSContentEditorDefinition.error.message.missingjdbctype"); //$NON-NLS-1$

      int jdbcType = Integer.parseInt(scType);
      int precision = -1;
      int scale = -1;
      if (!StringUtils.isBlank(size))
      {
         String[] temp = StringUtils.split(size, ".");
         if (StringUtils.isNumeric(temp[0]))
            precision = Integer.parseInt(temp[0]);
         if (temp.length == 2 && StringUtils.isNumeric(temp[1]))
            scale = Integer.parseInt(temp[1]);
      }
      String actualType = PSField.getActualDataTypeForJdbcType(jdbcType, scale);

      PSControlRef ref = null;
      String controlName = "";

      field.setDataType(actualType);
      if (actualType.equals(PSField.DT_BINARY))
      {
         controlName = FILE_CONTROL;
         field.setDataFormat("max");
      }
      else if (actualType.equals(PSField.DT_DATE)
            || actualType.equals(PSField.DT_TIME)
            || actualType.equals(PSField.DT_DATETIME))
      {
         controlName = CALENDAR_SIMPLE_CONTROL;
      }
      else if (actualType.equals(PSField.DT_BOOLEAN))
      {
         controlName = SINGLE_CHECKBOX_CONTROL;
      }
      else if (actualType.equals(PSField.DT_TEXT)
            && precision > 256)
      {
         controlName = EDITLIVE_CONTROL;
         field.setDataFormat(PSField.MAX_FORMAT);
      }
      else if(actualType.equals(PSField.DT_TEXT))
      {
         controlName = EDIT_BOX_CONTROL;
         field.setDataFormat(size);
      }
      else
      {
         controlName = EDIT_BOX_CONTROL;
      }
      ref = new PSControlRef(controlName);
      ref.setId(getUniqueId());
      mapping.getUISet().setControl(ref);
      return null;
   }
   
   /**
    * Gets the default data type and format for the given control name.
    * 
    * @param controlName, Name of the control for which the data type and format
    *           is needed.
    * @return List of datatype and format. The first value will be datatype and
    *         second value will be dataformat. If control name is
    *         <code>null</code> or empty or unknown control name then returns
    *         default datatype of text and dataformat of 50.
    */
   public static List<String> getDefaultDataTypeAndFormat(
         String controlName)
   {
      List<String> lst = new ArrayList<String>();
      String dataType = PSField.DT_TEXT;
      String dataFormat = "50";
      if (StringUtils.isBlank(controlName))
      {
         lst.add(dataType);
         lst.add(dataFormat);
      }
      
      if (controlName.equals(CALENDAR_SIMPLE_CONTROL))
      {
         dataType = PSField.DT_DATE;
         dataFormat = null;
      }
      else if (controlName.equals(SINGLE_CHECKBOX_CONTROL))
      {
         dataType = PSField.DT_BOOLEAN;
         dataFormat = null;
      }
      else if (controlName.equals(FILE_CONTROL)
            || controlName.equals(FILE_WORD_CONTROL)
            || controlName.equals(WEB_IMAGE_FX_CONTROL))
      {
         dataType = PSField.DT_BINARY;
         dataFormat = PSField.MAX_FORMAT;
      }
      else
      {
         dataType = PSField.DT_TEXT;
         if (ms_wysiwygControls.contains(controlName))
            dataFormat = PSField.MAX_FORMAT;
         else if (controlName.equals(TEXT_AREA_CONTROL))
            dataFormat = "1000";
         else
            dataFormat = "50";

      }
      lst.add(dataType);
      lst.add(dataFormat);
      return lst;
   }

   /**
    * List of controls that can be used to map fields in parent, complex child
    * and multiproperty simple child fieldsets. Gets initialized and filled in
    * <code>getContentEditorControls(boolean)</code>
    */
   private static List ms_fieldControls;

   /**
    * List of controls that can be used to map fields in simple child
    * fieldsets. Gets initialized and filled in
    * <code>getContentEditorControls(boolean)</code>
    */
   private static List ms_simpleChildFieldControls;

   /**
    * List of controls that can be used to map complex childs in parent
    * mapper. Gets initialized and filled in
    * <code>getContentEditorControls(boolean)</code>
    */
   private static List ms_complexChildControls;

   /**
    * The list of content editor controls, initialized in
    * {@link #getContentEditorControls(boolean)}.
    */
   private static List ms_controlList = null;
   
   /**
    * Constant to identify field mapping.
    */
   public static final int TYPE_FIELD = 0;
   
   /**
    * Constant to identify child mapping.
    */
   public static final int TYPE_CHILD = 1; 
   
   
   /** 
    * The default control name for a child mapping in parent.
    */
   private static final String ms_defComplexChildControlName = "sys_Table";    //$NON-NLS-1$

   /**
    * The list of minimum system columns required for any content editor table.
    * The entries will be added first time {@link #getSystemColumns } is called
    * and never changed after that.
    */
   private static List<String> ms_sysColumns = null;

   /**
    * The list of minimum system columns required for any complex child content
    * editor table. The entries will be added first time {@link
    * #getSystemColumns } is called for complex child type and never changed
    * after that.
    */
   private static List<String> ms_sysComplexChildColumns = null;

   /**
    * Instance of PSCoreFactory
    */
   private static PSCoreFactory ms_factory = PSCoreFactory.getInstance();

   /**
    * A static list that holds the reserved words that can not be used for
    * field names. The entries will be added first {@link #getReservedWords }
    * is called and never changed after that.
    */
   private static List<String> ms_reservedWords = null;
   
   /*
    * Static variable initialization.
    */
   static
   {
      ms_sysColumns = new ArrayList<String>();
      ms_sysColumns.add(IPSConstants.ITEM_PKEY_CONTENTID);
      ms_sysColumns.add(IPSConstants.ITEM_PKEY_REVISIONID);

      ms_sysComplexChildColumns = new ArrayList<String>();
      ms_sysComplexChildColumns.addAll(ms_sysColumns);
      ms_sysComplexChildColumns.add(IPSConstants.CHILD_ITEM_PKEY);
   }

   /**
    * Constant that represents the EditLive content editor control
    */
   private static final String EDITLIVE_CONTROL = "sys_EditLive";
   
   /**
    * Constant that represents the eWebEditPro content editor control
    */
   private static final String WEP_CONTROL = "sys_eWebEditPro";

   /**
    * Constant that represents the EditLive dynamic content editor control
    */
   private static final String EDITLIVE_DYNAMIC_CONTROL = "sys_EditLiveDynamic";

   /**
    * Constant that represents the calendar simple content editor control
    */
   private static final String CALENDAR_SIMPLE_CONTROL = "sys_CalendarSimple";
   
   /**
    * Constant that represents the single check box content editor control
    */
   private static final String SINGLE_CHECKBOX_CONTROL = "sys_SingleCheckBox";

   /**
    * Constant that represents the text area content editor control
    */
   private static final String TEXT_AREA_CONTROL = "sys_TextArea";

   /**
    * Constant that represents the html editor content editor control
    */
   private static final String HTML_EDITOR_CONTROL = "sys_HtmlEditor";

   /**
    * Constant that represents the web image fx content editor control
    */
   private static final String WEB_IMAGE_FX_CONTROL = "sys_webImageFX";

   /**
    * Constant that represents the file word content editor control
    */
   private static final String FILE_WORD_CONTROL = "sys_FileWord";

   /**
    * Constant that represents the file content editor control
    */
   private static final String FILE_CONTROL = "sys_File";

   /**
    * Constant that represents the edit box content editor control
    */
   private static final String EDIT_BOX_CONTROL = "sys_EditBox";
   
   /**
    * These are the default control names to be used either when user adds a 
    * mapping in UI or guess mappings when a table is dropped on xml resource.
    */
   /** The default control name for field in parent, multiproperty simple child
    *  and complexchild fieldsets.
    */
   private static final String ms_defFieldControlName = EDIT_BOX_CONTROL; 
   
   /**
    * List of parameters to be preserved when switching between EditLive and
    * eWebEditPro controls.
    */
   private static final List<String> ms_preserveList = new ArrayList<String>(5);
   
   static
   {
      ms_preserveList.add("height");
      ms_preserveList.add("width");
      ms_preserveList.add("InlineLinkSlot");
      ms_preserveList.add("InlineImageSlot");
      ms_preserveList.add("InlineVariantSlot");      
   }
    
   /**
    * List of wysiwyg controls. When switched between them, we need to preserve
    * certain properties.
    */
   private static final List<String> ms_wysiwygControls = new ArrayList<String>();

   static
   {
      ms_wysiwygControls.add(EDITLIVE_CONTROL);
      ms_wysiwygControls.add(WEP_CONTROL);
      ms_wysiwygControls.add(EDITLIVE_DYNAMIC_CONTROL);
      ms_wysiwygControls.add(HTML_EDITOR_CONTROL);
   }

   /**
    * Constant for the editor type parent.
    */
   public static final int LOCALDEF_PARENT_EDITOR = 1;

   /**
    * Constant for the editor type child.
    */
   public static final int LOCALDEF_CHILD_EDITOR = 2;

   /**
    * Constant for the editor type system.
    */
   public static final int SYSTEMDEF_EDITOR = 3;

   /**
    * Constant for the editor type shared.
    */
   public static final int SHAREDDEF_EDITOR = 4;
   
   /**
    * Constant for the child field in parent
    */
   public static final int PARENT_CHILD_FIELD = 5;
   
   /**
    * Option to get all controls
    */
   public static final int ALL_CONTROLS = 1 << 1;
   
   /**
    * Option to get single dimension controls only
    */
   public static final int SINGLE_DIM_CONTROLS = 1 << 2;
   
   /**
    * Option to get array dimension controls only
    */
   public static final int ARRAY_DIM_CONTROLS = 1 << 3;
   
   /**
    * Option to get table dimension controls only
    */
   public static final int TABLE_DIM_CONTROLS = 1 << 4;

   /**
    * Constant for error message for invalid charcters in field/set name. 
    */
   public static final String FIELD_NAME_INVALID_CHARS =
         PSMessages.getString("PSContentEditorDefinition.error.invalidcharsinfieldname.message"); //$NON-NLS-1$

   /**
    * Constant for error message for invalid first charcter in field/set name. 
    */
   public static final String FIELD_NAME_INVALID_FIRSTCHAR =
         PSMessages.getString("PSContentEditorDefinition.error.invalidfirstcharinfieldname.message"); //$NON-NLS-1$

   /**
    * Constant for error message for first two characters in field name. 
    * A lowercase letter followed by an upper case letter is not allowed (i.e. aBcd)
    */
   public static final String FIELD_NAME_INVALID_FIRST_TWO_CHARS =
         PSMessages.getString("PSContentEditorDefinition.error.invalidfirsttwocharsinfieldname.message"); //$NON-NLS-1$

   /**
    * Constant for error message for empty field/set name. 
    */
   public static final String FIELD_NAME_EMPTY =
         PSMessages.getString("PSContentEditorDefinition.error.invalidemptyfieldname.message"); //$NON-NLS-1$

   /**
    * Constant for error message for field/set name is a reserved word. 
    */
   public static final String FIELD_NAME_RESERVED_WORD =
         PSMessages.getString("PSContentEditorDefinition.error.invalidreservedfieldname.message"); //$NON-NLS-1$

   /**
    * Constant for error message for no white spaces allowed in field/set name. 
    */
   public static final String FIELD_NAME_NOWHITESPACE =
         PSMessages.getString("PSContentEditorDefinition.error.invalidwhitespacesfieldname.message"); //$NON-NLS-1$
  
   /**
    * Constant for control parameter name max length.
    */
   public static final String CONTROL_PARAM_MAXLENGTH = "maxlength";
   
   
   

}
