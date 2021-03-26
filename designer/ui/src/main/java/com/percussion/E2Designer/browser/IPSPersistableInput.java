/*[ IPSPersistableInput.java ]**************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;


/**
 * This interface was designed to provide a common interface to a set of UI
 * components such as a set of tabbed panels. It provides the basic methods
 * needed for interacting with them, i.e., load the panel, allow the user to
 * interact with it, validate the data they entered and if successful, save
 * it.
 */
public interface IPSPersistableInput
{
   /**
    * The implementing class will verify that all data in the ui component(s)
    * has been entered correctly.
    *
    * @return <code>true</code> if all data was successfully validated,
    *    <code>false</code> otherwise. If validation fails, a message will be
    *    displayed to the end user and focus will be left in the offending
    *    component, with the text selected if a text field.
    */
   public boolean validateData();

   /**
    * Transfers the attributes in the UI components to the data object that
    * was supplied in the {@link #load(Object)} method.
    *
    * @return Before the transfer begins, {@link #validateData()} is called.
    *    If validation fails, <code>false</code> is returned immediately,
    *    otherwise, <code>true</code> is returned.
    */
   public boolean save();

   /**
    * Transfers the attributes from the supplied object into the UI components
    * so they can be viewed/edited by the end user.
    *
    * @param data Must be a data type recognizable by the implementor and
    *    never <code>null</code>, otherwise an IllegalArgumentException will
    *    be thrown.
    */
   public void load(Object data);
}
