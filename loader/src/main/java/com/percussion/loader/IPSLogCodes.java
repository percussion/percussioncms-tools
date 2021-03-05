/*[ IPSLogCodes.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader;

/**
 * The IPSLogCodes inteface is provided as a convenient mechanism
 * for accessing the various loader related error codes.
 */
public interface IPSLogCodes
{

   /**
    * Added a node to the PSContentTreeModel
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TH>0</TH><TH>A node description</TH></TR>
    * </TABLE>
    */
   public static final int LOG_ADDNODE = 1;

   /**
    * Update a node in the PSContentTreeModel
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>A node description</TD></TR>
    * <TR><TD>1</TD><TD>The node's current status</TD></TR>
    * </TABLE>
    */
   public static final int LOG_UPDATENODE = 2;

   /**
    * Information message for IPSCrawlListener
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>A node description</TD></TR>
    * <TR><TD>1</TD><TD>The node's current status</TD></TR>
    * <TR><TD>2</TD><TD>Exception Message if an error occurred</TD></TR>
    * </TABLE>
    */
   public static final int LOG_SELECTORINFO = 3;

   /**
    * Error message from selector
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>A node description</TD></TR>
    * <TR><TD>1</TD><TD>Exception Message if an error occurred</TD></TR>
    * </TABLE>
    */
   public static final int LOG_SELECTORERROR = 4;

   /**
    * Error on a not well-formed XML node, detected by tidy
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>A node description</TD></TR>
    * </TABLE>
    */
   public static final int ERROR_WELL_FORMED_XML = 5;

   /**
    * Error on broken link while fixing up inline links
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>A node description</TD></TR>
    * <TR><TD>0</TD><TD>The broken link</TD></TR>
    * </TABLE>
    */
   public static final int BROKEN_LINK = 6;

   /**
    * Updating a node in the PSContentTreeModel
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>A node resource id</TD></TR>
    * </TABLE>
    */
   public static final int UPLOAD_NODE = 20;

   /**
    * Performing actions for a <code>IPSContentTreeNode</code>
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>A node resource id</TD></TR>
    * </TABLE>
    */
   public static final int PERFORM_ACTIONS_4_NODE = 21;

   /**
    * Error occurred while processing a <code>IPSContentTreeNode</code>
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>The resource id of the node</TH></TR>
    * <TR><TD>0</TD><TD>Exception that cause the error</TD></TR>
    * </TABLE>
    */
   public static final int ERROR_PROCESS_NODE = 22;

   /**
    * The static extractor does not recognize the selector item.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Extractor name</TD></TR>
    * </TABLE>
    */
   public static final int ERROR_ASSIGN_EXTRACTOR= 30;

   /**
    * The extractor does not recognize the selector item.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Extractor Name</TD></TR>
    * </TABLE>
    */
   public static final int WARNING_ASSIGN_EXTRACTOR = 31;

   /**
    * Invalid community id in a extractor definition
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid community id</TD></TR>
    * <TR><TD>1</TD><TD>The name of the extractor</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_COMMUNITY_ID = 50;

   /**
    * Invalid content type name in a community
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid content type name</TD></TR>
    * <TR><TD>1</TD><TD>The name of the community</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CONTENTTYPE_NAME = 51;

   /**
    * Invalid field name in a content type
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the invalid field</TD></TR>
    * <TR><TD>1</TD><TD>The name of the content type</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_FIELD_NAME = 52;
   
   /**
    * Invalid workflow id in a community
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid workflow id</TD></TR>
    * <TR><TD>1</TD><TD>The name of the community</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_WORKFLOW_ID = 53;
   
   /**
    * Invalid workflow name in a community
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid workflow name</TD></TR>
    * <TR><TD>1</TD><TD>The name of the community</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_WORKFLOW_NAME = 54;
   
   /**
    * Invalid transition trigger name in a workflow
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid transition trigger name</TD></TR>
    * <TR><TD>1</TD><TD>The name of the workflow</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_TRANSITION_TRIGGER = 55;
   
   /**
    * Failed to loader extractor definition
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The extractor definition in XML</TD></TR>
    * </TABLE>
    */
   public static final int FAILED_LOAD_EXTRACTOR_DEF = 56;

   /**
    * Failed to reset a definition
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the definition</TD></TR>
    * </TABLE>
    */
   public static final int FAILED_RESET_DEFINITION = 57;
   
   /**
    * Failed to loader field transformer definition
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the transformer definition</TD></TR>
    * </TABLE>
    */
   public static final int FAILED_LOAD_FIELD_TRANSFORMER_DEF = 58;

   /**
    * Failed to loader item transformer definition
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the transformer definition</TD></TR>
    * </TABLE>
    */
   public static final int FAILED_LOAD_ITEM_TRANSFORMER_DEF = 59;
   
   /**
    * Failed to fields for a content type
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the content type</TD></TR>
    * </TABLE>
    */
   public static final int FAILED_GET_CONTENTTYPE_FIELDS = 60;
   
}