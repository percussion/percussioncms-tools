/*[ IGroupProviderEditor.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.IPSGroupProviderInstance;

import java.util.Collection;


/**
 * Interface used by editors that create or modify IPSGroupProviderInstance
 * objects.  Provides a common interface for setting required information and
 * retrieving the created or modified instance.
 */

public interface IGroupProviderEditor
{
   /**
    * This method can be called after the editor returns to determine if the
    * end user cancelled the editing session or, if editing, changed the
    * supplied group provider instance.
    *
    * @return <code>true</code> if the caller had set an instance and the end
    * user modified it or a new instance was created (i.e. the user pressed
    * the ok button), <code>false</code> otherwise.
    */
   public boolean isInstanceModified();

   /**
    * After editing has completed, the caller can use this method to obtain the
    * newly created or modified provider. If a provider was previously
    * successfully set with the <code>setinstance</code> method, that instance
    * is returned.
    *
    * @return If in edit mode, the edited instance is returned, otherwise a
    * new instance is returned. If the user cancels, <code>null</code> is
    * returned.
    */
   public IPSGroupProviderInstance getInstance();

   /**
    * Sets initial data required by this dialog to create and edit group
    * provider instances. This should be called before making the dialog
    * visible.
    *
    * @param type One of the <code>PSSecurityProvider.SP_TYPE_xxx</code>
    * types, used to set the type of new group provider instance being created.
    *
    * @param providerClassName the jndi group provider class name used to get
    * the default group properties to create new group provider, may not be
    * <code>null</code> or empty.
    *
    * @param groupProviderNames A list of existing group provider names. The
    * editor will not allow the end user to create a new group provider with
    * any name that matches a name on the supplied list, case insensitive. If
    * <code>null</code>, the current list will be discarded. all entries in the
    * list must be non-<code>null</code>, string objects.
    *
    * @param groupProviderInst The group provider to edit. use <code>null</code>
    * to create a new instance. Any existing instance is lost.  If you want to
    * keep the existing instance, retrieve it before calling this method. This
    * allows the dialog to be used multiple times w/o creating a new one. If
    * called while the dialog is visible, the call is ignored. The type of this
    * provider must match the type used when the dialog was created.  Use
    * {@link #isInstanceModified()} to determine if the current instance has
    * been changed or created by this dialog.  If supplied, must be of the
    * correct type for this dialog.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void setProviderData(int type, String providerClassName,
      Collection groupProviderNames,
      IPSGroupProviderInstance groupProviderInst);

}
