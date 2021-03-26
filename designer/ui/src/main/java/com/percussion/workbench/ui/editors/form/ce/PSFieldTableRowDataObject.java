/******************************************************************************
 *
 * [ PSFieldDataObject.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.cms.objectstore.PSFieldDefinition;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSDependency;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.PSValidationException;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Convenient class to represent each data row of the fields and fieldset table.
 */
public class PSFieldTableRowDataObject
{

   /**
    * Creates an object of this class using the passed in PSFieldDefinition
    * object.
    * 
    * @param fieldDef must be a valid object of PSFieldDefinition must not be
    *           <code>null</code>.
    */
   public PSFieldTableRowDataObject(PSFieldDefinition fieldDef)
   {
      setFieldDefinition(fieldDef);
   }

   /**
    * Empty Constructor used for creating new table rows.
    */
   PSFieldTableRowDataObject()
   {
   }

   /**
    * Creates new field or field set based on the type of field and sets.
    * 
    * @param fieldName the name to be set to field reference, may not be
    *           <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>fieldName</code> is invalid.
    */
   public void setDefaultField(String fieldName)
   {
      if (fieldName == null || fieldName.length() == 0)
         throw new IllegalArgumentException(
               "fieldName can not be null or empty");
      PSField fld = m_fieldDef.getField();
      if (fld == null)
      {
         fld = new PSField(fieldName, null);
         fld.setDataType(DT_TEXT);
         try
         {
            fld.setOccurrenceDimension(PSField.OCCURRENCE_DIMENSION_OPTIONAL,
                  null);
         }
         catch (PSValidationException e)
         {
            // should not come here as it is setting default
         }
      }
   }

   /**
    * Get the actula field definition object, that this row data object is
    * representing.
    * 
    * @return PSFieldDefinition object may be <code>null</code>.
    */
   public PSFieldDefinition getFieldDefinition()
   {
      return m_fieldDef;
   }

   /**
    * Get the actula filed definition object, that this row data object is
    * representing.
    * 
    * @return PSFieldDefinition object may be <code>null</code>.
    */
   public void setFieldDefinition(PSFieldDefinition fieldDef)
   {
      if (fieldDef == null)
         throw new IllegalArgumentException("fieldDef must not be null");
      m_fieldDef = fieldDef;
   }

   /**
    * Gets the list of control dependencies from the data object and returns.
    * @return List of dependencies or <code>null</code>, if not set.
    */
   public List<PSDependency> getControlDependencies()
   {
      return m_fieldDef.getCtrlDependencies();
   }

   /**
    * Sets the list of control dependencies on the data object.
    * May be <code>null</code>.
    */
   public void setControlDependencies(List<PSDependency> deps)
   {
      m_fieldDef.setCtrlDependencies(deps);
   }

   /**
    * Checks whether all the required fileds are empty or not and returns
    * <code>false</code> even any one is empty.
    * 
    * @return boolean <code>true</code> if the object is not empty otherwise
    *         <code>false</code>.
    */
   public boolean isEmpty()
   {
      boolean ret = false;
      if (m_fieldDef == null || StringUtils.isBlank(getName()))
         ret = true;
      return ret;
   }

   /**
    * The actual PSFieldDefinition object corresponding to this convenient
    * class. Initialized in non empty constructor. If null gets created in
    * toFieldDefinition method.
    * 
    */
   private PSFieldDefinition m_fieldDef;

   /**
    * Gets the controlref.
    * 
    * @return PSControlRef object
    */
   public PSControlRef getControlRef()
   {
      PSControlRef cref = null;
      if (getUISet() != null)
      {
         cref = getUISet().getControl();
      }
      return cref;
   }

   /**
    * Sets the controlRef
    * 
    * @param controlRef may be <code>null</code>.
    */
   public void setControlRef(PSControlRef controlRef)
   {
      if (controlRef == null)
      {
         controlRef = new PSControlRef("sys_EditBox");
         controlRef.setId(PSContentEditorDefinition.getUniqueId());
      }
      if (getUISet() != null)
         getUISet().setControl(controlRef);
   }

