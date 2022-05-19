/******************************************************************************
 *
 * [ PSActionResponse.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions;

/**
 * A little container class to bundle up the information needed
 * for an action response.
 */
public class PSActionResponse
{

   public PSActionResponse(String responseData, int responseType)
   {
      m_responseData = responseData;
      m_responseType = responseType;
   }
   
   /**
    * Returns the response mime type string for the specified 
    * response type integer. If the response type integer
    * passed in is invalid then the response type of string
    * will be returned.
    * @return never <code>null</code> or empty.
    */
   public String getResponseTypeString()
   {
      if(m_responseType >= ms_responseStrings.length || m_responseType < 0)
         m_responseType = RESPONSE_TYPE_PLAIN;
      return ms_responseStrings[m_responseType];
   }
   
   /**
    * see {@link #m_responseData} for more detail.
    * @return Returns the responseData.
    */
   public String getResponseData()
   {
      return m_responseData;
   }
     
   /**
    * The response data string. May be <code>null</code> or
    * empty. Initialized in the ctor
    */
   private String m_responseData;
   
   /**
    * The response type that will be returned to the
    * caller. USe the RESPONSE_TYPE_XXX constants.
    * Defaults to the RESPONSE_TYPE_PLAIN type.
    */
   private int m_responseType = RESPONSE_TYPE_PLAIN;
      
   /**
    * Response type constant that represents the &quot;text/html&quot;
    * mime type.
    */
   public static final int RESPONSE_TYPE_HTML = 0;
   
   /**
    * Response type constant that represents the &quot;text/plain&quot;
    * mime type.
    */
   public static final int RESPONSE_TYPE_PLAIN = 1;
   
   /**
    * Response type constant that represents the &quot;text/xml&quot;
    * mime type.
    */
   public static final int RESPONSE_TYPE_XML = 2;
   
   /**
    * Response type constant that represents the &quot;text/json&quot;
    * mime type.
    */
   public static final int RESPONSE_TYPE_JSON = 3;
   
   /**
    * Response string array.
    */
   private static String[] ms_responseStrings = new String[]
                                                           {
      "text/html",
      "text/plain",
      "text/xml",
      "text/json"
                                                           };
  
   

}
