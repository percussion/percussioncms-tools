/******************************************************************************
 *
 * [ OSField.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.utils.collections.PSIteratorUtils;
import com.percussion.workbench.ui.editors.form.ce.PSContentEditorDefinition;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is OS... wrapper class for PSField. Holds the additional information
 * required for content editor UI.
 */
public class OSField extends PSField
{
   /**
    * Creates new object with supplied parameters.
    *
    * @param fieldType the field type to create, must be one of TYPE_SYSTEM |
    *    TYPE_SHARED | TYPE_LOCAL.
    * @param fieldName the field name, not <code>null</code> or empty.
    * @throws IllegalArgumentException if the provided name is
    *    <code>null</code> or empty or the type is unknown.
    */
   public OSField(int fieldType, String fieldName)
   {
      super(fieldType, fieldName, null);
   }

   /**
    * Creates a new field for the provided DataLocator.
    *
    * @param type the field type to create, must be one of TYPE_SYSTEM |
    *    TYPE_SHARED | TYPE_LOCAL.
    * @param name the field name, not <code>null</code> or empty.
    * @param locator the data locator for this field, may be <code>null</code>.
    * @throws IllegalArgumentException if the provided name is
    *    <code>null</code> or the name is empty or the type is unknown.
    */
   public OSField(int type, String name, IPSBackEndMapping locator)
   {
      super(type, name, locator);
   }

   /**
    * Creates a new object that deep copies all of its properties from the
    * supplied field.
    * @param field a valid field , may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>field</code> is <code>null</code>
    */
   public OSField(PSField field)
   {
      if(field == null)
         throw new IllegalArgumentException("field can not be null");

      deepCopyFrom(field);
   }

   /**
    * Shallow copies from passed in field.
    *
    * @param field a valid field , may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>field</code> is <code>null</code>
    */
   public void copyFrom(OSField field)
   {
      super.copyFrom(field);

      m_fieldSetType = field.getFieldSetType();
      m_fieldSetID = field.getFieldSetID();
   }

   /**
    * Deep copies the passed in object to this object.
    *
    * @param field the object to be copied, may not be <code>null</code>
    *
    * @throws RuntimeException if the field's to and from XML does not work
    * properly.
    */
   public void deepCopyFrom(PSField field)
   {
      if(field == null)
         throw new IllegalArgumentException("field can not be null");

      /* As deep copy of this involves deep copying many objects which does
       * not support deep copy, the better approach thought is write it to
       * xml and read from it again.
       */
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = field.toXml(doc);

      try {
         fromXml(root, null, null);
      }
      catch(PSUnknownNodeTypeException e)
      {
         //We should not come here as we are constructing from xml which is
         //created by a valid field's toXml()
         throw new RuntimeException(e.getMessage());
      }
      if(field instanceof OSField)
      {
         OSField osfield = ((OSField)field);

         m_fieldSetType = osfield.getFieldSetType();
         m_fieldSetID = osfield.getFieldSetID();
      }
   }

   /**
    * Set this field's data type and format.
    *
    * @param type the field data type, not <code>null</code> may be
    * empty, must be a valid datatype if not empty.  See {@link
    * #getValidDataTypes()} for more info.
    * @param format the format of field's data type, may be <code>null</code>.
    * If the specified type is empty or does not support specifying a format,
    * then must be <code>null</code>.  If the specified type does support a
    * format, then must be a valid format for field datatype.  See {@link
    * #getDefinedDataTypeFormats(String)} for more info.
    * If the specified type supports formats, then may not be <code>null</code>.
    */
   public void setDataType(String type, String format)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null");

      if (type.trim().length() != 0)
      {
         if (!ms_dtInfoMap.keySet().contains(type))
            throw new IllegalArgumentException("type is invalid");

         if(format != null && !isValidDataTypeFormat(type, format))
            throw new IllegalArgumentException("format (" + format +
               ") is not valid for type: " + type);
         else if (format == null && supportsFormat(type))
            throw new IllegalArgumentException(
               "format must be supplied for type: " + type);
      }
      else if (format != null)
         throw new IllegalArgumentException(
            "format may not be supplied for empty type");