   /**
    * Gets the label
    * 
    * @return label may be <code>null</code>.
    */
   public PSDisplayText getLabel()
   {
      PSDisplayText label = null;
      if (m_fieldDef != null)
      {
         label = m_fieldDef.getMapping().getUISet().getLabel();
      }
      return label;
   }

   /**
    * Sets the Label
    * 
    * @param label if <code>null</code> an empty PSDisplayText is created and
    *           assigned
    */
   public void setLabel(PSDisplayText label)
   {
      if (label == null)
         label = new PSDisplayText(StringUtils.EMPTY);
      if (m_fieldDef != null)
      {
         m_fieldDef.getMapping().getUISet().setLabel(label);
      }
   }

   /**
    * Gets the name
    * 
    * @return String name, may be empty, but never <code>null</code>.
    */
   public String getName()
   {
      String name = StringUtils.EMPTY;
      if (m_fieldDef != null)
      {
         if (m_fieldDef.isFieldSet())
            name = m_fieldDef.getFieldset().getName();
         else
            name = m_fieldDef.getField().getSubmitName();
      }
      return StringUtils.defaultString(name);
   }

   /**
    * Sets the name
    * 
    * @param name if may be <code>null</code> or empty.
    */

   public void setName(String name)
   {
      if (name == null || name.length() == 0)
      {
         throw new IllegalArgumentException("name must not be null or empty");
      }
      if (m_fieldDef == null)
      {
         throw new IllegalArgumentException(
               "name can not be set when the m_fieldDef is empty");
      }
      if (m_fieldDef.isFieldSet())
         m_fieldDef.getFieldset().setName(StringUtils.defaultString(name));
      else
         m_fieldDef.getField().setSubmitName(StringUtils.defaultString(name));
   }

   /*
    * Other convenient methods to get and set the inner objects.
    */
   public String getLabelText()
   {
      String label = StringUtils.EMPTY;
      if (getLabel() != null)
         label = getLabel().getText();
      return label;
   }

   public void setLabelText(String labelTxt)
   {
      PSDisplayText label = getLabel();
      if (label == null)
         label = new PSDisplayText(StringUtils.EMPTY);
      label.setText(StringUtils.defaultString(labelTxt));
   }

   public String getControlName()
   {
      String cname = StringUtils.EMPTY;
      if (getControlRef() != null)
         cname = getControlRef().getName();
      return StringUtils.defaultString(cname);
   }

   public void setControlName(String ctrlName)
   {
      PSControlRef ref = null;
      if (getControlRef() == null
            || !ctrlName.equals(getControlRef().getName()))
      {
         ref = new PSControlRef(ctrlName);
         ref.setId(PSContentEditorDefinition.getUniqueId());
         setControlRef(ref);
      }
   }

   public PSField getField()
   {
      PSField field = null;
      if (m_fieldDef != null)
         field = m_fieldDef.getField();
      return field;
   }

   public PSFieldSet getFieldSet()
   {
      PSFieldSet fset = null;
      if (m_fieldDef != null)
         fset = m_fieldDef.getFieldset();
      return fset;
   }

   public PSDisplayMapping getDisplayMapping()
   {
      PSDisplayMapping dm = null;
      if (m_fieldDef != null)
         dm = m_fieldDef.getMapping();
      return dm;
   }

   public PSUISet getUISet()
   {
      PSUISet uset = null;
      if (getDisplayMapping() != null)
         uset = getDisplayMapping().getUISet();
      return uset;
   }

   public boolean isFieldSet()
   {
      if (m_fieldDef != null)
         return m_fieldDef.isFieldSet();
      else
         return false;
   }

   /**
    * One of the indentifiers for the allowed Data types. Indicates that the
    * field contains character data.
    */
   public static final String DT_TEXT = "text";
}
