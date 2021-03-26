/*[ PSStaticItemExtractorDef.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;

import org.w3c.dom.Element;

/**
 * Encapsulates the definition for static item extractors, which are not
 * item extractors.
 */
public class PSStaticItemExtractorDef extends PSExtractorDef
   implements java.io.Serializable
{
   /**
    * Constructs the object from the given parameters.
    *
    * @param strName The value of the 'name' attribute, may not be
    *    <code>null</code> or empty.
    *
    * @param strType The value of the 'type' attribute, may not be
    *    <code>null</code> or empty. It must be <code>TYPE_STATIC</code>.
    *
    * @param strSeq The value of the 'sequence' attribute, may not be
    *    <code>null</code> or empty. Defaults to 'staticItem'.
    *
    * @param strClass The value of the 'Class' element, may not be
    *    <code>null</code> or empty.
    *
    * @param contentVariableName The content variable name, may not be
    *    <code>null</code> or empty.
    *
    * @param srcLocation The source location, may not be <code>null</code> or
    *    empty.
    *
    * @param tgtLocation The target location, may not be <code>null</code> or
    *    empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSStaticItemExtractorDef(String strName, String strType,
      String strClass, String contentVariableName,
      String srcLocation, String tgtLocation)
   {
      super(strName, strType, strClass);

      if (contentVariableName == null ||
         contentVariableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "contentVariableName may not be null or empty");
      if (srcLocation == null || srcLocation.trim().length() == 0)
         throw new IllegalArgumentException(
            "srcLocation may not be null or empty");
      if (tgtLocation == null || tgtLocation.trim().length() == 0)
         throw new IllegalArgumentException(
            "tgtLocation may not be null or empty");

      if (! strType.equals(TYPE_STATIC))
         throw new IllegalArgumentException("strType must be " + TYPE_STATIC);

      PSProperty prop;
      prop = new PSProperty(CONTEXT_VARIABLE_NAME, contentVariableName);
      m_properties.addComponent(prop);

      prop = new PSProperty(SOURCE_LOCATION, srcLocation);
      m_properties.addComponent(prop);

      prop = new PSProperty(TARGET_LOCATION, tgtLocation);
      m_properties.addComponent(prop);
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code> or the type is not <code>TYPE_STATIC</code>.
    *
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    *
    * @throws PSLoaderException if any of the required properties are missing.
    *    The required properties are <code>CONTENT_VARIABLE_NAME</code>,
    *    <code>SOURCE_LOCATION</code> and <code>TARGET_LOCATION</code>.
    */
   public PSStaticItemExtractorDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      super(source);

      if (! m_strType.equals(TYPE_STATIC))
         throw new IllegalArgumentException("m_strType must be " + TYPE_STATIC);

      // make sure all required properties exist
      getContextVariableName();
      getSourceLocation();
      getTargetLocation();
   }

  /**
   * Get the context variable name of the object.
   *
   * @return The context variable name, never <code>null</code> or empty.
   *
   * @throws PSLoaderException if cannot find property of
   *    <code>CONTENT_VARIABLE_NAME</code>
   */
   public String getContextVariableName() throws PSLoaderException
   {
      return getProperty(CONTEXT_VARIABLE_NAME).getValue();
   }

   /**
    * Set the context variable name
    * 
    * @param varName The to be set variable name, may not be <code>null</code>.
    */
   public void setContextVariableName(String varName)
   {
      if (varName == null)
         throw new IllegalArgumentException("varName may not be null");
         
      PSProperty prop = null;
      try
      {
         prop = getProperty(CONTEXT_VARIABLE_NAME);
      }
      catch (PSLoaderException e) // this is not possible
      {
         throw new RuntimeException("Unexpected exception: " + e.toString());
      }
      
      prop.setValue(varName);
   }
   
  /**
   * Get the source location of the object.
   *
   * @return The source location, never <code>null</code> or empty.
   *
   * @throws PSLoaderException if cannot find property of
   *    <code>SOURCE_LOCATION</code>
   */
   public String getSourceLocation() throws PSLoaderException
   {
      return getProperty(SOURCE_LOCATION).getValue();
   }

   /**
    * Set the source location.
    *
    * @param srcLocation The to be set source location, may not
    *    <code>null</code> or empty.
    *
   * @throws PSLoaderException if cannot find property of
   *    <code>SOURCE_LOCATION</code>
    */
   public void setSourceLocation(String srcLocation) throws PSLoaderException
   {
      if (srcLocation == null || srcLocation.trim().length() == 0)
         throw new IllegalArgumentException(
            "srcLocation may not be null or empty");

      PSProperty prop = getProperty(SOURCE_LOCATION);
      prop.setValue(srcLocation);
   }

  /**
   * Get the target location of the object.
   *
   * @return The target location, never <code>null</code> or empty.
   *
   * @throws PSLoaderException if cannot find property of
   *    <code>TARGET_LOCATION</code>
   */
   public String getTargetLocation() throws PSLoaderException
   {
      return getProperty(TARGET_LOCATION).getValue();
   }

  /**
   * Set the target location of the object.
   *
    * @param tgtLocation The to be set target location, may not
    *    <code>null</code> or empty.
   *
   * @throws PSLoaderException if cannot find property of
   *    <code>TARGET_LOCATION</code>
   */
   public void setTargetLocation(String tgtLocation) throws PSLoaderException
   {
      if (tgtLocation == null || tgtLocation.trim().length() == 0)
         throw new IllegalArgumentException(
            "tgtLocation may not be null or empty");

      PSProperty prop = getProperty(TARGET_LOCATION);
      prop.setValue(tgtLocation);
   }

   /**
    * The property name of the context type name.
    */
   final static public String CONTEXT_VARIABLE_NAME = "ContextVariableName";

   /**
    * The property name of the source location.
    */
   final static public String SOURCE_LOCATION = "Source Location";

   /**
    * The property name of the target location.
    */
   final static public String TARGET_LOCATION = "Target Location";

   /**
    * The plugin class for this plugin definition
    */
   final static public String PLUGIN_CLASS =
      "com.percussion.loader.extractor.PSStaticItemExtractor";
}
