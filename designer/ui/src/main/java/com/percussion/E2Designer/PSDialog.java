/******************************************************************************
 *
 * [ PSDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.tools.help.PSJavaHelp;
import com.percussion.workbench.ui.help.PSHelpManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The Base class for all future e2 dialogs.  PSDialog has a ValidationFramework
 * for validating programmer specified components.  This is to make sure that
 * users do not enter incorrect values for that component.
 *
 * @see ValidationFramework
 */
public class PSDialog extends JDialog
{
   // mimic all constructors of JDialog
   /**
    * The basic dialog is modal, non-resizable. If the properties file has an
    * key called "title", its value is read in and the title of the dialog is
    * set to this value.   The main frame is set as the owner.
    */
   public PSDialog()
   {
      super((JFrame) null);
      init(true);
   }

   public PSDialog(String title)
   {
      super();
      setTitle(title);
      init(false);
   }

   public PSDialog(Dialog d)
   {
      super(d, true);
      init( true );
   }

   public PSDialog(Dialog d, String title)
   {
      super(d, title, true);
      init( false );
   }

   public PSDialog(Window f)
   {
      super(f);
      init( true );
   }

   public PSDialog(Frame f, String title)
   {
      super(f, title, true);
      init( false );
   }

   /** Returns a reference to the current ValidationFramework.
    *
    */
   public ValidationFramework getValidationFramework()
   { return m_componentTest; }

   /**
    * Reinitialized the ValidationFramework within the PSDialog.
    *
    * @param
    *           c an array of Component
    *           v an array of ValidationConstraint
    */
   public void setValidationFramework(Component[] c, ValidationConstraint[] v)
   {
      m_componentTest.setFramework(this, c, v);
   }

   /** Starts the validation process.  Framework MUST be instantiated prior to this
    * call.  Typically used in a ActionListener method in response to an action
    * generated by a button.
    *
    * @return a boolean that either permits the method to stop or go further.
    */
   public boolean activateValidation()
   {
      return m_componentTest.checkValidity();
   }

   /**
    * Centers the dialog on the screen, based on its current size.
    */
   public void center()
   {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = getSize();
      setLocation(( screenSize.width - size.width ) / 2,
            ( screenSize.height - size.height ) / 2 );
   }

   /** The action performed by the Ok button. This is to be overridden by
    * subclasses that implements the actual functionality. Dialog closing action
    * must be handled here as well (ie: dispose(); ).
    */
   public void onOk()
   {}

   /**
    * The action performed by the Cancel/Close button.  This is to be overridden
    * by subclasses that implements the actual functionality. Dialog closing
    * action must be handled here as well for anything more than calling
    * dispose().
    * <P>
    * <B>NOTE</B>: The method name <BIG>DOES NOT</BIG> mean it is only for the
    * &quot;Cancel&quot; button.  It can be called and used to handle
    * &quot;Close&quot; actions as well.  It is up to the discretion of the
    * programmer. Simply keep the idea, that this is for cancel/closing operation,
    * consistent.
    */
   public void onCancel()
   {
      setVisible(false);
      dispose();
   }

   /**
    * The action performed by the Help button. If the child dialog does not
    * have help, override <CODE>onHelp</CODE> and do nothing. Launches JavaHelp
    * viewer to display the help.
    */
   public void onHelp()
   {      
      // Need to test and see if we are in the eclipse environment
      // to determine which way to display help.
      try
      {
         Class.forName("org.osgi.framework.BundleActivator");
      }
      catch (ClassNotFoundException e)
      {
         // We are not in an the eclipse framework so use old help
         String fullname = getClass().getName();
         String helpId = fullname.substring( fullname.lastIndexOf(".") + 1);
         System.out.println("  " + helpId );
         PSJavaHelp.launchHelp( subclassHelpId( helpId ), false, this);
         return;
      }
      PSHelpManager.displayHelpFromLegacy(
         subclassHelpId(getClass().getName()));
   }
   
