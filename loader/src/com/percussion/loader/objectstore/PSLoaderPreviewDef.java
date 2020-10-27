/*[ PSLoaderPreviewDef.java ]**************************************************
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
 * Represents a PSLoaderPreviewDef xml object.
 */
public class PSLoaderPreviewDef extends PSLoaderDef
{
   /**
    * Constructs the object from the given parameters.
    *
    * @param strName The value of the 'name' attribute, may not be
    *    <code>null</code> or empty.
    *
    * @param strClass The value of the 'class' attribute, may not be
    *    <code>null</code> or empty.
    *
    * @param strUser The value of the 'user' element. Never <code>
    *    null</code> may be empty.
    *
    * @param strPass The value of the 'Password' element. Never
    *    <code>null</code> may be empty.
    *
    * @param bEnc boolean if <code>true</code> the password is encoded,
    *    otherwise it is not encoded.
    *
    * @param prevPath The fully qualified directory path to the to be
    *    uploaded contents. Never <code>null</code> may be empty.
    *
    * @param staticRoot The directory name for the uploaded static files.
    *    The static root is a directory under the <code>prevPath</code>.
    *    Never <code>null</code> may be empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSLoaderPreviewDef(String strName, String strClass,
      String prevPath, String staticRoot)
   {
      super(strName, strClass);

      if (prevPath == null || prevPath.trim().length() == 0)
         throw new IllegalArgumentException(
            "prevPath may not be null or empty");
      if (staticRoot == null || staticRoot.trim().length() == 0)
         throw new IllegalArgumentException(
            "staticRoot may not be null or empty");

      addProperty(new PSProperty(PREVPATH_PROP, prevPath));
      addProperty(new PSProperty(STATIC_ROOT_PROP, staticRoot));
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSLoaderPreviewDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      super(source);

      // make sure the required properties exist
      getPreviewPath();
      getStaticRoot();
   }

   /**
    * Get the preview path.
    *
    * @return The preview path, never <code>null</code> or empty.
    */
   public String getPreviewPath() throws PSLoaderException
   {
      return getProperty(PREVPATH_PROP).getValue();
   }

   /**
    * Sets the preview path.
    *
    * @param path, preview path, never <code>null</code> or empty.
    */
   public void setPreviewPath(String path) throws PSLoaderException
   {
      if (path == null || path.length() == 0)
         return;
      PSProperty prop = getProperty(PREVPATH_PROP);
      prop.setValue(path);
   }

   /**
    * Get the static root directory, which should be relative to the
    * preview path.
    *
    * @return The static root directory, never <code>null</code> or empty.
    */
   public String getStaticRoot() throws PSLoaderException
   {
      return getProperty(STATIC_ROOT_PROP).getValue();
   }

  /**
   * Public constants for property names
   */
   final static public String PREVPATH_PROP = "PreviewPath";
   final static public String STATIC_ROOT_PROP = "StaticRoot";
}
