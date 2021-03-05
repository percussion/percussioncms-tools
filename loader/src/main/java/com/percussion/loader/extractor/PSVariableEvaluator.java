/*[ PSVariableEvaluator.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.extractor;

import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSLoaderUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * This class is used to evaluate an extractor variable against its current
 * context. The <code>VAR_XXX</code> is a list of pre-defined extractor
 * variables.
 */
public class PSVariableEvaluator
{
   /**
    * Constructs this object from a set of parameters as the current context.
    * The encoding of the current JVM will be used to convert bytes to text
    * if it is not set by calling {@link #setEncoding(String)}.
    *
    * @param itemCtx The data source context, it may not be <code>null</code>.
    *
    * @param isBinaryContent <code>true</code> if the content of the data source
    *    is binary.
    *
    * @param in  The stream that can be used to get the source data. The caller
    *    of this method is responsible to close this stream, but not the method
    *    itself. It may be <code>null</code>.
    */
   public PSVariableEvaluator(PSItemContext itemCtx, boolean isBinaryContent,
      InputStream in)
   {
      if (itemCtx == null)
         throw new IllegalArgumentException("itemCtx may not be null");

      m_itemCtx = itemCtx;
      m_isBinaryContent = isBinaryContent;
      m_sourceData = in;
   }

   /**
    * Set the encoding that is used to convert byte content to text. This will
    * only effect text content, has no effect for binary content.
    *
    * @param encoding The specified encoding, may not be <code>null</code>.
    */
   public void setEncoding(String encoding)
   {
      if (encoding == null || encoding.trim().length() == 0)
         throw new IllegalArgumentException("encoding may not be null");

      m_encoding = encoding;
   }

   /**
    * Evaluates the specified expression against current source data of this
    * object.
    *
    * @param expression The to be evaluated expression. It may be one of the
    *    <code>VAR_XXX</code> or a string constant. It may be empty or
    *    <code>null</code>.
    *
    * @return The evaluated field value, never <code>null</code>.
    *
    * @throws IllegalStateException source data is <code>null</code> and
    *    <code>expression</code> is <code>VAR_CONENT</code>.
    *
    * @throws java.io.IOException if error occurs while retrieving source data.
    */
   public IPSFieldValue evaluate(String expression)
      throws IOException
   {
      if (expression == null)
         expression = "";

      IPSFieldValue fieldValue = null;

      if (expression.equalsIgnoreCase(VAR_CONTENT))
      {
         fieldValue = getContent();
      }
      else if (expression.equalsIgnoreCase(VAR_CURRENT_DATETIME))
      {
         fieldValue = new PSDateValue(new Date(System.currentTimeMillis()));
      }
      else if (expression.equalsIgnoreCase(VAR_FILEEXTENSION))
      {
         fieldValue = new PSTextValue(m_itemCtx.getResourceExtension());
      }
      else if (expression.equalsIgnoreCase(VAR_FILENAME))
      {
         fieldValue = new PSTextValue(m_itemCtx.getResourceFileName());
      }
      else if (expression.equalsIgnoreCase(VAR_FILENAME_EXTENSION))
      {
         String name_extension = new File(m_itemCtx.getResourceId()).getName();
         fieldValue = new PSTextValue(name_extension);
      }
      else if (expression.equalsIgnoreCase(VAR_FILEPATH))
      {
         String rootUrl = m_itemCtx.getRootResourceId();
         String filepath = m_itemCtx.getResourceId();
         if (rootUrl != null && filepath.startsWith(rootUrl))
         {
            if (filepath.length() > (rootUrl.length() + 1))
               filepath = filepath.substring(rootUrl.length() + 1); // skip '/'
         }
         fieldValue = new PSTextValue(filepath);
      }
      else if (expression.equalsIgnoreCase(VAR_FILESIZE))
      {
         byte[] rawContent = getRawContent();
         m_itemCtx.setResourceDataLength(rawContent.length);
         fieldValue = new PSTextValue(
            Long.toString(m_itemCtx.getResourceDataLength()));
      }
      else if (expression.equalsIgnoreCase(VAR_MIMETYPE))
      {
         fieldValue = new PSTextValue(m_itemCtx.getResourceMimeType());
      }
      else // must be a string constant
      {
         fieldValue = new PSTextValue(expression);
      }

      return fieldValue;
   }

