/******************************************************************************
 *
 * [ PSProgressStatus.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui.data;

/**
 * @author erikserating
 *
 */
public class PSProgressStatus
{
   private PSProgressStatus(TYPE type)
   {
      m_type = type;
   }
   
   /**
    * Create a new start progress status.
    * @param message
    * @param note
    * @return never <code>null</code>.
    */
   public static PSProgressStatus createStartStatus(String message, String note)
   {
      PSProgressStatus status = new PSProgressStatus(TYPE.START);
      status.m_message = message;
      status.m_note = note;
      return status;
   }
   
   /**
    * 
    * @param message
    * @param note
    * @return never <code>null</code>.
    */
   public static PSProgressStatus createUpdateStatus(String note)
   {
      PSProgressStatus status = new PSProgressStatus(TYPE.UPDATE);      
      status.m_note = note;      
      return status;
   }   
   
   /**
    * Create a new end progress status.
    * @return never <code>null</code>.
    */
   public static PSProgressStatus createEndStatus()
   {
      PSProgressStatus status = new PSProgressStatus(TYPE.END);
      return status;
   }
   
   /**
    * @return the type
    */
   public TYPE getType()
   {
      return m_type;
   }   
   /**
    * @return the message
    */
   public String getMessage()
   {
      return m_message;
   }
   /**
    * @return the note
    */
   public String getNote()
   {
      return m_note;
   } 
   
   /**
    * @return the enableCancel
    */
   public boolean isEnableCancel()
   {
      return m_enableCancel;
   }

   /**
    * Set flag indicating that cancel is allowed. Defaults
    * to <code>true</code>.
    * @param enableCancel the enableCancel to set
    */
   public void setEnableCancel(boolean enableCancel)
   {
      m_enableCancel = enableCancel;
   }
   
   public enum TYPE
   {
      START,
      UPDATE,
      END
   }
   
   private TYPE m_type;   
   private String m_message;
   private String m_note;
   private boolean m_enableCancel = true;
   
   
}
