/*[ ISecurityProviderEditor.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSSecurityProviderInstance;

import java.util.Collection;


/**
 * This interface allows the administrator to treat all security provider
 * editors in the same fashion. It is used in concert w/ the
 * SecurityProviderEditorFactory which returns editors that implement this
 * interface for a particular provider type.
 */
public interface ISecurityProviderEditor
{
   /**
    * This method can be called after the editor returns to determine if the
    * end user cancelled the editing session or changed the supplied instance,
    * if editing.
    *
    * @return <code>true</code> if the caller had set an instance and the end
    * user modified it or a new instance was created, <code>false</code> otherwise.
    */
   public boolean isInstanceModified();


   /**
    * The editor can function in edit or create mode. This method should be
    * called before the editor is shown the first time to set it into edit mode.
    * When in edit mode, the properties of the supplied instance will be displayed
    * to the user for editing. It can also be used to reset the dialog to its
    * initial state (except for instance names).
    *
    * @param providerInst The provider to edit. If <code>null</code>, a new
    * instance is created, any existing instance is lost. If you want to keep
    * the existing instance, retrieve it before calling this method. This
    * allows the dialog to be used multiple times w/o creating a new one. If
    * called while the dialog is visible, the call is ignored.
    *
    * @return <code>true</code> if the provided instance is used, <code>false
    * </code> otherwise.
    *
    * @throws IllegalArgumentException if providerInst is not the type supported
    * by the editor.
    */
   public boolean setInstance( PSSecurityProviderInstance providerInst );


   /**
    * After editing has completed, the caller can use this method to obtain the
    * newly created or modified provider. If a provider was previously successfully
    * set with the <code>setInstance</code> method, that instance is returned.
    *
    * @return If in edit mode, the edited instance is returned, otherwise a
    * new instance is returned. If the user cancels, <code>null</code> is
    * returned.
    */
   public PSSecurityProviderInstance getInstance();


   /**
    * Sets a list of existing security provider names. The editor will not allow the
    * end user to create a new security provider with any name that matches a
    * name on the supplied list, case insensitive. If <code>null</code>, the
    * current list will be discarded. All entries in the list must be
    * non-<code>null</code>, String objects or an exception will be thrown.
    *
    * @param names A list of existing security providers, used to prevent
    * duplicate provider names. This object takes ownership of the list.
    *
    * @throws IllegalArgumentException If any entry in names is <code>null
    * </code> or is not a String object.
    */
   public void setInstanceNames( Collection names );


   /**
    * A clean up method. Should be called after the user is finished with the
    * editor. Releases all system resources. The editor can be shown again by
    * calling the <code>setVisible</code> method again.
    */
   public void dispose();


   /**
    * A direct interface to the underlying editors setVisible method.
    *
    * @param show If <code>true</code>, the editor is made visible, otherwise
    * it is hidden.
    */
   public void setVisible( boolean show );
}