   /**
    * If the supplied table is currently being edited this stops the edit
    * mode to make sure that the changes of the currently edited cell are
    * accepted.
    * 
    * @param table the table to stop the edit mode for, may be 
    *    <code>null</code> in which case this does nothing.
    */
   public void stopTableEditor(JTable table)
   {
      if (table != null && table.isEditing())
      {
         TableCellEditor editor = table.getCellEditor();
         editor.stopCellEditing();
      }
   }

   /**
    * By default, the base class name is used as the help id. However, if a dialog
    * needs to support more than 1 help page, then it should modify the id.
    * Typically, an integer will be appended to differentiate among the multiple
    * help pages associated with a single dialog.
    *
    * @param helpId The key to use in the help id mapping resource. This must
    * always be a valid, non-empty string.
    *
    * @return The passed in Id, unmodified.
   **/
   protected String subclassHelpId( String helpId )
   {
      return helpId;
   }

   /**
    * Utility method for all PSDialog subclasses to retrieve its ResourceBundle.
    *
    * @return ResourceBundle, may be <code>null</code>.
    */
   protected ResourceBundle getResources()
   {
      try {
         if ( null == m_res )
            m_res = ResourceBundle.getBundle( getResourceName(),
                       Locale.getDefault() );
      }
      catch(MissingResourceException mre)
      {
         mre.printStackTrace();
      }
      return m_res;
   }

   /**
    * Gets resource file name for this class.
    *
    * @return resource file name, never <code>null</code> or empty.
    **/
   protected String getResourceName()
   {
      return getClass().getName() + "Resources";
   }

   /**
    * Overridden method to display the dialog only if it is properly
    * initialized. Also adds the default listeners as component initialization
    * is guaranteed to be complete.
    *
    * @see Component#setVisible(boolean)
    **/
   @Override
   public void setVisible(boolean visible)
   {
      if(visible && m_bInitialized)
      {
         addDefaultKeyListeners(this);
         if (PSMainFrameInitFlag.isMainFrameInitialized())
         {
            E2Designer.getApp().getMainFrame().registerDialog(this);
         }
         super.setVisible(visible);
      }
      else
      {
         /* The following code is added to avoid the help viewer getting closed
          * when this dialog is closed if it is visible before this dialog is
          * activated the help.
          */
         PSJavaHelp helpViewer = PSJavaHelp.getInstance();
         if(helpViewer.isHelpDisplayed())
         {
            helpViewer.setParent(null, true);
         }
         super.setVisible(false);
      }
   }
   
   /**
    * Overriden to add the default listeners
    */
   @Override
   public void pack()
   {
       addDefaultKeyListeners(this);
       super.pack();       
   }

   /**
    * A service method to set mnemonics on a tab panel
    * This method can be used if the convention of resourcename.mn is used 
    * for adding mnemonics in the resource bundle
    * @param tabPane the tabbed pane on which to set the mnemonic
    * @param res the resource bundle cannot be <code>null</code>
    * @param resId the resource id from the bundle cannot be <code>null</code>.
    * @param tabIx is the tab index on which a mnemonic has to be set 
    *        valid values are between 0 and number of tabs
    */
   public void setMnemonicForTabIndex(JTabbedPane tabPane, ResourceBundle res, 
                                      String resId, int tabIx)
   {
      if ( res == null )
         throw new IllegalArgumentException(
             "PSDialog.setMnemonicForTabIndex: Resource Bundle cannot be null");
      if ( resId == null )
         throw new IllegalArgumentException(
            "PSDialog.setMnemonicForTabIndex: Resource id cannot be null");
      if ( tabIx < 0 || tabIx > tabPane.getTabCount())
         throw new IllegalArgumentException(
         "PSDialog.setMnemonicForTabIndex: tab index can be between 0 and " + 
         tabPane.getTabCount());
      String tabName = res.getString(resId);
      char mn = res.getString(resId+".mn").charAt(0);
      char mnUpperCase = (""+mn).toUpperCase().charAt(0);
      tabPane.setMnemonicAt(tabIx, mnUpperCase);
      tabPane.setDisplayedMnemonicIndexAt(tabIx, tabName.indexOf(mn));
   }