   /**
    * Returns an array of all allowed variables
    * @return array of variables, Never <code>null</code>.
    */
   public static String[] getAllowedVariables()
   {
      return ms_allowedVars;
   }

   /**
    * Get the content value of the source data.
    *
    * @return the content value, never <code>null</code>. The type of the
    *    value is based on <code>m_isBinaryContent</code>.
    *
    * @throws java.io.IOException if error occurs while retrieving source data.
    */
   private IPSFieldValue getContent()
      throws IOException
   {
      IPSFieldValue fvalue = null;
      if (m_isBinaryContent)
      {
         fvalue = new PSBinaryValue(getRawContent());
      }
      else
      {
         byte[] data = getRawContent();
         String textData;
         if (m_encoding == null)
            textData = new String(data);
         else
            textData = new String(data, m_encoding);
         fvalue = new PSTextValue(textData);
      }
      return fvalue;
   }

   /**
    * Get the raw content from the source data.
    * 
    * @return The retrieved raw content in bytes, never <code>null</code>.
    *
    * @throws java.io.IOException if error occurs while retrieving source data.
    */
   private byte[] getRawContent()
      throws IOException
   {
      if (m_sourceData == null)
         throw new IllegalStateException("m_sourceData must not be null");

      if (m_rawContent == null)
         m_rawContent = PSLoaderUtils.getRawData(m_sourceData);
      
      return m_rawContent;
   }
   
   /**
    * See {@link #setEncoding(String)} for its description. It may be
    * <code>null</code>.
    */
   private String m_encoding = null;

   /**
    * See {@link #PSVariableEvaluator(PSItemContext, boolean, InputStream)}
    * for its definition. Initialized by ctor, may be <code>null</code>.
    */
   private InputStream m_sourceData;

   /**
    * See {@link #PSVariableEvaluator(PSItemContext, boolean, InputStream)}
    * for its definition. Initialized by ctor, never <code>null</code> after
    * that.
    */
   private PSItemContext m_itemCtx;

   /**
    * See {@link #PSVariableEvaluator(PSItemContext, boolean, InputStream)}
    * for its definition. Initialized by ctor.
    */
   private boolean m_isBinaryContent;

   /**
    * The raw content of the source data. It is set by {@link #getRawContent()}.
    * See {@link #getRawContent()} for its description. It is <code>null</code>
    * if has not been set.
    */
   private byte[] m_rawContent = null;
   
   /**
    * The content of the source file or URL. It is binary content for binary
    * extractors, but is text / character content for text extractors
    */
   public final static String VAR_CONTENT       = "$content";

   /**
    * The file extension of the source file or URL.
    */
   public final static String VAR_FILEEXTENSION = "$fileextension";

   /**
    * The name of the source file or URL, which does not include file extension
    * and the parent directories
    */
   public final static String VAR_FILENAME      = "$filename";

   /**
    * The name and extension of the source file or URL, which does not include 
    * the parent directories.
    */
   public final static String VAR_FILENAME_EXTENSION  = "$filename_extension";

   /**
    * The path of the source file or URL. It is relative to the search root.
    */
   public final static String VAR_FILEPATH      = "$filepath";


   /**
    * The size (in bytes) of the source file or URL.
    */
   public final static String VAR_FILESIZE      = "$filesize";

   /**
    * The mime-type of the source file or URL.
    */
   public final static String VAR_MIMETYPE      = "$mimetype";

   /**
    * The mime-type of the source file or URL.
    */
   public final static String VAR_CURRENT_DATETIME      = "$currentdatetime";

   /**
    * Array of all allowed variables. Never <code>null</code>.
    * Initialized in static init block
    */
   private final static String[] ms_allowedVars;

   static
   {
      ms_allowedVars = new String[8]; // <-- make sure the size matches below
      ms_allowedVars[0] = VAR_CONTENT;
      ms_allowedVars[1] = VAR_CURRENT_DATETIME;
      ms_allowedVars[2] = VAR_FILEEXTENSION;
      ms_allowedVars[3] = VAR_FILENAME;
      ms_allowedVars[4] = VAR_FILENAME_EXTENSION;
      ms_allowedVars[5] = VAR_FILEPATH;
      ms_allowedVars[6] = VAR_FILESIZE;
      ms_allowedVars[7] = VAR_MIMETYPE;
   }
}
