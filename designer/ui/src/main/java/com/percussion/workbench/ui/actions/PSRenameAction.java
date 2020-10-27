/******************************************************************************
 *
 * [ PSRenameAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSHierarchyManager;
import com.percussion.client.models.PSLockException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.services.security.PSPermissions;
import com.percussion.workbench.ui.PSEditorRegistry;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import com.percussion.workbench.ui.validators.PSControlValueTextIdValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This action opens an inline editor on a Rhythmyx design object's name within
 * the tree. 
 * <p>
 * The action is applicable to selections containing elements of type
 * <code>PSUiReference</code> that refer to design objects. 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 6.0
 */
public final class PSRenameAction extends  PSBaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId()
         + ".RenameAction"; //$NON-NLS-1$

   /**
    * Create a new action that knows how to rename a design object.
    * 
    * @param viewer Used to perform inline editing of the selected node. Never
    * <code>null</code>.
    * @param pasteAction the paste action that resides in the same group that the
    * rename action lives in. Cannot be <code>null</code>.
    */
   public PSRenameAction(StructuredViewer viewer, PSPasteAction pasteAction)
   {
      super(PSMessages.getString("PSRenameAction.action.label"), viewer);//$NON-NLS-1$
      if ( null == viewer)
      {
         throw new IllegalArgumentException("viewer cannot be null"); //$NON-NLS-1$ 
      }
      if ( null == pasteAction)
      {
         throw new IllegalArgumentException("pasteAction cannot be null"); //$NON-NLS-1$ 
      }
      m_viewer = viewer;
      m_pasteAction = pasteAction;
      setToolTipText(PSMessages.getString("PSRenameAction.action.tooltip"));//$NON-NLS-1$
      //for global action support
      setActionDefinitionId("org.eclipse.ui.edit.rename");//$NON-NLS-1$
      setId(PSRenameAction.ID);
   }

   /**
    * @inheritDoc
    * Gets the selection from the base class. If this is an
    * <code>IStructuredSelection</code>, each element is processed by opening
    * the appropriate editor using the {@link PSEditorRegistry} to open
    * the proper one
    */
   @Override
   public void run()
   {
      try
      {
         if (!isEnabled())
            return;
         
         IStructuredSelection sel = getStructuredSelection();
         if (sel == null || sel.size() != 1)
            return;
         
         if (!(sel.getFirstElement() instanceof PSUiReference))
            return;
         
         PSUiReference node = (PSUiReference) sel.getFirstElement();
         if (node.getObjectType() == null || node.isReference())
            return;
         IPSReference ref = node.getReference();
         IPSCmsModel model = PSCoreFactory.getInstance().getModel(ref);
         Collection<? extends IPSReference> refs;
         if (model.isHierarchyModel())
         {
            m_isHierarchyNode = true;
            IPSHierarchyManager mgr = model.getHierarchyManager(ref);
            refs = mgr.getChildren(((IPSHierarchyNodeRef) ref).getParent());
         }
         else
         {
            m_isHierarchyNode = false;
            refs = model.catalog(false);
         }
         Collection<String> existingNames = new ArrayList<String>();
         for (IPSReference existingRef : refs)
         {
            existingNames.add(existingRef.getName().toLowerCase());
         }
         existingNames.remove(node.getName().toLowerCase());
         createEditor(node, existingNames);
      }
      catch (PSModelException e)
      {
         //should never happen
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Creates an inline editor, activates it and gives it the focus. 
    * 
    * @param node The node that is being renamed.
    * @param existingNames Assumed not <code>null</code>.
    */
   private void createEditor(final PSUiReference node,
         final Collection<String> existingNames)
   {
      if(!(m_viewer instanceof TreeViewer))
         return;
      m_lastEnableState = m_pasteAction.isEnabled();
      TreeViewer viewer = (TreeViewer)m_viewer;
      final TreeEditor editor = new TreeEditor(viewer.getTree());
      //The editor must have the same size as the cell and must
      //not be any smaller than 50 pixels.
      editor.horizontalAlignment = SWT.LEFT;
      editor.grabHorizontal = true;
      editor.minimumWidth = 50;
      
      // Clean up any previous editor control
      Control oldEditor = editor.getEditor();
      if (oldEditor != null) 
         oldEditor.dispose();

      final TreeItem[] sel = viewer.getTree().getSelection();
      if (sel == null || sel.length != 1)
         return;
      
      // The control that will be the editor must be a child of the Tree
      final Composite textEditorParent = new Composite(viewer.getTree(),
            SWT.NONE);
      editor.setEditor(textEditorParent, sel[0]);
      final Text textEditor = new Text(textEditorParent, SWT.NONE);      
      textEditorParent.setBackground(textEditor.getBackground());
      
      textEditorParent.addListener(SWT.Paint, new Listener()
      {
         public void handleEvent(Event e)
         {
            Point textSize = textEditor.getSize();
            Point parentSize = textEditorParent.getSize();
            e.gc.drawRectangle(0, 0, Math.min(textSize.x + 4,
                  parentSize.x - 1), parentSize.y - 1);
         }
      });
      textEditor.addListener(SWT.Modify, new Listener()
      {
         @SuppressWarnings("unused")
         public void handleEvent(Event e)
         {
            Point textSize = textEditor.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            textSize.x += textSize.y; // Add extra space for new characters.
            Point parentSize = textEditorParent.getSize();
            textEditor.setBounds(2, 1, Math.min(textSize.x,
                  parentSize.x - 4), parentSize.y - 2);
            textEditorParent.redraw();
         }
      });
      textEditor.addListener(SWT.Traverse, new Listener()
      {
         @SuppressWarnings("synthetic-access")
         public void handleEvent(Event event)
         {
            switch (event.detail)
            {
               case SWT.TRAVERSE_ESCAPE:
                  // Do nothing in this case
                  editor.setEditor(null);
                  textEditorParent.dispose();
                  m_pasteAction.setEnabled(m_lastEnableState);
                  event.doit = true;
                  event.detail = SWT.TRAVERSE_NONE;
                  break;
               case SWT.TRAVERSE_RETURN:
                  m_ignoreFocusLost = true;
                  if (validateName(textEditor.getText(), existingNames, node))
                  {
                     boolean finished = true;
                     try
                     {
                        finished = save(node, textEditor.getText());
                     }
                     finally
                     {
                        if (finished)
                        {
                           editor.setEditor(null);
                           textEditorParent.dispose();
                           m_pasteAction.setEnabled(m_lastEnableState);
                        } 
                     }
                  }
                  m_ignoreFocusLost = false;
                  event.doit = true;
                  event.detail = SWT.TRAVERSE_NONE;
                  break;
            }
         }
      });
      textEditor.addFocusListener(new FocusAdapter()
      {
         @Override
         @SuppressWarnings("synthetic-access")
         public void focusLost(@SuppressWarnings("unused") FocusEvent e)
         {
            /*
             * We want to catch focus lost events that were NOT caused by 
             * ourselves. Thus, we set this flag while doing processing that
             * would cause such an event, then ignore it. See above.
             */
            if (m_ignoreFocusLost)
            {
               return;
            }
            if (!validateName(textEditor.getText(), existingNames, node))
            {
               textEditor.setFocus();
               ((TreeViewer)m_viewer).getTree().setSelection(sel);
            }
            else
            {
               if (save(node, textEditor.getText()))
               {
                  editor.setEditor(null);
                  textEditorParent.dispose();
                  m_pasteAction.setEnabled(m_lastEnableState);
               }
            }
         }
      });
            
      textEditor.setText(node.getName());
      Enum type = node.getReference().getObjectType().getPrimaryType();
      int limit = DEFAULT_MAX_NAME_LENGTH;
      if(type == PSObjectTypes.UI_SEARCH || type == PSObjectTypes.UI_VIEW)
         limit = 255;
      textEditor.setTextLimit(limit);
      textEditor.selectAll();
      m_pasteAction.setEnabled(true);
      textEditor.setFocus();
   }
    
   
   /**
    * Checks for problems with the supplied name and if any are found, displays
    * an appropriate message to the user. Problems include the following:
    * <ol>
    *    <li>name can't be just white space</li>
    *    <li>no spaces in name unless it is a hierarchy node</li>
    *    <li>can't duplicate existing name, case-insensitive</li>
    *    <li>content type names cannot contain reserved, unsafe url characters or .</li>
    * <ol>
    *
    * @param name Assumed not <code>null</code>.
    * @param existingNames Assumed not <code>null</code>.
    * @param ref Assumed not <code>null</code>, used for content type name
    * validation.
    * 
    * @return <code>true</code> if the name is OK, otherwise <code>false</code>. 
    */
   private boolean validateName(String name, Collection<String> existingNames,
         PSUiReference ref)
   {
      String errorMsg = PSUiUtils.validateObjectName(name.trim(), existingNames,
            m_isHierarchyNode);
      
      // name is a valid id
      {
         final String msg = getIdValidator(ref).validateId(name,
               PSMessages.getString("PSRenameAction.action.name.label"));
         if (msg != null)
         {
            if (errorMsg != null)
            {
               errorMsg += "\n" + msg;
            }
            else
            {
               errorMsg = msg;
            }
         }
      }

      if (errorMsg != null)
      {
         MessageDialog.openError(PSUiUtils.getShell(), 
               PSMessages.getString("PSRenameAction.validation.title"), 
               errorMsg);
         return false;
      }
      
      return true;
   }

   /**
    * Attempts to change the name of the design object wrapped by
    * <code>node</code>. If any problems occur, a message is shown to the
    * user. Certain errors mean the user cannot continue (such as 'locked by
    * another user'.)
    * 
    * @param node The node that is being edited. Assumed not <code>null</code>.
    * 
    * @param name The new name. Assumed not <code>null</code> or empty
    * 
    * @return <code>true</code> if the save was successful or the save failed
    * but the user asked not to continue (i.e. they are finished with the
    * editing operation), <code>false</code> otherwise.
    */
   private boolean save(final PSUiReference node, String name)
   {
      final String normalizedName = name.trim();
      final boolean[] finished = new boolean[1];
      finished[0] = true;
      final boolean[] unexpectedEnd = new boolean[1];
      unexpectedEnd[0] = true;
      try
      {
         // TODO remove println
         System.out.println("Renaming object to: " + normalizedName);
         BusyIndicator.showWhile(Display.getCurrent(), new Runnable()
         {
            public void run()
            {
               try
               {
                  final IPSReference ref = node.getReference();
                  final IPSCmsModel model = PSCoreFactory.getInstance()
                        .getModel(ref);
                  model.rename(ref, normalizedName);
               }
               catch (PSModelException e)
               {
                  // should never happen
                  unexpectedEnd[0] = false;
                  PSWorkbenchPlugin.handleException(null, null, null, e);
               }
               catch (PSAuthorizationException e)
               {
                  unexpectedEnd[0] = false;
                  MessageDialog.openError(PSUiUtils.getShell(),
                     PSMessages.getString("PSRenameAction.error.notAuthorized.title"),//$NON-NLS-1$
                     PSMessages.getString("PSRenameAction.error.notAuthorized.message"));//$NON-NLS-1$
               }
               catch (PSLockException e)
               {
                  unexpectedEnd[0] = false;
                  String msg = MessageFormat.format(PSMessages
                        .getString("PSRenameAction.error.locked.message"),//$NON-NLS-1$
                        new Object[] { e });
                  MessageDialog.openError(PSUiUtils.getShell(),
                     PSMessages.getString("PSRenameAction.error.locked.title"),//$NON-NLS-1$
                     msg);
               }
               catch (PSDuplicateNameException e)
               {
                  unexpectedEnd[0] = false;
                  MessageDialog.openError(PSUiUtils.getShell(),
                     PSMessages.getString("PSRenameAction.error.dupeName.title"),//$NON-NLS-1$
                     PSMessages.getString("PSRenameAction.error.dupeName.message"));//$NON-NLS-1$
                  finished[0] = false;
               }
            }
         });
         unexpectedEnd[0] = false;
      }
      finally
      {
         if (unexpectedEnd[0])
         {
            MessageDialog.openError(PSUiUtils.getShell(),
               PSMessages.getString("PSRenameAction.error.unexpectedException.title"),//$NON-NLS-1$
               PSMessages.getString("PSRenameAction.error.unexpectedException.message"));//$NON-NLS-1$
         }
      }
      return finished[0];
   }
   
   /**
    * @inheritDoc
    * 
    * The selection must contain exactly 1 element that is a design object to be
    * enabled.
    */
   @Override
   protected boolean updateSelection(IStructuredSelection sel)
   {
      //as documented by super class
      if (!super.updateSelection(sel))
         return false;
      
      if (sel.size() != 1)
         return false;

      //element must be a design object
      if (!(sel.getFirstElement() instanceof PSUiReference))
         return false;
      
      PSUiReference node = (PSUiReference) sel.getFirstElement();
      //can only rename the 'real' instance
      if (node.isReference())
         return false;
      
      if (node.getObjectType() == null)
         return false;
      
      if (node.getHandler() == null || !node.getHandler().supportsRename(node))
         return false;
      
      // Make sure the user has update permissions
      boolean canUpdate = false;
      //Folders are not secured and hence can be renamed
      if (node.isFolder() || !node.getHandler().supportsSecurity(node))
         canUpdate = true;
      else
      {
         for (int perm : node.getReference().getPermissions())
         {
            if (perm == PSPermissions.UPDATE.getOrdinal())
            {
               canUpdate = true;
               break;
            }
         }
      }
      return canUpdate;
   }

   /**
    * Get the name validator for the given UI component.
    * @param ref the UI component, assumed not <code>null</code>. 
    * @return the id validator. Never <code>null</code>.
    */
   private PSControlValueTextIdValidator getIdValidator(PSUiReference ref)
   {
      PSControlValidatorFactory factory = PSControlValidatorFactory
            .getInstance();
      return factory.getIdValidatorForType(ref.getObjectType().getPrimaryType());
   }
   
   /**
    * This is used to display the 'inline' edit box. Never <code>null</code>
    * after ctor.
    */
   private final StructuredViewer m_viewer;

   /**
    * This is used while an object name is being edited. It indicates whether
    * the node is hierarchical. This controls whether spaces are allowed in the
    * name.
    */
   boolean m_isHierarchyNode = false;
   
   /**
    * This is used while an object name is being edited. It is used to manage
    * behavior between different methods.
    */
   boolean m_ignoreFocusLost = false;
   
   /**
    * The paste action from the same group that this rename action
    * resides in. This is needed to work around an issue with paste
    * being ignored when in the rename text editor. Initialized in
    * the ctor, never <code>null</code> after that.
    */
   PSPasteAction m_pasteAction;
   
   /**
    * Flag indicating the enable state for this action at the time the
    * rename editor was created.
    */
   boolean m_lastEnableState;
   
   /**
    * The default text limit for an object name
    */
   private static int DEFAULT_MAX_NAME_LENGTH = 50;
}
