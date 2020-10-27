/******************************************************************************
 *
 * [ PSFieldRuleDetailsComposites.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSRule;
import com.percussion.design.objectstore.PSSingleHtmlParameter;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.rx.utils.PSContentTypeUtils;
import com.percussion.util.PSCollection;
import com.percussion.util.PSDataTypeConverter;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSAbstractLabelProvider;
import com.percussion.workbench.ui.controls.PSButtonFactory;
import com.percussion.workbench.ui.controls.PSCalendarDialog;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A convenient class to hold rule composites as inner classes that implement
 * IRuleComposite.
 * 
 */
public class PSFieldRuleDetailsComposites
{

   /**
    * A convenient interface for rule details composites to set and get data.
    */
   public static interface IRuleComposite
   {
      /**
       * Sets the rules data, if the rule is <code>null</code> then sets empty
       * values for the fields.
       * 
       * @param rule object of PSRule, may be <code>null</code>.
       */
      void setRuleData(PSRule rule);

      /**
       * Implementing composites should create the rule and return. Should
       * validate the fields appropriately and show the validation error message
       * and return <code>null</code>.
       * 
       * @return PSRule object corresponds to the selected composite or
       *         <code>null</code> incase of validation failure or unable to
       *         create the rule.
       */
      PSRule getRuleData();

      /**
       * Clear rules composite data.
       */
      void clearData();

      /**
       * The name of the composite to display.
       * 
       * @return String name of the composite.
       */
      String getDisplayName();

      /**
       * @return The display string for the rule.
       */
      String getRuleDisplayName(PSRule rule);

      /**
       * @return The name of the extension the composite supports.
       */
      String getExtensionName();
   }

   /**
    * A convenient interface for passing the rule details to rule composites
    */
   public static interface IRuleFieldDetails
   {
      /**
       * Should return the name of the current filed.
       * 
       * @return String, current field name
       */
      String getFieldName();

      /**
       * Should return the names of the fileds in the content editor def except
       * the current field name.
       * 
       * @return String[], array of field names except the current field name.
       */
      String[] getFieldNames();
   }

   /**
    * Inner class for the Number Range rule composite.
    */
   public static class NumberRangeRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      Text firstNumber;
      Text secondNumber;

      ComboViewer firstOpCV;
      Combo firstOp;

      ComboViewer secondOpCV;
      Combo secondOp;

      private IRuleFieldDetails m_fieldDetails;

      NumberRangeRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         firstNumber = new Text(this, SWT.BORDER);
         final FormData fd = new FormData();
         fd.top = new FormAttachment(0, 0);
         fd.left = new FormAttachment(10, 0);
         firstNumber.setLayoutData(fd);


         firstOpCV = new ComboViewer(this, SWT.BORDER | SWT.READ_ONLY);
         firstOp = firstOpCV.getCombo();

         final FormData fd1 = new FormData();
         fd1.top = new FormAttachment(firstNumber, LABEL_VSPACE_OFFSET,
               SWT.BOTTOM);
         fd1.left = new FormAttachment(20, 0);
         firstOp.setLayoutData(fd1);

         firstOpCV.setContentProvider(new PSDefaultContentProvider());
         firstOpCV.setLabelProvider(new StringLabelProvider());
         firstOpCV.setInput(Arrays.asList(ms_lessThanOps));
         firstOp.select(0);

         final Label fieldName = new Label(this, SWT.NONE);
         final FormData fd2 = new FormData();
         fd2.top = new FormAttachment(firstOp, 0, SWT.TOP);
         fd2.left = new FormAttachment(firstOp, LABEL_HSPACE_OFFSET, SWT.RIGHT);
         fieldName.setLayoutData(fd2);
         fieldName.setText(m_fieldDetails.getFieldName());

         secondOpCV = new ComboViewer(this, SWT.BORDER | SWT.READ_ONLY);
         secondOp = secondOpCV.getCombo();

         final FormData fd3 = new FormData();
         fd3.top = new FormAttachment(firstOp, 0, SWT.TOP);
         fd3.left = new FormAttachment(fieldName, LABEL_HSPACE_OFFSET,
               SWT.RIGHT);
         secondOp.setLayoutData(fd3);

         secondOpCV.setContentProvider(new PSDefaultContentProvider());
         secondOpCV.setLabelProvider(new StringLabelProvider());
         secondOpCV.setInput(Arrays.asList(ms_lessThanOps));
         secondOp.select(0);

         secondNumber = new Text(this, SWT.BORDER);
         final FormData fd4 = new FormData();
         fd4.top = new FormAttachment(firstOp, LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         fd4.left = new FormAttachment(10, 0);
         secondNumber.setLayoutData(fd4);

         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         if (rule != null)
         {
            String[] params = getExtensionParamValues(rule, false);
            String param1 = getParamValue(params, 1);
            firstNumber.setText(param1);
            String param2 = getParamValue(params, 2);
            if (param2.equalsIgnoreCase("true") //$NON-NLS-1$
                  || param2.equalsIgnoreCase("t") //$NON-NLS-1$
                  || param2.equalsIgnoreCase(STRING_YES))
               firstOp.select(1);
            else
               firstOp.select(0);

            String param3 = getParamValue(params, 3);
            secondNumber.setText(param3);
            String param4 = getParamValue(params, 4);
            if (param4.equalsIgnoreCase("true") //$NON-NLS-1$
                  || param4.equalsIgnoreCase("t") //$NON-NLS-1$
                  || param4.equalsIgnoreCase(STRING_YES))
               secondOp.select(1);
            else
               secondOp.select(0);
         }
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }

         PSExtensionParamValue param0 = null;
         PSExtensionParamValue param1 = null;
         PSExtensionParamValue param2 = null;
         PSExtensionParamValue param3 = null;
         PSExtensionParamValue param4 = null;
         param0 = new PSExtensionParamValue(new PSSingleHtmlParameter(
               m_fieldDetails.getFieldName()));
         String firstNumberVal = firstNumber.getText();
         String secondNumberVal = secondNumber.getText();
         if (StringUtils.isEmpty(firstNumberVal)
               && StringUtils.isEmpty(secondNumberVal))
         {
            String[] args =
            {RULE_NUMBER_RANGE};
            showRuleCreationError(getShell(),
                  ATLEAST_ONE_VALID_PARAMETER_REQUIRED, args);
            return null;
         }
         if (!StringUtils.isEmpty(firstNumberVal))
         {
            try
            {
               Integer.parseInt(firstNumberVal);
            }
            catch(NumberFormatException e)
            {
               String msg = PSMessages
               .getString("PSFieldRuleDetailsComposites.error.msg.invalidfirstnumber"); //$NON-NLS-1$
               showRuleCreationError(getShell(), msg, null);
               return null;
            }
            param1 = new PSExtensionParamValue(new PSTextLiteral(firstNumberVal));

            if (firstOp.getText() != null
                  && firstOp.getText().equals(OP_LESSTHAN_EQUAL))
               param2 = new PSExtensionParamValue(new PSTextLiteral(STRING_YES));
            else
               param2 = new PSExtensionParamValue(new PSTextLiteral(STRING_NO));
         }
         else
         {
            param1 = new PSExtensionParamValue(new PSTextLiteral("")); //$NON-NLS-1$
            param2 = new PSExtensionParamValue(new PSTextLiteral("")); //$NON-NLS-1$
         }

