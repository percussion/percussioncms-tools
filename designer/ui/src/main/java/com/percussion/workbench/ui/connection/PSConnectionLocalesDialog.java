/******************************************************************************
 *
 * [ PSConnectionLocalesDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.connection;

import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.dialog.PSDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import java.util.Collections;
import java.util.List;

/**
 * Workbench always attempts to connect with "en_us". But if it is diabled, it 
 * fails to connect with the next active locale.
 *  
 * Connection locales dialog: This dialog is realized when a user connects with
 * with a default locale: en_us disabled, but  different locale[s] 
 * are enabled. This dialog is shown only when there is a choice of enabled 
 * locales
 *
 * @author Vamsi Nukala
 */
public class PSConnectionLocalesDialog extends PSDialog
      implements
         IPSUiConstants,
         ISelectionChangedListener
{
   /**
    * Creates new dialog.
    * @param parentShell the parent shell, or <code>null</code> to create
    * a top-level shell.
    * @param locales the list of valid locales never <code>null</code> or
    * empty
    */
   public PSConnectionLocalesDialog(Shell parentShell,
         List<String> locales)
   {
      super(parentShell);
      if (locales == null || locales.size()== 0)
      {
         throw new IllegalArgumentException(
               "locales may not be null or empty.");
      }
      setShellStyle(getShellStyle() | SWT.RESIZE);  
      m_locales = locales;
      Collections.sort(m_locales);
      m_curLocale = m_locales.get(0);
   }

   // see base class
   @Override
   protected Control createDialogArea(Composite parent)
   {
      final Composite container = (Composite) super.createDialogArea(parent);
      container.setSize(100, 50);
      container.setLayout(new FormLayout());
      
      final Label msgLabel = createMessageLabel(container);
      final Label localeLabel = createLocaleLabel(container, msgLabel);
      m_localeCombo = createLocalesCombo(container, localeLabel);
      
      return container;
   }

   /**
    * Creates a label for message field.
    * @param container the control container.
    * Assumed to be not <code>null</code>.
    */
   private Label createMessageLabel(final Composite container)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(getMessage("label.localeMessage"));
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      label.setLayoutData(formData);
      return label;
   }



   /**
    * Creates a label for locale field.
    * @param container the control container.Assumed never <code>null</code>
    * @param msgLabel the label control, assumed never <code>null</code>
    */
   private Label createLocaleLabel(final Composite container, Label msgLabel)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(getMessage("label.locale"));
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      
      formData.top = new FormAttachment(msgLabel,
            LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET*2, SWT.BOTTOM);
      formData.left = new FormAttachment(msgLabel,
            0, SWT.LEFT);
      
      label.setLayoutData(formData);
      return label;
   }
   
   /**
    * Creates locale combo field.
    * @param container the field container. Assumed not <code>null</code>.
    * Assumed to have {@link FormLayout}.
    * @param localeLabel the label for this control.
    * Assumed not <code>null</code>.
    */
   private ComboViewer createLocalesCombo(Composite container, Label localeLabel)
   {
      ComboViewer localesCombo = new ComboViewer(container, SWT.BORDER
            | SWT.READ_ONLY);
      final Combo combo = localesCombo.getCombo();
      final FormData formData = new FormData();
      formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      formData.top = new FormAttachment(localeLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData.left = new FormAttachment(localeLabel,
            LABEL_HSPACE_OFFSET, SWT.RIGHT);
      combo.setLayoutData(formData);
      localesCombo.add(m_locales.toArray());
      localesCombo.getCombo().select(0);
      localesCombo.addSelectionChangedListener(this);
      return localesCombo;
   }

   
   // see base class
   @Override
   protected void createButtonsForButtonBar(Composite parent)
   {
      // use our own label for Ok button to specify mnemonic,
      // so user can easy get out of the text field
      createButton(parent, IDialogConstants.OK_ID,
            PSMessages.getString("common.label.ok"), true);
      createButton(parent, IDialogConstants.CANCEL_ID,
            IDialogConstants.CANCEL_LABEL, false);
   }
   
   
   /**
    * In addition to super functionality sets correct size and lays out the
    * dialog. 
    * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createContents(Composite parent)
   {
      final Control contents = super.createContents(parent);

      final int x = getDisplayBounds().width / 4;
      final int y = getDisplayBounds().height / 3;
      final int width = getDisplayBounds().width / 4 ;
      final int height = getDisplayBounds().height / 5;
      getShell().setBounds(x, y, width, height);
      getShell().layout();
      return contents;
   }

   /**
    * In addition to the base functionality specified dialog title.
    * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(getMessage("title"));
   }

   /**
    * Current display bounds. Not <code>null</code>.
    */
   private Rectangle getDisplayBounds()
   {
      return getParentShell().getDisplay().getBounds();
   }

   /**
    * Retrieves message for this class.  
    * @param key part of the message key coming after the class name.
    * Assumed to be not <code>null</code>.
    */
   private String getMessage(final String key)
   {
      return PSMessages.getString("PSConnectionLocalesDialog." + key);
   }

   
   /**
    * Selection on the combo box has changed, save it.
    * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
    */
   public void selectionChanged(SelectionChangedEvent event)
   {
      if (event.getSource() == m_localeCombo)
      {
         int index = m_localeCombo.getCombo().getSelectionIndex();
         m_curLocale = m_localeCombo.getCombo().getItem(index);
      }
   }

   /**
    * Return the locale selected, never <code>null</code> or empty. This is
    * ONLY used when en_us locale(the default locale) has been disabled *AND* 
    * has more than one choice of enabled locales
    * @return the new locale 
    */
   public String getLocaleSelection()
   {
      return m_curLocale;
   }
   
   /**
    * Variable completions for the expression editor. Initialized in
    * constructor.
    */
   private final List<String> m_locales;
   
   
   /**
    * Locale seletion combo
    */
   private ComboViewer m_localeCombo; 

   
   /**
    * the current locale being selected from the combo, return it on okPressed
    * may be empty if no selection occurred.
    */
   private String m_curLocale = "";
   
}
