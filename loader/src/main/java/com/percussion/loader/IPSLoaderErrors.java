/*[ IPSLoaderErrors.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

/**
 * The IPSLoaderErrors inteface is provided as a convenient mechanism
 * for accessing the various loader related error codes.
 */
public interface IPSLoaderErrors
{

   /**
    * Unexpected error
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int UNEXPECTED_ERROR = 1;


   /**
    * Initialize plugin error
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The class name of the plugin</TD></TR>
    * <TR><TD>1</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int INIT_PLUGIN_ERROR = 2;

   /**
    * Error occurred during loading configuration information
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int LOAD_CONFIG_ERROR = 3;

   /**
    * Config file does not exist
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The config file path</TD></TR>
    * </TABLE>
    */
   public static final int CONFIG_FILE_NOT_EXIST = 4;

   /**
    * Invalid descriptor path
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The descriptor file path</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_DESCRIPTOR_PATH = 5;

   /**
    * Descriptor file not exists
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The descriptor file path</TD></TR>
    * </TABLE>
    */
   public static final int DESCRIPTOR_FILE_NOT_EXIST = 6;

   /**
    * Unknown command line argument
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The command line argument</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_ARG = 7;

   /**
    * Missing required property
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The missing property name</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_PROPERTY = 8;

   /**
    * Missing required mime type
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The XML node name</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_MIME_TYPE = 9;


   /**
    * Modified extractor definition, which is not supported
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of current extractor definition</TD></TR>
    * <TR><TD>1</TD><TD>the name of previous extractor definition</TD></TR>
    * <TR><TD>2</TD><TD>The resource id of the current item</TD></TR>
    * </TABLE>
    */
   public static final int MODIFIED_EXTRACTOR_DEF = 10;

   /**
    * Extracted more than one items is not supported
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the class name of extractor</TD></TR>
    * </TABLE>
    */
   public static final int EXTRACTED_TOO_MANY_ITEMS = 11;

   /**
    * Got wrong soap response while processing webservices
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>expected response element</TD></TR>
    * <TR><TD>1</TD><TD>the unexpected soap response (error)</TD></TR>
    * </TABLE>
    */
   public static final int WS_WRONG_SOAP_RESP = 12;

   /**
    * Caught unexpected error while processing webservice
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>unexpected error</TD></TR>
    * </TABLE>
    */
   public static final int WS_UNEXPECTED_ERROR = 13;

   /**
    * non-unique transformer name
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the transformer</TD></TR>
    * </TABLE>
    */
   public static final int NON_UNIQUE_TRANSFORMER_NAME = 14;

   /**
    * "url" attribute for <Root> element in ListContentSelector has to be
    *  specified.
    */
   public static final int ROOT_ATTRIB_MISSING = 15;

   /**
    * error when processing text through tidy
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the error message from tidy</TD></TR>
    * </TABLE>
    */
   public static final int TIDY_ERROR = 16;

   /**
    * Fail to load the status tree that is previous scan and/or uploaded
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the error message from exception</TD></TR>
    * </TABLE>
    */
   public static final int FAIL_TO_LOAD_STATUS = 17;


   /**
    * Invalid value type encountered
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the invalid value type</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_VALUE_TYPE = 18;
   
   /**
    * Cannot find a field which is specified in an item extractor
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>The (wrong) field name</TH></TR>
    * <TR><TD>0</TD><TD>The name of the extractor</TD></TR>
    * </TABLE>
    */
   public static final int CANNOT_FIND_FIELD = 20;
   
   /**
    * Failed to perform workflow transition
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>The resource id of the item</TH></TR>
    * <TR><TD>0</TD><TD>The name of the workflow transition</TD></TR>
    * </TABLE>
    */
   public static final int FAILED_WF_TRANSITION = 21;
   
}