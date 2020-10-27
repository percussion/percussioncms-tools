/******************************************************************************
 *
 * [ LookupRequestDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

/**
 * Provides a dialog for creating and editing PSUrlRequests to specific
 * Rhythmyx application pages.  The Rhythmyx application page is selected
 * through ComboBoxes and a table is provided for entering URL parameter names
 * and values.
 */
public class LookupRequestDialog extends PSDialogAPI
{
   /**
    * Initializes a newly created <code>LookupRequestDialog</code> object with
    * default values.
    */
   public LookupRequestDialog()
   {
      super((JFrame)null);
      init();
      reset();
   }


   /**
    * Performs basic initialization of this dialog by creating the UI
    * components, then packing and centering.
    *
    * <pre><code>
    *   (CENTER)              (EAST)
    * +---------------------+----------------------+
    * | {@link #createCenterPanel()} | createCommandPanel() |
    * +---------------------+----------------------+
    * </code></pre>
    */
   protected void init()
   {
      try
      {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e)
      {
         //This should not happen as it comes from the swing package
         e.printStackTrace();
      }
      setResizable( true );

      JPanel panel = new JPanel();
      getContentPane().add( panel );

      panel.setLayout( new BorderLayout( 5, 5 ) );
      panel.setBorder( new EmptyBorder( 5, 10, 5, 10 ) );

      panel.add( createCommandPanel(), BorderLayout.EAST );
      panel.add( createCenterPanel(), BorderLayout.CENTER );

      pack();
      center();
   }


   /**
    * Lays out the center panel (main panel) of the dialog.
    *
    * <pre><code>
    *         +---------------------------------+
    * (NORTH) | {@link #createAppRsrcPanel()}            |
    *         +---------------------------------+
    * (CENTER)| {@link #createParamTablePanel()}         |
    *         +---------------------------------+
    * </code></pre>
    * @return valid JPanel with BorderLayout manager that contains the objects
    */
   protected JPanel createCenterPanel()
   {
      JPanel panel = new JPanel( new BorderLayout( 5, 5 ) );
      panel.add( createAppRsrcPanel(), BorderLayout.NORTH );
      panel.add( createParamTablePanel(), BorderLayout.CENTER );
      return panel;
   }


   /**
    * Gets the resource string identified by the specified key.  If the
    * resource cannot be found, the key itself is returned.
    *
    * @param key identifies the resource to be fetched; assumed not
    *            <code>null</code>
    * @return String value of the resource identified by <code>key</code>, or
    *         <code>key</code> itself.
    */
   protected String resource(String key)
   {
      String resourceValue = key;
      try
      {
         if (getResources() != null)
            resourceValue = getResources().getString( key );
      } catch (MissingResourceException e)
      {
         // not fatal; warn and continue
         System.err.println( this.getClass() );
         System.err.println( e );
      }
      return resourceValue;
   }


   /**
    * Creates and lays out the objects that make up the parameter table.
    *
    * <pre><code>
    *   (WEST)    (CENTER)
    * +---------+-----------------------------+
    * | Params: | (JScrollPane)               |
    * |         | +-------------------------+ |
    * |         | | {@link ParameterNameValueTable} | |
    * |         | +-------------------------+ |
    * +---------+-----------------------------+
    * </code></pre>
    * @return valid JPanel with BorderLayout manager that contains the objects
    */
   protected JPanel createParamTablePanel()
   {
      // create table and scroll pane
      m_paramTable = new ParameterNameValueTable( 12 );
      JScrollPane pane = new JScrollPane( m_paramTable );
      pane.setPreferredSize( new Dimension( 250, 150 ) );

      ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(
            (OSBackendDatatank) null, null );
      m_paramTable.addValueDialog( new ValueSelectorDialog( this,
            h.getDataTypes(), null ), resource( "Other" ) );

      // create top-justified tabel
      JLabel resourceNameLabel = new JLabel( resource( "Params" ) );
      resourceNameLabel.setLabelFor( m_paramTable );
      resourceNameLabel.setHorizontalAlignment( SwingConstants.RIGHT );
      resourceNameLabel.setVerticalAlignment( SwingConstants.TOP );

      // layout
      JPanel panel = new JPanel( new BorderLayout( 5, 0 ) );
      panel.add( resourceNameLabel, BorderLayout.WEST );
      panel.add( pane, BorderLayout.CENTER );
      return panel;
   }


