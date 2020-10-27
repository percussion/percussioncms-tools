/******************************************************************************
 *
 * [ PSDlgUtil.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.workbench.ui.legacy.AwtSwtModalDialogBridge;
import com.percussion.workbench.ui.util.PSErrorDialog;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Shows error messages.
 * Methods can be called from either SWT E2Designer-based application or
 * from Swing application only using some of E2Designer classes but not
 * initializing E2Designer and E2Designer main frame.   
 *
 * @author Andriy Palamarchuk
 */
public class PSDlgUtil
{
   private static final Logger ms_log = Logger.getLogger(PSDlgUtil.class);

   /** A error dialog &quot;wrapper&quot; method that calls upon
    * JOptionPane.showMessageDialog.
    *
    * @param message The text to be displayed in the body of the dialog.
    * @param title The title text of the dialog.
    */
   public static void showErrorDialog(final String message, final String title)
   {
      assertNotCalledFromTest(message);
      if (isUseSwing())
      {
         showSwingMessageDialog(message, title, JOptionPane.ERROR_MESSAGE);
      }
      else
      {
         getDisplay().syncExec(new Runnable()
               {
                  public void run()
                  {
                     MessageDialog.openError(getShell(), title, message);
                  }
               });
      }
   }
   
   /**
    * Corresponds to
    * {@link JOptionPane#showMessageDialog(java.awt.Component, java.lang.Object)}.
    * @param message text to show in the message box.
    */
   public static void showMessageDialog(final String message)
   {
      assertNotCalledFromTest(message);
      if (isUseSwing())
      {
         JOptionPane.showMessageDialog(getPermanetFocusOwner(), message);
      }
      else
      {
         getDisplay().syncExec(new Runnable()
               {
                  public void run()
                  {
                     MessageDialog.openInformation(getShell(),
                           UIManager.getString("OptionPane.messageDialogTitle", null), message);
                  }
               });
      }
   }

   /**
    * Returns SWT shell to show dialogs on.
    */
   private static Shell getShell()
   {
      if (E2Designer.getApp() == null || E2Designer.getApp().getMainFrame() == null)
      {
         return null;
      }
      return E2Designer.getApp().getMainFrame().m_parentShell;
   }
   
   /**
    * Returns SWT display of the shell to show dialogs on.
    */
   private static Display getDisplay()
   {
      final Display display = Display.findDisplay(Thread.currentThread());
      if (display != null)
      {
         return display;
      }
      else
      {
         return Display.getDefault();
      }
   }

   /**
    * Indicates when to use Swing to show dialogs.
    */
   private static boolean isUseSwing()
   {
      return !ms_useSWT || isSwtLocked();
   }

   /**
    * Indicates that SWT UI is locked. Thid could happen only when Swing dialog
    * is shown, so all other modal dialogs should be shown on top of this Swing
    * dialog. Otherwise the messages should be shown on top of SWT dialog.
    */
   private static boolean isSwtLocked()
   {
      return AwtSwtModalDialogBridge.isSwtLocked();
   }

   private static void showSwingMessageDialog(String errorBody, String errorTitle, int type)
   {
      final JTextArea textBox = new JTextArea(errorBody, 8, 20);
      textBox.setWrapStyleWord(true);
      textBox.setLineWrap(true);
      textBox.setEditable(false);
      JScrollPane pane = new JScrollPane(textBox);
      pane.setPreferredSize(new Dimension(400, 185));
      JOptionPane.showMessageDialog(getPermanetFocusOwner(), pane, errorTitle, type);
   }

   /**
    * Shows warning dialog.
    * @param body The text to be displayed in the body of the dialog.
    * @param title The dialog title.
    */
   public static void showWarningDialog(final String body, final String title)
   {
      assertNotCalledFromTest(body);
      if (isUseSwing())
      {
         showSwingMessageDialog(body, title, JOptionPane.WARNING_MESSAGE);
      }
      else
      {
         getDisplay().syncExec(new Runnable()
               {
                  public void run()
                  {
                     MessageDialog.openWarning(getShell(), title, body);
                  }
               });
      }
   }

