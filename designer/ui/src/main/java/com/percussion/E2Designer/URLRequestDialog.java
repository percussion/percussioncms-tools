/*[ URLRequestDialog.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.extension.PSExtensionRef;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * This dialog is used to creating PSUrlRequest objects.  It consists
 * of a name text field, two radio buttons for type, either a text area or
 * combo boxes for the base href, and a parameter table.
 *
 * @todo add a constructor for editing?
 */
public class URLRequestDialog extends LookupRequestDialog
{
   /**
    * Initializes a newly created <code>URLRequestDialog</code> object with
    * default values.
    */
   public URLRequestDialog()
   {
      super();
   }


   /**
    * Lays out the center panel (main panel) of the dialog.
    *
    * <pre><code>
    * +------------------------------------+
    * | +-------------------------+ (Box)  | (NORTH)
    * | | {@link #createNamePanel}         |        |
    * | | createTypeChoicePanel() |        |
    * | | createBaseHrefPanel()   |        |
    * | | {@link #createAppRsrcPanel}      |        |
    * | +-------------------------+        |
    * +------------------------------------+
    * | {@link #createParamTablePanel}              | (CENTER)
    * +------------------------------------+
    * </code></pre>
    *
    * @return new JPanel with BorderLayout manager that contains the objects
    */
   protected JPanel createCenterPanel()
   {
      Box northPanel = new Box( BoxLayout.Y_AXIS );

      northPanel.add( createNamePanel() );
      northPanel.add( createTypeChoicePanel() );

      m_baseHrefPanel = createBaseHrefPanel();
      m_appRsrcPanel = createAppRsrcPanel();
      northPanel.add( m_baseHrefPanel );
      northPanel.add( m_appRsrcPanel );

      /* NOTE: even though only one of m_baseHrefPanel and m_appRsrcPanel can
         be visible at a time, the BoxLayout reserves vertical space for both.
         This results in a slightly taller dialog than necessary.
       */

      JPanel panel = new JPanel( new BorderLayout( 0, 5 ) );
      panel.add( northPanel, BorderLayout.NORTH );
      panel.add( createParamTablePanel(), BorderLayout.CENTER );
      return panel;
   }

   /**
    * Performs basic initialization of this dialog by creating its UI
    * components (layout is delegated to the superclass).
    */
   protected void init()
   {
      // create the controls used in this class
      m_nameEditor = new JTextField();
      m_baseHrefEditor = new JTextField();

      // create the controls used in the superclass
      super.init();
   }


   /**
    * Modifies this dialog's help id to reflect which type radio button is
    * currently selected.
    *
    * @param helpId The base of the key to use in the help id mapping resource,
    * assumed not <code>null</code> and not empty.
    * @return <code>helpId</code> appended with an underscore and a string
    * representing the selected type radio button.  Never <code>null</code> or
    * empty.
    */
   protected String subclassHelpId(String helpId)
   {
      StringBuffer myHelpId = new StringBuffer( helpId );
      myHelpId.append("_").append( getType2() );
      return myHelpId.toString();
   }


   /**
    * Processes the OK button by performing any necessary validations, and if
    * they pass, disposing this dialog.
    */
   public void onOk()
   {
      // TODO: validations, such as:
      // 1. base href not empty for external
      // 2. no param names w/o values and vice versa ?
      super.onOk();
   }

   /**
    * Creates and lays out the objects that make the URL request name: a JLabel
    * and a JTextField within a horizontal BorderLayout.
    *
    * <pre><code>
    *   (WEST)    (CENTER)
    * +-------+------------+
    * | Name: | JTextField |
    * +-------+------------+
    * </code></pre>
    *
    * @return a new JPanel that contains all the UI objects
    */
   protected JPanel createNamePanel()
   {
      JLabel nameLabel = new JLabel( resource( "Name" ) );
      nameLabel.setVerticalAlignment( SwingConstants.TOP );
      nameLabel.setHorizontalAlignment( SwingConstants.RIGHT );
      nameLabel.setLabelFor( m_nameEditor );

      JPanel panel = new JPanel( new BorderLayout( 5, 0 ) );
      panel.setAlignmentX( LEFT_ALIGNMENT );
      panel.add( nameLabel, BorderLayout.WEST );
      panel.add( m_nameEditor, BorderLayout.CENTER );
      return panel;
   }


