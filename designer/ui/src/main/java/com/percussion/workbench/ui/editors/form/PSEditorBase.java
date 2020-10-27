/******************************************************************************
 *
 * [ PSEditorBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSLockHelper;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelChangedEvent.ModelEvents;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSModelListener;
import com.percussion.client.models.PSLockException;
import com.percussion.services.security.PSPermissions;
import com.percussion.workbench.ui.PSEditorInput;
import com.percussion.workbench.ui.PSEditorRegistry;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.IPSTableColumnSelectionListener;
import com.percussion.workbench.ui.controls.IPSTableModifiedListener;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.controls.PSTableEvent;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.help.IPSHelpProvider;
import com.percussion.workbench.ui.help.PSHelpManager;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSErrorDialog;
import com.percussion.workbench.ui.util.PSProblemSet;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import com.percussion.workbench.ui.validators.PSControlValueDuplicateNameValidator;
import com.percussion.workbench.ui.validators.PSControlValueTextIdValidator;
import com.percussion.workbench.ui.views.PSHelpView;
import com.percussion.workbench.ui.views.PSProblemsView;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.replace;

/**
 * The common base class for all IEditorPart type editors that are used to
 * modify design objects. It's main purpose is to manage name changes of the
 * object.
 *
 * @version 6.0
 * @author Paul Howard
 */
