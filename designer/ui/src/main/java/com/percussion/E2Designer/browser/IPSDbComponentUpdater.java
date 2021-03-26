/*[ IPSDbComponentUpdater.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.IPSDbComponent;

/**
 * This interface is used to control persistence of CMS objects between the
 * UI controls and the component for which they provide editing access to the
 * user.
 * <p>The framework calls these methods as follows:
 * <ol>
 *    <li>When the framework is ready to begin editing of a component, the
 *       onUpdateData method will be called with the transfer flag set to
 *       <code>false</code>.</li>
 *    <li>When the framework is ready to end editing (either temporarily
 *       within this session, or between sessions), first, the onValidateData
 *       method will be called. If this returns <code>true</code>, then the
 *       onUpdateData method will be called with the transfer flag set to
 *       <code>true</code>.</li>
 *    <li>After the framework has persisted any changes to the database, the
 *       onDataPersisted method will be called.</li>
 * </ol>
 */
public interface IPSDbComponentUpdater
{
   /**
    * Called by the framework to transfer data between a CMS object and the
    * UI components used to edit that object. The meaning of the flag is viewed
    * from the object's viewpoint.
    * <p>Before initiating the transfer from the UI to the object (saving),
    * the UI must call the onValidateData method. Only if this returns
    * <code>true</code> can the transfer proceed, otherwise, the method
    * must return without modifying the component.
    *
    * @param comp object to read from or update. Never <code>null</code>.
    *
    * @param isSaving, If <code>true</code> the flow of data will be
    *    from the 'view' to the object. If <code>false</code>, the flow of
    *    data will be from the object to the 'view'.
    *
    * @param isQuiet flag indicating that error messages should be suppressed
    * if flag set to <code>true</code> if validation occurs.
    *
    * @return if <code>false</code>updating will stop if <code>true</code>
    *    updating will continue and assumed to be valid.
    */
   public boolean onUpdateData(
      IPSDbComponent comp, boolean isSaving, boolean isQuiet);

   /**
    * Performs all necessary validation for this object. If any violations
    * occur, a dialog explaining the problem must be shown to the user, and
    * the offending component must be made visible and it must request the
    * focus.
    *
    * @param comp object being validated. Never <code>null</code>.
    *
    * @param isQuiet flag indicating that error messages should be suppressed
    * if flag set to <code>true</code>.
    *
    * @return if <code>false</code> data from this panel is not valid
    *    and therefore saving this object is not allowable. if <code>true
    *    </code> the data from this panel is in a valid state and should
    *    be saveable.
    */
   public boolean onValidateData(IPSDbComponent comp, boolean isQuiet);

   /**
    * After the object associated with the panel has been written to permanent
    * storage, this method will be called by the framework. Implementing
    * classes should perform any enabling/disabling of controls that may need
    * to happen after such an event.
    */
   public void onDataPersisted();
}
