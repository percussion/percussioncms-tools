/*[ PSStatusEvent.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import java.util.EventObject;

/**
 * A status event, which will be fired upon a status change
 */
public class PSStatusEvent extends EventObject
{
   /**
    * Constructs a <code>PSStatusEvent</code> object from a given status.
    *
    * @param eventObj The object which fired the event, may not 
    *    <code>null</code>.
    * @param status The status of the event, must be one of the
    *    <code>STATUS_XXX</code> values.
    * @param processId The process id of the event, must be one of the
    *    <code>PROCESS_XXX</code> values.
    *
    * @throws IllegalArgumentException if any parameter is invalid
    */
   public PSStatusEvent(Object eventObj, int status, int processId)
   {
      this(eventObj, status, null, processId);
   }

   /**
    * Constructs a <code>PSStatusEvent</code> object from a given status and
    * a message.
    *
    * @param eventObj The object which fired the event, may not 
    *    <code>null</code>.
    * @param status The status of the event, must be one of the
    *    <code>STATUS_XXX</code> values.
    * @param message The message of the event, it may be <code>null</code> or
    *    empty.
    * @param processId The process of the event, must be one of the
    *    <code>PROCESS_XXX</code> values.
    *
    * @throws IllegalArgumentException if any parameter is invalid
    */
   public PSStatusEvent(Object eventObj, int status, String message, 
      int processId)
   {
      super(eventObj);

      if (eventObj == null)
         throw new IllegalArgumentException("eventObj may not be null");
     
      if (! isValidStatus(status))
         throw new IllegalArgumentException(
            "status must be one of the STATUS_XXX values");

      if (! isValidProcessId(processId))
         throw new IllegalArgumentException(
            "processId must be one of the PROCESS_XXX values");
            
      m_status = status;
      m_message = message;
      m_processId = processId;
   }

   /**
    * Get the status of the event.
    *
    * @return The status of the event, is one of the <code>STATUS_XXX</code> 
    *    values.
    */
   public int getStatus()
   {
      return m_status;
   }

   /**
    * Get the process id of the event.
    * 
    * @return The process id, it is one of the <code>PROCESS_XXX</code> values.
    */
   public int getProcessId()
   {
      return m_processId;
   }
   
   /**
    * Get the message of the event.
    *
    * @return The message of the event, it may be <code>null</code> or empty.
    */
   public String getMessage()
   {
      return m_message;
   }

   /**
    * Determines if a given status is one of the <code>STATUS_XXX</code>
    * values.
    *
    * @param status The to be checked value.
    *
    * @return <code>true</code> if <code>status</code> is valid;
    *    <code>false</code> otherwise.
    */
   private boolean isValidStatus(int status)
   {
      return (status == STATUS_STARTED ||
              status == STATUS_ABORTED ||
              status == STATUS_COMPLETED );
   }

   /**
    * Determines if a given process id is one of the 
    * <code>PROCESS_XXX</code> values.
    *
    * @param processId The to be checked value.
    *
    * @return <code>true</code> if <code>process</code> is valid;
    *    <code>false</code> otherwise.
    */
   private boolean isValidProcessId(int processId)
   {
      return (processId == PROCESS_FIXING_LINKS     ||
              processId == PROCESS_LOADING_CONTENTS ||
              processId == PROCESS_MANAGER          ||
              processId == PROCESS_MARKING_TREE     ||
              processId == PROCESS_SCANNING);
   }

   /**
    * String describing the event.
    */
   public String toDescription()
   {
      String strMsg = "";
      int nId = getProcessId();
      int nStatus = getStatus();
      String strStatusMsg = getMessage();

      switch (nId)
      {
         case PROCESS_FIXING_LINKS:
            strMsg = "Fixing links ";
            break;
         case PROCESS_LOADING_CONTENTS:
            strMsg = "Loading contents ";
            break;
         case PROCESS_MANAGER:
            strMsg = "Manager ";
            break;
         case PROCESS_MARKING_TREE:
            strMsg = "Marking tree ";
            break;
         case PROCESS_SCANNING:
            strMsg = "Scanning ";
            break;
         default:
            break;
      }

      switch (nStatus)
      {
         case STATUS_ABORTED:
            strMsg += " status: ABORTED ";
            break;
         case STATUS_COMPLETED:
            strMsg += " status: COMPLETED ";
            break;
         case STATUS_STARTED:
            strMsg += " status: STARTED ";
            break;
         default:
            break;
      }

      if (strStatusMsg != null)
         if (strStatusMsg.trim().length() > 0)
            strMsg += " " + strStatusMsg;

      return strMsg;
   }
   
   /**
    * Indicating a process has been started
    */
   public final static int STATUS_STARTED = 1;

   /**
    * Indicating a process has been aborted
    */
   public final static int STATUS_ABORTED = 2;

   /**
    * Indicating a process has been completed
    */
   public final static int STATUS_COMPLETED = 3;

   /**
    * Indicating the source is in the process of scanning
    */
   public final static int PROCESS_SCANNING = 100;
   
   /**
    * Indicating the source is in the process of marking tree
    */
   public final static int PROCESS_MARKING_TREE = 101;
   
   /**
    * Indicating the source is in the process of loading content
    */
   public final static int PROCESS_LOADING_CONTENTS = 102;
   
   /**
    * Indicating the source is in the process of fixing up links
    */
   public final static int PROCESS_FIXING_LINKS = 103;
   
   /**
    * Indicating the source of the event is selector or loader manager. 
    * For example, when the selector manager just started, the manager will 
    * send an event with the values <code>STATUS_STARTED</code> and 
    * <code>SRC_MANAGER</code>, see {@link #getStatus()} and 
    * {@link #getEventSource()} for detail info.
    */
   public final static int PROCESS_MANAGER = 104;
   
   /**
    * The status of the event, initialized by constructors, it is one
    * of the <code>STATUS_XXX</code> values after that.
    */
   private int m_status;

   /**
    * The process id of the event, initialized by constructors, it is one
    * of the <code>PROCESS_XXX</code> values after that.
    */
   private int m_processId;
   
   /**
    * The message of the event, initialized by constructors, it may be
    * <code>null</code> or empty
    */
   private String m_message;
}