public abstract class PSEditorBase extends EditorPart implements IReusableEditor,
   IPSDesignerObjectUpdater, IPSHelpProvider
{
   /**
    * Overridden to manage changes to title if object is renamed, as
    * well as validating the passed in <code>IPSReference</code>.
    *
    * @param site
    * @param input
    */
   @Override
   public void init(IEditorSite site, IEditorInput input) throws PartInitException
   {

      Assert.isTrue(input instanceof PSEditorInput);
      m_editorInput = (PSEditorInput)input;
      // Get the reference and set it to a global variable
      m_reference = m_editorInput.getReference();
      Assert.isTrue(isValidReference(m_reference));
      // Are we dirty?
      if(!m_reference.isPersisted())
      {
         m_dirty = true;
         firePropertyChange(PROP_DIRTY);
      }
      setSite(site);
      setInput(input);
      setPartName(m_reference.getName());

      try
      {
         // load the design object
         load(m_reference);

         // Add listener to handle external modification events
         PSCoreFactory factory = PSCoreFactory.getInstance();
         IPSCmsModel model = factory.getModel(m_reference);
         m_modelListener = new IPSModelListener()
         {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public void modelChanged(PSModelChangedEvent event)
            {
               handleModelChanged(event);
            }
         };

         model.addListener(m_modelListener,
            ModelEvents.RENAMED.getFlag() | ModelEvents.MODIFIED.getFlag());

      }
      catch (PartInitException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         final String msg;
         if (isNotBlank(e.getLocalizedMessage()))
         {
            msg = e.getLocalizedMessage();
         }
         else if (isNotBlank(e.getMessage()))
         {
            msg = e.getMessage();
         }
         else
         {
            msg = "Editor initialization error";      //$NON-NLS-1$
         }
         throw new PartInitException(msg, e);
      }
   }

   /**
    * This method checks to see if the reference passed in
    * the <code>PSEditorInput</code> is valid for this editor.
    * This method MUST be implemented by
    * all subclasses of <code>PSEditorBase</code>. Called
    * by {@link #init(IEditorSite, IEditorInput)}.
    *
    * @param ref the reference to be checked, cannot be
    * <code>null</code>.
    *
    * @return <code>true</code> if the reference is valid.
    */
   public abstract boolean isValidReference(IPSReference ref);


   /**
    * By default, all design objects support save-as. Derived editors must
    * override this if it is not supported.
    *
    * @return Always <code>true</code>.
    */
   @Override
   public boolean isSaveAsAllowed()
   {
      return true;
   }

   @Override
   public void doSave(IProgressMonitor pMonitor)
   {

      PSModelTracker tracker = PSModelTracker.getInstance();
      try
      {
         if(m_isReadOnlyMode)
         {
            doSaveAs();
            return;
         }
         Object[] args = new Object[]{m_reference.getName()};
         pMonitor.beginTask(
            PSMessages.getString("PSEditorBase.progress.message.saving", //$NON-NLS-1$
               args),
            IProgressMonitor.UNKNOWN);
         tracker.save(m_reference, false, true);
         onSaveCompleted(pMonitor);
         //fire property change event
         m_dirty = false;
         firePropertyChange(PROP_DIRTY);
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
      finally
      {
         pMonitor.done();
      }

   }

   /**
    * Is called after save operation is completed.
    * Created for subclasses to add own processing after save.
    * Default implementation does nothing.
    * @deprecated Used for application editor only which uses custom legacy API
    * for saving. Use model API to properly handle save operation.
    */
   @Deprecated
   protected void onSaveCompleted(
         @SuppressWarnings("unused") IProgressMonitor pMonitor) throws Exception
   {
   }

   /* (non-Javadoc)
    * @see org.eclipse.ui.part.EditorPart#doSaveAs()
    */
   @Override
   public void doSaveAs()
   {
      // Pop-up a dialog to get save as name from user
      InputDialog dialog =
         new InputDialog(getSite().getShell(),
            PSMessages.getString(
               "PSEditorBase.saveas.inputdialog.title"), //$NON-NLS-1$
            PSMessages.getString(
               "PSEditorBase.saveas.inputdialog.prompt"), //$NON-NLS-1$
            null,
            new IInputValidator(){
               @SuppressWarnings("synthetic-access") //$NON-NLS-1$
               public String isValid(String name)
               {
                  if(name.length() == 0)
                     return PSMessages.getString(
                        "PSEditorBase.saveas.validation.message.name.required"); //$NON-NLS-1$

                  if(!name.equals(StringUtils.deleteWhitespace(name)))
                     return PSMessages.getString(
                        "PSEditorBase.saveas.validation.message.name.nowhitespace"); //$NON-NLS-1$
                  PSControlValueDuplicateNameValidator validator =
                     new PSControlValueDuplicateNameValidator(
                        m_reference.getParentModel());
                  
                  if(validator.isDuplicateName(name))
                     return PSMessages.getString(
                     "PSControlValueDuplicateNameValidator.error.duplicateName"); //$NON-NLS-1$

                  return getIdValidator().validateId(
                        name,
                        PSMessages
                              .getString("PSRenameAction.action.name.label"));
               }
         });
      if(dialog.open() == Window.OK)
      {
         final String name = dialog.getValue();
         BusyIndicator.showWhile(getSite().getShell().getDisplay(),
               new Runnable()
               {
                  public void run()
                  {
                     runSaveAs(name);
                  }
               });
      }
   }

   /**
    * Get the name validator for the current UI component.
    * @param ref the UI component, assumed not <code>null</code>. 
    * @return the id validator. Never <code>null</code>.
    */
   private PSControlValueTextIdValidator getIdValidator()
   {
      PSControlValidatorFactory factory = PSControlValidatorFactory
            .getInstance();
      return factory.getIdValidatorForType(m_reference.getObjectType()
            .getPrimaryType());
   }
   
   /**
    * Saves the current object to the object with provided name.
    * @param newName name for the newly created object.
    * Assumed not <code>null</code>.
    */
   private void runSaveAs(final String newName)
   {
      IPSCmsModel model = m_reference.getParentModel();
      PSModelTracker tracker = PSModelTracker.getInstance();

      try
      {
         PSUiReference parent = null;

         IWorkbenchWindow window =
            PSWorkbenchPlugin.getDefault().getWorkbench().
            getActiveWorkbenchWindow();
         if(window != null)
         {
            ISelectionService service = window.getSelectionService();
            if (service.getSelection() instanceof StructuredSelection)
            {
               StructuredSelection selection =
                  (StructuredSelection)service.getSelection();
               if(selection != null)
               {
                  if(selection.size() == 1)
                  {
                     parent = (PSUiReference)selection.getFirstElement();
                  }
               }
            }
         }

         // Clone the reference with the new name
         IPSReference newRef = tracker.create(
               new IPSReference[] { m_reference },
               new String[] { newName }, parent)[0];
         IPSReference oldRef = m_reference;

         // load the new object to the editor
         final Object oldData = m_data;
         try
         {
            load(newRef);
         }
         catch (Exception e)
         {
            // no data is loaded yet, so clean up the ref to discard it
            m_data = oldData;
            // If we failed to load the object we are in sad state.
            // Big chance deletion will fail, so ignore any deletion exceptions,
            // report only the original exception.
            try
            {
               model.delete(newRef);
            }
            catch (Exception ignore)
            {
            }
            throw e;
         }

         if (!tuneForSaveAs())
         {
            m_data = oldData;
            model.delete(newRef);
            return;
         }
         m_reference = newRef;

         // update the editor ui
         m_isReadOnlyMode = false;
         setPartName(newRef.getName());
         reloadControlValues();

         // fire property change event
         m_dirty = true;
         firePropertyChange(PROP_DIRTY);

         // release the lock from the old object reference
         tracker.releaseLock(oldRef, true);

         // save new object
         saveForSaveAs();
         setInput(PSEditorRegistry.getInstance().createEditorInputForRef(newRef));

         // fire property change event
         m_dirty = false;
         firePropertyChange(PROP_DIRTY);
      }
      catch (PSDuplicateNameException e)
      {
         Object[] args = new Object[]{newName};
         String msg =
            PSMessages.getString(
               "common.error.duplicatename", args); //$NON-NLS-1$
         new PSErrorDialog(getSite().getShell(), msg).open();

      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
   }

   /**
    * Forces the editor to reload controls with data from the object it holds.
    */
   public void reloadControlValues()
   {
      blockRegisteredControlListeners(true);
      try
      {
         loadControlValues(m_data);
      }
      finally
      {
         blockRegisteredControlListeners(false);
      }
   }

   /**
    * Actually saves the current object during "Save As" operation.
    */
   protected void saveForSaveAs() throws Exception
   {
      final PSModelTracker tracker = PSModelTracker.getInstance();
      tracker.save(m_reference, false, true);
   }

   /**
    * Called to update the newly cloned object for save as.
    * At the time of the call the object is stored in {@link #m_data}.
    * Is called from {@link #doSaveAs()} before current editor data is updated,
    * so it is possible to cancel the "Save As" operation without any changes
    * to the editor.
    * Default implementation does nothing and always returns <code>true</code>.
    * @return <code>true</code> if this operation is completed successfully and
    * save as operation should continue. If <code>false</code> is returned
    * {@link #doSaveAs()} will delete the object referenced by <code>ref</code>.
    * @throws Exception passes up exceptions for handling in the calling method.
    */
   protected boolean tuneForSaveAs()
         throws Exception
   {
      return true;
   }

   /* (non-Javadoc)
    * @see org.eclipse.ui.part.EditorPart#isDirty()
    */
   @Override
   public boolean isDirty()
   {
      return m_dirty;
   }

   /**
    * Marks editor as dirty.
    */
   public void setDirty()
   {
      if(!m_dirty)
      {
         m_dirty = true;
         firePropertyChange(PROP_DIRTY);
      }
   }
   
   /**
    * Marks the editor as clean.
    */
   protected void setClean()
   {
      if (m_dirty)
      {
         m_dirty = false;
         firePropertyChange(PROP_DIRTY);
      }
   }

   /**
    * Sets the status bar message
    * @param msg may be <code>null</code> in which case the message
    * will be cleared.
    */
   public void setStatusLineMessage(String msg)
   {
      IStatusLineManager manager =
         getEditorSite().getActionBars().getStatusLineManager();
      manager.setMessage(msg);
   }

   /**
    * @return <code>true</code> if editor is in read only mode.
    */
   public boolean isReadOnly()
   {
      return m_isReadOnlyMode;
   }

   /* (non-Javadoc)
    * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(
    * org.eclipse.swt.widgets.Composite)
    */
   @Override
   public final void createPartControl(Composite comp)
   {
      Composite parentComp = new Composite(comp, SWT.NONE);
      parentComp.setLayout(new FormLayout());

      Composite scrollComp = null;
      if(isMultiPage())
      {
         scrollComp = new Composite(parentComp, SWT.NONE);
         scrollComp.setLayout(new FillLayout());
      }
      else
      {
         scrollComp =
            new ScrolledComposite(parentComp, SWT.V_SCROLL | SWT.H_SCROLL);
      }

      m_container = createContainer(scrollComp);
      m_container.setLayout(new FillLayout());

      final FormData childformData = new FormData();
      childformData.left =
         new FormAttachment(0, 5);
      childformData.right = new FormAttachment(100, -5);
      childformData.top =
         new FormAttachment(0, 10);
      childformData.bottom = new FormAttachment(100, 0);
      scrollComp.setLayoutData(childformData);
      blockRegisteredControlListeners(true);
      createControl(m_container);
      blockRegisteredControlListeners(false);
      initHelpManager();

      // This code must appear after the createControl() call or the scrollable
      // composite will appear blank when the editor is first loaded.
      if(!isMultiPage())
      {
         Point preferred = m_container.computeSize(SWT.DEFAULT, SWT.DEFAULT);
         ((ScrolledComposite)scrollComp).setContent(m_container);
         ((ScrolledComposite)scrollComp).setMinSize(
            preferred.x, preferred.y);
         ((ScrolledComposite)scrollComp).setExpandHorizontal(true);
         ((ScrolledComposite)scrollComp).setExpandVertical(true);
      }

      reloadControlValues();
      //Disable all controls for read only
      if(isReadOnly())
         disableEditorControls();
      runValidation(true); // run initial validation
   }

   /**
    * Will disable all editor field controls, should be implemented
    * by specialized subclasses if they have a specific need.
    */
   protected void disableEditorControls()
   {
      disableAllControls(m_container);
   }

   /**
    * Disables the control and all of its children recursively.
    * Label controls will not be disabled.
    * @param control the control to be disabled cannot be <code>null</code>.
    */
   protected void disableAllControls(Control control)
   {
      if(control == null)
         throw new IllegalArgumentException("control cannot be null.");
      if(!(control instanceof Label))
         control.setEnabled(false);
      if(control instanceof Composite)
      {
         for(Control child : ((Composite)control).getChildren())
         {
            disableAllControls(child);
         }
      }
   }

   /**
    * Initializes the help manager for this editor
    */
   protected void initHelpManager()
   {
      m_helpManager = new PSHelpManager(this, m_container);
   }


   protected Composite createContainer(Composite parent)
   {
      return new Composite(parent, SWT.NONE);
   }

   /**
    * Creates the SWT controls for this editor.
    * @see org.eclipse.ui.IWorkbenchPart#createPartControl(
    * org.eclipse.swt.widgets.Composite) for more details
    * @param comp the parent composite to add other SWT controls to,
    * never <code>null</code>.
    */
   public abstract void createControl(Composite comp);

   /* (non-Javadoc)
    * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
    */
   @Override
   public void setFocus()
   {
      // Set focus to first registered control
      if(!m_controlIndex.isEmpty())
      {
         m_controlIndex.get(0).setFocus();
      }
   }

   /**
    * Load the object referenced by the <code>IPSReference</code>
    * that was passed to this editor.
    */
   protected void load(IPSReference ref) throws Exception
   {
      PSModelTracker tracker = PSModelTracker.getInstance();

      try
      {
         if (ref.isPersisted()
               && !hasPermission(ref.getPermissions(), PSPermissions.UPDATE))
         {
            if (PSWorkbenchPlugin.getDefault().getPreferences()
                  .isShowWarningForReadOnlyObjects())
            {
               String title = PSMessages
                     .getString("PSEditorBase.noUpdatePermission.title");
               String msg = PSMessages
                     .getString("PSEditorBase.noUpdatePermission.message");
               MessageDialog.openInformation(getSite().getShell(), title, msg);
            }
            m_data = tracker.load(ref, false, false, true);
            m_isReadOnlyMode = true;
         }
         else
            m_data = tracker.load(ref, true, false, true);
      }
      catch (PSLockException e)
      {
         final Object data = handleLockException(ref, e);
         if (data == null)
         {
            //load as read-only
            m_data = tracker.load(ref, false, false, true);
            m_isReadOnlyMode = true;
         }
         else
         {
            m_data = data;
         }
      }
   }

   /**
    * Resolves locking problem by interacting with user.
    * If the object was locked to the same user elsewhere it asks confirmation
    * to overwrite the lock. If the object is locked to somebody else it
    * notifies the user.
    * Must be called in SWT event thread.
    * @param ref the object for which the exception is thrown.
    * Not <code>null</code>.
    * @param e the locking exception. Not <code>null</code>.
    * @return the object locked for editing or <code>null</code> if it is
    * not possible to lock the object for editing.
    */
   public static Object handleLockException(IPSReference ref, PSLockException e)
         throws Exception
   {
      assert e != null;
      final PSModelTracker tracker = PSModelTracker.getInstance();
      PSCoreFactory factory = PSCoreFactory.getInstance();
      PSLockHelper lockHelper = factory.getLockHelper();
      final Shell shell =
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      if(lockHelper.isLockedToMeElsewhere(ref))
      {
         Object[] args = new Object[]{ref.getName()};
         String overrideTitle = PSMessages.getString(
            "PSEditorBase.question.overrideLock.title"); //$NON-NLS-1$
         String overrideMsg = PSMessages.getString(
            "PSEditorBase.question.overrideLock.message", args); //$NON-NLS-1$
         boolean override = MessageDialog.openQuestion(
               shell,
               overrideTitle,
               overrideMsg);
         return override ? tracker.load(ref, true, true, true) : null;
      }
      else if (lockHelper.isLockedToOther(ref))
      {
         if (PSWorkbenchPlugin.getDefault().getPreferences()
                  .isShowWarningForReadOnlyObjects())
         {
            Object[] args = new Object[] 
            {
               ref.getName(), 
               ref.getLockUserName()
            };
            String lockedTitle = PSMessages.getString(
               "PSEditorBase.warning.lockedBySomeoneElse.title"); //$NON-NLS-1$
            String lockedMsg = PSMessages.getString(
               "PSEditorBase.warning.lockedBySomeoneElse.message", args); //$NON-NLS-1$
            
            MessageDialog.openWarning(shell, lockedTitle, lockedMsg);
         }
         
         return null;
      }
      else
      {
         // Should never get here
         PSWorkbenchPlugin.handleException(null, null, null, e);
         return null;
      }
   }

   /**
    * Checks the ordinal of the supplied permission enum against each entry in
    * the supplied array.
    *
    * @return <code>true</code> if one of the array entries match,
    * <code>false</code> otherwise.
    */
   private boolean hasPermission(int[] permissions, PSPermissions toCheck)
   {
      // fixme: This code is here to get around a bug that sharedfield objects
      // have no acls and as such will only open in read only.
      final Enum primaryType = m_reference.getObjectType().getPrimaryType();
      if (primaryType.equals(PSObjectTypes.SHARED_FIELDS)
            || primaryType.equals(PSObjectTypes.XML_APPLICATION )
               || primaryType.equals(PSObjectTypes.EXTENSION))
      {
         return true;
      }
      // End of hack for shared fields

      int checkPerm = toCheck.getOrdinal();
      for (int perm : permissions)
      {
         if (perm == checkPerm)
            return true;
      }
      return false;
   }

   /**
    * Register the control so that the editor can handle modifcation
    * type events on the control. This Method sets the appropriate
    * listeners on the passed in control which upon modifcation will
    * call {@link #onControlModified(Control, boolean)}
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
    * @param hint the property change hint for this control, may be
    * <code>null</code>.
    */
   public void registerControl( String displayName,
      final Object control, final IPSControlValueValidator[] validators,
      final Map<String, String> hint)
   {
      registerControl(displayName, control, validators, -1, hint);
   }

   /**
    * Convienience method that calls registerControl(
    * String, Object, IPSControlValueValidator[], -1)}
    */
   public void registerControl( String displayName,
      final Object control, final IPSControlValueValidator[] validators)
   {
      registerControl(displayName, control, validators, -1);
   }

   /**
    * Convienience method that calls registerControl(
    * String, Object, IPSControlValueValidator[], int, Map)
    */
   public void registerControl( String displayName,
      final Object control, final IPSControlValueValidator[] validators,
      final int page)
   {
      registerControl(displayName, control, validators, page, null);
   }

   /**
    * Register the control so that the editor can handle modifcation
    * type events on the control. This Method sets the approppriate
    * listeners on the passed in control which upon modifcation will
    * call {@link #onControlModified(Control, boolean)}. Controls will be
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
    * @param page the page that this control is on for a mult-page editor
    * or -1 for a regular editor.
    * @param hint the property change hint for this control, may be
    * <code>null</code>.
    */
   public void registerControl( String displayName,
      final Object control, final IPSControlValueValidator[] validators,
      final int page, final Map<String, String> hint)
   {
      registerControl(displayName, control, validators, page, hint, false);
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
    * @param page the page that this control is on for a mult-page editor
    * or -1 for a regular editor.
    */
   public void registerControlHelpOnly(String displayName,
      final Object control, int page)
   {
      registerControl(displayName, control, null, page, null, true);
   }

   /**
    * Convenince method to call
    * registerControlHelpOnly(String, Object, -1)
    */
   public void registerControlHelpOnly(String displayName,
      final Object control)
   {
      registerControlHelpOnly(displayName, control, -1);
   }

   /**
    * Register the control so that the editor can handle modifcation
    * type events on the control. This Method sets the approppriate
    * listeners on the passed in control which upon modifcation will
    * call {@link #onControlModified(Control, boolean)}. Controls will be
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
    * @param page the page that this control is on for a mult-page editor
    * or -1 for a regular editor.
    * @param hint the property change hint for this control, may be
    * <code>null</code>.
    * @param helpOnly flag indicating that the control is registered only
    * so it will show help hints and will not call the update designer
    * object method
    */
   public void registerControl( String displayName,
      final Object control, final IPSControlValueValidator[] validators,
      final int page, final Map<String, String> hint, boolean helpOnly)
   {
      if(displayName == null || displayName.trim().length() == 0)
         throw new IllegalArgumentException(
            "displayName cannot be null or empty."); //$NON-NLS-1$
      if(control == null)
         throw new IllegalArgumentException("Control cannot be null."); //$NON-NLS-1$
      if(!(control instanceof Control || control instanceof Viewer))
         throw new IllegalArgumentException(
            "The passed in control must be an instance of Control or Viewer"); //$NON-NLS-1$
      Control widget = null;
      if(control instanceof Viewer)
         widget = ((Viewer)control).getControl();
      else
         widget = (Control)control;
      m_controlIndex.add(widget);
      m_controlInfo.put(widget,
         new PSControlInfo(widget, replace(displayName, "&", ""), //$NON-NLS-1$ //$NON-NLS-2$
            validators, PSControlInfo.TYPE_NORMAL, page, hint, helpOnly));

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
      // change really occured on the control in question.
      // We will use reflection to determine if the add listener methods
      // exist for the passed in control. We also invoke using reflection.
      Class clazz = widget.getClass();
      Method addModifyListenerMethod = null;
      Method addSelectionListenerMethod = null;
      try
      {
         addModifyListenerMethod = clazz.getMethod("addModifyListener",  //$NON-NLS-1$
            new Class[]{ModifyListener.class});
      }
      catch (NoSuchMethodException ignore){}
      try
      {
         addSelectionListenerMethod = clazz.getMethod("addSelectionListener",  //$NON-NLS-1$
            new Class[]{SelectionListener.class});
         if((widget instanceof Text) || (widget instanceof StyledText)
            ||(widget instanceof Combo) || (widget instanceof CCombo))
            addSelectionListenerMethod = null;
      }
      catch (NoSuchMethodException ignore){}

      if(addModifyListenerMethod != null)
      {
         ModifyListener mListener = new ModifyListener()
         {
            public void modifyText(ModifyEvent e)
            {
               if(m_blockControlListeners)
                  return;
               Object source = e.getSource();
               m_textControlModified = true;
               onControlModified((Control)source, false);
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

      if(addSelectionListenerMethod != null &&
         !(widget instanceof PSSortableTable))
      {
         SelectionListener sListener = new SelectionAdapter()
         {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
               if(m_blockControlListeners)
                  return;
               Object source = e.getSource();
               onControlModified((Control)source, true);
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

      // Any control that has a modify listener needs a corresponding
      // focus listener
      if(addModifyListenerMethod != null)
      {
         widget.addFocusListener(new FocusAdapter()
            {

            @Override
            public void focusLost(FocusEvent e)
            {
               if(m_blockControlListeners)
                  return;
               Object source = e.getSource();

               onControlModified((Control)source, true);
               m_textControlModified = false;
            }

            });
      }

      // Handle table modifier for sortable table composite
      if(widget instanceof PSSortableTable)
      {
         ((PSSortableTable)widget).addTableModifiedListener(
            new IPSTableModifiedListener()
            {
               public void tableModified(PSTableEvent event)
               {
                  if(m_blockControlListeners)
                     return;
                  Object source = event.getSource();
                  onControlModified((Control)source, true);
               }

            });
      }


   }

   /**
    * Unregisters the specified control
    * @param control cannot be <code>null</code>.
    */
   public void unregisterControl(Control control)
   {
      if(control == null)
         throw new IllegalArgumentException("control cannot be null."); //$NON-NLS-1$
      m_controlIndex.remove(control);
      m_controlInfo.remove(control);
   }

   /**
    * Does the following upon a control modification event.
    * <p>
    * <pre>
    *   <li> Validators are run
    *   <li> The cached design object is updated to reflect the new value.
    *   <li> The dirty flag is set.
    *   <li> The model tracker is informed of a property change.
    * </pre>
    * </p>
    * @param control the Control that was modified, cannot be <code>null</code>
    * @param notifyTracker notifies the model tracker if this flag
    * is <code>true</code>.
    */
   protected void onControlModified(Control control, boolean notifyTracker)
   {
      if(control == null)
         throw new IllegalArgumentException("control cannot be null."); //$NON-NLS-1$
      PSControlInfo info = m_controlInfo.get(control);

      try
      {
         //  Update the cached design object
         updateDesignerObject(m_data, control);

         if(((control instanceof Text) || (control instanceof StyledText)
            || (control instanceof Combo) || (control instanceof CCombo))
            && !m_textControlModified)
            return; // skip notification if no modification occured
         // Run validators if any
         runValidation(true);
         // to the text control.
         // Set dirty
         setDirty();


         // Notify the Model tracker of a property change
         if(notifyTracker)
         {
            firePropertyChange(info.getHint());
         }
      }
      catch (PSLockException le)
      {
         PSWorkbenchPlugin.handleException(null, null, null, le);
      }
      catch (Exception e)
      {
         PSUiUtils.log("Update Error Occurred.", e);
         runValidation(true);
      }
   }

   /**
    * Notifies the model tracker that a modification took place
    * on the object that this editor represents.
    * @param hint property change hints, may be <code>null</code>.
    * @throws PSLockException if a locking error occurs
    */
   public void firePropertyChange(Map<String, String> hint)
      throws PSLockException
   {
      try
      {
         m_ignoreModifyEvent = true;
         PSModelTracker tracker = PSModelTracker.getInstance();
         m_blockModelListener = true;
         tracker.propertyChanged(m_reference, hint);

      }
      finally
      {
         m_blockModelListener = false;
         m_ignoreModifyEvent = false;
      }
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
      if(isNotBlank(key))
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
    * Base class implementation just returns name of the class.
    * @see com.percussion.workbench.ui.help.IPSHelpProvider#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   public String getHelpKey(@SuppressWarnings("unused") Control control)
   {
      return getClass().getName();
   }

   /**
    * Runs validation on all registered controls in this editor.
    *
    * @param showError If <code>true</code> then error messages will be
    * displayed.
    *
    * @return <code>true</code> if validation problems exist.
    */
   public boolean runValidation(boolean showError)
   {
      return runValidation(showError, false);
   }

   /**
    * Runs validation on all registered controls in this editor.
    *
    * @param showError If <code>true</code> then error messages will be
    * displayed.
    *
    * @param suppressOpen suppress the auto open of the problems view.
    * @return <code>true</code> if validation problems exist.
    */
   public boolean runValidation(boolean showError, boolean suppressOpen)
   {
      if(!m_allowValidation)
         return false;

      String error = null;
      PSProblemSet problems = new PSProblemSet();
      for(Control key : m_controlIndex)
      {
         PSControlInfo cInfo = m_controlInfo.get(key);
         IPSControlValueValidator[] validators =
            cInfo.getValidators();
         if(validators != null)
         {
            for(IPSControlValueValidator validator : validators)
            {
               error = validator.validate(cInfo);
               if(error != null)
                  problems.addProblem(PSProblemSet.TYPE_ERROR,
                     cInfo.getDisplayName(),
                     error, getPagename(cInfo.getPage()));
            }
         }
      }
      if(problems.isEmpty())
      {
         IViewPart view = verifyProblemsViewOpened(false, false);
         if(view != null)
            ((PSProblemsView)view).displayProblems(null);
      }
      else
      {
         if(showError)
         {
            boolean bringToFront = !problems.equals(m_lastProblemSet);
            IViewPart view = verifyProblemsViewOpened(
               true && !suppressOpen, bringToFront);
            if(view != null)
               ((PSProblemsView)view).displayProblems(problems);
         }
      }
      m_lastProblemSet = problems;
      return !problems.isEmpty();
   }

   /**
    * Return the pagename by the page index passed in. Should be implemented
    * by multi-page editor subclasses. The base class implementation is to
    * always return <code>null</code>.
    * @param index the page index
    * @return the page name or <code>null</code> if no page
    * found.
    */
   protected String getPagename(@SuppressWarnings("unused") int index)
   {
      return null;
   }

   /**
    * Helper method that will verify if the problems view is currently
    * opened and if not depending on the passed in paremter and user's
    * preferences, will open the view. The view is returned if it is
    * open(ed).
    * @param openIfAllowed will attempt to open the view if it is not
    * already open and preferences allow auto opening of this view.
    * @param bringToFront bring problems view to front if opened.
    * @return the view or <code>null</code> if not open(ed).
    */
   protected IViewPart verifyProblemsViewOpened(boolean openIfAllowed,
      boolean bringToFront)
   {
      boolean canAutoOpen =
         PSWorkbenchPlugin.getDefault().getPreferences().
         isAutoOpenProblemsView();
      IViewPart view = PSUiUtils.findView(PSProblemsView.ID);
      if(view != null)
      {
         if(bringToFront && canAutoOpen)
            PSUiUtils.bringToTop(view);
         return view;
      }

      if(openIfAllowed && canAutoOpen)
      {
         try
         {
            PSUiUtils.openView(PSProblemsView.ID);
            view = PSUiUtils.findView(PSProblemsView.ID);
            if(bringToFront && canAutoOpen)
               PSUiUtils.bringToTop(view);
            return view;
         }
         catch (ExecutionException e)
         {
            PSWorkbenchPlugin.handleException(null, null, null, e);
            return null;
         }
      }
      return null;
   }

   /*
    * @see org.eclipse.ui.IReusableEditor#setInput(
    * org.eclipse.ui.IEditorInput)
    */
   @Override
   public void setInput(IEditorInput input)
   {
      super.setInput(input);
   }

   /*
    * @see org.eclipse.ui.part.WorkbenchPart#dispose()
    */
   @Override
   public void dispose()
   {
      // Remove model listener
      m_reference.getParentModel().removeListener(m_modelListener);
      // Call release lock when editor closes
      PSModelTracker tracker = PSModelTracker.getInstance();
      try
      {
         tracker.releaseLock(m_reference, true);
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
      super.dispose();
   }

   /**
    * Used to set flag that determines if listeners should be blocked
    * on registered controls. This method is primarily used when calling
    * loadControlValues. You set the blocking to <code>true</code>
    * before calling the method and then set to <code>false</code> right after.
    * This keeps the editor from thinking that a user modified a field when
    * the editor actually called the load values method.
    * @param block
    */
   protected void blockRegisteredControlListeners(boolean block)
   {
      m_blockControlListeners = block;
   }


   /**
    * Retrieves the underlying designer object that this editor
    * operates on.
    * @return the designer object, never <code>null</code>.
    */
   public Object getDesignerObject()
   {
      return m_data;
   }

   /**
    * Flag indicating if this editor is multi page
    * @return <code>true</code> if multi page.
    */
   protected boolean isMultiPage()
   {
      return false;
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
    * Reference to the object that this editor edits. Initialized
    * in {@link #init(IEditorSite, IEditorInput)}, never <code>null</code>
    * after that.
    */
   public IPSReference getReference()
   {
      return m_reference;
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
    * This method is called whenever a notification is received from the model
    * for a data modify event or a rename event.
    * 
    * @param event the change event. Framework guarantees never
    * <code>null</code>.
    */
   protected void handleModelChanged(PSModelChangedEvent event)
   {
      if(!event.getSource()[0].equals(m_reference))
         return;
      if(m_ignoreModifyEvent &&
         event.getEventType() == ModelEvents.MODIFIED)
         return;
      if(event.getEventType() == ModelEvents.RENAMED)
      {
         setPartName(m_reference.getName());
      }
      if(m_blockModelListener)
         return;

      boolean oldState = m_blockControlListeners;
      try
      {
         m_blockControlListeners = true;
         Display.getDefault().syncExec(new Runnable()
            {
               public void run()
               {
                  loadControlValues(m_data);
               }

            });

      }
      finally
      {
         m_blockControlListeners = oldState;
      }
      if(event.getEventType() == ModelEvents.MODIFIED)
         Display.getDefault().asyncExec(new Runnable()
            {
               public void run()
               {
                  setDirty();
               }

            });
   }

   /**
    * @see #getReference()
    */
   protected IPSReference m_reference;

   /**
    * The editor input passed into this editor. Initialized
    * in {@link #init(IEditorSite, IEditorInput)}, never <code>null</code>
    * after that.
    */
   protected PSEditorInput m_editorInput;

   /**
    * This is the actual object that the editor will edit and is referenced
    * by <code>IPSReference</code>. Initialized in {@link #load(IPSReference)},
    * never <code>null</code> after that.
    */
   protected Object m_data;

   /**
    * Flag indicating if this editor is dirty
    */
   private boolean m_dirty;

   /**
    * Flag indicating that listeners on registered controls should be
    * blocked.
    */
   protected boolean m_blockControlListeners;

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
    * The container widget.
    */
   protected Composite m_container;


   /**
    * Model listener, initialized in {@link #init(IEditorSite, IEditorInput)}
    */
   private IPSModelListener m_modelListener;

   /**
    * Flag indicating that validation is allowed, defaults to <code>true</code>
    */
   protected boolean m_allowValidation = true;

   /**
    * Flag indicating that the current focused text control was
    * modified.
    */
   protected boolean m_textControlModified;

   /**
    * The help manager for editors
    */
   protected PSHelpManager m_helpManager;

   /**
    * Flag indicating that the tracker listener should perform
    * no action as a modification was made by this editor.
    */
   protected boolean m_blockModelListener;

   /**
    * Flag indicating that this editor is in read-only mode. It
    * needs to be saved via the Save As method.
    */
   protected boolean m_isReadOnlyMode;

   /**
    * The last occuring problem set
    */
   protected PSProblemSet m_lastProblemSet;

   /**
    * Flag indicating that the modify event coming from the model should
    * be ignored.
    */
   protected boolean m_ignoreModifyEvent;
}