   /**
    * Creates and lays out the objects that make the base href: a JLabel
    * and a JTextArea within a horizontal BorderLayout.
    *
    * <pre><code>
    *   (WEST)       (CENTER)
    * +------------+------------+
    * | Base href: | JTextArea  |
    * |            |            |
    * +------------+------------+
    * </code></pre>
    *
    * @return a new JPanel that contains all the UI objects
    */
   private JPanel createBaseHrefPanel()
   {
      JLabel hrefLabel = new JLabel( resource( "BaseHref" ) );
      hrefLabel.setVerticalAlignment( SwingConstants.TOP );
      hrefLabel.setHorizontalAlignment( SwingConstants.RIGHT );
      hrefLabel.setLabelFor( m_baseHrefEditor );

      JPanel panel = new JPanel( new BorderLayout( 5, 0 ) );
      panel.setAlignmentX( LEFT_ALIGNMENT );
      panel.add( hrefLabel, BorderLayout.WEST );
      panel.add( m_baseHrefEditor, BorderLayout.CENTER );
      return panel;
   }


   /**
    * Sets the selected button in a ButtonGroup by matching on the specified
    * action command.  If the action command is not found within the group,
    * the selection state will not be changed.
    *
    * @param group the group of buttons being acted on; assumed not <code>null
    * </code>
    * @param command the action command of the button to be selected; assumed
    * not <code>null</code>
    */
   private void setButton(ButtonGroup group, String command)
   {
      Enumeration buttons = group.getElements();
      while (buttons.hasMoreElements())
      {
         JRadioButton button = (JRadioButton) buttons.nextElement();
         if (button.getActionCommand().equals( command ))
         {
            button.setSelected( true );
            break;
         }
      }
   }