         if (!StringUtils.isEmpty(secondNumberVal))
         {
            try
            {
               Integer.parseInt(secondNumberVal);
            }
            catch(NumberFormatException e)
            {
               String msg = PSMessages
               .getString("PSFieldRuleDetailsComposites.error.msg.invalidsecondnumber"); //$NON-NLS-1$
               showRuleCreationError(getShell(), msg, null);
               return null;
            }
            param3 = new PSExtensionParamValue(new PSTextLiteral(
                  secondNumberVal));
            if (secondOp.getText() != null
                  && secondOp.getText().equals(OP_LESSTHAN_EQUAL))
               param4 = new PSExtensionParamValue(new PSTextLiteral(STRING_YES));
            else
               param4 = new PSExtensionParamValue(new PSTextLiteral(STRING_NO));
         }
         else
         {
            param3 = new PSExtensionParamValue(new PSTextLiteral("")); //$NON-NLS-1$
            param4 = new PSExtensionParamValue(new PSTextLiteral("")); //$NON-NLS-1$
         }
         PSExtensionParamValue[] params = new PSExtensionParamValue[5];
         params[0] = param0;
         params[1] = param1;
         params[2] = param2;
         params[3] = param3;
         params[4] = param4;
         setExtensionParams(rule, params);
         return rule;
      }

      public void clearData()
      {
         firstNumber.setText(""); //$NON-NLS-1$
         secondNumber.setText(""); //$NON-NLS-1$
         firstOp.select(0);
         secondOp.select(0);
      }

      public String getDisplayName()
      {
         return RULE_NUMBER_RANGE;
      }

      public String getExtensionName()
      {
         return RULE_NUMBER_RANGE_EXTNAME;
      }

      public String getRuleDisplayName(PSRule rule)
      {
         String[] params = getExtensionParamValues(rule, true);
         String dn = getDisplayName() + "("; //$NON-NLS-1$
         if (!StringUtils.isEmpty(params[1]))
         {
            dn += params[1] + " "; //$NON-NLS-1$
            if (params[2].equalsIgnoreCase(STRING_YES))
               dn += OP_LESSTHAN_EQUAL + " "; //$NON-NLS-1$
            else
               dn += OP_LESSTHAN + " "; //$NON-NLS-1$
         }
         dn += params[0];
         if (!StringUtils.isEmpty(params[3]))
         {
            if (params[4].equalsIgnoreCase(STRING_YES))
               dn += " " + OP_LESSTHAN_EQUAL; //$NON-NLS-1$
            else
               dn += " " + OP_LESSTHAN; //$NON-NLS-1$
            dn += " " + params[3]; //$NON-NLS-1$
         }
         dn += ")"; //$NON-NLS-1$

         return dn;
      }

   }

   /**
    * Inner class for the Date Range rule composite.
    */
   public static class DateRangeRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      ComboViewer firstDateCV;

      Combo firstDate;

      ComboViewer firstOpCV;

      Combo firstOp;

      ComboViewer secondDateCV;

      Combo secondDate;

      ComboViewer secondOpCV;

      Combo secondOp;

      private final DateFormat m_formatter = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$ 

      private IRuleFieldDetails m_fieldDetails;

      DateRangeRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         firstDateCV = new ComboViewer(this, SWT.BORDER);
         firstDate = firstDateCV.getCombo();
         final FormData fd = new FormData();
         fd.top = new FormAttachment(0,
               LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
         fd.left = new FormAttachment(10, 0);
         firstDate.setLayoutData(fd);

         firstDateCV.setContentProvider(new PSDefaultContentProvider());
         firstDateCV.setLabelProvider(new StringLabelProvider());
         firstDateCV.setSorter(new ViewerSorter());
         firstDateCV.setInput(Arrays.asList(m_fieldDetails.getFieldNames()));

         Button calBtn1 = PSButtonFactory.createCalendarButton(this);
         final FormData fda = new FormData();
         fda.top = new FormAttachment(firstDate,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         fda.left = new FormAttachment(firstDate, LABEL_HSPACE_OFFSET,
               SWT.RIGHT);
         calBtn1.setLayoutData(fda);
         calBtn1.addSelectionListener(new SelectionAdapter()
         {
            @Override
            public void widgetSelected( 
                  @SuppressWarnings("unused") SelectionEvent e)
            {
               final PSCalendarDialog caldlg = new PSCalendarDialog(
                     getDisplay());
               caldlg.addDateChangedListener(new SWTCalendarListener()
               {
                  public void dateChanged(SWTCalendarEvent calendarEvent)
                  {
                     firstDate.setText(m_formatter.format(calendarEvent
                           .getCalendar().getTime()));
                  }
               });

               if (StringUtils.isNotBlank(firstDate.getText()))
               {
                  try
                  {
                     caldlg.setDate(m_formatter.parse(firstDate.getText()));
                  }
                  catch (ParseException ignore)
                  {
                  }
               }
               caldlg.open();
            }
         });
         firstOpCV = new ComboViewer(this, SWT.BORDER | SWT.READ_ONLY);
         firstOp = firstOpCV.getCombo();
         final FormData fd1 = new FormData();
         fd1.top = new FormAttachment(firstDate, LABEL_VSPACE_OFFSET,
               SWT.BOTTOM);
         fd1.left = new FormAttachment(20, 0);
         firstOp.setLayoutData(fd1);

         firstOpCV.setContentProvider(new PSDefaultContentProvider());
         firstOpCV.setLabelProvider(new StringLabelProvider());
         firstOpCV.setInput(Arrays.asList(ms_lessThanOps));
         firstOp.select(0);

         final Label fieldName = new Label(this, SWT.NONE);
         final FormData fd2 = new FormData();
         fd2.top = new FormAttachment(firstOp, 0, SWT.TOP);
         fd2.left = new FormAttachment(firstOp, LABEL_HSPACE_OFFSET, SWT.RIGHT);
         fieldName.setLayoutData(fd2);
         fieldName.setText(m_fieldDetails.getFieldName());

         secondOpCV = new ComboViewer(this, SWT.BORDER | SWT.READ_ONLY);
         secondOp = secondOpCV.getCombo();
         final FormData fd3 = new FormData();
         fd3.top = new FormAttachment(firstOp, 0, SWT.TOP);
         fd3.left = new FormAttachment(fieldName, LABEL_HSPACE_OFFSET,
               SWT.RIGHT);
         secondOp.setLayoutData(fd3);

         secondOpCV.setContentProvider(new PSDefaultContentProvider());
         secondOpCV.setLabelProvider(new StringLabelProvider());
         secondOpCV.setInput(Arrays.asList(ms_lessThanOps));
         secondOp.select(0);

         secondDateCV = new ComboViewer(this, SWT.BORDER);
         secondDate = secondDateCV.getCombo();
         final FormData fd4 = new FormData();
         fd4.top = new FormAttachment(firstOp, LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         fd4.left = new FormAttachment(10, 0);
         secondDate.setLayoutData(fd4);

         secondDateCV.setContentProvider(new PSDefaultContentProvider());
         secondDateCV.setLabelProvider(new StringLabelProvider());
         secondDateCV.setSorter(new ViewerSorter());
         secondDateCV.setInput(Arrays.asList(m_fieldDetails.getFieldNames()));

         Button calBtn2 = PSButtonFactory.createCalendarButton(this);
         final FormData fd3a = new FormData();
         fd3a.top = new FormAttachment(secondDate,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         fd3a.left = new FormAttachment(secondDate, LABEL_HSPACE_OFFSET,
               SWT.RIGHT);
         calBtn2.setLayoutData(fd3a);
         calBtn2.addSelectionListener(new SelectionAdapter()
         {
            @Override
            public void widgetSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
            {
               final PSCalendarDialog caldlg = new PSCalendarDialog(
                     getDisplay());
               caldlg.addDateChangedListener(new SWTCalendarListener()
               {
                  public void dateChanged(SWTCalendarEvent calendarEvent)
                  {
                     secondDate.setText(m_formatter.format(calendarEvent
                           .getCalendar().getTime()));
                  }
               });

               if (StringUtils.isNotBlank(secondDate.getText()))
               {
                  try
                  {
                     caldlg.setDate(m_formatter.parse(secondDate.getText()));
                  }
                  catch (ParseException ignore)
                  {
                  }
               }
               caldlg.open();
            }
         });

         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         if (rule != null)
         {
            String[] params = getExtensionParamValues(rule, false);
            String param1 = getParamValue(params, 1);
            // Check whether we have value here are not and if we have a value
            // and if it is not a number then
            if (!(StringUtils.isEmpty(param1)))
            {
               if (PSDataTypeConverter.parseStringToDate(param1) != null)
                  firstDate.setText(param1);
               else
                  firstDate.select(getItemIndex(firstDate.getItems(), param1));
               String param2 = getParamValue(params, 2);
               if (param2.equalsIgnoreCase("true") //$NON-NLS-1$
                     || param2.equalsIgnoreCase("t") //$NON-NLS-1$
                     || param2.equalsIgnoreCase(STRING_YES))
                  firstOp.select(1);
               else
                  firstOp.select(0);
            }
            String param3 = getParamValue(params, 3);
            if (!(StringUtils.isEmpty(param3)))
            {
               if (PSDataTypeConverter.parseStringToDate(param3) != null)
                  secondDate.setText(param3);
               else
                  secondDate
                        .select(getItemIndex(secondDate.getItems(), param3));
               String param4 = getParamValue(params, 4);
               if (param4.equalsIgnoreCase("true") //$NON-NLS-1$
                     || param4.equalsIgnoreCase("t") //$NON-NLS-1$
                     || param4.equalsIgnoreCase(STRING_YES))
                  secondOp.select(1);
               else
                  secondOp.select(0);
            }

         }
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }

         PSExtensionParamValue param0 = null;
         PSExtensionParamValue param1 = null;
         PSExtensionParamValue param2 = null;
         PSExtensionParamValue param3 = null;
         PSExtensionParamValue param4 = null;
         param0 = new PSExtensionParamValue(new PSSingleHtmlParameter(
               m_fieldDetails.getFieldName()));
         String firstDateVal = firstDate.getText();
         String secondDateVal = secondDate.getText();

         if (StringUtils.isEmpty(firstDateVal)
               && StringUtils.isEmpty(secondDateVal))
         {
            String[] args = {getDisplayName()};
            showRuleCreationError(getShell(),
                  ATLEAST_ONE_VALID_PARAMETER_REQUIRED, args);
            return null;
         }

         if (!StringUtils.isEmpty(firstDateVal))
         {
            if (PSContentEditorDefinition.getItemIndex(m_fieldDetails
                  .getFieldNames(), firstDateVal) == -1)
            {
               if (PSDataTypeConverter.parseStringToDate(firstDateVal) == null)
               {
                  String msg = PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.invalidfirstdate"); //$NON-NLS-1$
                  showRuleCreationError(getShell(), msg, null);
                  return null;
               }

               param1 = new PSExtensionParamValue(new PSTextLiteral(firstDate
                     .getText()));
            }
            else
            {
               param1 = new PSExtensionParamValue(new PSSingleHtmlParameter(
                     firstDateVal));
            }
            if (firstOp.getText() != null
                  && firstOp.getText().equals(OP_LESSTHAN_EQUAL))
               param2 = new PSExtensionParamValue(new PSTextLiteral(STRING_YES));
            else
               param2 = new PSExtensionParamValue(new PSTextLiteral(STRING_NO));
         }
         else
         {
            param1 = new PSExtensionParamValue(new PSTextLiteral("")); //$NON-NLS-1$
            param2 = new PSExtensionParamValue(new PSTextLiteral("")); //$NON-NLS-1$
         }

         if (!StringUtils.isEmpty(secondDateVal))
         {
            if (PSContentEditorDefinition.getItemIndex(m_fieldDetails
                  .getFieldNames(), secondDateVal) == -1)
            {
               if (PSDataTypeConverter.parseStringToDate(secondDateVal) == null)
               {
                  String msg = PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.invalidseconddate"); //$NON-NLS-1$
                  showRuleCreationError(getShell(), msg, null);
                  return null;
               }
               param3 = new PSExtensionParamValue(new PSTextLiteral(secondDate
                     .getText()));
            }
            else
            {
               param3 = new PSExtensionParamValue(new PSSingleHtmlParameter(
                     secondDateVal));
            }
            if (secondOp.getText() != null
                  && secondOp.getText().equals(OP_LESSTHAN_EQUAL))
               param4 = new PSExtensionParamValue(new PSTextLiteral(STRING_YES));
            else
               param4 = new PSExtensionParamValue(new PSTextLiteral(STRING_NO));
         }
         else
         {
            param3 = new PSExtensionParamValue(new PSTextLiteral("")); //$NON-NLS-1$
            param4 = new PSExtensionParamValue(new PSTextLiteral("")); //$NON-NLS-1$
         }
         PSExtensionParamValue[] params = new PSExtensionParamValue[5];
         params[0] = param0;
         params[1] = param1;
         params[2] = param2;
         params[3] = param3;
         params[4] = param4;
         setExtensionParams(rule, params);
         return rule;
      }

      public void clearData()
      {
         firstDate.select(-1);
         firstDate.setText(""); //$NON-NLS-1$
         secondDate.select(-1);
         secondDate.setText(""); //$NON-NLS-1$
         firstOp.select(0);
         secondOp.select(0);
      }

      public String getDisplayName()
      {
         return RULE_DATE_RANGE;
      }

      public String getExtensionName()
      {
         return RULE_DATE_RANGE_EXTNAME;
      }

      public String getRuleDisplayName(PSRule rule)
      {
         String[] params = getExtensionParamValues(rule, true);
         String dn = RULE_DATE_RANGE + "("; //$NON-NLS-1$
         if (!StringUtils.isEmpty(params[1]))
         {
            dn += params[1] + " "; //$NON-NLS-1$
            if (params[2].equalsIgnoreCase(STRING_YES))
               dn += OP_LESSTHAN_EQUAL + " "; //$NON-NLS-1$
            else
               dn += OP_LESSTHAN + " "; //$NON-NLS-1$
         }
         dn += params[0];
         if (!StringUtils.isEmpty(params[3]))
         {
            if (params[4].equalsIgnoreCase(STRING_YES))
               dn += " " + OP_LESSTHAN_EQUAL; //$NON-NLS-1$
            else
               dn += " " + OP_LESSTHAN; //$NON-NLS-1$
            dn += " " + params[3] + " "; //$NON-NLS-1$ //$NON-NLS-2$
         }
         dn += ")"; //$NON-NLS-1$

         return dn;
      }

   }

   /**
    * Inner class for the String Length rule composite.
    */
   public static class StringLengthRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      Text minLength;
      Text maxLength;
      private IRuleFieldDetails m_fieldDetails;

      StringLengthRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());

         final Label minLengthLabel = new Label(this, SWT.NONE | SWT.RIGHT);
         final FormData fd = new FormData();
         fd.top = new FormAttachment(0,
               LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
         fd.left = new FormAttachment(10, 0);
         fd.right = new FormAttachment(40, 0);
         minLengthLabel.setLayoutData(fd);
         minLengthLabel.setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.minlength")); //$NON-NLS-1$

         minLength = new Text(this, SWT.BORDER);
         final FormData fd1 = new FormData();
         fd1.top = new FormAttachment(0, 0);
         fd1.left = new FormAttachment(minLengthLabel, LABEL_HSPACE_OFFSET, 0);
         minLength.setLayoutData(fd1);

         final Label maxLengthLabel = new Label(this, SWT.NONE | SWT.RIGHT);
         final FormData fd2 = new FormData();
         fd2.top = new FormAttachment(minLengthLabel, LABEL_VSPACE_OFFSET,
               SWT.BOTTOM);
         fd2.left = new FormAttachment(10, 0);
         fd2.right = new FormAttachment(40, 0);
         maxLengthLabel.setLayoutData(fd2);
         maxLengthLabel.setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.maxlength")); //$NON-NLS-1$

         maxLength = new Text(this, SWT.BORDER);
         final FormData fd3 = new FormData();
         fd3.top = new FormAttachment(maxLengthLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         fd3.left = new FormAttachment(maxLengthLabel, LABEL_HSPACE_OFFSET,
               SWT.RIGHT);
         maxLength.setLayoutData(fd3);

         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         if (rule != null)
         {
            String[] params = getExtensionParamValues(rule, false);
            String param1 = getParamValue(params, 1);
            minLength.setText(param1);

            String param2 = getParamValue(params, 2);
            maxLength.setText(param2);
         }
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }

         PSExtensionParamValue param0 = null;
         PSExtensionParamValue param1 = null;
         PSExtensionParamValue param2 = null;
         param0 = new PSExtensionParamValue(new PSSingleHtmlParameter(
               m_fieldDetails.getFieldName()));
         String minLen = minLength.getText();
         String maxLen = maxLength.getText();
         
         if (StringUtils.isEmpty(minLen) && StringUtils.isEmpty(maxLen))
         {
            String[] args =
            {RULE_STRING_LENGTH};
            showRuleCreationError(getShell(),
                  ATLEAST_ONE_VALID_PARAMETER_REQUIRED, args);
            return null;
         }

         if (!StringUtils.isEmpty(minLen))
         {
            if (!StringUtils.isNumeric(minLen))
            {
               String msg = PSMessages
                     .getString("PSFieldRuleDetailsComposites.error.msg.invalidminlength"); //$NON-NLS-1$
               showRuleCreationError(getShell(), msg, null);
               return null;
            }
            param1 = new PSExtensionParamValue(new PSTextLiteral(minLength
                  .getText()));
         }
         else
         {
            param1 = new PSExtensionParamValue(new PSTextLiteral("")); //$NON-NLS-1$
         }

         if (!StringUtils.isEmpty(maxLen))
         {
            if (!StringUtils.isNumeric(maxLen))
            {
               String msg = PSMessages
                     .getString("PSFieldRuleDetailsComposites.error.msg.invalidmaxlength"); //$NON-NLS-1$
               showRuleCreationError(getShell(), msg, null);
               return null;
            }
            param2 = new PSExtensionParamValue(new PSTextLiteral(maxLength
                  .getText()));
         }
         else
         {
            param2 = new PSExtensionParamValue(new PSTextLiteral("")); //$NON-NLS-1$
         }
         PSExtensionParamValue[] params = new PSExtensionParamValue[3];
         params[0] = param0;
         params[1] = param1;
         params[2] = param2;
         setExtensionParams(rule, params);
         return rule;
      }

      public void clearData()
      {
         minLength.setText(""); //$NON-NLS-1$
         maxLength.setText(""); //$NON-NLS-1$
      }

      public String getDisplayName()
      {
         return RULE_STRING_LENGTH;
      }

      public String getExtensionName()
      {
         return RULE_STRING_LENGTH_EXTNAME;
      }

      public String getRuleDisplayName(PSRule rule)
      {
         String[] params = getExtensionParamValues(rule, true);
         String dn = RULE_STRING_LENGTH + "("; //$NON-NLS-1$
         if (!StringUtils.isEmpty(params[1]))
         {
            dn += PSMessages.getString("PSFieldRuleDetailsComposites.label.min") + params[1]; //$NON-NLS-1$
         }
         if (!StringUtils.isEmpty(params[2]))
         {
            if (!StringUtils.isEmpty(params[1]))
            {
               dn += " "; //$NON-NLS-1$
            }
            dn += PSMessages.getString("PSFieldRuleDetailsComposites.label.max") + params[2]; //$NON-NLS-1$
         }
         dn += ")"; //$NON-NLS-1$

         return dn;
      }

   }

   /**
    * Inner class for the Regular Expression rule composite.
    */
   public static class RegExRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants,
            ISelectionChangedListener
   {
      ComboViewer regExCC;

      Text regEx;

      private IRuleFieldDetails m_fieldDetails;

      RegExRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         final Label preDefLabel = new Label(this, SWT.NONE);
         final FormData fd = new FormData();
         fd.top = new FormAttachment(0, 0);
         fd.left = new FormAttachment(10, 0);
         fd.right = new FormAttachment(100, 0);
         preDefLabel.setLayoutData(fd);
         preDefLabel.setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.predefined")); //$NON-NLS-1$

         regExCC = new ComboViewer(this, SWT.BORDER | SWT.READ_ONLY);
         final FormData fd1 = new FormData();
         fd1.top = new FormAttachment(0, 0);
         fd1.left = new FormAttachment(10, 0);
         fd1.top = new FormAttachment(preDefLabel,
               LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
         regExCC.getCombo().setLayoutData(fd1);
         regExCC.setContentProvider(new PSDefaultContentProvider());
         regExCC.setLabelProvider(new StringLabelProvider());
         regExCC.setInput(ms_regularExps.keySet());
         regExCC.addSelectionChangedListener(this);
         final Label regExLabel = new Label(this, SWT.NONE);
         final FormData fd2 = new FormData();
         fd2.top = new FormAttachment(regExCC.getCombo(),
               LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
         fd2.left = new FormAttachment(10, 0);
         fd2.right = new FormAttachment(100, 0);
         regExLabel.setLayoutData(fd2);
         regExLabel.setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.regularexpression")); //$NON-NLS-1$

         regEx = new Text(this, SWT.BORDER);
         final FormData fd3 = new FormData();
         fd3.top = new FormAttachment(regExLabel,
               LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
         fd3.left = new FormAttachment(10, 0);
         fd3.height = DESCRIPTION_FIELD_HEIGHT / 2;
         fd3.right = new FormAttachment(100, 0);
         regEx.setLayoutData(fd3);
         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         if (rule != null)
         {
            String[] params = getExtensionParamValues(rule, false);
            String param1 = getParamValue(params, 1);
            regEx.setText(param1);
         }
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }
         try
         {
            Pattern.compile(regEx.getText());
         }
         catch (PatternSyntaxException p)
         {
            showRuleCreationError(getShell(),
                  PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.resyntaxerror") //$NON-NLS-1$
                        + p.getLocalizedMessage(), null);
            return null;
         }
         PSExtensionParamValue param0 = null;
         PSExtensionParamValue param1 = null;
         param0 = new PSExtensionParamValue(new PSSingleHtmlParameter(
               m_fieldDetails.getFieldName()));
         param1 = new PSExtensionParamValue(new PSTextLiteral(regEx.getText()));
         PSExtensionParamValue[] params = new PSExtensionParamValue[2];
         params[0] = param0;
         params[1] = param1;
         setExtensionParams(rule, params);
         return rule;
      }

      public void clearData()
      {
         regEx.setText(""); //$NON-NLS-1$
      }

      /**
       * Map of predefined regular expressions.
       */
      private static final Map<String, String> ms_regularExps = new HashMap<String, String>();

      static
      {
         ms_regularExps.put(PSMessages.getString("PSFieldRuleDetailsComposites.label.floatingpointnumbers"), //$NON-NLS-1$
               "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?"); //$NON-NLS-1$
         ms_regularExps.put(PSMessages.getString("PSFieldRuleDetailsComposites.label.email"), ".+@.+\\.[a-z]+"); //$NON-NLS-1$ //$NON-NLS-2$
         ms_regularExps.put(PSMessages.getString("PSFieldRuleDetailsComposites.label.phonenumbers"), "(\\d-)?(\\d{3}-)?\\d{3}-\\d{4}"); //$NON-NLS-1$ //$NON-NLS-2$
      }

      public String getDisplayName()
      {
         return RULE_REGULAR_EXPRESSION;
      }

      public String getExtensionName()
      {
         return RULE_REGULAR_EXPRESSION_EXTNAME;
      }

      public String getRuleDisplayName(PSRule rule)
      {
         String[] params = getExtensionParamValues(rule, true);
         return RULE_REGULAR_EXPRESSION + "(" + params[1] + ")"; //$NON-NLS-1$ //$NON-NLS-2$
      }

      public void selectionChanged(
            @SuppressWarnings("unused") SelectionChangedEvent event)
      {
         String re = regExCC.getCombo().getText();
         regEx.setText(ms_regularExps.get(re));
      }

   }

   /**
    * Inner class for the JEXL rule composite.
    */
   public static class JEXLRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      Text jexlText;

      private IRuleFieldDetails m_fieldDetails;

      JEXLRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         jexlText = new Text(this, SWT.BORDER);
         final FormData fd = new FormData();
         fd.top = new FormAttachment(0, 0);
         fd.left = new FormAttachment(10, 0);
         fd.right = new FormAttachment(100, 0);
         fd.height = DESCRIPTION_FIELD_HEIGHT;
         jexlText.setLayoutData(fd);
         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         if (rule != null)
         {
            String[] params = getExtensionParamValues(rule, false);
            String param1 = getParamValue(params, 1);
            jexlText.setText(param1);
         }
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }

         PSExtensionParamValue param0 = null;
         PSExtensionParamValue param1 = null;
         param0 = new PSExtensionParamValue(new PSSingleHtmlParameter(
               m_fieldDetails.getFieldName()));
         param1 = new PSExtensionParamValue(new PSTextLiteral(jexlText
               .getText()));
         PSExtensionParamValue[] params = new PSExtensionParamValue[2];
         params[0] = param0;
         params[1] = param1;
         setExtensionParams(rule, params);
         return rule;
      }

      public void clearData()
      {
         jexlText.setText(""); //$NON-NLS-1$
      }

      public String getDisplayName()
      {
         return RULE_JEXL;
      }

      public String getExtensionName()
      {
         return RULE_JEXL_EXTNAME;
      }

      public String getRuleDisplayName(PSRule rule)
      {
         String[] params = getExtensionParamValues(rule, true);
         return RULE_JEXL + "(" + params[1] + ")"; //$NON-NLS-1$ //$NON-NLS-2$
      }

   }

   /**
    * Inner class for the Conditional rule composite.
    */
   public static class ConditionalRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {

      private PSConditionalsComposite condComp;

      @SuppressWarnings("unused") //$NON-NLS-1$
      private IRuleFieldDetails m_fieldDetails;

      ConditionalRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         condComp = new PSConditionalsComposite(this, SWT.NONE);
         final FormData fd = new FormData();
         fd.top = new FormAttachment(0, 0);
         fd.left = new FormAttachment(0, 0);
         fd.right = new FormAttachment(100, 0);
         fd.bottom = new FormAttachment(100, 0);
         condComp.setLayoutData(fd);
         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         condComp.setRuleData(rule);
      }

      public PSRule getRuleData()
      {
         return condComp.getRuleData();
      }

      public void clearData()
      {
         condComp.clearConditionals();
      }

      public String getDisplayName()
      {
         return RULE_CONDITIONAL;
      }

      public String getExtensionName()
      {
         return RULE_CONDITIONAL;
      }

      public String getRuleDisplayName(PSRule rule)
      {
         String dn = RULE_CONDITIONAL + ": "; //$NON-NLS-1$
         Iterator iter = rule.getConditionalRules();
         while (iter.hasNext())
         {
            dn += ((PSConditional) iter.next()).toString() + " "; //$NON-NLS-1$
         }
         // Remove last AND or OR boolean
         if (dn.endsWith(" AND ")) //$NON-NLS-1$
            dn = dn.substring(0, dn.length() - 5);
         else if (dn.endsWith(" OR ")) //$NON-NLS-1$
            dn = dn.substring(0, dn.length() - 4);
         return dn;
      }
   }

   /**
    * Inner class for the Extension rule composite.
    */
   public static class ExtnRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      public ComboViewer m_extensionCV;

      public Button m_extParamsButton;

      private PSExtensionDefSelector m_extDefControl;

      @SuppressWarnings("unused") //$NON-NLS-1$
      private IRuleFieldDetails m_fieldDetails;

      /**
       * Construct this composite
       * @param parent The parent composite, see 
       * {@link Composite#Composite(Composite, int)}
       * @param style The style to use, see 
       * {@link Composite#Composite(Composite, int)}
       * @param rule The rule, may be <code>null</code> if creating new.
       * @param fieldDetails The details of the field for which the rule is
       * being created, may not be <code>null</code>.
       * @param ifname The extension interface name to include in the
       * possible choices for the rule definition, may be <code>null</code> 
       * or empty.  Extensions matching both this interface (if supplied) as 
       * well as the {@link IPSUdfProcessor} are listed also see
       * {@link #getExtensions(Set)} as some hard coded interfaces are filtered.
       */
      ExtnRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails, String ifname)
      {
         super(parent, style);
         if (fieldDetails == null)
            throw new IllegalArgumentException("fieldDetails may not be null");
         
         
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());

         final Label nameLabel = new Label(this, SWT.NONE | SWT.RIGHT);
         final FormData fd = new FormData();
         fd.top = new FormAttachment(0,
               LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
         fd.left = new FormAttachment(10, 0);
         fd.right = new FormAttachment(30, 0);
         nameLabel.setLayoutData(fd);
         nameLabel.setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.name")); //$NON-NLS-1$
         boolean showAllExtns = PSWorkbenchPlugin.getDefault().getPreferences()
               .isShowLegacyInterfacesForExtns();
         Set<String> ifnames = new HashSet<String>();
         
         if (!StringUtils.isBlank(ifname))
            ifnames.add(ifname);
         if ( showAllExtns)
            ifnames.add(IPSUdfProcessor.class.getName());
         
         m_extDefControl = new PSExtensionDefSelector(this, SWT.NONE,
               getExtRuleExtensions(ifnames));
         final FormData fd1 = new FormData();
         fd1.top = new FormAttachment(0, 0);
         fd1.left = new FormAttachment(nameLabel, LABEL_HSPACE_OFFSET,
               SWT.RIGHT);
         fd1.right = new FormAttachment(100, 0);
         m_extDefControl.setLayoutData(fd1);

         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         if (rule != null)
         {
            PSExtensionCallSet callSet = rule.getExtensionRules();
            Iterator iter = callSet.iterator();
            if (iter.hasNext())
            {
               PSExtensionCall call = (PSExtensionCall) iter.next();
               m_extDefControl.setExtensionCall(call);
            }
         }
      }

      public PSRule getRuleData()
      {
         PSRule rule = null;
         PSExtensionCall ecall = m_extDefControl.getExtensionCall();
         if (ecall != null)
         {
            PSExtensionCallSet eset = new PSExtensionCallSet();
            eset.add(ecall);
            rule = new PSRule(eset);
         }
         if (rule == null)
         {
            // This should not happen as we just catalogged and user is going to
            // select
            // from a catalogged extensions.
            String msg = PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.rulecreationfailure"); //$NON-NLS-1$
            showRuleCreationError(getShell(), msg, null);
            return null;
         }
         return rule;
      }

      public void clearData()
      {
         m_extDefControl.clearExtensionCall();
      }

      public String getDisplayName()
      {
         return RULE_EXTENSION;
      }

      public String getExtensionName()
      {
         return RULE_EXTENSION;
      }

      public String getRuleDisplayName(PSRule rule)
      {
         String dn = RULE_EXTENSION + ": "; //$NON-NLS-1$
         Iterator eiter = rule.getExtensionRules().iterator();
         if (eiter.hasNext())
         {
            PSExtensionCall ext = (PSExtensionCall) eiter.next();
            dn += ext.toString();
         }
         return dn;
      }

   }

   /**
    * Inner class for the Required rule composite.
    */
   public static class LowercaseRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      private IRuleFieldDetails m_fieldDetails;

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      LowercaseRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         createNoParamsLabel(this);
         setRuleData(rule);
      }

      public void setRuleData(
            @SuppressWarnings("unused") PSRule rule)
      {
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }
         PSExtensionParamValue param0 = new PSExtensionParamValue(
               new PSSingleHtmlParameter(m_fieldDetails.getFieldName()));
         PSExtensionParamValue[] params = new PSExtensionParamValue[1];
         params[0] = param0;
         setExtensionParams(rule, params);
         return rule;
      }

      public String getDisplayName()
      {
         return RULE_TO_LOWER;
      }

      public void clearData()
      {
         // There are no fields to clear for this rule composite.
      }

      public String getExtensionName()
      {
         return RULE_TO_LOWER_EXTNAME;
      }

      public String getRuleDisplayName(
            @SuppressWarnings("unused") PSRule rule)
      {
         return RULE_TO_LOWER;
      }
   }

   /**
    * Inner class for the Number Range rule composite.
    */
   public static class UpperCaseRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      private IRuleFieldDetails m_fieldDetails;

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      UpperCaseRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         createNoParamsLabel(this);
         setRuleData(rule);
      }

      public void setRuleData(
            @SuppressWarnings("unused") PSRule rule)
      {
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }
         PSExtensionParamValue param0 = new PSExtensionParamValue(
               new PSSingleHtmlParameter(m_fieldDetails.getFieldName()));
         PSExtensionParamValue[] params = new PSExtensionParamValue[1];
         params[0] = param0;
         setExtensionParams(rule, params);
         return rule;
      }

      public String getDisplayName()
      {
         return RULE_TO_UPPER;
      }

      public void clearData()
      {
         // There are no fields to clear for this rule composite.
      }

      public String getExtensionName()
      {
         return RULE_TO_UPPER_EXTNAME;
      }

      public String getRuleDisplayName(
            @SuppressWarnings("unused") PSRule rule)
      {
         return RULE_TO_UPPER;
      }
   }

   /**
    * Inner class for the Date Range rule composite.
    */
   public static class ProperCaseRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      private IRuleFieldDetails m_fieldDetails;

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      ProperCaseRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         createNoParamsLabel(this);
         setRuleData(rule);
      }

      public void setRuleData(
            @SuppressWarnings("unused") PSRule rule)
      {
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }
         PSExtensionParamValue param0 = new PSExtensionParamValue(
               new PSSingleHtmlParameter(m_fieldDetails.getFieldName()));
         PSExtensionParamValue[] params = new PSExtensionParamValue[1];
         params[0] = param0;
         setExtensionParams(rule, params);
         return rule;
      }

      public String getDisplayName()
      {
         return RULE_PROPER_CASE;
      }

      public void clearData()
      {
         // There are no fields to clear for this rule composite.
      }

      public String getExtensionName()
      {
         return RULE_PROPER_CASE_EXTNAME;
      }

      public String getRuleDisplayName(
            @SuppressWarnings("unused") PSRule rule)
      {
         return RULE_PROPER_CASE;
      }
   }

   /**
    * Inner class for the String Length rule composite.
    */
   public static class TrimRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      private IRuleFieldDetails m_fieldDetails;

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      TrimRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         createNoParamsLabel(this);
         setRuleData(rule);
      }

      public void setRuleData(
            @SuppressWarnings("unused") PSRule rule)
      {
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }
         PSExtensionParamValue param0 = new PSExtensionParamValue(
               new PSSingleHtmlParameter(m_fieldDetails.getFieldName()));
         PSExtensionParamValue[] params = new PSExtensionParamValue[1];
         params[0] = param0;
         setExtensionParams(rule, params);
         return rule;
      }

      public String getDisplayName()
      {
         return RULE_TRIM;
      }

      public void clearData()
      {
         // There are no fields to clear for this rule composite.
      }

      public String getExtensionName()
      {
         return RULE_TRIM_EXTNAME;
      }

      public String getRuleDisplayName(
            @SuppressWarnings("unused") PSRule rule)
      {
         return RULE_TRIM;
      }
   }

   /**
    * Inner class for the Regular Expression rule composite.
    */
   public static class NormalizeDateRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      /**
       * The format text field, never <code>null</code>.
       */
      private Text mi_inputFormat;

      private IRuleFieldDetails m_fieldDetails;

      /**
       * The user help text field, never <code>null</code>.
       */
      private Text mi_userHelpText;

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      NormalizeDateRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         final Label preDefLabel = new Label(this, SWT.NONE | SWT.RIGHT);
         final FormData fd = new FormData();
         fd.top = new FormAttachment(2,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
         fd.left = new FormAttachment(0, 0);
         fd.right = new FormAttachment(30, 0);
         preDefLabel.setLayoutData(fd);
         preDefLabel.setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.inputformat")); //$NON-NLS-1$

         mi_inputFormat = new Text(this, SWT.BORDER);
         final FormData fd1 = new FormData();
         fd1.top = new FormAttachment(preDefLabel, 0, SWT.TOP);
         fd1.left = new FormAttachment(preDefLabel, LABEL_HSPACE_OFFSET,
               SWT.RIGHT);
         fd1.right = new FormAttachment(100, 0);
         mi_inputFormat.setLayoutData(fd1);

         final Label userHelpTextLabel = new Label(this, SWT.NONE | SWT.RIGHT);
         final FormData fd2 = new FormData();
         fd2.top = new FormAttachment(preDefLabel, LABEL_VSPACE_OFFSET,
               SWT.BOTTOM);
         fd2.left = new FormAttachment(0, 0);
         fd2.right = new FormAttachment(30, 0);
         userHelpTextLabel.setLayoutData(fd2);
         userHelpTextLabel.setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.userhelptext")); //$NON-NLS-1$

         mi_userHelpText = new Text(this, SWT.BORDER | SWT.READ_ONLY
               | SWT.WRAP | SWT.V_SCROLL);
         final FormData fd3 = new FormData();
         fd3.top = new FormAttachment(userHelpTextLabel, TEXT_VSPACE_OFFSET,
               SWT.BOTTOM);
         fd3.left = new FormAttachment(5, 0);
         fd3.height = DESCRIPTION_FIELD_HEIGHT / 2;
         fd3.right = new FormAttachment(100, 0);
         mi_userHelpText.setLayoutData(fd3);
         PSExtensionDef def = getExtensionDef(getExtensionName());
         mi_userHelpText.setText(def
               .getInitParameter(PSExtensionDef.INIT_PARAM_DESCRIPTION));
         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         if (rule != null)
         {
            String[] params = getExtensionParamValues(rule, false);
            String param1 = getParamValue(params, 1);
            mi_inputFormat.setText(param1);
         }
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }

         PSExtensionParamValue param0 = null;
         PSExtensionParamValue param1 = null;
         param0 = new PSExtensionParamValue(new PSSingleHtmlParameter(
               m_fieldDetails.getFieldName()));
         param1 = new PSExtensionParamValue(new PSTextLiteral(mi_inputFormat
               .getText()));
         PSExtensionParamValue[] params = new PSExtensionParamValue[2];
         params[0] = param0;
         params[1] = param1;
         setExtensionParams(rule, params);
         return rule;
      }

      public String getDisplayName()
      {
         return RULE_NORMALIZE_DATE;
      }

      public void clearData()
      {
         mi_inputFormat.setText(""); //$NON-NLS-1$
      }

      public String getExtensionName()
      {
         return RULE_NORMALIZE_DATE_EXTNAME;
      }

      public String getRuleDisplayName(PSRule rule)
      {
         String[] params = getExtensionParamValues(rule, true);
         return RULE_NORMALIZE_DATE + "(" + params[1] + ")"; //$NON-NLS-1$ //$NON-NLS-2$
      }
   }

   /**
    * Inner class for the JEXL rule composite.
    */
   public static class ReplaceRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      /**
       * The Search For text field, never <code>null</code>.
       */
      private Text mi_searchFor;

      /**
       * The Replace With text field, never <code>null</code>.
       */
      private Text mi_replaceWith;

      private IRuleFieldDetails m_fieldDetails;

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      ReplaceRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());

         final Label minLengthLabel = new Label(this, SWT.NONE | SWT.RIGHT);
         final FormData fd = new FormData();
         fd.top = new FormAttachment(0,
               LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
         fd.left = new FormAttachment(10, 0);
         fd.right = new FormAttachment(40, 0);
         minLengthLabel.setLayoutData(fd);
         minLengthLabel.setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.searchfor")); //$NON-NLS-1$

         mi_searchFor = new Text(this, SWT.BORDER);
         final FormData fd1 = new FormData();
         fd1.top = new FormAttachment(0, 0);
         fd1.left = new FormAttachment(minLengthLabel, LABEL_HSPACE_OFFSET, 0);
         fd1.right = new FormAttachment(100, 0);
         mi_searchFor.setLayoutData(fd1);

         final Label maxLengthLabel = new Label(this, SWT.NONE | SWT.RIGHT);
         final FormData fd2 = new FormData();
         fd2.top = new FormAttachment(minLengthLabel, LABEL_VSPACE_OFFSET,
               SWT.BOTTOM);
         fd2.left = new FormAttachment(10, 0);
         fd2.right = new FormAttachment(40, 0);
         maxLengthLabel.setLayoutData(fd2);
         maxLengthLabel.setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.replacewith")); //$NON-NLS-1$

         mi_replaceWith = new Text(this, SWT.BORDER);
         final FormData fd3 = new FormData();
         fd3.top = new FormAttachment(maxLengthLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         fd3.left = new FormAttachment(maxLengthLabel, LABEL_HSPACE_OFFSET,
               SWT.RIGHT);
         fd3.right = new FormAttachment(100, 0);
         mi_replaceWith.setLayoutData(fd3);

         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         if (rule != null)
         {
            String[] params = getExtensionParamValues(rule, false);
            String param1 = getParamValue(params, 1);
            mi_searchFor.setText(param1);
            String param2 = getParamValue(params, 2);
            mi_replaceWith.setText(param2);
         }
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }

         if (StringUtils.isEmpty(mi_searchFor.getText()))
         {
            String msg = PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.searchforisrequired"); //$NON-NLS-1$
            showRuleCreationError(getShell(), msg, null);
            return null;
         }
         PSExtensionParamValue param0 = null;
         PSExtensionParamValue param1 = null;
         PSExtensionParamValue param2 = null;
         param0 = new PSExtensionParamValue(new PSSingleHtmlParameter(
               m_fieldDetails.getFieldName()));
         param1 = new PSExtensionParamValue(new PSTextLiteral(mi_searchFor
               .getText()));
         param2 = new PSExtensionParamValue(new PSTextLiteral(mi_replaceWith
               .getText()));
         PSExtensionParamValue[] params = new PSExtensionParamValue[3];
         params[0] = param0;
         params[1] = param1;
         params[2] = param2;
         setExtensionParams(rule, params);
         return rule;
      }

      public String getDisplayName()
      {
         return RULE_REPLACE;
      }

      public void clearData()
      {
         mi_searchFor.setText(""); //$NON-NLS-1$
         mi_replaceWith.setText(""); //$NON-NLS-1$
      }

      public String getExtensionName()
      {
         return RULE_REPLACE_EXTNAME;
      }

      public String getRuleDisplayName(PSRule rule)
      {
         String[] params = getExtensionParamValues(rule, true);
         String dn = RULE_REPLACE + "("; //$NON-NLS-1$
         dn += PSMessages.getString("PSFieldRuleDetailsComposites.label.searchfordisplay") + params[1] + PSMessages.getString("PSFieldRuleDetailsComposites.label.replacewithdisplay") + params[2]; //$NON-NLS-1$ //$NON-NLS-2$
         return dn;
      }
   }

   /**
    * Inner class for the Conditional rule composite.
    */
   public static class SetFieldRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      /**
       * The Jexl expression text field, never <code>null</code>.
       */
      private Text mi_jexlText;

      private IRuleFieldDetails m_fieldDetails;

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      SetFieldRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         final Label preDefLabel = new Label(this, SWT.NONE | SWT.RIGHT);
         final FormData fd = new FormData();
         fd.top = new FormAttachment(2,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
         fd.left = new FormAttachment(0, 0);
         fd.right = new FormAttachment(40, 0);
         preDefLabel.setLayoutData(fd);
         preDefLabel.setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.jexlexpression")); //$NON-NLS-1$

         mi_jexlText = new Text(this, SWT.BORDER | SWT.MULTI);
         final FormData fd1 = new FormData();
         fd1.top = new FormAttachment(preDefLabel, TEXT_VSPACE_OFFSET,
               SWT.BOTTOM);
         fd1.left = new FormAttachment(10, 0);
         fd1.right = new FormAttachment(100, 0);
         fd1.height = DESCRIPTION_FIELD_HEIGHT;
         mi_jexlText.setLayoutData(fd1);

         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         if (rule != null)
         {
            String[] params = getExtensionParamValues(rule, false);
            String param1 = getParamValue(params, 1);
            mi_jexlText.setText(param1);
         }
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }

         PSExtensionParamValue param0 = null;
         PSExtensionParamValue param1 = null;
         param0 = new PSExtensionParamValue(new PSSingleHtmlParameter(
               m_fieldDetails.getFieldName()));
         param1 = new PSExtensionParamValue(new PSTextLiteral(mi_jexlText
               .getText()));
         PSExtensionParamValue[] params = new PSExtensionParamValue[2];
         params[0] = param0;
         params[1] = param1;
         setExtensionParams(rule, params);
         return rule;
      }

      public String getDisplayName()
      {
         return RULE_SET_FIELD;
      }

      public void clearData()
      {
         mi_jexlText.setText(""); //$NON-NLS-1$
      }

      public String getExtensionName()
      {
         return RULE_SET_FIELD_EXTNAME;
      }

      public String getRuleDisplayName(PSRule rule)
      {
         String[] params = getExtensionParamValues(rule, true);
         return RULE_SET_FIELD + "(" + params[1] + ")"; //$NON-NLS-1$ //$NON-NLS-2$
      }
   }

   /**
    * Inner class for the Conditional rule composite.
    */
   public static class InputMapRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      /**
       * The map rule composite used by this composite, never <code>null</code>.
       */
      private PSMapRuleComposite mi_mapRuleComp;

      private IRuleFieldDetails m_fieldDetails;

      final String errorContext = "Input Transformation Map Rule"; //$NON-NLS-1$

      final String errorTitle = PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.inputmaprule"); //$NON-NLS-1$

      final String errorMessageSet = PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.maprulesetting"); //$NON-NLS-1$

      final String errorMessageGet = PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.maprulegetting"); //$NON-NLS-1$

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      InputMapRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         mi_mapRuleComp = new PSMapRuleComposite(this, SWT.NONE, true);
         final FormData fd = new FormData();
         fd.top = new FormAttachment(0, 0);
         fd.left = new FormAttachment(0, 0);
         fd.right = new FormAttachment(100, 0);
         fd.bottom = new FormAttachment(100, 0);
         mi_mapRuleComp.setLayoutData(fd);
         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         if (rule != null)
         {
            String[] params = getExtensionParamValues(rule, false);
            String param1 = getParamValue(params, 1);
            try
            {
               mi_mapRuleComp.setMapData(param1);
            }
            catch (UnsupportedEncodingException e)
            {
               PSWorkbenchPlugin.handleException(errorContext, errorTitle,
                     errorMessageSet + "\n", e); //$NON-NLS-1$
            }
         }

      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }

         String errorMsg = mi_mapRuleComp.validateMapData();
         if (errorMsg != null)
         {
            showRuleCreationError(getShell(), errorMsg, null);
            return null;
         }
         PSExtensionParamValue param0 = null;
         PSExtensionParamValue param1 = null;
         param0 = new PSExtensionParamValue(new PSSingleHtmlParameter(
               m_fieldDetails.getFieldName()));
         String mapString = ""; //$NON-NLS-1$
         try
         {
            mapString = mi_mapRuleComp.getMapData();
         }
         catch (UnsupportedEncodingException e)
         {
            PSWorkbenchPlugin.handleException(errorContext, errorTitle,
                  errorMessageGet + "\n", e); //$NON-NLS-1$
            return null;
         }
         param1 = new PSExtensionParamValue(new PSTextLiteral(mapString));
         PSExtensionParamValue[] params = new PSExtensionParamValue[2];
         params[0] = param0;
         params[1] = param1;
         setExtensionParams(rule, params);
         return rule;
      }

      public String getDisplayName()
      {
         return RULE_INPUT_MAP_VALUE;
      }

      public void clearData()
      {
         try
         {
            mi_mapRuleComp.setMapData(""); //$NON-NLS-1$
         }
         catch (UnsupportedEncodingException e)
         {
            // This should not happen as we are passing empty string from here
            e.printStackTrace();
         }
      }

      public String getExtensionName()
      {
         return RULE_INPUT_MAP_VALUE_EXTNAME;
      }

      public String getRuleDisplayName(
            @SuppressWarnings("unused") PSRule rule)
      {
         return RULE_INPUT_MAP_VALUE;
      }
   }

   /**
    * Inner class for the Regular Expression rule composite.
    */
   public static class FormatDateRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      /**
       * Text field for format spec, never <code>null</code>.
       */
      Text mi_useThisFormat;

      /**
       * Text field for user help text, never <code>null</code>.
       */
      Text mi_userHelpText;

      /**
       * Radio buttons for format option, never <code>null</code>.
       */
      Button[] mi_formatOption = new Button[2];

      private IRuleFieldDetails m_fieldDetails;

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      FormatDateRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());

         mi_formatOption[0] = new Button(this, SWT.NONE | SWT.RADIO);
         final FormData fd = new FormData();
         fd.top = new FormAttachment(2, 0);
         fd.left = new FormAttachment(0, 0);
         fd.right = new FormAttachment(100, 0);
         mi_formatOption[0].setLayoutData(fd);
         mi_formatOption[0].setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.useinverseofinputtransform")); //$NON-NLS-1$

         mi_formatOption[1] = new Button(this, SWT.NONE | SWT.RADIO);
         final FormData fda = new FormData();
         fda.top = new FormAttachment(mi_formatOption[0], LABEL_VSPACE_OFFSET,
               SWT.BOTTOM);
         fda.left = new FormAttachment(0, 0);
         fda.right = new FormAttachment(100, 0);
         mi_formatOption[1].setLayoutData(fda);
         mi_formatOption[1].setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.usethisformat")); //$NON-NLS-1$

         mi_useThisFormat = new Text(this, SWT.BORDER);
         final FormData fd1 = new FormData();
         fd1.top = new FormAttachment(mi_formatOption[1], TEXT_VSPACE_OFFSET,
               SWT.BOTTOM);
         fd1.left = new FormAttachment(mi_formatOption[1], 0, SWT.LEFT);
         fd1.right = new FormAttachment(100, 0);
         mi_useThisFormat.setLayoutData(fd1);

         final Label userHelpTextLabel = new Label(this, SWT.NONE | SWT.RIGHT);
         final FormData fd2 = new FormData();
         fd2.top = new FormAttachment(mi_useThisFormat, LABEL_VSPACE_OFFSET,
               SWT.BOTTOM);
         fd2.left = new FormAttachment(0, 0);
         fd2.right = new FormAttachment(30, 0);
         userHelpTextLabel.setLayoutData(fd2);
         userHelpTextLabel.setText(PSMessages.getString(
               "PSFieldRuleDetailsComposites.label.userhelptext")); //$NON-NLS-1$

         mi_userHelpText = new Text(this, SWT.BORDER | SWT.READ_ONLY
               | SWT.WRAP | SWT.V_SCROLL);
         final FormData fd3 = new FormData();
         fd3.top = new FormAttachment(userHelpTextLabel, TEXT_VSPACE_OFFSET,
               SWT.BOTTOM);
         fd3.left = new FormAttachment(5, 0);
         fd3.height = DESCRIPTION_FIELD_HEIGHT / 2;
         fd3.right = new FormAttachment(100, 0);
         mi_userHelpText.setLayoutData(fd3);
         PSExtensionDef def = getExtensionDef(getExtensionName());
         mi_userHelpText.setText(def
               .getInitParameter(PSExtensionDef.INIT_PARAM_DESCRIPTION));
         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         if (rule != null)
         {
            String[] params = getExtensionParamValues(rule, false);
            String param1 = getParamValue(params, 1);
            mi_useThisFormat.setText(param1);
         }
      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }

         PSExtensionParamValue param0 = null;
         PSExtensionParamValue param1 = null;
         param0 = new PSExtensionParamValue(new PSSingleHtmlParameter(
               m_fieldDetails.getFieldName()));
         param1 = new PSExtensionParamValue(new PSTextLiteral(mi_useThisFormat
               .getText()));
         PSExtensionParamValue[] params = new PSExtensionParamValue[2];
         params[0] = param0;
         params[1] = param1;
         setExtensionParams(rule, params);
         return rule;
      }

      public String getDisplayName()
      {
         return RULE_FORMAT_DATE;
      }

      public void clearData()
      {
         mi_useThisFormat.setText(""); //$NON-NLS-1$
      }

      public String getExtensionName()
      {
         return RULE_FORMAT_DATE_EXTNAME;
      }

      public String getRuleDisplayName(PSRule rule)
      {
         String[] params = getExtensionParamValues(rule, true);
         return RULE_FORMAT_DATE + "(" + params[1] + ")"; //$NON-NLS-1$ //$NON-NLS-2$
      }
   }

   /**
    * Inner class for the Conditional rule composite.
    */
   public static class OutputMapRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      /**
       * Map rule composite used by this composite, never <code>null</code>.
       */
      private PSMapRuleComposite mi_mapRuleComp;

      /**
       * Map option radio buttons, never <code>null</code>.
       */
      private Button[] mi_mapOption = new Button[2];

      private IRuleFieldDetails m_fieldDetails;

      final String errorContext = "Output Transformation Map Rule"; //$NON-NLS-1$

      final String errorTitle = PSMessages.getString("PSFieldRuleDetailsComposites.error.title.outputtransformationruleerror"); //$NON-NLS-1$

      final String errorMessageSet = PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.maprulesetting"); //$NON-NLS-1$

      final String errorMessageGet = PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.maprulegetting"); //$NON-NLS-1$

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      OutputMapRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());

         mi_mapOption[0] = new Button(this, SWT.NONE | SWT.RADIO);
         final FormData fd = new FormData();
         fd.top = new FormAttachment(2, 0);
         fd.left = new FormAttachment(0, 0);
         fd.right = new FormAttachment(100, 0);
         mi_mapOption[0].setLayoutData(fd);
         mi_mapOption[0].setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.useinverseofinput")); //$NON-NLS-1$

         mi_mapOption[1] = new Button(this, SWT.NONE | SWT.RADIO);
         final FormData fda = new FormData();
         fda.top = new FormAttachment(mi_mapOption[0], LABEL_VSPACE_OFFSET,
               SWT.BOTTOM);
         fda.left = new FormAttachment(0, 0);
         fda.right = new FormAttachment(100, 0);
         mi_mapOption[1].setLayoutData(fda);
         mi_mapOption[1].setText(PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.maprulesettingusethismap")); //$NON-NLS-1$

         mi_mapRuleComp = new PSMapRuleComposite(this, SWT.NONE, false);
         final FormData fd1 = new FormData();
         fd1.top = new FormAttachment(mi_mapOption[1], TEXT_VSPACE_OFFSET,
               SWT.BOTTOM);
         fd1.left = new FormAttachment(0, 0);
         fd1.right = new FormAttachment(100, 0);
         fd1.bottom = new FormAttachment(100, 0);
         mi_mapRuleComp.setLayoutData(fd1);
         setRuleData(rule);
      }

      public void setRuleData(PSRule rule)
      {
         if (rule != null)
         {
            String[] params = getExtensionParamValues(rule, false);
            String param1 = getParamValue(params, 1);
            try
            {
               mi_mapRuleComp.setMapData(param1);
            }
            catch (UnsupportedEncodingException e)
            {
               PSWorkbenchPlugin.handleException(errorContext, errorTitle,
                     errorMessageSet + "\n", e); //$NON-NLS-1$
            }
         }

      }

      public PSRule getRuleData()
      {
         PSRule rule = createNewRule(getExtensionName());
         if (rule == null)
         {
            String[] args = {getDisplayName(), getExtensionName()};
            showRuleCreationError(getShell(), MISSING_RULE_DEFINITION_ERROR,
                  args);
            return null;
         }
         String errorMsg = mi_mapRuleComp.validateMapData();
         if (errorMsg != null)
         {
            showRuleCreationError(getShell(), errorMsg, null);
            return null;
         }

         PSExtensionParamValue param0 = null;
         PSExtensionParamValue param1 = null;
         param0 = new PSExtensionParamValue(new PSSingleHtmlParameter(
               m_fieldDetails.getFieldName()));
         String mapString;
         try
         {
            mapString = mi_mapRuleComp.getMapData();
         }
         catch (UnsupportedEncodingException e)
         {
            PSWorkbenchPlugin.handleException(errorContext, errorTitle,
                  errorMessageGet + "\n", e); //$NON-NLS-1$
            return null;
         }
         param1 = new PSExtensionParamValue(new PSTextLiteral(mapString));
         PSExtensionParamValue[] params = new PSExtensionParamValue[2];
         params[0] = param0;
         params[1] = param1;
         setExtensionParams(rule, params);
         return rule;
      }

      public String getDisplayName()
      {
         return RULE_OUTPUT_MAP_VALUE;
      }

      public void clearData()
      {
         try
         {
            mi_mapRuleComp.setMapData(""); //$NON-NLS-1$
         }
         catch (UnsupportedEncodingException e)
         {
            // This should not happen as we are passing empty string from here
            e.printStackTrace();
         }
      }

      public String getExtensionName()
      {
         return RULE_OUTPUT_MAP_VALUE_EXTNAME;
      }

      public String getRuleDisplayName(
            @SuppressWarnings("unused") PSRule rule)
      {
         return RULE_OUTPUT_MAP_VALUE;
      }
   }

   /**
    * Inner class for the Create Only rule composite.
    */
   public static class CreateOnlyRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      @SuppressWarnings("unused") //$NON-NLS-1$
      private IRuleFieldDetails m_fieldDetails;

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      CreateOnlyRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         createNoParamsLabel(this);
         setRuleData(rule);
      }

      public void setRuleData(
            @SuppressWarnings("unused") PSRule rule)
      {
      }

      public PSRule getRuleData()
      {
         PSCollection condRules = new PSCollection(PSConditional.class);
         PSConditional cond = new PSConditional(new PSSingleHtmlParameter(
               SYS_CONTENTID), PSConditional.OPTYPE_ISNULL, null);
         condRules.add(cond);
         return new PSRule(condRules);
      }

      public String getDisplayName()
      {
         return RULE_CREATE_ONLY;
      }

      public void clearData()
      {
         // There are no fields to clear for this rule composite.
      }

      public String getExtensionName()
      {
         return RULE_CREATE_ONLY;
      }

      public String getRuleDisplayName(
            @SuppressWarnings("unused") PSRule rule)
      {
         return RULE_CREATE_ONLY;
      }
   }

   /**
    * Inner class for the Modify Only rule composite.
    */
   public static class ModifyOnlyRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      @SuppressWarnings("unused") //$NON-NLS-1$
      private IRuleFieldDetails m_fieldDetails;

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      ModifyOnlyRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         createNoParamsLabel(this);
         setRuleData(rule);
      }

      public void setRuleData(
            @SuppressWarnings("unused") PSRule rule)
      {
      }

      public PSRule getRuleData()
      {
         PSCollection condRules = new PSCollection(PSConditional.class);
         PSConditional cond = new PSConditional(new PSSingleHtmlParameter(
               SYS_CONTENTID), PSConditional.OPTYPE_ISNOTNULL, null);
         condRules.add(cond);
         return new PSRule(condRules);
      }

      public String getDisplayName()
      {
         return RULE_MODIFY_ONLY;
      }

      public void clearData()
      {
         // There are no fields to clear for this rule composite.
      }

      public String getExtensionName()
      {
         return RULE_MODIFY_ONLY;
      }

      public String getRuleDisplayName(
            @SuppressWarnings("unused") PSRule rule)
      {
         return RULE_MODIFY_ONLY;
      }
   }

   /**
    * Inner class for the Read Only rule composite.
    */
   public static class AlwaysReadOnlyRuleComposite extends Composite
         implements
            IRuleComposite,
            IPSUiConstants
   {
      @SuppressWarnings("unused") //$NON-NLS-1$
      private IRuleFieldDetails m_fieldDetails;

      /**
       * Default ctor
       * 
       * @param parent The parent composite, assumed not <code>null</code>.
       * @param style The syte of widget to construct
       * @param rule The rule to edit, may be <code>null</code>.
       */
      AlwaysReadOnlyRuleComposite(Composite parent, int style, PSRule rule,
            IRuleFieldDetails fieldDetails)
      {
         super(parent, style);
         m_fieldDetails = fieldDetails;
         setLayout(new FormLayout());
         createNoParamsLabel(this);
         setRuleData(rule);
      }

      /*
       * (non-Javadoc)
       * @see com.percussion.workbench.ui.editors.form.ce.PSFieldRuleDetailsComposites.IRuleComposite#setRuleData(com.percussion.design.objectstore.PSRule)
       */
      public void setRuleData(
            @SuppressWarnings("unused") PSRule rule)
      {
      }

      /*
       * (non-Javadoc)
       * @see com.percussion.workbench.ui.editors.form.ce.PSFieldRuleDetailsComposites.IRuleComposite#getRuleData()
       */
      public PSRule getRuleData()
      {
         PSCollection condRules = new PSCollection(PSConditional.class);
         PSConditional cond = new PSConditional(new PSTextLiteral(
               "1"), PSConditional.OPTYPE_EQUALS, new PSTextLiteral("1"));
         condRules.add(cond);
         return new PSRule(condRules);
      }

      /*
       * (non-Javadoc)
       * @see com.percussion.workbench.ui.editors.form.ce.PSFieldRuleDetailsComposites.IRuleComposite#getDisplayName()
       */
      public String getDisplayName()
      {
         return ALWAYS_RULE_READ_ONLY;
      }

      /*
       * (non-Javadoc)
       * @see com.percussion.workbench.ui.editors.form.ce.PSFieldRuleDetailsComposites.IRuleComposite#clearData()
       */
      public void clearData()
      {
         // There are no fields to clear for this rule composite.
      }

      /*
       * (non-Javadoc)
       * @see com.percussion.workbench.ui.editors.form.ce.PSFieldRuleDetailsComposites.IRuleComposite#getExtensionName()
       */
      public String getExtensionName()
      {
         return ALWAYS_RULE_READ_ONLY;
      }

      /*
       * (non-Javadoc)
       * @see com.percussion.workbench.ui.editors.form.ce.PSFieldRuleDetailsComposites.IRuleComposite#getRuleDisplayName(com.percussion.design.objectstore.PSRule)
       */
      public String getRuleDisplayName(
            @SuppressWarnings("unused") PSRule rule)
      {
         return ALWAYS_RULE_READ_ONLY;
      }
   }

   /**
    * A convenient method to create a common no parameters required label.
    * 
    * @param parent Parent composite for this label.
    */
   private static void createNoParamsLabel(Composite parent)
   {
      final Label noparams = new Label(parent, SWT.NONE);
      final FormData fd = new FormData();
      fd.top = new FormAttachment(50, 0);
      fd.left = new FormAttachment(10, 0);
      noparams.setLayoutData(fd);
      noparams.setText(PSMessages.getString("PSFieldRuleDetailsComposites.label.noparams")); //$NON-NLS-1$
   }

   /**
    * Creates a new rule with one extension call with null parameters based on
    * the supplied rule extension name. Returns <code>null</code> if the
    * supplied ruleExtName is not supported or could not get the extension def
    * for the supplied rule.
    * 
    * @param ruleExtName The name of the rule extension.
    * @return Object of PSRule correspodning to the supplied rule extension
    *         name. or <code>null</code> if could not find one.
    */
   private static PSRule createNewRule(String ruleExtName)
   {
      PSRule rule = null;
      PSExtensionCallSet callSet = new PSExtensionCallSet();
      PSExtensionDef def = null;
      def = getExtensionDef(ruleExtName);
      if (def != null)
      {
         PSExtensionCall call = new PSExtensionCall(def.getRef(), null);
         callSet.add(call);
         rule = new PSRule(callSet);
      }
      return rule;
   }

   /**
    * Convenient method to get the extension def object corresponding to the
    * supplied extension name.
    * 
    * @param ruleExtName Name of the extension may not be <code>null</code> or
    *           empty.
    * @return PSExtensionDef object of PSExtensionDef or <code>null</code>,
    *         if does not exist.
    */
   private static PSExtensionDef getExtensionDef(String ruleExtName)
   {
      if (StringUtils.isEmpty(ruleExtName))
         throw new IllegalArgumentException(
               "ruleExtName must not be null or empty"); //$NON-NLS-1$
      try
      {
         IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               PSObjectTypes.EXTENSION);
         List<IPSReference> all = PSCoreUtils.catalog(PSObjectTypes.EXTENSION,
               false);
         for (IPSReference ref : all)
         {
            if (ref.getName().endsWith(ruleExtName))
               return (PSExtensionDef) model.load(ref, false, false);
         }

      }
      catch (Exception e)
      {
         System.out.println(e.getMessage());
         e.printStackTrace();
         PSWorkbenchPlugin.handleException("Cataloging extensions.", "Error", e //$NON-NLS-1$ //$NON-NLS-2$
               .getLocalizedMessage(), e);
      }
      return null;
   }

   /**
    * A convenient method to set the extension parma values.
    * 
    * @param rule Object of PSRule must not be null and must be an extension set
    *           rule
    * @param params array of PSExtensionParamValue to set may be null.
    */
   private static void setExtensionParams(PSRule rule,
         PSExtensionParamValue[] params)
   {
      if (rule == null || !rule.isExtensionSetRule())
         throw new IllegalArgumentException(
               "rule must not be null and must be extension set rule"); //$NON-NLS-1$
      PSExtensionCallSet callSet = rule.getExtensionRules();
      Iterator iter = callSet.iterator();
      if (iter.hasNext())
      {
         PSExtensionCall call = (PSExtensionCall) iter.next();
         call.setParamValues(params);
      }
   }

   /**
    * A convenient method to get the extension parma values.
    * 
    * @param rule Object of PSRule must not be null and must be an extension set
    * rule
    * @param valueText If <code>true</code>,
    * {@link com.percussion.design.objectstore.IPSReplacementValue#getValueText()}
    * method is used get the param value otherwise
    * {@link com.percussion.design.objectstore.IPSReplacementValue#getValueDisplayText()}
    * method is used get the value text of the param value.
    * 
    * @return String[] of valueText or valueDisplayText values of parameters of
    * the supplied rule.
    */
   private static String[] getExtensionParamValues(PSRule rule,
         boolean valueText)
   {
      if (rule == null || !rule.isExtensionSetRule())
         throw new IllegalArgumentException(
               "rule must not be null and must be extension set rule"); //$NON-NLS-1$
      String[] paramValues = new String[0];
      PSExtensionCallSet callSet = rule.getExtensionRules();
      Iterator iter = callSet.iterator();
      if (iter.hasNext())
      {
         PSExtensionCall call = (PSExtensionCall) iter.next();
         PSExtensionParamValue[] params = call.getParamValues();
         paramValues = new String[params.length];
         for (int i = 0; i < params.length; i++)
         {
            PSExtensionParamValue param = params[i];
            if (param != null && param.getValue() != null)
            {
               String val = valueText ? param.getValue().getValueText() : param
                     .getValue().getValueDisplayText();
               paramValues[i] = StringUtils.defaultString(val);
            }
            else
               paramValues[i] = ""; //$NON-NLS-1$
         }
      }
      return paramValues;
   }

   /**
    * Convenient method to get the param at the given index from an array of
    * Strings. If the index is greater than or equal to the length of the array
    * or param array is <code>null</code> or param is <code>null</code>
    * returns empty string.
    * 
    * @param paramArray array of Strings.
    * @param index of the required param.
    * 
    * @return String param at the the given index or empty String.
    */
   private static String getParamValue(String[] paramArray, int index)
   {
      String param = StringUtils.EMPTY;
      if (!(paramArray == null || paramArray.length < 1 || index >= paramArray.length))
         param = StringUtils.defaultString(paramArray[index]);
      return param;
   }

   /**
    * Returns the index of the supplied stirng in the supplied array.
    * 
    * @param input Array of strings, in the form paramType/paramName from which
    *           the index of the item needs to be found. If <code>null</code>
    *           empty returns -1. If it has duplicate items, the index of the
    *           first found item is returned.
    * @param item String as paramName whose index needs to be found. If
    *           <code>null</code> or empty returns -1.
    * @return int index of the supplied string or -1 if not found or supplied
    *         array is null of supplied string is null
    */
   private static int getItemIndex(String[] input, String item)
   {
      if (input != null && input.length > 0 && !StringUtils.isBlank(item))
      {
         String fieldName = null;
         int index = item.lastIndexOf('/');
         if (index != -1)
            fieldName = item.substring(index + 1);
         else
            fieldName = item;

         for (int i = 0; i < input.length; i++)
         {
            if (fieldName.equals(input[i]))
               return i;
         }
      }
      return -1;
   }

   /**
    * Wrapper method that calls {@link #getExtensions(Set)} and filters out
    * udfs used by hard-coded rules from the results.  See that method for info.  
    * 
    * @param ifnames The names of the interfaces to catalog, may not be 
    * <code>null</code> or empty 
    * 
    * @return The list of extensions, never <code>null</code>, may be empty.
    */
   private static List<PSExtensionDef> getExtRuleExtensions(Set<String> ifnames)
   {
      List<PSExtensionDef> exts = new ArrayList<PSExtensionDef>();
      exts = getExtensions(ifnames);
      List<PSExtensionDef> rems = new ArrayList<PSExtensionDef>();
      for (PSExtensionDef ext : exts)
      {
         String extName = ext.getRef().getExtensionName();
         extName = extName.split("/")[extName.split("/").length - 1]; //$NON-NLS-1$ //$NON-NLS-2$
         if (extName.equals(RULE_REQUIRED_EXTNAME)
               || extName.equals(RULE_NUMBER_RANGE_EXTNAME)
               || extName.equals(RULE_DATE_RANGE_EXTNAME)
               || extName.equals(RULE_STRING_LENGTH_EXTNAME)
               || extName.equals(RULE_REGULAR_EXPRESSION_EXTNAME)
               || extName.equals(RULE_JEXL_EXTNAME))
         {
            rems.add(ext);
         }

      }
      exts.removeAll(rems);
      return exts;
   }

   /**
    * Retrieves all of the extensions that specified the supplied interface
    * 
    * @param ifname The name of the interface to catalog, may not be 
    * <code>null</code> or empty 
    * 
    * @return The list of extensions, never <code>null</code>, may be empty.
    */
   public static List<PSExtensionRef> getExtensions(String ifname)
   {
      if (StringUtils.isBlank(ifname))
         throw new IllegalArgumentException("ifname may not be null or empty");
      
      Set<String> ifnames = new HashSet<String>();
      ifnames.add(ifname);
      
      List<PSExtensionDef> defs = getExtensions(ifnames);
      List<PSExtensionRef> refs = new ArrayList<PSExtensionRef>();
      for (PSExtensionDef def : defs)
         refs.add(def.getRef());
      return refs;
   }
   
   /**
    * Retrieves all of the extensions that implement any of the supplied 
    * interfaces
    * 
    * @param ifnames The names of the interfaces to catalog, may not be 
    * <code>null</code> or empty 
    * 
    * @return The list of extensions, never <code>null</code>, may be empty.
    */
   public static List<PSExtensionDef> getExtensions(Set<String> ifnames)
   {
      if (ifnames == null || ifnames.isEmpty())
         throw new IllegalArgumentException("ifnames may not be null or empty");
      
      List<PSExtensionDef> exts = new ArrayList<PSExtensionDef>();
      try
      {
         IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               PSObjectTypes.EXTENSION);
         List<IPSReference> all = PSCoreUtils.catalog(PSObjectTypes.EXTENSION,
               false);
         for (IPSReference ref : all)
         {
            PSExtensionDef def = (PSExtensionDef) model.load(ref, false, false);
            for (String ifname : ifnames)
            {
               if (def.implementsInterface(ifname))
               {
                  exts.add(def);
                  break;
               }
            }
         }
      }
      catch (Exception e)
      {
         System.out.println(e.getMessage());
         e.printStackTrace();
         PSWorkbenchPlugin.handleException("Cataloging extensions.", "Error", e //$NON-NLS-1$ //$NON-NLS-2$
               .getLocalizedMessage(), e);
      }
      return exts;
   }   

   /**
    * A convenient method to show the rule creation error.
    * 
    * @param shell The Shell object assumed not null.
    * @param msg The message to be displayed. If <code>null</code> or empty
    *           message dialog is not displayed.
    * @param args The message is formated with the supplied arguments if
    *           <code>null</code> or empty message format is not applied.
    */
   private static void showRuleCreationError(Shell shell, String msg,
         String[] args)
   {
      ms_ruleErrorOccured = true;
      if (StringUtils.isEmpty(msg) || ms_surpressErrors)
         return;
      if (args != null && args.length > 0)
         msg = MessageFormat.format(msg, (Object[]) args);
      MessageDialog.openError(shell, VALIDATION_ERROR_DIALOG_TITLE, msg);
   }
   
   /**
    * Allows one to suppress the display of error dialogs.
    * @param suppress if <code>true</code> then error dialogs
    * will be suppressed.
    */
   public static void setSuppressErrors(boolean suppress)
   {
      ms_surpressErrors = suppress;
   }
   
   /**
    * Indicates that a rule error occurred since the last
    * time that this flag was cleared.
    * @return <code>true</code> if an error occurred.
    */
   public static boolean ruleErrorOccurred()
   {
      return ms_ruleErrorOccured;
   }
   
   /**
    * Clears the error occurred flag.
    */
   public static void clearErrorOccurredFlag()
   {
      ms_ruleErrorOccured = false;
   }

   /**
    * A convenient String label provider for the combo box control used in this
    * class.
    */
   public static class StringLabelProvider extends PSAbstractLabelProvider
   {
      public String getText(Object element)
      {
         if (element instanceof String)
            return (String) element;
         else
            return ""; //$NON-NLS-1$
      }
   }
     
   /**
    * Flag indicating that error popups should be suppressed.
    */
   private static boolean ms_surpressErrors;
   
   /**
    * Flag indicating that a rule error occurred
    */
   private static boolean ms_ruleErrorOccured;

   /**
    * Constant String representing less than operator.
    */
   private static final String OP_LESSTHAN = "<"; //$NON-NLS-1$

   /**
    * Constant String representing less than or equal operator.
    */
   private static final String OP_LESSTHAN_EQUAL = "<="; //$NON-NLS-1$

   /**
    * Array of strings representing the less than and less than or equal to
    * operators.
    */
   private static final String[] ms_lessThanOps =
   {OP_LESSTHAN, OP_LESSTHAN_EQUAL};

   /**
    * Extension name of the Required rule.
    */
   public static final String RULE_REQUIRED_EXTNAME = PSContentTypeUtils.RULE_REQUIRED_EXTNAME;

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_NUMBER_RANGE_EXTNAME = "sys_ValidateNumberRange"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_DATE_RANGE_EXTNAME = "sys_ValidateDateRange"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_STRING_LENGTH_EXTNAME = "sys_ValidateStringLength"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_REGULAR_EXPRESSION_EXTNAME = "sys_ValidateStringPattern"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_JEXL_EXTNAME = "sys_ValidateJexlFieldExpression"; //$NON-NLS-1$

   /**
    * Constant String for the rule type Number Range.
    */
   private static final String RULE_NUMBER_RANGE = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.numberrange"); //$NON-NLS-1$

   /**
    * Constant String for the rule type Date Range.
    */
   private static final String RULE_DATE_RANGE = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.daterange"); //$NON-NLS-1$

   /**
    * Constant String for the rule type String Length.
    */
   private static final String RULE_STRING_LENGTH = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.stringlength"); //$NON-NLS-1$

   /**
    * Constant String for the rule type Regular Expression.
    */
   private static final String RULE_REGULAR_EXPRESSION = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.regularexpression"); //$NON-NLS-1$

   /**
    * Constant String for the rule type JEXL.
    */
   private static final String RULE_JEXL = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.jexl"); //$NON-NLS-1$

   /**
    * Constant String for the rule type Conditional.
    */
   public static final String RULE_CONDITIONAL = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.conditional"); //$NON-NLS-1$

   /**
    * Constant String for the rule type Extension.
    */
   public static final String RULE_EXTENSION = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.extension"); //$NON-NLS-1$

   /**
    * Constant String for the rule type to lower.
    */
   private static final String RULE_TO_LOWER = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.tolower"); //$NON-NLS-1$

   /**
    * Constant String for the rule type to upper.
    */
   private static final String RULE_TO_UPPER = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.toupper"); //$NON-NLS-1$

   /**
    * Constant String for the rule type proper case
    */
   private static final String RULE_PROPER_CASE = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.topropercase"); //$NON-NLS-1$

   /**
    * Constant String for the rule type trim.
    */
   private static final String RULE_TRIM = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.trim"); //$NON-NLS-1$

   /**
    * Constant String for the rule type Regular Expression.
    */
   private static final String RULE_NORMALIZE_DATE = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.normalizedate"); //$NON-NLS-1$

   /**
    * Constant String for the rule type Replace.
    */
   private static final String RULE_REPLACE = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.replace"); //$NON-NLS-1$

   /**
    * Constant String for the rule type Set Field.
    */
   private static final String RULE_SET_FIELD = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.setfield"); //$NON-NLS-1$

   /**
    * Constant String for the rule type input map.
    */
   private static final String RULE_INPUT_MAP_VALUE = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.inputmap"); //$NON-NLS-1$

   /**
    * Constant String for the rule type output map.
    */
   private static final String RULE_OUTPUT_MAP_VALUE = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.outputmap"); //$NON-NLS-1$

   /**
    * Constant String for the rule type Regular Expression.
    */
   private static final String RULE_FORMAT_DATE = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.formatdate"); //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_TO_LOWER_EXTNAME = "sys_ToLowerCase"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_TO_UPPER_EXTNAME = "sys_ToUpperCase"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_PROPER_CASE_EXTNAME = "sys_ToProperCase"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_TRIM_EXTNAME = "sys_TrimString"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_NORMALIZE_DATE_EXTNAME = "sys_NormalizeDate"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_REPLACE_EXTNAME = "sys_Replace"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_SET_FIELD_EXTNAME = "sys_TranslateJexlExpressionValue"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_INPUT_MAP_VALUE_EXTNAME = "sys_MapInputValue"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_OUTPUT_MAP_VALUE_EXTNAME = "sys_MapOutputValue"; //$NON-NLS-1$

   /**
    * Extension name of the Required rule.
    */
   private static final String RULE_FORMAT_DATE_EXTNAME = "sys_FormatDate"; //$NON-NLS-1$

   /**
    * Constant String for the rule type Create Only.
    */
   public static final String RULE_CREATE_ONLY = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.createonly"); //$NON-NLS-1$

   /**
    * Constant String for the rule type Create Only.
    */
   public static final String RULE_MODIFY_ONLY = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.modifyonly"); //$NON-NLS-1$

   /**
    * Constant String for the rule type Read Only.
    */
   public static final String ALWAYS_RULE_READ_ONLY = PSMessages.getString("PSFieldRuleDetailsComposites.label.rule.alwaysreadonly"); //$NON-NLS-1$

   /**
    * Constant String for the rule type Create Only.
    * 
    */
   public static final String SYS_CONTENTID = "sys_contentid"; //$NON-NLS-1$

   /**
    * Constant string for yes value.
    */
   private static final String STRING_YES = "yes"; //$NON-NLS-1$

   /**
    * Constant string for no value.
    */
   private static final String STRING_NO = "no"; //$NON-NLS-1$

   /**
    * String constant for title for validation rule dialog error.
    */
   private static final String VALIDATION_ERROR_DIALOG_TITLE = PSMessages.getString("PSFieldRuleDetailsComposites.error.title.rulevalidationerror"); //$NON-NLS-1$

   /**
    * String constant for missing rule extention definition.
    */
   private static final String MISSING_RULE_DEFINITION_ERROR = PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.rulecreation"); //$NON-NLS-1$

   /**
    * String constant for showing the error message for at least one valid
    * parameter is missing to create the rule.
    */
   private static final String ATLEAST_ONE_VALID_PARAMETER_REQUIRED = PSMessages.getString("PSFieldRuleDetailsComposites.error.msg.atleastoneparamreq"); //$NON-NLS-1$

}