   /**
    * Shows information dialog.
    * @param body The text to be displayed in the body of the dialog.
    * @param title The dialog title.
    */
   public static void showInfoDialog(final String body, final String title)
   {
      assertNotCalledFromTest(body);
      if (isUseSwing())
      {
         showSwingMessageDialog(body, title, JOptionPane.INFORMATION_MESSAGE);
      }
      else
      {
         getDisplay().syncExec(new Runnable()
               {
                  public void run()
                  {
                     MessageDialog.openInformation(getShell(), title, body);
                  }
               });
      }
   }

   /**
    * Makes dialog messages use SWT whenever possible. 
    */
   public static void useSwtIfPossible()
   {
      ms_useSWT = true;
   }
   
   public static void showError(final Exception e, boolean bPrintCallStack,
         final String title)
   {
      assertNotCalledFromTest(e.getMessage());
      if (bPrintCallStack)
      {
         ms_log.error("Showing error dialog \"" + title + "\"", e);
      }
      if (isUseSwing())
      {
         showSwingErrorDialog(e, title);
      }
      else
      {
         getDisplay().syncExec(
               new Runnable()
               {
                  public void run()
                  {
                     final PSErrorDialog dlg = 
                        new PSErrorDialog(getShell(), e.getLocalizedMessage(), e);
                     dlg.open();
                  }
               });
      }
   }

   private static void showSwingErrorDialog(Exception e, String title)
   {
      final Window window = getActiveWindow();
      if (window instanceof Dialog)
      {
         new ExceptionDialog((Dialog) window, getExceptionDisplayText(e), title);
      }
      else if (window instanceof Frame)
      {
         new ExceptionDialog((Frame) window, getExceptionDisplayText(e), title);
      }
      else
      {
         new ExceptionDialog((Frame) null, getExceptionDisplayText(e), title);
      }
   }

   private static String getExceptionDisplayText(Exception e)
   {
      final String displayText;
      if (StringUtils.isBlank(e.getMessage()))
      {
         displayText = "An exception occurred, but no text is available. The exception class was: "
            + e.getClass().getName();
      }
      else
      {
         displayText = e.getMessage().trim();
      }
      return displayText;
   }
   
   /**
    * This function will give the error message from an exceptions as a dialog box.
    * The title of the dialog will be set to the base class name of the exception.
    */
   public static void showError(Exception e)
   {
      String classFullName = e.getClass().getName();
      showError(e, true, classFullName.substring(classFullName.lastIndexOf(".") + 1));
   }
   
   /**
    * Shows confirmation dialog.
    * @param message the confirmation request text.
    * @param title the dialog title.
    * @param optionType same as optionType parameter of
    * {@link JOptionPane#showConfirmDialog(java.awt.Component, java.lang.Object,
    * java.lang.String, int, int)} call.
    * @param messageType same as messageType parameter of
    * {@link JOptionPane#showConfirmDialog(java.awt.Component, java.lang.Object,
    * java.lang.String, int, int)} call. 
    * @return one of values returned by
    * {@link JOptionPane#showConfirmDialog(java.awt.Component, java.lang.Object,
    * java.lang.String, int, int)} call. 
    */
   public static int showConfirmDialog(final String message, final String title,
         final int optionType, final int messageType)
   {
      assertNotCalledFromTest(message);
      if (isUseSwing())
      {
         return JOptionPane.showConfirmDialog(getPermanetFocusOwner(),
               message, title, optionType, messageType);
      }
      else
      {
         ResultRunnable runnable = new ResultRunnable()
         {
            public void run()
            {
               final MessageBox messageBox = new MessageBox(getShell(),
                     convertToMessageBoxStyle(optionType, messageType));
               messageBox.setMessage(message);
               messageBox.setText(title);
               m_result = convertToJOptionPaneResult(messageBox.open());
            }
         };
         getDisplay().syncExec(runnable);
         return runnable.m_result;
      }
   }

   /**
    * Translates <code>optionType</code>, <code>messageType</code> parameters as
    * provided to
    * {@link JOptionPane#showConfirmDialog(java.awt.Component, java.lang.Object,
    * java.lang.String, int, int)} call to <code>style</code> parameter of
    * {@link MessageBox#open()} call.
    * Note, when running under SWT the call never returns
    * {@link JOptionPane#CLOSED_OPTION} because SWT does not allow dialog to be
    * closed if CANCEL style is not specified. If the style is specified the
    * method returns on closing {@link JOptionPane#CANCEL_OPTION}. 
    */
   private static int convertToMessageBoxStyle(int optionType, int messageType)
   {
      return getOptionTypeStyleComponent(optionType)
            | getMessageTypeStyleComponent(messageType);
   }