   /**
    * Creates the standard three buttons command panel (OK, Cancel, Help), plus
    * a Remove button for use with the parameter table.
    *
    * <pre><code>
    * +------------+
    * | (Box)      | (NORTH)
    * | +--------+ |
    * | | OK     | |
    * | | Cancel | |
    * | | Help   | |
    * | +--------+ |
    * +------------+
    * |   Remove   | (SOUTH)
    * +------------+
    * </code></pre>
    * @return valid JPanel with BorderLayout manager that contains the objects
    * @see UTStandardCommandPanel
    */
   private JPanel createCommandPanel()
   {
      JPanel commandPanel = new UTStandardCommandPanel( this, "",
            SwingConstants.VERTICAL )
      {
         @Override
         public void onOk()
         {
            LookupRequestDialog.this.onOk();
         }


         @Override
         public void onCancel()
         {
            LookupRequestDialog.this.onCancel();
         }
      };

      // create botton-justified remove button
      JButton removeButton = new JButton( resource( "Remove" ) );
      removeButton.addActionListener( new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            onRemove();
         }
      } );
      commandPanel.add( removeButton, BorderLayout.SOUTH );

      return commandPanel;
   }


   /**
    * Creates and lays out the objects that allow selection of a particular
    * Rhythmyx application and resource. 
    *
    * @return The resource panel, never <code>null</code>.
    */
   protected JPanel createAppRsrcPanel()
   {
      m_resourcePanel = new ResourceSelectionPanel();
      return m_resourcePanel;
   }

   /**
    * Gets the string value of the currently selected object in the application
    * name combo box.
    *
    * @return String value of selected application name; <code>null</code> if
    *         no selection
    */
   protected String getApplicationName()
   {
      return m_resourcePanel.getApplicationName();
   }


   /**
    * Gets the string value of the currently selected object in the resource
    * name combo box.
    *
    * @return String value of selected resource name; <code>null</code> if
    *         no selection
    */
   protected String getRequestPage()
   {
      return m_resourcePanel.getRequestPage();
   }


   /**
    * If the dialog was exited by pressing the OK button, returns an object
    * that encapsulates the information from this dialog.  If the dialog was
    * exited by pressing the Cancel button, returns <code>null</code>.
    * <p>
    * If an object was used to setup the panel (using {@link #setData}), that
    * object will be returned.  Otherwise, a new object will be created.
    *
    * @return PSUrlRequest that reflects the panel's data; or <code>null</code>
    * if the dialog was cancelled.
    */
   @Override
   public Object getData()
   {
      if (OKButtonPressed())
      {
         PSUrlRequest model = m_model;

         PSCollection parameters = m_paramTable.getParameters();
         System.out.println( "parameters.size() = " + parameters.size() );
         if (null == model)
            model = new PSUrlRequest( null, getInternalHref(), parameters );
         else
         {
            model.setName( null );
            model.setHref( getInternalHref() );
            model.setQueryParameters( parameters );
         }
         return model;
      }
      else
         return null;
   }

   /**
    * Builds a URL to the Rhythmyx application specified by the application
    * and request page combo boxes.
    *
    * @return non-empty String of the form ../&lt;appName>/&lt;rsrcName>
    */
   protected String getInternalHref()
   {
      String appName = getApplicationName();
      String resourceName = getRequestPage();
      StringBuffer href = new StringBuffer( RELATIVE_PREFIX );
      href.append( appName ).append( PATH_SEP ).append( resourceName );
      return href.toString();
   }


   /**
    * @return <code>true</code> if the dialog was exited by pressing the OK
    *         button; <code>false</code> otherwise.
    */
   protected boolean OKButtonPressed()
   {
      return m_isValid;
   }


   // see interface for description
   @Override
   public void reset()
   {
      m_isValid = false;
      // TODO: which application name should be selected, none or first?
      m_paramTable.setModel(null);
   }


   /**
    * Checks to see if the specified object is editable by this dialog.
    *
    * @return <code>true</code> if <code>model</code> instanceof <code>
    * PSUrlRequest</code>; <code>false</code> otherwise.
    */
   @Override
   public boolean isValidModel(Object model)
   {
      return myIsValidModel(model);
   }

   /**
    * Checks to see if the specified object is editable by this dialog.  This
    * method is private so that <code>setData</code> can call this method,
    * without being overriden.
    *
    * @return <code>true</code> if <code>model</code> instanceof <code>
    * PSUrlRequest</code>; <code>false</code> otherwise.
    */
   private boolean myIsValidModel(Object model)
   {
      return (model instanceof PSUrlRequest);
   }


   /**
    * Sets the dialog to an edit state, loading the values from the specified
    * model into the controls.
    *
    * @param model object to be edited by the dialog; cannot be <code>null
    * </code>
    * @throws IllegalArgumentException if <code>model</code> is invalid
    */
   @Override
   public void setData(Object model)
   {
      if (!myIsValidModel( model ))
         throw new IllegalArgumentException( "Cannot provide a invalid model" );

      m_model = (PSUrlRequest) model;
      m_isValid = false;

      String href = m_model.getHref();
      // parse the model's href to split into ../<appName>/<rsrcName>
      int pos = href.indexOf( RELATIVE_PREFIX );
      if (pos == 0)
      {
         StringTokenizer tok = new StringTokenizer( href, PATH_SEP );
         tok.nextToken(); // skip ".."
         if (tok.hasMoreTokens())
            m_resourcePanel.setApplicationName( tok.nextToken() );
         if (tok.hasMoreTokens())
            m_resourcePanel.setRequestPage( tok.nextToken() );
      }
      else
      {

         // href is not in the expected format, so set combos to first items
         // pop up a warning message, so the user would correct the url
         E2Designer designer = E2Designer.getApp();
         UIMainFrame mainFrame = null;
         if (designer != null)
            mainFrame = designer.getMainFrame();

         JOptionPane.showMessageDialog(mainFrame, resource("wrong_href_format_warn"),
                                       resource("title_warn"),
                                       JOptionPane.WARNING_MESSAGE);


         m_resourcePanel.setApplicationName( null );
         m_resourcePanel.setRequestPage( null );
      }

      m_paramTable.setParameters( m_model.getQueryParameters() );
   }


   /**
    * Processes the OK button by performing any necessary validations, and if
    * they pass, disposing this dialog.
    */
   @Override
   public void onOk()
   {
      m_isValid = true;
      dispose();
   }


   /**
    * Processes the cancel button by disposing this dialog.
    */
   @Override
   public void onCancel()
   {
      m_isValid = false;
      dispose();
   }


   /**
    * Processes clicking the remove button by removing the selected rows from
    * the parameter table
    */
   protected void onRemove()
   {
      m_paramTable.removeSelectedRows();
   }

   /** The string that indicates a relative url */
   private static final String RELATIVE_PREFIX = "../";

   /** The character that separates each part of the base url */
   private static final String PATH_SEP = "/";

   /** The parameter name / value table */
   protected ParameterNameValueTable m_paramTable;

   /**
    * Remembers whether the OK or Cancel button was selected to exit the
    * dialog.  Data is only available when dialog was OK'ed.
    */
   private boolean m_isValid = false;

   /**
    * The object used to setup the dialog.  Any changes in the dialog will be
    * applied to this object when it is fetched from {@link #getData}.
    * <p>
    * Applying changes to the same object (instead of always creating a new
    * object), ensures that we won't lose data that the dialog doesn't know
    * about.
    * <p>
    * May be <code>null</code>, in which case a new object is created in
    * {@link #getData}.
    */
   protected PSUrlRequest m_model = null;

   /**
    * The app/resource selection panel, initialized during construction, never
    * <code>null</code> after that.
    */
   private ResourceSelectionPanel m_resourcePanel;

}
