/******************************************************************************
 *
 * [ PSRhythmyxPageEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.PSConnectionInfo;
import com.percussion.client.PSCoreFactory;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSButtonFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import java.net.URL;
import java.util.Formatter;

/**
 * A "fake" editor to show a page from running Rhythmyx server.
 * It makes an attempt to load the specified url to the browser.
 * If the browser is forwarded to some other page than specified one this class
 * assumes that it was forwarded to the login page and submits a user name
 * and password to it to log in. It is expected that upon successful login
 * the server forwards browser to the original page.
 *
 * @author Andriy Palamarchuk
 */
public class PSRhythmyxPageEditor extends EditorPart
{
   /**
    * The class logger.
    */
   private static final Logger ms_log =
         Logger.getLogger(PSRhythmyxPageEditor.class);

   // see base
   @Override
   public void init(IEditorSite site, IEditorInput input)
         throws PartInitException
   {
      setSite(site);
      setInput(input);
      if (getUrl() == null)
      {
         throw new PartInitException(
               "The editor input should be able to provide URL to open"); 
      }
   }

   // see base
   @Override
   public void createPartControl(Composite parent)
   {
      m_pager = new StackLayout();
      parent.setLayout(m_pager);
      
      {
         final Composite labelContainer = new Composite(parent, SWT.NONE);
         labelContainer.setLayout(new FillLayout(SWT.VERTICAL));

         // pad label from top
         new Composite(labelContainer, SWT.NONE);
         
         final Label loadingLabel = new Label(labelContainer, SWT.CENTER);
         loadingLabel.setFont(JFaceResources.getBannerFont());
         loadingLabel.setText(
               PSMessages.getString("PSRhythmyxPageEditor.message.pageLoading"));

         // pad label from bottom
         new Composite(labelContainer, SWT.NONE);

         m_pager.topControl = labelContainer;
      }
      
      m_browserContainer = new Composite(parent, SWT.NONE);
      m_browserContainer.setLayout(new FormLayout());

      final Button backButton = createBackButton(m_browserContainer);
      createForwardButton(m_browserContainer, backButton);

      m_browser = createBrowser(m_browserContainer, backButton);
      m_browser.addLocationListener(new LocationListener()
            {
               @SuppressWarnings("unused")
               public void changing(LocationEvent event)
               {
                  // do nothing
               }

               /**
                * Shows browser component when the requested page is loaded.
                */
               public void changed(LocationEvent event)
               {
                  if (isBrowserShown())
                  {
                     return;
                  }

                  if (mi_showBrowserOnNextPage)
                  {
                     showBrowser();
                     return;
                  }
                  
                  if (mi_skipAutoFormChangedEvent)
                  {
                     mi_skipAutoFormChangedEvent = false;
                     // show browser when the next page after autologin form
                     // is loaded
                     mi_showBrowserOnNextPage = true;
                     return;
                  }

                  if (getUrl().toString().equals(event.location))
                  {
                     showBrowser();
                  }
                  else
                  {
                     // we are forwarded to the login page
                     loginToRythmyx(event.location);
                     mi_skipAutoFormChangedEvent = true;
                  }
               }

               /**
                * <code>true</code> indicates that on the next location change 
                * event the browser must be shown.
                */
               private boolean mi_showBrowserOnNextPage;

               /**
                * Is used to skip loading of auto-submitting form
                * {@link #AUTOLOGIN_FORM}.
                */
               private boolean mi_skipAutoFormChangedEvent;
            });
      m_browser.setUrl(getUrl().toString());
      parent.layout();
   }

   /**
    * Creates browser control.
    * @param container the control to create browser in.
    * Assumed to have form layout. Assumed not <code>null</code>.
    * @param browserButton the browser button above the browser.
    * Used for layout. Assumed not <code>null</code>.
    * @return the browser instance. Never <code>null</code>.
    */
   private Browser createBrowser(final Composite container,
         final Button browserButton)
   {
      final Browser browser = new Browser(container, SWT.NONE);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(
            browserButton, BUTTON_PADDING, SWT.BOTTOM);
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.bottom = new FormAttachment(100, 0);
      browser.setLayoutData(formData);
      return browser;
   }

