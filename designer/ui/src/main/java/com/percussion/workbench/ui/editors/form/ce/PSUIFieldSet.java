/******************************************************************************
*
* [ PSUIFieldSet.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is OS... wrapper class for PSFieldSet. Holds the additional information
 * required for content editor UI.
 */
public class PSUIFieldSet extends PSFieldSet
{
   /**
    * Creates a new object that is deep copy of the supplied field set.
    *
    * @param set a valid field set, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>set</code> is <code>null</code>
    */
   public PSUIFieldSet(PSUIFieldSet set)
   {
      deepCopyFrom(set);
   }

   /**
    * Creates a new object that takes all of its properties from the supplied
    * field set.
    *
    * @param set a valid field set, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>set</code> is <code>null</code>
    */
   public PSUIFieldSet(PSFieldSet set)
   {
      copyFrom(set);
   }


   /**
    * Creates a new field set.
    *
    * @param name the name of the field set, may not be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if <code>name</code> is invalid.
    */
   public PSUIFieldSet(String name)
   {
      super(name);
   }

   /**
    * Shallow copies from passed in field set.
    *
    * @param set a valid field set, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>set</code> is <code>null</code>
    */
   public void copyFrom(PSUIFieldSet set)
   {
      super.copyFrom(set);
      m_tableAlias = set.getTableAlias();
      setNameChanged(set.isNameChanged());
      m_setID = set.getSetID();
   }

   /**
    * Deep copies the passed in object to this object.
    *
    * @param set the object to be copied, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if set is <code>null</code>
    * @throws RuntimeException if the set's to and from XML does not work
    * properly.
    */
   public void deepCopyFrom(PSUIFieldSet set)
   {
      if(set == null)
         throw new IllegalArgumentException("set can not be null");

      copyFrom(set);
      removeAll();

      Iterator objects = set.getAll();
      while(objects.hasNext())
      {
         Object object = objects.next();
         if(object instanceof PSUIFieldSet)
         {
            PSUIFieldSet fieldset = (PSUIFieldSet)object;
            PSUIFieldSet copySet = new PSUIFieldSet(fieldset);
            add(copySet);
         }
         else
         {
            PSUIField field = (PSUIField)object;
            PSUIField copyField = new PSUIField(field);
            add(copyField);
         }
      }
   }

   /**
     * Performs a shallow copy of the data in the supplied component to this
     * component and converts the PS.. sub-objects to OS.. sub-objects. Sets the
    * table name from existing fields.
    *
    * @param set a valid field set, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>set</code> is
    * <code>null</code>
    * @throws UnsupportedOperationException if <code>set</code> is of type
    * <code>TYPE_MULTI_PROPERTY_SIMPLE_CHILD</code>
     *
     */
   public void copyFrom(PSFieldSet set )
   {
      super.copyFrom(set);

      List osfields = new ArrayList();
      Iterator fields = getAll();

      String tableName = null;
      while(fields.hasNext())
      {
         Object field = fields.next();
         if(field instanceof PSField)
         {
            PSUIField osfield = new PSUIField((PSField)field);
            osfields.add(osfield);
         }
         else
         {
            PSUIFieldSet fieldset = new PSUIFieldSet((PSFieldSet)field);
            osfields.add(fieldset);
         }
         fields.remove();
      }

      fields = osfields.iterator();
      while(fields.hasNext())
      {
         Object field = fields.next();
         if(field instanceof PSUIField)
            add((PSUIField)field);
         else
            add((PSUIFieldSet)field);
      }
   }

   /**
    * Overriden to set the field set id and type to the field getting added.
    * <br>
    * This information is useful to find out the field set information from the
    * field not by name and it is required for content editor UI.
    * <br>
    * See {@link PSFieldSet#add(PSField)} for parameter description
    */
   public Object add(PSField field)
   {
      PSUIField osfield;
      if(field instanceof PSUIField)
         osfield = (PSUIField)field;
      else
         osfield = new PSUIField(field);

      osfield.setFieldSetType(getType());
      osfield.setFieldSetID(getSetID());
      return super.add(field);
   }

   /**
    * Gets immediate child fieldset of this fieldset with supplied fieldset id.
    *
    * @param fieldSetID the fieldset id to check for, must be > 0
    *
    * @return the child fieldset if found, otherwise <code>null</code>
    *
    * @throws IllegalArgumentException if fieldset id is <= 0
    */
   public PSFieldSet getChildFieldSet(int fieldSetID)
   {
      if(fieldSetID <= 0)
         throw new IllegalArgumentException("fieldSetID must be > 0");

      PSUIFieldSet set = null;

      Iterator fields = getAll();
      while(fields.hasNext())
      {
         Object field = fields.next();
         if(field instanceof PSUIFieldSet)
         {
            PSUIFieldSet fieldset = (PSUIFieldSet)field;
            if(fieldset.getSetID() == fieldSetID)
            {
               set = fieldset;
               break;
            }
         }
      }

      return set;
   }   

