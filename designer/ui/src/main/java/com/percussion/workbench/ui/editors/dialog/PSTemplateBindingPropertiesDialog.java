/******************************************************************************
 *
 * [ PSTemplateBindingPropertiesDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.dialog;

import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.form.PSBindingEditor;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.List;

import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.TEXT_VSPACE_OFFSET;

/**
 * Template binding variable properties editor.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateBindingPropertiesDialog extends PSDialog
{
   /**
    * Creates new dialog.
    * @param parentShell the parent shell, or <code>null</code> to create
    * a top-level shell.
    * @param varCompletions variable completions for the expression editor.
    * Not <code>null</code>.
    * @param methodCompletions method completions for the expression editor.
    * Not <code>null</code>.
    * @param binding the binding to edit.
    * Not <code>null</code>.
    */
   public PSTemplateBindingPropertiesDialog(Shell parentShell,
         List<String[]> varCompletions, List<Object[]> methodCompletions,
         PSTemplateBinding binding)
   {
      super(parentShell);
      if (varCompletions == null)
      {
         throw new IllegalArgumentException(
               "Variable completions must be specified.");
      }
      if (methodCompletions == null)
      {
         throw new IllegalArgumentException(
               "Method completions must be specified.");
      }
      if (binding == null)
      {
         throw new IllegalArgumentException(
               "Binding to change must be specified");
      }
      setShellStyle(getShellStyle() | SWT.RESIZE);
      m_varCompletions = varCompletions;
      m_methodCompletions = methodCompletions;
      m_binding = binding;
   }

   // see base class
   @Override
   protected Control createDialogArea(Composite parent)
   {
      final Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new FormLayout());
      
      final Label nameLabel = createNameLabel(container);
      m_nameText = createNameText(container, nameLabel);
      m_nameText.setText(StringUtils.defaultString(m_binding.getVariable()));

      final Label expressionLabel = createExpressionLabel(container, m_nameText);
      m_expressionText = createExpressionText(container, expressionLabel);
      m_expressionText.getDocument().set(
            prepareExpressionForEditor(
                  StringUtils.defaultString(m_binding.getExpression())));

      return container;
   }

   /**
    * Creates label for the expression text field.
    * @param container the container composite. Assumed not <code>null</code>.
    * @param previousControl the control before this one.
    * Assumed not <code>null</code>.
    */
   private Label createExpressionLabel(Composite container,
         Control previousControl)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(getMessage("label.expressionEditor"));
      final FormData formData = new FormData();
      formData.top =
            new FormAttachment(previousControl, TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      label.setLayoutData(formData);
      return label;
   }

   /**
    * Creates name text field.
    * @param container the field container. Assumed not <code>null</code>.
    * Assumed to have {@link FormLayout}.
    * @param nameLabel the label for this control.
    * Assumed not <code>null</code>.
    */
   private Text createNameText(Composite container, Label nameLabel)
   {
      final Text nameText = new Text(container, SWT.BORDER);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(nameLabel, 0, SWT.LEFT);
      formData.top = new FormAttachment(nameLabel, 0, SWT.BOTTOM);
      formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      nameText.setLayoutData(formData);
      return nameText;
   }

   /**
    * Creates a label for variable name field.
    * @param container the control container.
    * Assumed to be not <code>null</code>.
    */
   private Label createNameLabel(final Composite container)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(getMessage("label.variableName"));
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      label.setLayoutData(formData);
      return label;
   }
   
   /**
    * Creates expression editor text control.
    * @param container the control containing this text.
    * Assumed not <code>null</code>.
    * @param expressionLabel the label for this text field.
    * Assumed not <code>null</code>. 
    */
   private PSBindingEditor createExpressionText(Composite container,
         Label expressionLabel)
   {
      final PSBindingEditor editor = new PSBindingEditor(container,
            SWT.BORDER | SWT.MULTI | SWT.WRAP);
      editor.setVarCompletions(m_varCompletions);
      editor.setMethodCompletions(m_methodCompletions);

      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      formData.top = new FormAttachment(expressionLabel, 0, SWT.BOTTOM);
      formData.bottom = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      final StyledText text = editor.getTextWidget();
      text.setLayoutData(formData);

      return editor;
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
      final int width = getDisplayBounds().width / 2;
      final int height = getDisplayBounds().height / 3;
      getShell().setBounds(x, y, width, height);
      getShell().layout();
      return contents;
   }
   
   /**
    * Wraps expression text to fit {@link #EXPRESSION_EDITOR_MARGIN} in
    * expression text field.
    * @param expression assumed to be not <code>null</code>.
    * @return the reformatted expression.
    */
   private String prepareExpressionForEditor(final String expression)
   {
      if (expression.length() > EXPRESSION_EDITOR_MARGIN)
      {
         String parts[] = expression.split("\\s");
         int cur = 0;
         StringBuilder exptodisplay = new StringBuilder();
         for(int i = 0; i < parts.length; i++)
         {
            String part = parts[i];
            if (cur + part.length() > EXPRESSION_EDITOR_MARGIN)
            {
               exptodisplay.append("\n");
               exptodisplay.append(part);
               cur = part.length();
            }
            else
            {
               if (i > 0) exptodisplay.append(' ');
               exptodisplay.append(part);
               cur += part.length() + 1;
            }
         }
         return exptodisplay.toString();
      }
      else
      {
         return expression;
      }
   }

   // see base class
   @Override
   protected void okPressed()
   {
      updateBindingName();
      updateBindingExpression();
      super.okPressed();
   }

   /**
    * Sets binding variable name from UI.
    */
   private void updateBindingName()
   {
      String name = m_nameText.getText().trim();
      if (StringUtils.isNotBlank(name))
      {
         if (!name.startsWith("$"))
         {
            name = "$" + name;
         }
      }
      m_binding.setVariable(name);
   }
   
   /**
    * Sets binding expression from UI.
    */
   private void updateBindingExpression()
   {
      String text = m_expressionText.getDocument().get();
      m_binding.setExpression(text);
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
      return PSMessages.getString("PSTemplateBindingPropertiesDialog." + key);
   }

   /**
    * Margin to wrap text for the expression editor.
    */
   private static final int EXPRESSION_EDITOR_MARGIN = 120;

   /**
    * Variable completions for the expression editor.
    * Initialized in constructor.
    */
   private final List<String[]> m_varCompletions;
   
   /**
    * Method completions for the expression editor.
    * Initialized in constructor.
    */
   private final List<Object[]> m_methodCompletions;

   /**
    * Test field to edit binding name.
    */
   private Text m_nameText;
   
   /**
    * Text viewer to edit JEXL expressions for the variable.
    */
   private PSBindingEditor m_expressionText;

   /**
    * Binding object to be edited by the dialog.
    * Initialized in constructor.
    */
   private final PSTemplateBinding m_binding;
}