   /**
    * Creates a compound border used on panels that group controls. The outer
    * border is a titled border. The inner border is an empty border. The
    * empty border is added because insufficient space is left between
    * components and the line of the titled border. This is a utility method
    * for derived classes, it is not used by this class.
    *
    * @param title The title for the border. <code>null</code> is the same as
    * the empty string.
    */
   public static Border createGroupBorder( String title )
   {
      if ( null == title )
         title = "";
      Border tb = BorderFactory.createTitledBorder(
         new EtchedBorder( EtchedBorder.LOWERED ), title );
      // leave more space between the titled border and the components
      Border eb = BorderFactory.createEmptyBorder( 0, 5, 5, 5 );
      return BorderFactory.createCompoundBorder( tb, eb );
   }


   /**
    * Creates a box set up with a label that is right justified, with glue
    * on the left side. The supplied component is used as the component that
    * this label is being used for.
    *
    * @param text The text for the label.
    *
    * @param labelFor The control that this label describes.
    */
   public static Box createLabel( String text, JComponent labelFor )
   {
      UTFixedLabel label = new UTFixedLabel( text, SwingConstants.RIGHT );
      if ( null != labelFor )
         label.setLabelFor( labelFor );
      Box panel = Box.createHorizontalBox();
      panel.add( Box.createHorizontalGlue());
      panel.add( label );
      return panel;
   }

  /**
   * Recursively adds the default key listener to the component
   * passed in and all child components. The default listener listens for
   * the escape and f1 keys which should have the same behaviour in all dialogs
   * i.e. escape calls <code>onCancel()</code> and f1 call onHelp()</code>.
   *
   * This method is called by {@link #setVisible(boolean)} or {@link #pack()}
   * and can also be explicitly called by a subclass.
   *
   * @param comp component to add the listeners to. Cannot be <code>null</code>.
   */
  protected void addDefaultKeyListeners(Component comp)
  {
      if(null == comp)
         throw new IllegalArgumentException("Component cannot be null.");
      if(comp instanceof Container &&
         ((Container)comp).getComponentCount() > 0)
      {
          Component[] children = ((Container)comp).getComponents();
          for(int i = 0; i < children.length; i++)
             addDefaultKeyListeners(children[i]);
      }
      
      comp.addKeyListener(m_defaultKeyListener);
  }
  
 
  
 
//
// PRIVATE METHODS
//

   /**
    * Initializes the dialog, setting default values.
    *
    * @param bSetTitle if <code>true</code>, tries to load the title from the
    * resource bundle using the key "title". If found, it then sets the title
    * to this value.
    */
   private void init( boolean bSetTitle )
   {
      setAlwaysOnTop(true);
      setAutoRequestFocus(true);
      setModal(true);
      setResizable(true);
      setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter()
      {
         @Override
         public void windowClosing(@SuppressWarnings("unused") WindowEvent e)
         {
            onCancel();
         }
      });

      m_componentTest = new ValidationFramework();

      // Create the default key listener which will
      // listen for the escape key and f1 key in all
      // components of this dialog.
      m_defaultKeyListener = new KeyAdapter()
      {
         @Override
         public void keyReleased(KeyEvent e)
         {
            if (KeyEvent.VK_ESCAPE == e.getKeyCode())
               onCancel();
            if (KeyEvent.VK_F1 == e.getKeyCode())
               onHelp();

         }
      };

      try
      {
         if ( bSetTitle )
         {
            if(getResources() != null)
                setTitle(getResources().getString("title"));
         }
      }
      catch ( MissingResourceException e )
      {
         // don't do anything, the derived class didn't add a title key
      }
   }

   /**
    * Flag to indicate this dialog initialization. Initialized to
    * <code>true</code> and set to <code>false</code> when error happens in
    * derived class initialization.
    **/
   protected boolean m_bInitialized = true;

   /**
    * Key listener that listens for escape and f1 key
    * presses. If escape is caught then onCancel is called
    * if f1 is caught then onHelp is called. Initialized in
    * {@link #init(boolean)}
    */
   private KeyAdapter m_defaultKeyListener;
   private ValidationFramework m_componentTest;
   private ResourceBundle m_res = null;
}