   /**
    * Creates "Forward" browser button.
    * @param container the control to create browser in.
    * Assumed to have form layout. Assumed not <code>null</code>.
    * @param previousButton the button before this one.
    * Assumed not <code>null</code>.
    * @return the button instance. Never <code>null</code>.
    */
   private Button createForwardButton(final Composite container,
         final Button previousButton)
   {
      final Button button = PSButtonFactory.createForwardButton(container);
      button.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            m_browser.forward();
         }
      });
      button.setToolTipText(
            PSMessages.getString("PSRhythmyxPageEditor.forward.tooltip"));

      final FormData formData = new FormData();
      formData.bottom = new FormAttachment(previousButton, 0, SWT.BOTTOM);
      formData.top = new FormAttachment(previousButton, 0, SWT.TOP);
      formData.left = new FormAttachment(
            previousButton, BUTTON_PADDING, SWT.RIGHT);
      button.setLayoutData(formData);
      return button;
   }

   /**
    * Creates "Back" browser button.
    * @param container the button container. Assumed not <code>null</code>.
    * @return the button instance. Never <code>null</code>.
    */
   private Button createBackButton(final Composite container)
   {
      final Button button = PSButtonFactory.createBackButton(container);
      button.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            m_browser.back();
         }
      });
      button.setToolTipText(
            PSMessages.getString("PSRhythmyxPageEditor.back.tooltip"));

      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, BUTTON_PADDING);
      formData.left = new FormAttachment(0, BUTTON_PADDING);
      button.setLayoutData(formData);
      return button;
   }

   /**
    * Makes {@link #m_browserContainer} control visible.
    */
   private void showBrowser()
   {
      assert m_pager.topControl != m_browserContainer;
      m_pager.topControl = m_browserContainer;
      m_browserContainer.getParent().layout();
   }

   /**
    * <code>true</code> if the page is loaded and the browser is shown.
    */
   private boolean isBrowserShown()
   {
      return m_pager.topControl.equals(m_browserContainer);
   }

   /**
    * Submits a form to log in to the Rhythmyx server.
    * @param loginUrl the URL to submit the form to.
    * Assumed not <code>null</code>.
    */
   private void loginToRythmyx(String loginUrl)
   {
      if (!loginUrl.endsWith("/login"))
      {
         ms_log.error("Unrecognized login url: " + loginUrl);
      }
      final Formatter formatter = new Formatter();
      final String userId =
            escapeHtml(getConnectionInfo().getUserid());
      final String password =
            escapeHtml(getConnectionInfo().getClearTextPassword());
      formatter.format(AUTOLOGIN_FORM, loginUrl, userId, password);
      m_browser.setText(formatter.toString());
   }

   /**
    * Generates valid HTML value string from the provided string.
    * @param str the text to encode. Assumed not <code>null</code>.
    */
   private String escapeHtml(final String str)
   {
      return StringEscapeUtils.escapeHtml(str);
   }

   /**
    * Provides access to the connection info.
    * May return <code>null</code> if not logged into server successfully.
    * @see PSCoreFactory#getConnectionInfo()
    */
   private PSConnectionInfo getConnectionInfo()
   {
      return PSCoreFactory.getInstance().getConnectionInfo();
   }
   
   /**
    * Extracts url from editor input. Returns <code>null</code>
    * if the underlying  editor input can't provide URL. 
    */
   private URL getUrl()
   {
      return (URL) getEditorInput().getAdapter(URL.class);
   }

   @Override
   public void setFocus()
   {
      m_browser.setFocus();
   }

   /**
    * Mandated by implemented interface. Does nothing.
    * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
    */
   @SuppressWarnings("unused")
   @Override
   public void doSave(IProgressMonitor monitor)
   {
      // do nothing
   }

   /**
    * Mandated by implemented interface. Does nothing.
    * @see org.eclipse.ui.ISaveablePart#doSaveAs()
    */
   @Override
   public void doSaveAs()
   {
      // do nothing
   }

   /**
    * Always return <code>false</code> indicating that editor is never dirty.
    */
   @Override
   public boolean isDirty()
   {
      return false;
   }

   /**
    * Always returns <code>false</code>.
    */
   @Override
   public boolean isSaveAsAllowed()
   {
      return false;
   }

   /**
    * Actual text of the autologin form sent to Rhythmyx server for automatic
    * login.
    */
   private static final String AUTOLOGIN_FORM =
         "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" + 
         "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + 
         "<head>\n" + 
         "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />\n" + 
         "<title>Autologin form</title>\n" + 
         "\n" + 
         "<script type=\"text/javascript\">\n" + 
         "function submitForm() {\n" + 
         "  var forms = document.getElementsByTagName(\"form\");\n" + 
         "  forms[0].submit()\n" + 
         "}\n" + 
         "</script>\n" + 
         "\n" + 
         "</head>\n" + 
         "\n" + 
         "<body onload=\"submitForm()\">\n" + 
         "\n" + 
         "<form action=\"%s\" method=\"post\">\n" + 
         "  <fieldset>\n" + 
         "    <input name=\"j_username\" type=\"hidden\" value=\"%s\"/>\n" + 
         "    <input name=\"j_password\" type=\"hidden\" value=\"%s\"/>\n" + 
         "  </fieldset>\n" + 
         "</form>\n" + 
         "\n" + 
         "</body>\n" + 
         "</html>";

   /**
    * ID the editor is registered with in Eclipse. 
    */
   public static final String ID = PSRhythmyxPageEditor.class.getName();
   
   /**
    * Empty space around browser buttons.
    */
   private static int BUTTON_PADDING = 5;

   /**
    * Browser component displaying the specified html page.
    * Not <code>null</code> after UI initialization.
    */
   private Browser m_browser;
   
   /**
    * Control containing all the browser controls.
    * Is used with {@link #m_pager} to make browser visible.
    * Not <code>null</code> after UI initialization.
    */
   private Composite m_browserContainer;
   
   /**
    * Shows loading page indicator or browser control after the page is loaded.
    */
   private StackLayout m_pager;
}
