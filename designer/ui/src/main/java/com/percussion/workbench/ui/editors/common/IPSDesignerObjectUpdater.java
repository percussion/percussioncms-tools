/******************************************************************************
 *
 * [ IPSDesignerObjectUpdater.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
/**
 * 
 */
package com.percussion.workbench.ui.editors.common;

/**
 * Iterface used by editors, wizard pages or their composites to update
 * the underlying designer object and to load the editor control values from a
 * designer object.
 */

public interface IPSDesignerObjectUpdater
{
   /**
    * Responsible for updating the designer object that is cached by
    * the <code>IPSCmsModel</code> that this editor/wizard page is operating
    * on. May also be implemented by a composite that will be part of an editor
    * or wizard page. 
    * <p>
    * The method should be implemented to do the following:
    * </p> 
    * <pre>
    *    <ol>
    *       <li>An if/else block determines which control is passed in
    *       <li>Extract the controls value using its appropriate method
    *       <li>Set the value that the control represents in the designer
    *        object
    *    </ol>
    * </pre>
    * 
    * @param designObject the designer object that is cached by 
    * <code>IPSCmsModel</code>, cannot be <code>null</code>.
    * @param control the control that needs its new value persisted to the
    * designer object. Cannot be <code>null</code>.
    */
   public void updateDesignerObject(Object designObject, Object control);
   
   /**
    * This method is called when the controls need to be loaded with values
    * from the designer object which backs this editor.
    * This method MUST be implemented by all subclasses of
    * <code>PSEditorBase</code>. The <code>PSWizardPageBase</code> implements
    * this as a no-op method as it is not needed for the wizards.
    * @param designObject the designer object that is cached by 
    * <code>IPSCmsModel</code>, cannot be <code>null</code>.
    */
   public void loadControlValues(Object designObject);
}