   /**
    * Gets alias of table the field set is referring to. If this is
    * <code>null</code>, the field set is considered as new otherwise as
    * existing.
    *
    * @return table alias, may be <code>null</code>, never empty.
    */
   public String getTableAlias()
   {
      return m_tableAlias;
   }

   /**
    * Sets the table alias to which this field set refers.
    *
    * @param name the alias name, may be <code>null</code> or empty.
    *
    */
   public void setTableAlias(String name)
   {
      if (StringUtils.isEmpty(name))
      {
         name = null;
      }
      m_tableAlias = name;
   }

   /**
    * Gets this field set name.
    *
    * @return the field set name, never <code>null</code> or empty.
    */
   public String toString()
   {
      return getName();
   }

   /**
    * Checks whether the editor(field set) name is changed or not. Changing the
    * name of editor is changing the name of the table for first time. So when
    * loaded from template this flag is set to <code>false</code> and set to
    * <code>true</code> when user changes the editor name. This should be called
    * before saving the tables.
    *
    * @return <code>true</code> if it is changed, otherwise <code>false</code>
    */
   public boolean isNameChanged()
   {
      return m_isNameChanged;
   }

   /**
    * Sets the flag for editor(field set) name changed or not.
    *
    * @param change if <code>true</code>, sets the editor name as changed
    * otherwise not.
    *
    * @see #isNameChanged() for more description of the flag.
    */
   public void setNameChanged(boolean change)
   {
      m_isNameChanged = change;
   }

   /**
    * Gets the id of this fieldset. Please see {@link #setSetID(int)} for more
    * description.
    *
    * @return this fieldset id.
    */
   public int getSetID()
   {
      return m_setID;
   }

   /**
    * Sets the id to this field set and updates all fields of this fieldset with
    * this field set id. This id maintains the relationship between the fieldset
    * and field and is helpful to find the fieldset from field which is required
    * in Content Editor UI dialogs.
    *
    * @param id the id of field set
    */
   public void setSetID(int id)
   {
      m_setID = id;

      Iterator fields = getAll();
      while(fields.hasNext())
      {
         Object field = fields.next();
         if(field instanceof PSUIField)
            ((PSUIField)field).setFieldSetID(id);
      }
   }

   /**
    * Clears the locator for the local fields with <code>PSXBackEndColumn</code>
    * locator in this field set, recursively.
    */
   public void clearBackEndLocators()
   {
      Iterator fields = getAll();

      while(fields.hasNext())
      {
         Object field = fields.next();
         if(field instanceof PSField)
         {
            PSField psfield = (PSField)field;
            if(psfield.isLocalField() &&
               psfield.getLocator() instanceof PSBackEndColumn)
               psfield.setLocator(null);
         }
         else
            ((PSUIFieldSet) field).clearBackEndLocators();
      }
   }

   /**
    * Checks whether this fieldset has any new local fields with invalid
    * datatype and format.
    *
    * @return <code>true</code> if any of the new local fields were not
    * specified with proper datatype and format, otherwise <code>false</code>
    */
   public boolean hasInvalidDatatypeAndFormatFields()
   {
      Iterator fields = getAll();

      while(fields.hasNext())
      {
         Object field = fields.next();
         if(field instanceof PSUIField)
         {
            PSUIField osfield = (PSUIField)field;
            if(osfield.isLocalField() && osfield.getLocator() == null)
            {
               if(!osfield.hasValidDataTypeAndFormat())
                  return true;
            }
         }
      }

      return false;
   }


   /**
    * A flag to indicate whether the name of field set is changed after loading
    * from template, if <code>true</code> the name is changed otherwise not.
    * Initially flag is set to <code>true</code> always and set to
    * <code>false</code> when reading the fieldset from template. The method
    * <code>setNameChanged(boolean)</code> with <code>true</code> should be
    * called when the user changes the name of fieldset.
    */
   private boolean m_isNameChanged = true;

   /**
    * The table alias which the field set is referring to. Initialized from the
    * back-end locator of the fields of this field set.
    */
   private String m_tableAlias = null;

   /**
    * The id of the field set. Initilized to zero and will be set with unique id
    * if this field set is part of content editor mapper whenever the dialog is
    * loaded. This id is set as field set id in all fields of this fieldset to
    * maintain the relationship from field to fieldset.
    */
   private int m_setID = 0;
}