      super.setDataType(type);
      super.setDataFormat(format);
   }

   /**
    * Set this field's data type and sets the format to the default format for
    * the specified datatype.  Clears the format if format is not required
    * for the passed in data type.
    *
    * @param type the field data type, not <code>null</code>, if not
    * empty, must be a valid datatype.  See {@link #getValidDataTypes()} and
    * {@link #getDefaultFormat(String) getDefaultFormat} for more
    * info.
    *
    * @throws IllegalArgumentException if the provided data type is <code>null
    * </code> or invalid.
    */
   @Override
   public void setDataType(String type)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null");

      if (type.trim().length() != 0)
      {
         if (!ms_dtInfoMap.keySet().contains(type))
            throw new IllegalArgumentException("type is invalid: " + type);

         setDataType(type, getDefaultFormat(type));
      }
      else
         super.setDataType(type);
   }

   /**
    * Get the format of this field's data type.
    *
    * @return the format of this field's data type, may be <code>null</code>,
    * when format is not required for the field's datatype.
    */
   public String getFormat()
   {
      return super.getDataFormat();
   }

   /**
    * Sets the format for this field's datatype.  Must be a valid format for the
    * field's datatype.
    *
    * @param format A valid format for this field's datatype.   See
    * {@link #getDefinedDataTypeFormats(String)} for more
    *  info.
    */
   public void setFormat(String format)
   {
      if (!isValidDataTypeFormat(getDataType(), format))
         throw new IllegalArgumentException("format is invalid: " + format);

      super.setDataFormat(format);
   }

   /**
    * Sets the type of fieldset to which this field belongs to. This information
    * is useful to findout the controls to display for this field mapping in
    * content editor UI. This should be called whenever field gets added to
    * fieldset.
    *
    * @param type the type of fieldset, must be one of valid types of
    * <code>PSFieldSet</code>.
    *
    * @throws IllegalArgumentException if <code>type</code> is invalid.
    *
    */
   public void setFieldSetType(int type)
   {
      PSContentEditorDefinition.checkFieldSetType(type);
      m_fieldSetType = type;
   }

   /**
    * Gets the fieldset type to which this field belongs to. This information
    * is useful to findout the controls to display for this field mapping in
    * content editor UI.
    * 
    * @return One of the <code>TYPE_XXX</code> values.
    */
   public int getFieldSetType()
   {
      return m_fieldSetType;
   }

   /**
    * Sets the component id of fieldset to which this field belongs to. This
    * information is useful to find out the fieldset of the field when the field
    * name is changed. This should be called whenever field gets added to
    * fieldset.
    *
    * @param id the component id of fieldset
    */
   public void setFieldSetID(int id)
   {
      m_fieldSetID = id;
   }

   /**
    * Gets the fieldset id to which this field belongs to. This information is
    * useful to find out the fieldset of the field when the field name is
    * changed.
    * 
    * @return The id.
    */
   public int getFieldSetID()
   {
      return m_fieldSetID;
   }

   /**
    * Gets this field name.
    *
    * @return the field name, never <code>null</code> or empty.
    */
   @Override
   public String toString()
   {
      return getSubmitName();
   }

   /**
    * Checks whether this field has valid data type and format. If field's data
    * type is valid and the format is supported for the data type, it checks for
    * the validity of the field's format and returns the result.
    *
    * @return <code>true</code> if the data type and format are valid.
    */
   public boolean hasValidDataTypeAndFormat()
   {
      boolean isValid = false;

      String dataType = getDataType();
      if( dataType != null && dataType.trim().length() != 0 &&
         isValidDataType(dataType) )
      {
         String format = getFormat();
         if( supportsFormat(dataType) )
         {
            if( format != null && format.trim().length() != 0 )
            {
               isValid = isValidDataTypeFormat( dataType, format );
            }
         }
         else
            isValid = true;
      }

      return isValid;
   }

   /**
    * Determines the correct JDBC datatype and params for this field based on
    * datatype and format.  This method must be maintained as new datatypes and
    * format options are specified in the static intializer.
    *
    * @param params The buffer to which the params value is appended.  May not
    * be <code>null</code> and must be empty.  The params value is the value
    * supplied along with the datatype when creating a column, for example in
    * the statement:
    * <p>
    * <code>CREATE TABLE Foo (myCol VARCHAR(255) NOT NULL)</code>
    * <p>
    * "255" is the params value.  If no params value is to be supplied, the
    * buffer is left empty.
    *
    * @return One of the values from {@link java.sql.Types}.
    *
    * @throws IllegalArgumentException if params is <code>null</code> or not
    * empty.
    * @throws IllegalStateException if this field does not have a datatype
    * defined.
    */
   public int getJdbcDataType(StringBuffer params)
   {
      if (params == null)
         throw new IllegalArgumentException("params may not be null");

      if (params.length() > 0)
         throw new IllegalArgumentException("params must be empty");

      int jdbcDataType = Types.NULL;
      int size = -1;
      String contentDataType = getDataType();
      String format = getFormat();

      // if format is not "max", it's a number
      if (format != null)
      {
         if (!format.equalsIgnoreCase(MAX_FORMAT))
         {
            try
            {
               size = Integer.parseInt(format);
               format = null;
            }
            catch (NumberFormatException e)
            {
               throw new IllegalStateException("Invalid format \"" + format +
                  "\" for datatype \"" + contentDataType + "\"");
            }
         }
      }

      if (contentDataType.equals(DT_BINARY))
      {
         /**
          * Driver with smallest varbinary is Oracle with the max RAW being
          * 2000.
          */
         jdbcDataType = Types.BLOB;
         if (size != -1)
         {
            if (size <= 2000)
               jdbcDataType = Types.VARBINARY;
            else
            {
               // leave as blob and clear the size
               size = -1;
            }
         }
      }
      else if (contentDataType.equals(DT_TEXT))
      {
         /**
          * Driver with smallest varchar is Oracle with the max VARCHAR2 being
          * 4000.
          */
         jdbcDataType = Types.CLOB;
         if (size != -1)
         {
            if (size <= 4000)
               jdbcDataType = Types.VARCHAR;
            else
               size = -1;  // leave as clob and clear the size
         }
      }
      else if (contentDataType.equals(DT_INTEGER))
         jdbcDataType = Types.INTEGER;
      else if (contentDataType.equals(DT_FLOAT))
         jdbcDataType = Types.FLOAT;
      else if (contentDataType.equals(DT_DATE))
         jdbcDataType = Types.DATE;
      else if (contentDataType.equals(DT_TIME))
         jdbcDataType = Types.TIME;
      else if (contentDataType.equals(DT_DATETIME))
         jdbcDataType = Types.TIMESTAMP;
      else if (contentDataType.equals(DT_BOOLEAN))
         jdbcDataType = Types.BIT;
      else
         throw new IllegalStateException("Unknown datatype: " +
            contentDataType);

      if (size != -1)
         params.append(String.valueOf(size));

      return jdbcDataType;
   }

   /**
    * Gets the list of valid content editor datatypes.
    *
    * @return An iterator over one or more Strings, each specifying a valid
    * datatype.  Never <code>null</code>, sorted lexicographically.
    */
   public static Iterator getValidDataTypes()
   {
      return ms_dtInfoMap.keySet().iterator();
   }

   /**
    * Determines if specified dataType is valid.
    *
    * @param dataType The dataType to check, may not be <code>null</code>.
    *
    * @return <code>true</code> if the specified dataType is valid, <code>false
    * </code> if not.
    *
    * @throws IllegalArgumentException if dataType is <code>null</code>.
    */
   public static boolean isValidDataType(String dataType)
   {
      if (dataType == null)
         throw new IllegalArgumentException("dataType may not be null");

      return ms_dtInfoMap.containsKey(dataType);
   }

   /**
    * Gets the list of predefined formats for the specified content editor
    * datatype.
    *
    * @param dataType The dataType to return formats for, may not be <code>null
    * </code>, and must be a valid datatype (see {@link #getValidDataTypes()}).
    *
    * @return An iterator over zero or more Strings, each specifying a valid
    * format.  Will be empty if the specified type does not support
    * specifying a format, or if the specified type supports a format
    * but does not have any predefined.  The first item in the list should be
    * offered as the default choice if this list is presented to a user (i.e.
    * as choices in a combo box).
    *
    * @throws IllegalArgumentException if dataType is <code>null</code> or not
    * a valid datatype.
    */
   public static Iterator getDefinedDataTypeFormats(String dataType)
   {
      if (dataType == null || !isValidDataType(dataType))
         throw new IllegalArgumentException("dataType is invalid: " + dataType);

      CEDataTypeInfo dtInfo = ms_dtInfoMap.get(dataType);
      Iterator formats = null;
      if (dtInfo.supportsFormat())
         formats = dtInfo.getFormats();
      else
         formats = PSIteratorUtils.emptyIterator();

      return formats;
   }

   /**
    * Returns the default format for the specified type.
    *
    * @param dataType The dataType to return the default format for, may not be
    * <code>null</code>, and must be a valid datatype (see
    * {@link #getValidDataTypes()}).
    *
    * @return The default format, may be <code>null</code> if the specified
    * type does not have a default or if formats are not supported.
    *
    * @throws IllegalArgumentException if dataType is <code>null</code> or not
    * a valid datatype.
    */
   public static String getDefaultFormat(String dataType)
   {
      if (dataType == null || !isValidDataType(dataType))
         throw new IllegalArgumentException("dataType is invalid: " + dataType);

      CEDataTypeInfo dtInfo = ms_dtInfoMap.get(dataType);
      return dtInfo.getDefaultFormat();
   }

   /**
    * Determines if specified format is valid for specified dataType.
    *
    * @param dataType The dataType to check formats for, may not be <code>null
    * </code>, and must be a valid datatype (see {@link #getValidDataTypes()}).
    * @param format The format to check.  May not be <code>null</code> or empty.
    *
    * @return <code>true</code> if the specified format is valid, <code>false
    * </code> if not.
    *
    * @throws IllegalArgumentException if dataType is <code>null</code>,
    * invalid, or if format is <code>null</code> or empty.
    */
   public static boolean isValidDataTypeFormat(String dataType, String format)
   {
      if (dataType == null || !isValidDataType(dataType))
         throw new IllegalArgumentException("dataType is invalid:" + dataType);

      if (format == null || format.trim().length() == 0)
         throw new IllegalArgumentException("format may not be null or empty");

      CEDataTypeInfo dtInfo = ms_dtInfoMap.get(dataType);
      return dtInfo.isValid(format);
   }

   /**
    * Determines if specified dataType supports a format.
    *
    * @param dataType The dataType to check, may not be <code>null</code>, and
    * must be a valid datatype (see {@link #getValidDataTypes()}).
    *
    * @return <code>true</code> if the specified datatype supports specifying a
    * format, <code>false</code> if not.
    *
    * @throws IllegalArgumentException if dataType is <code>null</code> or
    * invalid.
    */
   public static boolean supportsFormat(String dataType)
   {
      if (dataType == null || !isValidDataType(dataType))
         throw new IllegalArgumentException("dataType is invalid:" + dataType);

      CEDataTypeInfo dtInfo = ms_dtInfoMap.get(dataType);
      return dtInfo.supportsFormat();
   }

   /**
    * The id of the field set to which this field belongs to. Initilized to zero
    * and should be set properly when this field gets added to the field set
    * with it's component id. This information is useful to find out the field
    * set of the field when the field name is changed.
    */
   private int m_fieldSetID = 0;

   /**
    * The type of field set to which this field belongs to. Initialized to
    * <code>TYPE_PARENT</code> and should be set properly when this field gets
    * added to the field set with it's type. This information is useful to find
    * out the controls to display for this field mapping in content editor UI.
    */
   private int m_fieldSetType = PSFieldSet.TYPE_PARENT;

   /**
    * The default datatype to be used when new field is created, currently
    * "text".
    */
   public static final String DEFAULT_DATATYPE = DT_TEXT;

   /**
    * Map of info for each datatype.  Never <code>null</code> or
    * empty, immutable. Must be maintained to contain an entry for each valid
    * datatype. The key is the datatype name as a String, and the value is a
    * {@link CEDataTypeInfo} object, never <code>null</code>.  Entries are
    * sorted by the key value lexicographically.
    */
   private static final Map<String, CEDataTypeInfo> ms_dtInfoMap =
         new TreeMap<String, CEDataTypeInfo>();

   static
   {
      /* create format map and add all valid types and their format info.  Any
       * type that supports formatting must supply a default format.  Predefined
       * values should be added so that the first value is the one that should
       * be offered as the default choice if presented to a user (i.e. as
       * choices in a combo box.
       */

      // for now "max" is the only predefined format available
      List<String> maxFormat = new ArrayList<String>(1);
      maxFormat.add(MAX_FORMAT);

      // setup text type, add it with "max" in format list and 50 as default
      CEDataTypeInfo text = new CEDataTypeInfo();
      text.setFormats(maxFormat.iterator());
      text.setDefaultFormat("50");
      ms_dtInfoMap.put(DT_TEXT, text);

      // setup binary type, add it with "max" in format list and as default
      CEDataTypeInfo bin = new CEDataTypeInfo();
      bin.setFormats(maxFormat.iterator());
      bin.setDefaultFormat(MAX_FORMAT);
      ms_dtInfoMap.put(DT_BINARY,  bin);

      CEDataTypeInfo none = new CEDataTypeInfo();
      none.setSupportsNumeric(false);

      ms_dtInfoMap.put(DT_INTEGER, none);
      ms_dtInfoMap.put(DT_FLOAT, none);
      ms_dtInfoMap.put(DT_DATE, none);
      ms_dtInfoMap.put(DT_TIME, none);
      ms_dtInfoMap.put(DT_DATETIME, none);
      ms_dtInfoMap.put(DT_BOOLEAN, none);
   }
}
