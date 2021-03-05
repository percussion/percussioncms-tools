/******************************************************************************
 *
 * [ PSWizardPageBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.IPSTableColumnSelectionListener;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.controls.PSTableEvent;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.help.IPSHelpProvider;
import com.percussion.workbench.ui.help.PSHelpManager;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.views.PSHelpView;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class PSWizardPageBase extends WizardPage
   implements IPSDesignerObjectUpdater, IPSHelpProvider
{

   
   public PSWizardPageBase(String pageName, String title, ImageDescriptor titleImage)
   {
      super(pageName, title, titleImage);
   }

   public PSWizardPageBase(String pageName)
   {
      super(pageName);      
   }
   
   /**
    * Register the control so that the wizard page can handle modifcation
    * type events on the controls. This Method sets the approppriate
    * listeners on the passed in control which upon modifcation will
    * call {@link #onControlModified(Control)}. Controls will be
    * automatically unregistered when they are disposed.
    * 
    * @param displayName the display name for the control. Used in any validation
    * error messages. This is typically the name that the label associated with
    * the control is displaying. Cannot be <code>null</code> or empty.
    * @param control the control to be registered, must be either a
    * <code>Control</code> or <code>Viewer</code>. Cannot be <code>null</code>.
    * @param validators an array of <code>IPSControlValueValidator</code>
    * objects. The validations will be done in the order that they exist in 
    * the array. This can be <code>null</code> if no validations are 
    * needed for this control.
    * @param type of control that this is. One of the PSControlInfo.TYPE_XXX
    *  constants.  
    */
   public void registerControl( String displayName,
      final Object control, final IPSControlValueValidator[] validators,
      final int type)
   {
      registerControl(displayName, control, validators, type, false);
   }
   
   /**
    * Register the control so that the wizard page can handle modifcation
    * type events on the controls. This Method sets the approppriate
    * listeners on the passed in control which upon modifcation will
    * call {@link #onControlModified(Control)}. Controls will be
    * automatically unregistered when they are disposed.
    * 
    * @param displayName the display name for the control. Used in any validation
    * error messages. This is typically the name that the label associated with
    * the control is displaying. Cannot be <code>null</code> or empty.
    * @param control the control to be registered, must be either a
    * <code>Control</code> or <code>Viewer</code>. Cannot be <code>null</code>.
    * @param validators an array of <code>IPSControlValueValidator</code>
    * objects. The validations will be done in the order that they exist in 
    * the array. This can be <code>null</code> if no validations are 
    * needed for this control.
    * @param type of control that this is. One of the PSControlInfo.TYPE_XXX
    *  constants.
    * @param helpOnly flag indicating that the control is registered only
    * so it will show help hints and will not call the update designer
    * object method 
    */
   public void registerControl( String displayName,
      final Object control, final IPSControlValueValidator[] validators,
      final int type, final boolean helpOnly)
   {
      if(displayName == null || displayName.trim().length() == 0)
         throw new IllegalArgumentException(
            "displayName cannot be null or empty.");
      if(control == null)
         throw new IllegalArgumentException("Control cannot be null.");
      if(!(control instanceof Control || control instanceof Viewer))
         throw new IllegalArgumentException(
            "The passed in control must be an instance of Control or Viewer");
      final Control widget;
      if(control instanceof Viewer)
         widget = ((Viewer)control).getControl();
      else
         widget = (Control)control;
       
      m_controlIndex.add(widget);
      m_controlInfo.put(widget, 
         new PSControlInfo(widget, StringUtils.replace(displayName, "&", ""),
            validators, type, null, helpOnly)); 
      
      // We must have a dispose listener that will unregister this control
      // if it is disposed.
      widget.addDisposeListener(new DisposeListener()
         {

            public void widgetDisposed(DisposeEvent e)
            {
               unregisterControl((Control)e.getSource());               
            }
         
         });
      
      // Add a focus listener for help hints
      widget.addFocusListener(new FocusAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.FocusAdapter#focusGained(
             * org.eclipse.swt.events.FocusEvent)
             */
            @Override
            public void focusGained(FocusEvent e)
            {
               // Call the method to display help hints in the
               // field help view
               PSControlInfo info = m_controlInfo.get(e.getSource());
               displayHelpHint(info);
            }
            
         });
      
      // Add Column selection listener for PSSortableTable widgets
      // so that the help hints can display for the selected columns
      if(widget instanceof PSSortableTable)
      {
         PSSortableTable sTable = (PSSortableTable)widget;
         sTable.addTableColumnSelectionListener(
            new IPSTableColumnSelectionListener()
            {

               /**
                * @param e
                */
               public void columnSelected(PSTableEvent e)
               {
                  // Call the method to display help hints in the
                  // field help view
                  PSControlInfo info = m_controlInfo.get(e.getSource());
                  displayHelpHint(info);
                  
               }
               
            });
      }
            
      // If help only then nothing else to add so return
      if(helpOnly)
         return;
      
      
      // Now we need to add the appropriate listeners to determine if
      // change really occurred on the control in question.
      // We will use reflection to determine if the add listener methods
      // exist for the passed in control. We also invoke using reflection.
      Class clazz = widget.getClass();
      Method addModifyListenerMethod = null;
      Method addSelectionListenerMethod = null;
      try
      {
         addModifyListenerMethod = clazz.getMethod("addModifyListener", 
            new Class[]{ModifyListener.class});
      }      
      catch (NoSuchMethodException ignore){}
      try
      {
         addSelectionListenerMethod = clazz.getMethod("addSelectionListener", 
            new Class[]{SelectionListener.class});
         if((widget instanceof Text))
            addSelectionListenerMethod = null;
      }      
      catch (NoSuchMethodException ignore){}
      
      if(addModifyListenerMethod != null)
      {
         ModifyListener mListener = new ModifyListener()
         {
            public void modifyText(ModifyEvent e)
            {
               Object source = e.getSource();
               onControlModified((Control)source);                
            }            
         };
         try
         {
            addModifyListenerMethod.invoke(widget, new Object[]{mListener});
         }
         catch (Exception ex)
         {
            PSWorkbenchPlugin.handleException(null, null, null, ex);
         }
         
      }
      
      if(addSelectionListenerMethod != null)
      {
         SelectionListener sListener = new SelectionAdapter()
         {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
               Object source = e.getSource();
               onControlModified((Control)source);                            
            }
            
         };
         try
         {
            addSelectionListenerMethod.invoke(widget, new Object[]{sListener});
         }
         catch (Exception ex)
         {
            PSWorkbenchPlugin.handleException(null, null, null, ex);
         }
      }
      
      
   }
   
   /**
    * Registers a control to only handle displaying of the help
    * hints and not to send modify notifications to the framework.
    * Controls will be automatically unregistered when they are disposed.
    * @param displayName the display name for the control. Used in any validation
    * error messages. This is typically the name that the label associated with
    * the control is displaying. Cannot be <code>null</code> or empty.
    * @param control the control to be registered, must be either a
    * <code>Control</code> or <code>Viewer</code>. Cannot be <code>null</code>. 
    */
   public void registerControlHelpOnly(String displayName, 
      final Object control)
   {
      registerControl(displayName, control, null, PSControlInfo.TYPE_NORMAL, true);
   }
   
   /**
    * Unregisters the specified control
    * @param control cannot be <code>null</code>.
    */
   public void unregisterControl(Control control)
   {
      if(control == null)
         throw new IllegalArgumentException("control cannot be null.");
      m_controlIndex.remove(control);
      m_controlInfo.remove(control);
   }
   
   /**
    * Convenience method calls 
    * registerControl(String, Object, IPSControlValueValidator[],
    *  false)
    */
   public void registerControl( String displayName,
      final Object control, final IPSControlValueValidator[] validators)
   {
      registerControl(displayName, control, validators, PSControlInfo.TYPE_NORMAL);
   }
   
   /**
    *  Does the following upon a control modification event.
    * <p>
    * <pre>
    *   <li> Validators are run on the control
    *   <li> If validation error then, pageComplete is set to <code>false</code>
    *        ,an error message is displayed and focus is returned to the control.
    *   <li> If the control is valid, then validation is run on all other control
    *        to determine if the page is complete. The first validation error found
    *        will be displayed.   
    * </pre>
    * </p>
    * @param control the Control that was modified, cannot be <code>null</code>
    */
   protected void onControlModified(Control control)
   {
      if(control == null)
         throw new IllegalArgumentException("control cannot be null.");
      PSControlInfo info = m_controlInfo.get(control);
      String error = null;
      // Run validators if any
      IPSControlValueValidator[] validators = info.getValidators();
      if(info.getControl().isEnabled() && validators != null)
      {
         for(IPSControlValueValidator validator : validators)
         {
            error = validator.validate(info);
            if(error != null)
               break;
         }
      }
      if(error == null)
      {
         // Run validation on all other controls and display
         // the first validation error found
         runPageValidation(true);
      }
      else
      {         
         setMessage(error, IMessageProvider.ERROR);
         setPageComplete(false);
         (control).setFocus();
      }            
   }
   
   
   /**
    * Runs validation on all registered controls in this
    * wizard. Controls must also be enabled.
    * @param showError if <code>true</code> then error
    * messages will be displayed.
    */
   public void runPageValidation(boolean showError)
   {       
      if(!m_allowValidation)
         return;
      String error = null;
      for(Control key : m_controlIndex)
      {
         PSControlInfo cInfo = m_controlInfo.get(key);         
         if(!cInfo.getControl().isEnabled())
            continue;
         IPSControlValueValidator[] validators = 
            cInfo.getValidators();
         if(validators != null)
         {
            for(IPSControlValueValidator validator : validators)
            {
               error = validator.validate(cInfo);
               if(error != null)
                  break;
            }
            if(error != null)
               break;
         }
      }
      if(error == null)
      {
         setMessage(null);
         setPageComplete(true);
      }
      else
      {
         if(showError)
            setMessage(error, IMessageProvider.ERROR);
         setPageComplete(false);
      }
   }
   
   /**
    * Initializes the help manager for this wizard page
    */
   protected void initHelpManager()
   {
      m_helpManager = new PSHelpManager(this, getControl());
   }
   
   /**
    * 
    * @return an iterator of all registered controls for
    * this page.
    */
   public Iterator<PSControlInfo> getRegisteredControls()
   {
      return m_controlInfo.values().iterator();
   }
   
   /**
    * Set the flag that will determine if validation will
    * be performed.
    * @param enable
    */
   public void setValidationEnabled(boolean enable)
   {
      m_allowValidation = enable;
   }
   
   /**
    * Convenience method to get message by specified key.
    */
   protected static String getMessage(final String key)
   {
      return PSMessages.getString(key);
   }
   
   /**
    * Base class implementation just returns name of the class. 
    * @see com.percussion.workbench.ui.help.IPSHelpProvider#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   public String getHelpKey(@SuppressWarnings("unused") Control control)
   {      
      return getClass().getName();
   }
   
   /**
    * Gets the help hint key and retrives the help message from
    * the help resource, then displays the message in the field help
    * view if it is open.
    * @param info the control info object, cannot be <code>null</code>.
    */
   protected final void displayHelpHint(PSControlInfo info)
   {
      IViewPart view = PSUiUtils.findView(PSHelpView.ID);
      if(view == null)
         return;
      String key = getHelpHintKey(info);
      if(StringUtils.isNotBlank(key))
      {
         ((PSHelpView)view).displayHelp(key);      
      }
   }
   
   /**
    * This method can be overriden by the subclass to allow mapping of controls
    * to help hint keys.  This method is called
    * by {@link #displayHelpHint(PSControlInfo)} when a registered control
    * gains focus. The default implementation returns the results
    * from {@link #getHelpKey(Control)}.
    * @param controlInfo the control info object, never <code>null</code>.
    * @return the help hint key, may be <code>null</code> or empty.
    */
   @SuppressWarnings("unused") //$NON-NLS-1$
   protected String getHelpHintKey(PSControlInfo controlInfo)
   {
      return getHelpKey(controlInfo.getControl());
   }   
   
   /**
    * This is a no-op method as it is only needed for editors
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   public final void loadControlValues(@SuppressWarnings("unused") Object designObject)
   {
      // no-op      
   }
   
   /**
    * Add a validator to the specified registered control
    * @param control the registered control, cannot be
    * <code>null</code>.
    * @param validator the validator to be added, cannot be
    * <code>null</code>.
    */
   public void addControlValidator(Control control,
      IPSControlValueValidator validator)
   {
      if(control == null)
         throw new IllegalArgumentException("control cannot be null.");
      if(validator == null)
         throw new IllegalArgumentException("validator cannot be null.");
      // Find registered control
      PSControlInfo controlInfo = m_controlInfo.get(control);
      if(controlInfo == null)
         throw new IllegalStateException("Control is not registered.");
      controlInfo.addValidator(validator);
   }
   
   /**
    * Remove a validator from the specified registered control
    * @param control the registered control, cannot be
    * <code>null</code>.
    * @param validator the validator to be removed, cannot be
    * <code>null</code>.
    */
   public void removeControlValidator(Control control,
      IPSControlValueValidator validator)
   {
      if(control == null)
         throw new IllegalArgumentException("control cannot be null.");
      if(validator == null)
         throw new IllegalArgumentException("validator cannot be null.");
      // Find registered control
      PSControlInfo controlInfo = m_controlInfo.get(control);
      if(controlInfo == null)
         throw new IllegalStateException("Control is not registered.");
      controlInfo.removeValidator(validator);
   }
   
   /**
    * Map of control info objects, initialized by
    * {@link #registerControl(String, Object, IPSControlValueValidator[])}.
    * Never <code>null</code>, may be empty.
    */
   protected Map<Control, PSControlInfo> m_controlInfo = 
      new HashMap<Control, PSControlInfo>();
   
   /**
    * Index used by the register control method to keep the controls
    * in the order they were registered.
    */
   protected List<Control> m_controlIndex = new ArrayList<Control>();
   
   /**
    * Flag indicating that validation is allowed, defaults to <code>true</code>
    */
   protected boolean m_allowValidation = true;
   
   /**
    * The help manager for wizard pages
    */
   protected PSHelpManager m_helpManager;

   

}