   /**
    * Creates a new radio button for setting the type of the URL request.
    *
    * @param group a group to assign the new button to; assumed not <code>null
    * </code>
    * @param label assigned to the new button's text; assumed not <code>null
    * </code>
    * @param actionCommand assigned to the new button's ActionCommand; assumed
    * not <code>null</code>
    *
    * @return a new JRadioButton whose ActionListener calls {@link #setType}
    */
   private JRadioButton createTypeButton(ButtonGroup group, String label,
                                         String actionCommand)
   {
      JRadioButton button = new JRadioButton( label );
      button.setAlignmentX( LEFT_ALIGNMENT );
      button.setActionCommand( actionCommand );
      button.addActionListener( new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            setType( e.getActionCommand() );
         }
      } );
      group.add( button );
      return button;
   }


   /**
    * Creates the panel used to select the URL request type.  Consists of two
    * radio buttons in a titled panel.
    * <pre><code>
    *  +-------------+
    *  | o External  |
    *  | o Internal  |
    *  +-------------+
    *   (Y_AXIS BoxLayout)
    * </code></pre>
    *
    * @return a new JPanel using BoxLayout to arrange the UI objects
    */
   private JPanel createTypeChoicePanel()
   {
      m_typeButtons = new ButtonGroup();
      JRadioButton externalBtn = createTypeButton( m_typeButtons,
            resource( "ExternalBtn" ), TYPE_EXTERNAL );
      JRadioButton internalBtn = createTypeButton( m_typeButtons,
            resource( "InternalBtn" ), TYPE_INTERNAL );

      JPanel panel = new JPanel();
      panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
      panel.setAlignmentX( LEFT_ALIGNMENT );
      panel.setBorder( BorderFactory.createTitledBorder( resource( "Type" ) ) );
      panel.add( externalBtn );
      panel.add( internalBtn );
      return panel;
   }


   // see interface for description
   public void reset()
   {
      super.reset();
      m_nameEditor.setText( "" );
      m_baseHrefEditor.setText( "" );
      setType( TYPE_EXTERNAL );
   }


   /**
    * Checks to see if the specified object is editable by this dialog.
    *
    * @return <code>true</code> if <code>model</code> instanceof <code>
    * PSUrlRequest</code> with an extension call whose extension reference
    * is either <code>sys_MakeAbsLink</code> or <code>sys_MakeIntLink</code>;
    * <code>false</code> otherwise.
    */
   public boolean isValidModel(Object model)
   {
      return myIsValidModel( model );
   }


   /**
    * Checks to see if the specified object is editable by this dialog.  This
    * method is used by <code>setData</code> because it cannot be overriden.
    * (If <code>setData</code> called <code>isValidModel</code>, then if
    * a derived class called <code>super.setData</code>, the super class might
    * use the derived class' <code>isValidModel</code> instead -- which would
    * cause a false result if the derived class had different requirements than
    * the super class.)
    *
    * @return <code>true</code> if <code>model</code> instanceof <code>
    * PSUrlRequest</code> with an extension call whose extension reference
    * is either <code>sys_MakeAbsLink</code> or <code>sys_MakeIntLink</code>;
    * <code>false</code> otherwise.
    */
   private boolean myIsValidModel(Object model)
   {
      if (super.isValidModel( model ))
      {
         PSUrlRequest request = (PSUrlRequest) model;
         PSExtensionCall udf = request.getConverter();
         if (udf != null)
         {
            String canonicalName = udf.getExtensionRef().getFQN();
            if (canonicalName.equals( MAKE_ABS_LINK ) ||
                canonicalName.equals( MAKE_INT_LINK ))
               return true;
         }
      }
      return false;
   }


   /**
    * Sets the dialog to an edit state, loading the values from the specified
    * model into the controls.
    *
    * @param model object to be edited by the dialog; cannot be <code>null
    * </code>
    */
   public void setData(Object model)
   {
      if (!myIsValidModel( model ))
         throw new IllegalArgumentException( "Cannot provide a invalid model" );

      PSUrlRequest request = (PSUrlRequest) model;
      PSExtensionCall udf = request.getConverter(); // never null in this case

      // convert the 1D array into a 2D table
      PSCollection parameters = new PSCollection( PSParam.class );
      PSExtensionParamValue[] extParams = udf.getParamValues();
      if (extParams.length >= 1) // href is first param
      {
         String href = extParams[0].getValue().getValueDisplayText();
         request.setHref( href );
         m_baseHrefEditor.setText( href );
      }
      for (int i=1; i < extParams.length-1; i+=2)
      {
         String name = extParams[i].getValue().getValueDisplayText();
         if (name.trim().length() != 0)
            parameters.add( new PSParam( name, extParams[i+1].getValue() ) );
      }
      request.setQueryParameters( parameters ); // copied from call to request

      super.setData( request );
      m_nameEditor.setText( request.getName() );

      // determine the type
      String canonicalName = udf.getExtensionRef().getFQN();
      if (canonicalName.equals( MAKE_ABS_LINK ))
         setType( TYPE_EXTERNAL );
      else if (canonicalName.equals( MAKE_INT_LINK ))
         setType( TYPE_INTERNAL );
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
   public Object getData()
   {
      if (OKButtonPressed())
      {
         PSUrlRequest request = m_model;
         String name = null;
         if (m_nameEditor.getText().trim().length() > 0)
            name = m_nameEditor.getText().trim();

         PSCollection parameters = m_paramTable.getParameters();
         PSExtensionParamValue[] paramValues =
               new PSExtensionParamValue[(2 * parameters.size()) + 1];
         int index = 0;

         try
         {
            PSExtensionRef ref;
            if (getType2().equals( TYPE_INTERNAL ))
            {
               ref = new PSExtensionRef( MAKE_INT_LINK );
               // baseHref is the first parameter to the make link functions
               paramValues[index++] = new PSExtensionParamValue(
                     new PSTextLiteral( getInternalHref() ) );
            }
            else
            {
               ref = new PSExtensionRef( MAKE_ABS_LINK );
               // baseHref is the first parameter to the make link functions
               paramValues[index++] = new PSExtensionParamValue(
                     new PSTextLiteral( m_baseHrefEditor.getText().trim() ) );
            }

            // convert the 2D table into a 1D array
            for (Iterator iter = parameters.iterator(); iter.hasNext();)
            {
               PSParam param = (PSParam) iter.next();
               paramValues[index++] =  new PSExtensionParamValue(
                     new PSTextLiteral( param.getName() ) );
               paramValues[index++] =  new PSExtensionParamValue(
                     param.getValue() );
            }
            PSExtensionCall udf = new PSExtensionCall( ref, paramValues );
            if (null == request)
               request = new PSUrlRequest( name, udf );
            else
            {
               request.setName( name );
               request.setConverter( udf );
            }
            return request;
         } catch (IllegalArgumentException e)
         {
            throw new IllegalArgumentException( e.getMessage() );
         }
      }
      else
      {
         // if cancel button was pressed, return null
         return null;
      }
   }


   /**
    * @return the action command of the currently selected URL request type
    * button:  either {@link #TYPE_EXTERNAL} or {@link #TYPE_INTERNAL}
    */
   private String getType2()
   {
      return m_typeButtons.getSelection().getActionCommand();
   }


   /**
    * Sets the URL request type by selecting the appropriate radio button and
    * by hiding and exposing the relevant base href controls.
    *
    * @param type either {@link #TYPE_EXTERNAL} or {@link #TYPE_INTERNAL}
    * assumed not <code>null</code>
    */
   private void setType(String type)
   {
      setButton( m_typeButtons, type );
      if (type.equals( TYPE_EXTERNAL ))
      {
         m_baseHrefPanel.setVisible( true );
         m_appRsrcPanel.setVisible( false );
      }
      else if (type.equals( TYPE_INTERNAL ))
      {
         m_baseHrefPanel.setVisible( false );
         m_appRsrcPanel.setVisible( true );
      }
      else
      {
         throw new IllegalArgumentException("unknown value supplied for type");
      }
   }


   /**
    * Control for the input of a base HREF for building a URL.  Set in
    * {@link #init}, and never <code>null</code> after that.
    */
   private JTextComponent m_baseHrefEditor;

   /**
    * Contains an optional identifier for this request. It should be unique
    * within the content editor in which it is used. Set in {@link #init}, and
    * never <code>null</code> after that.
    */
   private JTextField m_nameEditor;

   /**
    * Contains the base href text area and label.  Needs to be a class variable
    * so it can be hidden/exposed based on type. Set in
    * {@link #createCenterPanel}, and never <code>null</code> after that.
    */
   private JPanel m_baseHrefPanel;

   /**
    * Contains the application and resource name combo boxes.  Needs to be
    * a class variable so it can be hidden/exposed based on type. Set in
    * {@link #createCenterPanel}, and never <code>null</code> after that.
    */
   private JPanel m_appRsrcPanel;

   /** Holds the radio buttons used to set the URL request type */
   private ButtonGroup m_typeButtons;

   /** Constant for an external (used by browser) URL request */
   private static final String TYPE_EXTERNAL = "EXTERNAL";

   /** Constant for an internal (used by server) URL request */
   private static final String TYPE_INTERNAL = "INTERNAL";

   /** Name of the UDF that creates internal links */
   protected static final String MAKE_INT_LINK =
      "Java/global/percussion/generic/sys_MakeIntLink";

   /** Name of the UDF that creates external links */
   protected static final String MAKE_ABS_LINK =
      "Java/global/percussion/generic/sys_MakeAbsLink";
}
