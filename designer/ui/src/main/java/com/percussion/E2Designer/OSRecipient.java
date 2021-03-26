/*[ OSRecipient.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.EditableListBox.EditableListBoxCellNameHelper;
import com.percussion.design.objectstore.PSRecipient;
import com.percussion.error.PSIllegalArgumentException;

/** Used only as an element object wrapper for the PSRecipient EditableListBox
  * in NotifierPropertyDialog.
  *
  * @see NotifierPropertyDialog
  * @see PSRecipient
*/

public class OSRecipient extends PSRecipient 
                         implements EditableListBoxCellNameHelper
{
   /**
    * @throws PSIllegalArgumentException should never be thrown by this 
    * constructor
    */
   public OSRecipient()
         throws PSIllegalArgumentException
   {
      super( E2Designer.getResources().getString( "Dummy" ) );
   }


   /*
    * @param notifier a valid OSRecipient. If null, a NullPointerException is
    * thrown.
    *
    * @throws NullPointerException if notifier is null
    *
    * @throws PSIllegalArgumentException this should never be thrown 
    */
   public OSRecipient( PSRecipient recipient )
         throws PSIllegalArgumentException
   {
      super( recipient.getName());
      copyFrom( recipient );
    setSendEnabled(recipient.isSendEnabled());
    setErrorThresholdByCount(recipient.isErrorThresholdByCount());
    setErrorThresholdByInterval(recipient.isErrorThresholdByInterval());
    setErrorThresholdCount(recipient.getErrorThresholdCount());
    setErrorThresholdInterval(recipient.getErrorThresholdInterval());
    setAppAuthorizationFailureCount(recipient.getAppAuthorizationFailureCount());
    setAppAuthorizationFailureEnabled(recipient.isAppAuthorizationFailureEnabled());
    setAppDesignErrorEnabled(recipient.isAppDesignErrorEnabled());
    setAppValidationErrorEnabled(recipient.isAppValidationErrorEnabled());
    setAppXmlErrorEnabled(recipient.isAppXmlErrorEnabled());
    setAppHtmlErrorEnabled(recipient.isAppHtmlErrorEnabled());
    setAppRequestQueueLargeEnabled(recipient.isAppRequestQueueLargeEnabled());
    setAppRequestQueueMax(recipient.getAppRequestQueueMax());
    setAppResponseTimeEnabled(recipient.isAppResponseTimeEnabled());
    setAppResponseTimeMax(recipient.getAppResponseTimeMax());
    setBackEndAuthorizationFailureCount(recipient.getBackEndAuthorizationFailureCount());
    setBackEndAuthorizationFailureEnabled(recipient.isBackEndAuthorizationFailureEnabled());
    setBackEndDataConversionErrorEnabled(recipient.isBackEndDataConversionErrorEnabled());
    setBackEndQueryFailureEnabled(recipient.isBackEndQueryFailureEnabled());
    setBackEndRequestQueueLargeEnabled(recipient.isBackEndRequestQueueLargeEnabled());
    setBackEndRequestQueueMax(recipient.getBackEndRequestQueueMax());
    setBackEndServerDownFailureEnabled(recipient.isBackEndServerDownFailureEnabled());
    setBackEndUpdateFailureEnabled(recipient.isBackEndUpdateFailureEnabled());
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first. 
    *
    * @param recipient a valid OSRecipient. If null, an 
   * PSIllegalArgumentException is thrown.
    *
    * @throws PSIllegalArgumentException if notifer is null
    */
   public void copyFrom( OSRecipient recipient )
         throws PSIllegalArgumentException
   {
      copyFrom((PSRecipient) recipient );
   }

  /**
   * EditableListBoxCellNameHelper implementation.
   */
  public String getName()
  {
    return super.getName();
  }

  /**
   * EditableListBoxCellNameHelper implementation.
   */
  public void setName(String name)
  {
    try
    {
      super.setName(name);
    }
    catch (Exception e)
    {
      // empty-string named OSRecipients will never be set into OSNotifier.
      // so nothing is done here...
      
      //e.printStackTrace();
    }
  }

  public String toString()
  {
    return super.getName();
  }
} 