   /**
    * Derives {@link MessageBox} style from {@link JOptionPane} message type.
    */
   private static int getMessageTypeStyleComponent(final int messageType)
   {
      switch (messageType)
      {
         case JOptionPane.ERROR_MESSAGE:
            return SWT.ICON_ERROR;
         case JOptionPane.INFORMATION_MESSAGE:
            return SWT.ICON_INFORMATION;
         case JOptionPane.WARNING_MESSAGE:
            return SWT.ICON_WARNING;
         case JOptionPane.QUESTION_MESSAGE:
            return SWT.ICON_QUESTION;
         case JOptionPane.PLAIN_MESSAGE:
            return SWT.ICON_INFORMATION;
         default:
            throw new IllegalArgumentException(
                  "Unrecognized confirmation message type: " + messageType);
      }
   }

   /**
    * Derives {@link MessageBox} style from {@link JOptionPane} option type.
    * 
    * @param optionType The <code>JOptionPane.XXX_option</code> type.
    *  
    * @return The corresponding {@link SWT} message box style constant value.
    */
   private static int getOptionTypeStyleComponent(final int optionType)
   {
      switch (optionType)
      {
         case JOptionPane.YES_NO_OPTION:
            return SWT.YES | SWT.NO;
         case JOptionPane.YES_NO_CANCEL_OPTION:
            return SWT.YES | SWT.NO | SWT.CANCEL;
         case JOptionPane.OK_CANCEL_OPTION:
            return SWT.OK | SWT.CANCEL;
         default:
            throw new IllegalArgumentException(
                  "Unrecognized confirmation dialog type: " + optionType);
      }
   }

   /**
    * Translates values returned by {@link MessageBox#open()} to values returned
    * {@link JOptionPane#showConfirmDialog(java.awt.Component, java.lang.Object,
    * java.lang.String, int, int)} call. 
    */
   private static int convertToJOptionPaneResult(final int messageBoxResult)
   {
      switch (messageBoxResult)
      {
         case SWT.YES:
            return JOptionPane.YES_OPTION;
         case SWT.NO:
            return JOptionPane.NO_OPTION;
         case SWT.OK:
            return JOptionPane.OK_OPTION;
         case SWT.CANCEL:
            return JOptionPane.CANCEL_OPTION;
         default:
            throw new IllegalArgumentException(
                  "Unrecognized message box result: " + messageBoxResult);
      }
   }

   /**
    * Indicates whether the class should use SWT whenever possible.
    */
   private static boolean ms_useSWT;

   /**
    * Returns active top-level application window.
    */
   public static Window getActiveWindow()
   {
      return getOwnerWindow(getPermanetFocusOwner());
   }

   /**
    * Component currently keeping focus.
    */
   private static Component getPermanetFocusOwner()
   {
      return KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
   }

   /**
    * Returns dialog or frame owning current window.
    * @param component control to find window for. Can be <code>null</code>.  
    */
   private static Window getOwnerWindow(final Component component)
   {
      Component windowComponent = component; 
      while (windowComponent != null && !(windowComponent instanceof Window))
      {
         windowComponent = windowComponent.getParent();
      }
      return (Window) windowComponent;
   }
   
   /**
    * Throws an assertion error if called during running a unit test.
    */
   private static void assertNotCalledFromTest(final String message)
   {
      if (isCalledFromTest())
      {
         throw new AssertionError(
               "Attempt to call an error dialog from unit test with  message \"" +
               message + "\"");
      }
   }
 
   /**
    * Returns <code>true</code> if is called from unit test.
    */
   private static boolean isCalledFromTest()
   {
      return Arrays.asList(new Exception().getStackTrace()).toString().contains("junit"); 
   }

   /**
    * Returns integer results Runnable. 
    */
   private abstract static class ResultRunnable implements Runnable
   {
      public int m_result;
   }
}
