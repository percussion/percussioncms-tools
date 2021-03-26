/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.htmlConverter;

import com.percussion.tools.help.PSJavaHelp;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.xml.transform.TransformerException;

import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class MainFrame extends JFrame implements CaretListener
{

   /* Menu/toolbar model.
      A map contains all Actions that will be used in the menu and/or toolbar,
      except those that need JMenuCheckBox.
      The key to each entry in the map will be the internal name of the action,
      the value will be the Action object.
      The menu is built using the desired actions in the desired order, as is
      the toolbar.
      To add new menuitems/toolbar buttons, add the action, then add the action
      to the menu/toolbar as needed. */

   /* Action names. The names must be unique within the set of all actions. The
   names should not be tied to any particular main menu item. */
   private static final String NEW = "new";
   private static final String OPEN = "open";
   private static final String SAVEAS = "saveas";
   private static final String SAVEALL = "saveall";
   private static final String EXIT = "exit";

   private static final String UNDO = "undo";
   private static final String REDO = "redo";
   private static final String CUT = "cut";
   private static final String COPY = "copy";
   private static final String PASTE = "paste";
   private static final String DELETE = "delete";
   private static final String SELECT_ALL = "selectAll";
   private static final String FIND = "find";
   private static final String FIND_NEXT = "findNext";
   private static final String REPLACE = "replace";
   private static final String GOTO = "goto";
   private static final String RESET_ERROR = "resetError";

   private static final String SPLIT = "split";
   private static final String MERGE = "merge";
   private static final String OPTIONS = "options";
   private static final String LAUNCH_BROWSER = "launch";

   private static final String HELP = "help";
   private static final String ABOUT = "about";

   private static final String TOGGLE_VIEW_TIDIED_HTML = "tidied";
   private static final String TOGGLE_VIEW_XSL = "xsl";
   private static final String TOGGLE_VIEW_DTD = "dtd";
   private static final String TOGGLE_VIEW_FORMS = "forms";
   private static final String TOGGLE_VIEW_XML = "xml";
   private static final String TOGGLE_VIEW_MERGED_HTML   = "merged";
   private static final String TOGGLE_VIEW_RENDERED_HTML = "rendered";
   private static final String FORMAT = "formatSource";

   /* Internal names for all tabs. They must be unique within the set of tabs.
      These names are used whenever a tab needs to be processed in some way. */
   private static final String SOURCE_HTML = "srcHtml";
   private static final String TIDIED_HTML = "tidyHtml";
   private static final String XSL = "xsl";
   private static final String DTD = "dtd";
   private static final String FORMS_DTD = "forms";
   private static final String LOG = "log";
   private static final String XML = "xml";
   private static final String MERGED_HTML = "mergedHtml";
   private static final String RENDERED_HTML = "renderedHtml";

   private JMenuBar menubar = new JMenuBar();
   private PSToolBar toolbar = new PSToolBar();
   /**
    * The status bar to be shown at the bottom of the application.
    */
   private StatusBar m_statusBar = null;
   private HashMap m_actions = new HashMap(30);
   private Vector m_tabs = new Vector(12);
   private JOptionalTabbedPane m_tabbedPane = new JOptionalTabbedPane();
   /**
    * The error output stream to which the standard errors will be redirected,
    * initialized during construction, never <code>null</code> after that.
    */
   private ByteArrayOutputStream m_err = new ByteArrayOutputStream();
   /**
    * A text area which will receive the standard error outputs. Will be
    * read-only, initialized during construction, never <code>null</code>
    * after that.
    */
   private JTextArea m_errorOut = new JTextArea();
   /**
    * The scroll pane used for teh error text area, initialized during
    * construction, never <code>null</code> after that.
    */
   private JScrollPane m_errorPane = new JScrollPane(m_errorOut);
   /**
    * A split pane with the top component being the tabbed pane containing
    * all input/output tabs and the bottom component showing all error
    * outputs. Initialized during construction, never <code>null</code>
    * after that.
    */
   private JSplitPane m_split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
      m_tabbedPane, m_errorPane);

   BorderLayout borderLayout1 = new BorderLayout();
   JTabbedPane jTabbedPane = new JTabbedPane();

   // this flag indicates wether or not this belongs to an other application
   // such as E2Designer or not. its used to hide the application instead of exiting
   // it.
   private boolean m_belongsToAnOtherApplication = false;

   /**
    * Creates a MainFrame Object that is not standalone.
    * @param belongsToAnOtherApplication indicates wether or not this
    * belongs to an other application such as E2Designer or not.
    * Its used to hide the application instead of exiting it.
    *
    * @throws MissingResourceException if a resource does not exist.
    * @throws SplitterException when splitter encounters errors.
    */
   public MainFrame ( boolean belongsToAnOtherApplication)
     throws MissingResourceException, SplitterException
   {
      this(belongsToAnOtherApplication, false);
   }

   /**
    * Creates a MainFrame Object
    * @param belongsToAnOtherApplication indicates wether or not this
    * belongs to an other application such as E2Designer or not.
    * Its used to hide the application instead of exiting it.
    *
    * @param isStandalone indicates if this application is a standalone
    * application. Workbench is not standalone but the XSplit console
    * is.
    *
    * @throws MissingResourceException if a resource does not exist.
    * @throws SplitterException when splitter encounters errors.
    */
   public MainFrame( boolean belongsToAnOtherApplication, boolean isStandalone)
      throws MissingResourceException, SplitterException
   {
      m_isStandalone = isStandalone;
      m_belongsToAnOtherApplication = belongsToAnOtherApplication;
      m_config = new SplitterConfiguration(true);

      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      try
      {
         URL url = getClass().getResource( "images/rxsl16.gif" );
         if ( null != url )
         {
            ImageIcon mainIcon = new ImageIcon( url );
            if (null != mainIcon)
               setIconImage( mainIcon.getImage( ) );
         }

         JEditorPane renderedHtml = new JEditorPane("text/html;charset=ISO-8859-1", "");
         class PCL implements PropertyChangeListener
         {
            public void propertyChange(PropertyChangeEvent e)
            {
               if (!e.getPropertyName().equals("page"))
                  return;
               try
               {
                  Object o = e.getOldValue();
                  URL newURL = (URL) e.getNewValue();
               }
               finally
               {
                  setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               }
            }
         }

         renderedHtml.addPropertyChangeListener(new PCL());
         jbInit();

         m_findDialog = new FindDialog(this, getRes().getString("findTitle"));
         m_findDialog.pack();

         m_replaceDialog = new ReplaceDialog(this, getRes().getString("replaceTitle"));
         m_replaceDialog.pack();

         // set the new selected document for the search dialog
         int selectedIndex = m_tabbedPane.getSelectedIndex(true);
         m_findDialog.newSearchDocument((JScrollPane) m_tabbedPane.getComponentAt(selectedIndex));
         m_replaceDialog.newSearchDocument((JScrollPane) m_tabbedPane.getComponentAt(selectedIndex));
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Initializes the java help by loading the help set specified in the
    * configuration file. Loads the help topic id also. If the configuration
    * file does not have these properties defined it assumes the default values.
    */
   private void initializeHelp()
   {
      m_help = PSJavaHelp.getInstance();

      String helpFile = m_config.getProperty(HELPSETFILE,
         "file:Docs/Rhythmyx/XSplit/XSpLit.hs");
      String helpSetURL = PSJavaHelp.getHelpSetURL(helpFile);
      if(helpSetURL != null && helpSetURL.trim().length() != 0)
         m_help.setHelpSet(helpSetURL);

      m_helpTopicId = m_config.getProperty(HELPTOPICID, "O5882");
   }

   //Component initialization
   private void jbInit() throws Exception
   {
      this.getContentPane().setLayout(borderLayout1);
      this.setSize(new Dimension(800, 600));
      this.setTitle(BASE_TITLE);
      m_statusBar = new StatusBar("", 0, 0);
      this.setJMenuBar(menubar);
      this.getContentPane().add(m_statusBar, BorderLayout.SOUTH);
      this.getContentPane().add(m_split, BorderLayout.CENTER);
      this.getContentPane().add(toolbar, BorderLayout.NORTH);

      String HTMLFileTypeDesc = "HTML files";
      String [] HTMLExtensions = { "htm", "html", "asp", "jsp" };
      String DTDFileTypeDesc = "Document Type Defs";
      Font htmlFont = new Font("Monospaced", Font.PLAIN, 12);
      Font logFont = new Font("SansSerif", Font.PLAIN, 12);
      Font xmlFont = new Font("Monospaced", Font.PLAIN, 12);

      // initialize the error output text area
      m_errorOut.setEditable(false);
      m_errorOut.setWrapStyleWord(true);
      m_errorOut.setFont(htmlFont);
      m_errorOut.setBackground(new Color(230, 230, 230));

      // initialize the error scroll pane
      m_errorPane.setMinimumSize(new Dimension(800, 75));

      // initialize the split pane
      m_split.setResizeWeight(1.0);
      m_split.setOneTouchExpandable(true);
      m_split.setDividerSize(10);

      // redirect the standard error output to our stream
      System.setErr(new PrintStream(m_err, true));

      /* Build the vector of tabs, the order they appear below is the order they
      will appear in the tabbed pane. */
      DnDTab tab = new DnDTab("Source HTML", SOURCE_HTML, null, false, true, true);
      tab.addCaretListener(this);
      tab.setFont(htmlFont);
      tab.setTooltip("The source HTML document that will be split");
      tab.setFileAttributes(HTMLExtensions, HTMLFileTypeDesc, null);
      tab.setColor(new Color(170, 170, 170));
      m_tabs.add(tab);

      tab = new DnDTab("Tidied HTML", TIDIED_HTML, TOGGLE_VIEW_TIDIED_HTML,
      true, true, false);
      tab.addCaretListener(this);
      tab.setFont(htmlFont);
      tab.setTooltip("Source document after it has been run through Tidy");
      tab.setFileAttributes(HTMLExtensions, HTMLFileTypeDesc, "_t");
      m_tabs.add(tab);

      tab = new DnDTab("Log", LOG, null, false, true, false);
      tab.addCaretListener(this);
      tab.setFont(logFont);
      tab.setTooltip("Messages output while running the splitter");
      tab.setFileAttributes(new String [] { "log" }, "Log files", null);
      tab.setEditable(false);
      m_tabs.add(tab);

      tab = new DnDTab("DTD", DTD, TOGGLE_VIEW_DTD, true, true, false);
      tab.addCaretListener(this);
      tab.setFont(xmlFont);
      tab.setTooltip("The Document type definition generated from the Source HTML");
      tab.setFileAttributes(new String [] { "dtd" }, DTDFileTypeDesc, null);
      m_tabs.add(tab);

      tab = new DnDTab("Form Fields DTD", FORMS_DTD, TOGGLE_VIEW_FORMS,
      true, true, false);
      tab.addCaretListener(this);
      tab.setFont(xmlFont);
      tab.setTooltip("The Document type definition generated from all form fields in the Source HTML");
      tab.setFileAttributes(new String [] { "dtd" }, DTDFileTypeDesc, "_forms");
      m_tabs.add(tab);

      tab = new DnDTab("Format: XSL", XSL, TOGGLE_VIEW_XSL, true, true, true);
      tab.addCaretListener(this);
      tab.setFont(xmlFont);
      tab.setTooltip("The stylesheet generated from the Source HTML");
      tab.setFileAttributes(new String [] { "xsl" }, "Stylesheets", null);
      tab.setColor(new Color(170, 170, 170));
      m_tabs.add(tab);

      tab = new DnDTab("Content: XML", XML, TOGGLE_VIEW_XML, true, true, true);
      tab.addCaretListener(this);
      tab.setFont(xmlFont);
      tab.setTooltip("Sample XML data generated from the Source HTML");
      tab.setFileAttributes(new String [] { "xml" }, "XML files", null);
      tab.setColor(new Color(170, 170, 170));
      m_tabs.add(tab);

      tab = new DnDTab("Output HTML", MERGED_HTML, TOGGLE_VIEW_MERGED_HTML,
      true, true, false);
      tab.addCaretListener(this);
      tab.setFont(htmlFont);
      tab.setTooltip("The HTML generated by merging the stylesheet (XSL) and Sample XML");
      tab.setFileAttributes(HTMLExtensions, HTMLFileTypeDesc, "_m");
      tab.setColor(new Color(230, 230, 230));
      m_tabs.add(tab);

      // Initialize the tabbed pane. This needs to be done before the menu is built.
      m_tabbedPane.setBorder(BorderFactory.createEmptyBorder( 1, 1, 1, 1 ));
      for (int i = 0; i < m_tabs.size(); ++i)
      {
         tab = (DnDTab) m_tabs.get(i);
         JScrollPane scroller = new JScrollPane(tab);
         tab.addMouseListener(new MouseAdapter()
         {
            public void mouseReleased(MouseEvent event)
            {
               if (m_contextMenu != null && event.isPopupTrigger())
               {
                  m_contextMenu.show(event.getComponent(),
                  event.getX(), event.getY());
               }
            }
         });
         m_tabbedPane.addTab(tab.getTitle(), scroller, tab.isHideable());
         m_tabbedPane.setBackgroundAt(i, tab.getColor());
      }

      m_tabbedPane.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            jTabbedPane_stateChanged(e);
         }
      });

      // create the file menu actions
      KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_N,
      Event.CTRL_MASK, true);
      URL url = getClass().getResource(getRes().getString("gif_fileNew"));
      ImageIcon image = null;
      if (url != null)
         image = new ImageIcon(url);
      PSAction action = new PSAction(getRes().getString("fileNew"), 'N', ks, image)
      {
         public void actionPerformed(ActionEvent e)
         {
            newDocument();
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
      getRes().getString("fileNewDesc"));
      m_actions.put(NEW, action);

      ks = KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_fileOpen"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("fileOpen"), 'O', ks, image)
      {
         public void actionPerformed(ActionEvent e)
         {
            DnDTab tc = getCurrentTabInfo();
            if (tc.isLoadable())
               openFile(tc);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
      getRes().getString("fileOpenDesc"));
      m_actions.put(OPEN, action);

      ks = null;
      url = getClass().getResource(getRes().getString("gif_fileSaveAs"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("fileSaveAs"), 'A', ks,  image)
      {
         public void actionPerformed(ActionEvent e)
         {
            saveAs();
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
      getRes().getString("fileSaveAsDesc"));
      m_actions.put(SAVEAS, action);

      ks = null;
      url = getClass().getResource(getRes().getString("gif_fileSaveAllAs"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("fileSaveAllAs"), (char) 0, ks, image)
      {
         public void actionPerformed(ActionEvent e)
         {
            saveAllAs();
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
      getRes().getString("fileSaveAllAsDesc"));
      m_actions.put(SAVEALL, action);

      ks = null;
      url = getClass().getResource(getRes().getString("gif_fileExit"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("fileExit"), 'x', ks, image)
      {
         public void actionPerformed(ActionEvent e)
         {
            menuFileExit_actionPerformed(null);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
      getRes().getString("fileExit"));
      m_actions.put(EXIT, action);

      // Build the file menu
      PSMenu file = new PSMenu(getRes().getString("fileMenu"), 'F');
      file.add((Action) m_actions.get(NEW));
      file.add((Action) m_actions.get(OPEN));
      file.addSeparator();
      file.add((Action) m_actions.get(SAVEAS));
      file.add((Action) m_actions.get(SAVEALL));
      file.addSeparator();
      file.add((Action) m_actions.get(EXIT));
      menubar.add(file);

      ks = null;
      url = getClass().getResource("images/toolsSplit.gif");
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("Split", 'S', ks,   image)
      {
         public void actionPerformed(ActionEvent e)
         {
            split();
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, "Split the Source HTML file");
      m_actions.put(SPLIT, action);

      ks = null;
      url = getClass().getResource("images/toolsMerge.gif");
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("Merge", 'M', ks,   image)
      {
         public void actionPerformed( ActionEvent e )
         {
            mergeDocuments();
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
      "Merge the stylesheet (XSL) and sample XML data");
      m_actions.put(MERGE, action);

      ks = KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK, true);
      url = getClass().getResource("images/toolsBrowser.gif");
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("Launch Browser", 'L', ks,   image)
      {
         public void actionPerformed(ActionEvent e)
         {
            launchBrowser();
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
      "Display the Merged HTML with the default browser");
      m_actions.put(LAUNCH_BROWSER, action);

      ks = null;
      url = getClass().getResource("images/optionsFile.gif");
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("Options...", 'O', ks, image)
      {
         public void actionPerformed(ActionEvent e)
         {
            menuFileOptions_actionPerformed(e);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
      "Display the dialog to set program options");
      m_actions.put(OPTIONS, action);

      ks = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, true);
      url = getClass().getResource("images/help.gif");
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("Percussion XSpLit Help", 'H', ks, image)
      {
         public void actionPerformed(ActionEvent e)
         {
            if(m_help == null)
               initializeHelp();

            PSJavaHelp.launchHelp(m_helpTopicId, true, null);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
      "View the help using the default browser");
      m_actions.put(HELP, action);

      ks = null;
      url = getClass().getResource("images/about.gif");
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("About XSpLit...", 'A', ks,  image)
      {
         public void actionPerformed( ActionEvent e )
         {
            showAboutDialog();
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
      "Display information about the program");
      m_actions.put(ABOUT, action);

      /* normally, I would make this an anonymous class, but I want to create
      a bunch in a loop, so create it here for use below. */
      class ToggleAction extends PSAction
      {
         public ToggleAction( String title, String id)
         {
            super( title, (char)0, null, null );
            m_tabId = id;
         }
         public void actionPerformed( ActionEvent e )
         {
            //todo verify check mark matches real state
            toggleTabVisibility(m_tabId);
            appendErrorOutput();
         }
         private String m_tabId;
      }

      /* The view menu is built while the view actions are being created. */
      PSMenu view = new PSMenu(getRes().getString("viewMenu"), 'V');

      /* Walk thru tab list and add an entry for each tab that is hidable */
      Enumeration e = m_tabs.elements();
      while ( e.hasMoreElements())
      {
         // we assume that all entries are valid
         DnDTab tc = (DnDTab) e.nextElement();
         if ( tc.isHideable())
         {
            action = new ToggleAction( tc.getTitle(), tc.getId());
            action.putValue( Action.SHORT_DESCRIPTION, "Toggle the visibility of the tab" );
            m_actions.put( tc.getToggleAction(), action );
            view.addCheckBox(action);
         }
      }

      ks = null;
      url = getClass().getResource(getRes().getString("gif_viewFormat"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      // Format HTML source
      action = new PSAction(getRes().getString("viewFormat"), 'F', ks,  image)
      {
         public void actionPerformed(ActionEvent event)
         {
            InputStream r = null;
            ByteArrayOutputStream prettyHtml = null;

            try
            {
               String source = getCurrentTabInfo().getText();

               Tidy tidy = new Tidy();

               // Set tidy options
               tidy.setMakeClean(true);
               tidy.setQuiet(true);
               tidy.setShowWarnings(false);
               tidy.setTidyMark(false);
               tidy.setIndentContent(true);
               tidy.setRawOut(true);
               tidy.setDocType("loose");

               r = new ByteArrayInputStream(source.getBytes());
               prettyHtml = new ByteArrayOutputStream();
               // Parse to get pretty output
               tidy.parse(r, prettyHtml);

               getCurrentTabInfo().setText(prettyHtml.toString());
               getCurrentEditor().setCaretPosition(0);
            }
            catch (Exception e)
            {
               // just ignore this
               e.printStackTrace();
            }
            finally
            {
               // Clean up streams
               try
               {
                  if(null != r)
                     r.close();
                  if(null != prettyHtml)
                     prettyHtml.close();
               }
               catch (Exception e)
               {
                   // just ignore this
                   e.printStackTrace();
               }
            }
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("viewFormatDesc"));
      m_actions.put(FORMAT, action);
      view.addSeparator();
      view.add((Action) m_actions.get(FORMAT));

      // create and populate the edit menu
      ks = null;
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_editUndo"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("editUndo"), 'U', ks, image)
      {
         public void actionPerformed(ActionEvent event)
         {
            editUndo(event);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("editUndoDesc"));
      m_actions.put(UNDO, action);

      ks = null;
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_editRedo"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("editRedo"), 'R', ks, image)
      {
         public void actionPerformed(ActionEvent event)
         {
            editRedo(event);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("editRedoDesc"));
      m_actions.put(REDO, action);

      ks = null;
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_editCut"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("editCut"), 't', ks,  image)
      {
         public void actionPerformed(ActionEvent event)
         {
            editCut(event);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("editCutDesc"));
      m_actions.put(CUT, action);

      ks = null;
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_editCopy"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("editCopy"), 'C', ks, image)
      {
         public void actionPerformed(ActionEvent event)
         {
            editCopy(event);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("editCopyDesc"));
      m_actions.put(COPY, action);

      ks = null;
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_editPaste"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("editPaste"), 'P', ks, image)
      {
         public void actionPerformed(ActionEvent event)
         {
            // is the source a menu action or a key command?
            Object source = event.getSource();
            if (source instanceof JMenuItem)
            {
               Dimension dim = ((JMenuItem) source).getSize();
               if (dim.getHeight() != 0 && dim.getWidth() != 0)
                  editPaste(event);
            }
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("editPasteDesc"));
      m_actions.put(PASTE, action);

      ks = null;
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, true);
      url = getClass().getResource(getRes().getString("gif_editDelete"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("editDelete"), 'D', ks,  image)
      {
         public void actionPerformed(ActionEvent event)
         {
            editDelete(event);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("editDeleteDesc"));
      m_actions.put(DELETE, action);

      ks = null;
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_editSelectAll"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("editSelectAll"), 'A', ks, image)
      {
         public void actionPerformed(ActionEvent event)
         {
            editSelectAll(event);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("editSelectAllDesc"));
      m_actions.put(SELECT_ALL, action);

      ks = null;
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_editFind"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("editFind"), 'F', ks, image)
      {
         public void actionPerformed(ActionEvent event)
         {
            editFind(event);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("editFindDesc"));
      m_actions.put(FIND, action);

      ks = null;
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, true);
      url = getClass().getResource(getRes().getString("gif_editFindNext"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("editFindNext"), '0', ks, image)
      {
         public void actionPerformed(ActionEvent event)
         {
            editFindNext(event);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("editFindNextDesc"));
      m_actions.put(FIND_NEXT, action);

      ks = null;
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_editReplace"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("editReplace"), 'e', ks, image)
      {
         public void actionPerformed(ActionEvent event)
         {
            editReplace(event);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("editReplaceDesc"));
      m_actions.put(REPLACE, action);

      ks = null;
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_editGoto"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("editGoto"), 'G', ks, image)
      {
         public void actionPerformed(ActionEvent event)
         {
            editGoto(event);
            appendErrorOutput();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("editGotoDesc"));
      m_actions.put(GOTO, action);

      ks = null;
      url =  getClass().getResource(getRes().getString("gif_editResetError"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction(getRes().getString("editResetError"), 'R', ks, image)
      {
         public void actionPerformed(ActionEvent event)
         {
            m_errorOut.setText("");
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION, getRes().getString("editResetErrorDesc"));
      m_actions.put(RESET_ERROR, action);

      // create the edit menu
      PSMenu edit = new PSMenu(getRes().getString("editMenu"), 'E');
      edit.add((Action) m_actions.get(UNDO));
      edit.add((Action) m_actions.get(REDO));
      edit.addSeparator();
      edit.add((Action) m_actions.get(CUT));
      edit.add((Action) m_actions.get(COPY));
      edit.add((Action) m_actions.get(PASTE));
      edit.add((Action) m_actions.get(DELETE));
      edit.addSeparator();
      edit.add((Action) m_actions.get(SELECT_ALL));
      edit.addSeparator();
      edit.add((Action) m_actions.get(FIND));
      edit.add((Action) m_actions.get(FIND_NEXT));
      edit.add((Action) m_actions.get(REPLACE));
      edit.add((Action) m_actions.get(GOTO));
      menubar.add(edit);

      // create the context menu
      m_contextMenu = new JPopupMenu();
      m_contextMenu.add((Action) m_actions.get(UNDO));
      m_contextMenu.add((Action) m_actions.get(REDO));
      m_contextMenu.addSeparator();
      m_contextMenu.add((Action) m_actions.get(CUT));
      m_contextMenu.add((Action) m_actions.get(COPY));
      m_contextMenu.add((Action) m_actions.get(PASTE));
      m_contextMenu.add((Action) m_actions.get(DELETE));
      m_contextMenu.addSeparator();
      m_contextMenu.add((Action) m_actions.get(SELECT_ALL));

      /* The view menu is built above with the view actions */
      menubar.add( view );

      PSMenu tools = new PSMenu( "Tools", 'T' );
      tools.add((Action) m_actions.get( SPLIT ));
      tools.add((Action) m_actions.get( MERGE ));
      tools.add((Action) m_actions.get( LAUNCH_BROWSER ));
      tools.addSeparator();
      tools.add((Action) m_actions.get( OPTIONS ));
      menubar.add( tools );

      PSMenu help = new PSMenu( "Help", 'H' );
      help.add((Action) m_actions.get( HELP ));
      help.add((Action) m_actions.get( ABOUT ));
      menubar.add( help );

      /* Build toolbar */
      toolbar.add((PSAction) m_actions.get(NEW));
      toolbar.add((PSAction) m_actions.get(OPEN));
      toolbar.add((PSAction) m_actions.get(SAVEAS));
      toolbar.addSeparator();
      toolbar.add((PSAction) m_actions.get(UNDO));
      toolbar.add((PSAction) m_actions.get(REDO));
      toolbar.addSeparator();
      toolbar.add((PSAction) m_actions.get(SPLIT));
      toolbar.add((PSAction) m_actions.get(MERGE));
      toolbar.add((PSAction) m_actions.get(LAUNCH_BROWSER));
      toolbar.addSeparator();
      toolbar.add((PSAction) m_actions.get(RESET_ERROR));

      newDocument();
   }

   /**
    * Process the action launched through the menu edit-undo.
    *
    * @param event the action event to process.
    */
   protected synchronized void editUndo(ActionEvent event)
   {
      if (getCurrentEditor() instanceof DnDTab)
      {
         DnDTab editor = (DnDTab) getCurrentEditor();
         editor.undo(event);
      }
   }

   /**
    * Process the action launched through the menu edit-redo.
    *
    * @param event the action event to process.
    */
   protected synchronized void editRedo(ActionEvent event)
   {
      if (getCurrentEditor() instanceof DnDTab)
      {
         DnDTab editor = (DnDTab) getCurrentEditor();
         editor.redo(event);
      }
   }

   /**
    * Process the action launched through the menu edit-cut.
    *
    * @param event the action event to process.
    */
   protected void editCut(ActionEvent event)
   {
      getCurrentEditor().cut();
   }

   /**
    * Process the action launched through the menu edit-copy.
    *
    * @param event the action event to process.
    */
   protected void editCopy(ActionEvent event)
   {
      getCurrentEditor().copy();
   }

   /**
    * Process the action launched through the menu edit-paste.
    *
    * @param event the action event to process.
    */
   protected void editPaste(ActionEvent event)
   {
      getCurrentEditor().paste();
   }

   /**
    * Process the action launched through the menu edit-selectAll.
    *
    * @param event the action event to process.
    */
   protected void editSelectAll(ActionEvent event)
   {
      getCurrentEditor().selectAll();
   }

   /**
    * Process the action launched through the menu edit-delete.
    *
    * @param event the action event to process.
    */
   protected void editDelete(ActionEvent event)
   {
      getCurrentEditor().replaceSelection("");
   }

   /**
    * Process the action launched through the menu edit-find.
    *
    * @param event the action event to process.
    */
   protected void editFind(ActionEvent event)
   {
      m_findDialog.center();

      String strWhat = getCurrentEditor().getSelectedText();
      if (strWhat == null || strWhat.equals(""))
         m_findDialog.setVisible(true);
      else
         m_findDialog.setVisible(strWhat);
   }

   /**
    * Process the action launched through the menu edit-findNext.
    *
    * @param event the action event to process.
    */
   protected void editFindNext(ActionEvent event)
   {
      if ((m_findDialog != null) && m_findDialog.isInLegalState())
         m_findDialog.findNext();
      else
         editFind(event);
   }

   /**
    * Process the action launched through the menu edit-replace.
    *
    * @param event the action event to process.
    */
   protected void editReplace(ActionEvent event)
   {
      m_replaceDialog.center();

      String strWhat = getCurrentEditor().getSelectedText();
      if (strWhat == null || strWhat.equals(""))
         m_replaceDialog.setVisible(true);
      else
         m_replaceDialog.setVisible(strWhat);
   }

   /**
    * Process the action launched through the menu edit-goto.
    *
    * @param event the action event to process.
    */
   protected void editGoto(ActionEvent event)
   {
      GotoDialog dlg = new GotoDialog(this, getCurrentEditor());
      dlg.packCenter();
      dlg.setVisible(true);
   }

   //Overridden so we can exit on System Close
   protected void processWindowEvent(WindowEvent e)
   {
      if(e.getID() == WindowEvent.WINDOW_CLOSING)
         menuFileExit_actionPerformed(e);
      else
         super.processWindowEvent(e);
   }

   /**
    * Clears the contents of all tabs and creates a default HTML document in
    * the Source HTML tab.
    **/
   private void newDocument()
   {
      if (!resetContents(false))
         return;

      DnDTab tab = getTabInfo(SOURCE_HTML);
      JTextComponent editor = getEditor(SOURCE_HTML);
      tab.setText(getRes().getString("sourceHTML"));
      tab.discardAllEdits();
      m_tabbedPane.setSelectedTab(tab.getTitle());
      tab.requestFocus();
      setTitle();

      if (tab instanceof DnDTab)
      {
         ((DnDTab) tab).discardAllEdits();
      }

      m_statusBar.setStatusText(null == tab ? "" : tab.getTooltip());
      setLineColumnStatus(tab);
      updateMenu();
   }

   /**
    * Runs the splitter against the contents of the source HTML tab. If the tab
    * has no contents, a message is displayed to the user. <p/>
    * The results (if any) are stored in the DTD, XSL Forms and sample XML tabs.
    * The Log tab contains the output and errors/warning messages.
    **/
   private void split()
   {
      if (0 == getTabInfo(SOURCE_HTML).getText().trim().length())
      {
         String[] args =
         {
            getTabInfo(SOURCE_HTML).getTitle()
         };
         String msg = MainFrame.getRes().getString("missingSourceFile");
         JOptionPane.showMessageDialog(this, Util.dress(msg, args));

         return;
      }

      ((Window) this).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      m_tabbedPane.setSelectedTab(getTitleForName(LOG));
      try
      {
         String root = null;
         String fileName = getTabInfo(SOURCE_HTML).getFileName();
         if (fileName != null && !fileName.equals(""))
            root = Util.getRootName(new File(fileName));
         else
            root = "root";

         createLog("Tidying HTML....\n");
         Splitter splitter = new Splitter(new ByteArrayInputStream(
            getTabInfo(SOURCE_HTML).getText().getBytes(
               m_config.getProperty("inputEncoding"))), root, m_config);

         splitter.isStandalone(m_isStandalone);

         getTabInfo(TIDIED_HTML).setText(splitter.getTidiedContents());
         getTabInfo(TIDIED_HTML).discardAllEdits();
         if (splitter.didTidy())
         {
            appendLog("HTML tidied successfully.\n");
            if (splitter.hasWarnings())
               appendLog(splitter.getWarnings());
         }
         else
            appendLog("Skipped tidy (assumed source is well-formed).\n\n");

         appendLog("Generating Sample XML....\n");
         getTabInfo(XML).setText(splitter.getXMLSample(
            m_config.getProperty("outputEncoding")));
         getTabInfo(XML).discardAllEdits();
         appendLog("Sample XML Generation Successful.\n\n");

         appendLog("Generating DTD....\n");
         getTabInfo(DTD).setText(splitter.getDTDContent(
            m_config.getProperty("outputEncoding")));
         getTabInfo(DTD).discardAllEdits();
         appendLog("DTD Generation Successful.\n\n");

         appendLog("Generating Form Fields DTD....\n");
         getTabInfo(FORMS_DTD).setText(splitter.getFORMSContents(
            m_config.getProperty("outputEncoding")));
         getTabInfo(FORMS_DTD).discardAllEdits();
         appendLog("Form Fields DTD Generation Successful.\n\n");

         appendLog("Generating XSL....\n");
         getTabInfo(XSL).setText(splitter.getXSLContent(
            m_config.getProperty("outputEncoding")));
         getTabInfo(XSL).discardAllEdits();
         appendLog("XSL Generation Successful.\n\n");

         appendLog("All Tasks Completed.\n" );

         // select the XML tab and set the caret position to 0
         m_tabbedPane.setSelectedTab(getTitleForName(XML));
         getEditor(XML).setCaretPosition(0);
      }
      catch(SAXException se)
      {
         appendLog(se.getMessage());
         se.printStackTrace();
      }
      catch(IOException ex)
      {
         appendLog(ex.getMessage());
         ex.printStackTrace();
      }
      catch(Exception ex)
      {
         appendLog(ex.getLocalizedMessage());
         ex.printStackTrace();
         return;
      }
      finally
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
   }

   /**
    * Creates a new log and sets it to the provided text. All previous
    * loggings will be lost.
    */
   public void createLog(String text)
   {
      DnDTab tab = getTabInfo(LOG);
      tab.setText(text);
      tab.discardAllEdits();
   }

   /**
    * Append the provided text to the end of the log tab.
    *
    * @param text the new text to be appended.
    */
   public void appendLog(String text)
   {
      DnDTab tab = getTabInfo(LOG);
      tab.setText(tab.getText() + text);
      tab.discardAllEdits();
   }

   /**
    * Generates an <code>InputSource</code> from a file.
    *
    * @param file the file
    * @return the input source
    */
   private InputSource fileInputSource(File file)
   {
      String path = file.getAbsolutePath();

      String fSep = System.getProperty("file.separator");
      if (fSep != null && fSep.length() == 1)
         path = path.replace(fSep.charAt(0), '/');

      if (path.length() > 0 && path.charAt(0) != '/')
         path = '/' + path;

      try
      {
         return new InputSource(new URL("file", null, path).toString());
      }
      catch (MalformedURLException e)
      {
         e.printStackTrace();
      }

      return null;
   }

   /**
    * Runs XT using the content of the XSL tab and Sample XML tab. The result is
    * placed in the merged HTML tab. If either of the tabs have no content, a
    * message is displayed to the user and nothing is done.
    **/
   private void mergeDocuments()
   {
      if (0 == getTabInfo(XSL).getText().trim().length() ||
      0 == getTabInfo(XML).getText().trim().length())
      {
         String[] args =
         {
            getTabInfo(XSL).getTitle(),
            getTabInfo(XML).getTitle()
         };
         String msg = MainFrame.getRes().getString("missingDataFiles");
         JOptionPane.showMessageDialog(this, Util.dress(msg, args));

         return;
      }

      try
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         Reader xsl = new StringReader(getTabInfo(XSL).getText());
         Reader xml = new StringReader(getTabInfo(XML).getText());
         ByteArrayOutputStream html = new ByteArrayOutputStream();

         File xslFile = File.createTempFile("rxc", ".xsl",
            new File(m_config.getProperty("baseLocation")));
         xslFile.deleteOnExit();
         OutputStreamWriter xslWriter = new OutputStreamWriter(
            new FileOutputStream(xslFile), PSCharSets.getInternalEncoding());
         String xslContents = getTabInfo(XSL).getText();
         xslWriter.write(xslContents, 0, xslContents.length());
         xslWriter.flush();
         xslWriter.close();

         String errorMessage = PSXslMerger.merge(fileInputSource(xslFile),
            xml, html);
         if (errorMessage != null)
         {
            JOptionPane.showMessageDialog(this, errorMessage);
            return;
         }

         File htmlFile = File.createTempFile("xsp", ".html",
            new File(m_config.getProperty("baseLocation")));
         htmlFile.deleteOnExit();
         OutputStreamWriter writer = new OutputStreamWriter(
            new FileOutputStream(htmlFile),
            m_config.getProperty("outputEncoding"));
         String outputContents = html.toString(PSCharSets.getInternalEncoding());
         getTabInfo(MERGED_HTML).setText(outputContents);
         getTabInfo(MERGED_HTML).discardAllEdits();
         getEditor(MERGED_HTML).setCaretPosition(0);
         writer.write(outputContents, 0, outputContents.length());
         writer.flush();
         writer.close();

         m_tabbedPane.setSelectedTab(getTitleForName(MERGED_HTML));
      }
      catch (TransformerException e)
      {
         //System.out.println("...was here");
      }
      catch (SAXException se)
      {
         if (se instanceof SAXParseException)
         {
            SAXParseException pe = (SAXParseException) se;
            String tmp = pe.toString();
            if (pe.getColumnNumber() > 0 && pe.getLineNumber() > 0)
            {
               String line = "" + pe.getLineNumber();
               String column = "" + pe.getColumnNumber();
               String[] args =
               {
                  line,
                  column,
                  pe.getLocalizedMessage(),
                  getTitleForName(XML),
                  getTitleForName(XSL)
               };
               String msg = MainFrame.getRes().getString("parserError1");
               JOptionPane.showMessageDialog(this, Util.dress(msg, args));
            }
            else
            {
               String[] args =
               {
                  pe.getLocalizedMessage()
               };
               String msg = MainFrame.getRes().getString("parserError2");
               JOptionPane.showMessageDialog(this, Util.dress(msg, args));
            }
         }
         else
         {
            String[] args =
            {
               se.getLocalizedMessage()
            };
            String msg = MainFrame.getRes().getString("parserError2");
            JOptionPane.showMessageDialog(this, Util.dress(msg, args));
         }
      }
      catch(UnsupportedEncodingException ue)
      {
         ue.printStackTrace();
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
      finally
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
   }

   /**
    * Launches the default browser to display the content of the merged HTML
    * tab. If this tab has no content, nothing is done.
    */
   private void launchBrowser()
   {
      if (0 == getTabInfo(MERGED_HTML).getText().trim().length())
      {
         String[] args =
         {
            getTabInfo(MERGED_HTML).getTitle()
         };
         String msg = MainFrame.getRes().getString("missingOutputFile");
         JOptionPane.showMessageDialog(this, Util.dress(msg, args));

         return;
      }

      try
      {
         File htmlFile = File.createTempFile("xsp", ".html",
            new File(m_config.getProperty("baseLocation")));
         htmlFile.deleteOnExit();
         OutputStreamWriter writer = new OutputStreamWriter(
            new FileOutputStream(htmlFile),
            m_config.getProperty("outputEncoding"));
         String output = getTabInfo( MERGED_HTML ).getText();
         writer.write(output, 0, output.length());
         writer.close();
         URL url = new URL("file", "", 0, htmlFile.getAbsolutePath());
         UTBrowserControl.displayURL( url.toString());
      }
      catch ( IOException ioe )
      {
         ioe.printStackTrace();
      }
   }


   /**
    * Toggles the visible property of the tab with the supplied name.
    *
    * @return The state of the visibility property after this method has toggled
    * the state. <code>true</code> indicates the tab is visible.
    *
    * @throws IllegalArgumentException if there is no tab with this name
    *
    * @throws UnsupportedOperationException if the tab is not hidable
    **/
   private boolean toggleTabVisibility( String tabId )
   {
      DnDTab tc = getTabInfo( tabId );
      if ( null == tc )
         throw new IllegalArgumentException( "Tab \"" + tabId + "\" not found in tab list." );
      if ( false == tc.isHideable())
         throw new UnsupportedOperationException( "Tab \"" + tabId + "\" doesn't support toggling visibility." );

      boolean isVisible = m_tabbedPane.isTabVisible( tc.getTitle() );
      if ( isVisible )
         m_tabbedPane.hideTab( tc.getTitle() );
      else
         m_tabbedPane.showTab( tc.getTitle() );
      return m_tabbedPane.isTabVisible( tc.getTitle() );
   }


   /**
    * @return a filename appropriate for the content in the supplied tab, based
    * on the filename of the source HTML. If there is no source HTML, a default
    * name is returned. The filename includes the base name and the extension,
    * no path is included.
    **/
   private String createDefaultFilename( DnDTab tab, String defaultBase )
   {
      // create default name from source html file name
      DnDTab sourceHtml = getTabInfo( SOURCE_HTML );
      String defaultName = sourceHtml.getFileName();
      if ( null == defaultName || 0 == defaultName.trim().length())
         defaultName = defaultBase;
      else
      {
         int index = defaultName.lastIndexOf(File.separatorChar);
         defaultName = defaultName.substring( index+1 );
      }

      int index = defaultName.lastIndexOf( '.' );
      if ( index >= 0 )
         defaultName = defaultName.substring( 0, index );
      if ( null != tab.getFileSuffix() )
         defaultName += tab.getFileSuffix();
      if ( null != tab.getFileExtensions() )
         defaultName += "." + tab.getFileExtensions()[0];

      return defaultName;
   }

   /**
    * Displays the save as dialog for the current tab. If the user proceeds with
    * the save, the content of the tab is written to the user selected file. If
    * the current tab doesn't allow saving, no action is taken.
    *
    * @return <code>true</code> if the contents of the current tab are successfully
    * saved, <code>false</code> otherwise.
    **/
   private boolean saveAs()
   {
      DnDTab tab = getCurrentTabInfo();
      if ( !tab.isSaveable() )
         return false;

      String filePath = tab.getFileName();
      if ( null == filePath )
         filePath = createDefaultFilename( tab, "document" +
            getDefaultCounter());

      String tabLastOpenDirProperty = tab.getId() + LAST_OPEN_DIR;
      String lastOpenedDir = m_config.getProperty(tabLastOpenDirProperty,
         System.getProperty("user.dir"));
      final JFileChooser fc = new JFileChooser(lastOpenedDir);
      FileView fv = fc.getFileView();

      if ( null != filePath )
         fc.setSelectedFile(new File(filePath));
      FileFilter ff = new HTMLFileFilter(tab.getFileExtensions(),
         tab.getFileDescriptor() );
      fc.addChoosableFileFilter( ff );
      fc.setFileFilter( ff );
      int returnVal = fc.showSaveDialog(this);

      if (returnVal != JFileChooser.APPROVE_OPTION)
         return false;

      File file = produceOutputFile(fc.getSelectedFile(),
      tab.getFileExtensions());

      m_config.setProperty(tabLastOpenDirProperty,
         file.getParentFile().getPath());
      try
      {
         m_config.saveProperties();
      }
      catch (SplitterException e)
      { /* we just don't save the last file open location */ }

      boolean success = save(tab.getText(), file);
      if (success)
      {
         tab.setFileName(file.getAbsolutePath());
         //tab.setUnsaved(false);
      }

      setTitle();

      return success;
   }


   /**
    * Displays the save as all dialog. If the user proceeds, for each tab that has
    * a check next to it an attempt is made to save the contents of that tab to
    * the specified file.
    *
    * @return <code>true</code> if the save operation was confirmed (ok).
    */
   private boolean saveAllAs()
   {
      String path = getTabInfo( "srcHtml" ).getFileName();
      String defaultPath = null;
      String defaultDoc = "document" + getDefaultCounter();
      if ( null != path && 0 != path.length())
      {
         int index = path.lastIndexOf( File.separatorChar );
         defaultPath = path.substring( 0, index );
         defaultDoc = path.substring( index + 1 );
         index = defaultDoc.lastIndexOf( '.' );
         if(index > 0)
            defaultDoc = defaultDoc.substring( 0, index );
      }

      class SaveInfoEx extends SaveAllDialog.SaveInfo
      {
         public DnDTab m_tab;
      }

      Vector saveFiles = new Vector(10);
      Enumeration allTabs = m_tabs.elements();
      while (allTabs.hasMoreElements())
      {
         DnDTab tab = (DnDTab) allTabs.nextElement();
         if (tab.isSaveable())
         {
            SaveInfoEx info = new SaveInfoEx();
            info.m_tab = tab;
            info.m_label = tab.getTitle();
            info.m_defaultSave = tab.canUndo();
            info.m_extensions = tab.getFileExtensions();
            info.m_fileTypeDesc = tab.getFileDescriptor();
            String currentFile = tab.getFileName();
            String defaultFile = null;
            if (null != currentFile)
            {
               int baseIndex = currentFile.lastIndexOf(File.separatorChar);
               String currentPath = currentFile.substring(0, baseIndex);
               if (null == defaultPath)
                  defaultPath = currentPath;
               if (0 == currentPath.compareToIgnoreCase(defaultPath))
                  defaultFile = currentFile.substring(baseIndex + 1);
               else
                  defaultFile = currentFile;
            }
            else
            {
               defaultFile = createDefaultFilename( tab, defaultDoc );
            }
            info.m_defaultFilename = defaultFile;
            saveFiles.add(info);
         }
      }

      SaveAllDialog fc = new SaveAllDialog(defaultPath, (SaveAllDialog.SaveInfo[])
      saveFiles.toArray(new SaveAllDialog.SaveInfo[saveFiles.size()]));
      FileView fv = fc.getFileView();
      int returnVal = fc.showSaveDialog(this);
      if (returnVal != 0)
         return false;

      Enumeration e = saveFiles.elements();
      while (e.hasMoreElements())
      {
         SaveInfoEx info = (SaveInfoEx) e.nextElement();
         File file = null;
         if (fc.isSaveChecked(info.m_label))
         {
            try
            {
               file = produceOutputFile(fc.getFile(info.m_label),
               info.m_extensions);
               boolean success = save(info.m_tab.getText(), file);
               if (success)
               {
                  info.m_tab.setFileName(file.getAbsolutePath());
                  //info.m_tab.setUnsaved(false);
               }
            }
            catch(FileNotFoundException fnfe)
            {
               String[] args =
               {
                  info.m_tab.getTitle()
               };
               String msg = MainFrame.getRes().getString("missingFile");
               JOptionPane.showMessageDialog(this, Util.dress(msg, args));
            }
         }
      }

      setTitle();

      return true;
   }

   /**
    * This produces the output file. If the file provided does have a valid
    * extension, its just returned. If not, the first extension from the
    * provided list is appended.
    *
    * @param file the source file
    * @param extensions a list of valid extensions
    * @return the file name has garanteed a valid extension
    */
   private File produceOutputFile(File file, String[] extensions)
   {
      String filePath = file.getPath();
      for (int i=0; i<extensions.length; i++)
      {
         String extension = "." + extensions[i];
         if (filePath.toLowerCase().endsWith(extension))
            return file;
      }

      // add default extension
      filePath += "." + extensions[0];
      return new File(filePath);
   }

   /**
    * Creates a new file with the supplied name, if it exists, the user is prompted
    * whether to overwrite. If affirmed, the contents of the reader are written
    * to the file.
    *
    * @param content The content to write to a file with the supplied name.
    *
    * @param file The File object to store the content to.
    *
    * @return <code>true</code> if the file was successfully written. <code>false
    * </code> if the user cancels an overwrite.
    **/
   private boolean save(String content, File file)
   {
      boolean success = true;
      try
      {
         // does the file already exist?
         if (file.isFile())
         {
            String[] args =
            {
               file.getAbsoluteFile().toString()
            };
            String msg = MainFrame.getRes().getString("overwriteFile");
            int response = JOptionPane.showConfirmDialog(this,
            Util.dress(msg, args),
            "Overwrite file?",
            JOptionPane.YES_NO_OPTION );
            if (JOptionPane.YES_OPTION != response)
               return false;
         }
         OutputStreamWriter writer = new OutputStreamWriter(
            new FileOutputStream(file),
            m_config.getProperty("outputEncoding"));
         writer.write(content);
         writer.flush();
         writer.close();
      }
      catch(IOException ex)
      {
         String[] args =
         {
            file.getAbsoluteFile().toString(),
            ex.getLocalizedMessage()
         };
         String msg = MainFrame.getRes().getString("saveFileError");
         JOptionPane.showMessageDialog(this, Util.dress(msg, args));
         success = false;
      }
      return success;
   }

   /**
    * @return the title of the tab that has the supplied name. If no tab has this
    * name, null is returned. All valid tabs have a non-empty title.
    **/
   private String getTitleForName( String tabInternalName )
   {
      DnDTab tc = getTabInfo( tabInternalName );
      return null == tc ? null : tc.getTitle();
   }

   private DnDTab getCurrentTabInfo()
   {
      return (DnDTab) m_tabs.get( m_tabbedPane.getSelectedIndex( true ));
   }

   /**
    * Looks for a tab in the local container that has the supplied name and returns
    * it.
    *
    * @return the Tab object that has the supplied name, or null if one isn't found
    **/
   private DnDTab getTabInfo( String tabInternalName )
   {
      Enumeration e = m_tabs.elements();
      DnDTab tc = null;
      boolean found = false;
      while ( !found && e.hasMoreElements())
      {
         tc = (DnDTab) e.nextElement();
         if ( tc.getId().equals( tabInternalName ))
            found = true;
      }
      return found ? tc : null;
   }


   /**
    * Checks for unsaved data and asks user if they really want to exit if there
    * is any. On successful exit, the program is ended.
    *
    * @param windowEvent If not null, the event is passed to the super, otherwise, this
    * method disposes and hides the window.
    **/
   void menuFileExit_actionPerformed(WindowEvent windowEvent)
   {
      if (m_belongsToAnOtherApplication)
      {
         setVisible(false);
      }
      else
      {
         Enumeration e = m_tabs.elements();
         boolean hasUnsaved = false;
         while (e.hasMoreElements() && !hasUnsaved)
         {
            DnDTab tab = (DnDTab) e.nextElement();
            if (tab.canUndo())
               hasUnsaved = true;
         }

         String msg = MainFrame.getRes().getString("exitWithUnsavedData");
         if (hasUnsaved)
         {
            int result = JOptionPane.showConfirmDialog(this,
            msg,
            "Exit warning",
            JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.CANCEL_OPTION)
               return;
            else if (result == JOptionPane.YES_OPTION)
            {
               if (!saveAllAs())
                  return;
            }
         }

         if (null != windowEvent)
         {
            super.processWindowEvent(windowEvent);
         }
         else
         {
            setVisible(false);
            dispose();
         }

         System.exit(0);
      }
   }

   void menuHelpAbout_actionPerformed(ActionEvent e)
   {
      showAboutDialog();
   }
   void showAboutDialog()
   {
      AboutDialog dlg = new AboutDialog(this);
      dlg.setTitle( BASE_TITLE );
      dlg.setVisible(true);
   }

   /**
    * Pops up a file chooser for the user based on the properties in the supplied
    * tab. The contents of the chosen file replace any content already in the
    * editor associated with this tab.
    *
    * @param tab the tab component to open the file for, assumed not
    *    <code>null</code>.
    */
   void openFile(DnDTab tab)
   {
      boolean reset = false;
      if (tab.getId().equals(SOURCE_HTML))
      {
         if (!queryContinue(MainFrame.getRes().getString("newWithUnsavedData")))
            return;

         reset = true;
      }
      else if (tab.canUndo())
      {
         int response = JOptionPane.showConfirmDialog(this,
            MainFrame.getRes().getString("saveBeforeNewFile"));
         if (JOptionPane.CANCEL_OPTION == response)
            return;
         else if (JOptionPane.YES_OPTION == response)
            if (!saveAs())
               return;
      }

      String tabLastOpenDirProperty = tab.getId() + LAST_OPEN_DIR;
      String lastOpenedDir = m_config.getProperty(tabLastOpenDirProperty,
         System.getProperty("user.dir"));
      final JFileChooser fc = new JFileChooser(lastOpenedDir);
      String[] filters = tab.getFileExtensions();
      String fileTypeDesc = tab.getFileDescriptor();
      HTMLFileFilter ff = new HTMLFileFilter(filters,
         null == fileTypeDesc ? "" : fileTypeDesc);
      fc.addChoosableFileFilter(ff);
      fc.setFileFilter(ff);

      int returnVal = fc.showOpenDialog(this);

      if (returnVal == JFileChooser.APPROVE_OPTION)
      {
         if (reset)
            resetContents(true);

         File file = fc.getSelectedFile();
         tab.setFileName(maybeAddExtension( file.getAbsolutePath(),
         null == tab.getFileExtensions() ? null : tab.getFileExtensions()[0]));

         // remember so we can restore on next open
         m_config.setProperty(tabLastOpenDirProperty,
            file.getParentFile().getPath());
         try
         {
            m_config.saveProperties();
         }
         catch (SplitterException e)
         { /* we just don't save the last file open location */ }
         try
         {
            loadEditor(tab.getFileName(),
               m_config.getProperty("inputEncoding"), tab);
            tab.requestFocus();
         }
         catch (Exception ex)
         {
            ex.printStackTrace ();
            return;
         }

         setTitle();
      }
   }

   /**
    * Adds the supplied extension if the supplied filename doesn't have one and
    * doesn't end in a period.
    *
    * @param filename The filename that may need an extension.
    *
    * @param extension This string will be appended on the filename, if appropriate.
    * A leading period is optional.
    *
    * @return The final filename. If extension is null, the supplied filename is
    * returned.
    **/
   private String maybeAddExtension( String filename, String extension )
   {
      if ( null == extension || 0 == extension.trim().length())
         return filename;

      File file = new File( filename );
      String base = file.getName();
      int periodIndex = base.lastIndexOf( '.' );
      if ( periodIndex > 0 )
         // has extension or ends in period
         return filename;

      if ( extension.charAt(0) != '.' )
         filename += ".";

      return filename + extension;
   }

   void menuFileOptions_actionPerformed(ActionEvent event)
   {
      OptionsDialog dlg = new OptionsDialog(this,
         MainFrame.getRes().getString("optionsTitle"));

      dlg.initialize(m_config);

      dlg.setVisible(true);

      if (dlg.ok())
      {
         try
         {
            m_config.saveProperties();
         }
         catch (SplitterException e)
         {
            String msg = "Could not save the splitter properties file for the following reason: {0}.";
            String[] args = { e.getLocalizedMessage() };
            JOptionPane.showMessageDialog(MainFrame.this, Util.dress(msg, args));
         }
      }
   }


   /**
    * This method is useful when performing an operation that is going to clear
    * all tabs.<p/>
    * Checks all tabs to see if any of them have unsaved data. If they do, the
    * user is asked whether they want to continue with the current operation,
    * (thus loosing all unsaved data).
    *
    * @param message the confirm dialog message to show if nessecary.
    * @return <code>true</code> indicates it is ok to continue with the operation.
    **/
   private boolean queryContinue(String message)
   {
      // walk thru all editors and see if any have unsaved data
      Enumeration e = m_tabs.elements();
      boolean hasUnsaved = false;
      while (e.hasMoreElements() && !hasUnsaved)
      {
         DnDTab tab = (DnDTab) e.nextElement();
         if (tab.canUndo())
            hasUnsaved = true;
      }

      // ask user if they want to save them
      if (hasUnsaved)
      {
         int result = JOptionPane.showConfirmDialog(this,
         message,
         "Unsaved data",
         JOptionPane.YES_NO_CANCEL_OPTION);
         if (result == JOptionPane.NO_OPTION)
            return true;
         else if (result == JOptionPane.YES_OPTION)
            return saveAllAs();
         else if (result == JOptionPane.CANCEL_OPTION)
            return false;
      }

      return true;
   }

   /**
    * Checks if any tabs have unsaved data. If so, ask the user whether to continue.
    * Clears all tabs, then sets a default document in the source HTML tab.
    *
    * @param force If <code>true</code>, the user is not asked to save unsaved data.
    *
    * @return <code>true</code> if the reset was successful, <code>false</code>
    * if the user cancelled the reset
    **/
   private boolean resetContents(boolean force)
   {
      String msg = MainFrame.getRes().getString("newWithUnsavedData");
      if (!force && !queryContinue(msg))
         return false;

      // walk all editors again to clear their contents
      Enumeration e = m_tabs.elements();
      while (e.hasMoreElements())
      {
         DnDTab tab = (DnDTab) e.nextElement();
         tab.clear();

         if (tab instanceof DnDTab)
         {
            ((DnDTab) tab).discardAllEdits();
         }
      }

      return true;
   }

   void loadEditor( String filename, String encoding, DnDTab tab )
   throws IOException
   {
      FileInputStream is = new FileInputStream(filename);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      int read;
      byte[] buf = new byte[1024];
      while ((read = is.read(buf)) >= 0)
      {
         os.write(buf, 0, read);
         if (read < buf.length)
            break;
      }
      os.flush();

      tab.setText(os.toString( encoding ));
      tab.discardAllEdits();
   }


   void loadSourceFile( String sourceFile ) throws IOException
   {
      DnDTab tab = getTabInfo( SOURCE_HTML );
      File file = new File( sourceFile );
      tab.setFileName(file.getAbsolutePath());

      FileInputStream is = new FileInputStream(file);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      int read;
      byte[] buf = new byte[1024];
      while ((read = is.read(buf)) >= 0)
      {
         os.write(buf, 0, read);
         if (read < buf.length)
            break;
      }
      os.flush();

      tab.setText(os.toString(m_config.getProperty("inputEncoding")));
      tab.discardAllEdits();
   }

   String getCurrentFileContents()
   {
      return getCurrentEditor().getText();
   }

   /**
    * Get the editor for the currently selected tab.
    *
    * @return the editing component for the currently selected tab.
    */
   private JTextArea getCurrentEditor()
   {
      return getCurrentTabInfo();
   }

   /**
    * Get the editor for the provided tab ID.
    *
    * @return the editing component for the tab with the supplied ID, or null
    *    if no tab found for this ID.
    */
   private JTextArea getEditor(String tabId)
   {
      return getTabInfo(tabId);
   }

   /**
    * Sets the title of the main window based on the current tab and the file
    * name set for that tab.
    */
   public void setTitle()
   {
      int index = m_tabbedPane.getSelectedIndex(true);
      DnDTab tc = null;
      String filename = null;
      if (index >= 0)
      {
         tc = (DnDTab) m_tabs.get(index);
         filename = tc.getFileName();
         tc.requestFocus();
         if (null != filename)
         {
            int nameStart = filename.lastIndexOf(File.separatorChar);
            filename = filename.substring(nameStart+1);
         }
      }

      setTitle(BASE_TITLE + (null == filename ? "" : (" - " + filename)));
   }

   /**
    * This method handles state changes from the tabbed pane which containes
    * all edit/display tabs.
    */
   void jTabbedPane_stateChanged(ChangeEvent e)
   {
      setTitle();
      DnDTab tc = getCurrentTabInfo();
      getCurrentEditor().requestFocus();
      m_statusBar.setStatusText(null == tc ? "" : tc.getTooltip());
      setLineColumnStatus(tc);
      updateMenu();

      // set the new selected document for the search dialog
      int selectedIndex = m_tabbedPane.getSelectedIndex(true);
      m_findDialog.newSearchDocument((JScrollPane) m_tabbedPane.getComponentAt(selectedIndex));
      m_replaceDialog.newSearchDocument((JScrollPane) m_tabbedPane.getComponentAt(selectedIndex));
   }

   /**
    * Get the program resources.
    */
   public static ResourceBundle getRes()
   {
      /* load the resources first. this will throw an exception if we can't
      find them */
      if (m_res == null)
         m_res = ResourceBundle.getBundle( "com.percussion.htmlConverter.MainFrame"
         + "Resources", Locale.getDefault());

      return m_res;
   }

   /**
    * Implementation for the CaretListener interface.
    */
   public void caretUpdate(CaretEvent event)
   {
      if (event.getSource() instanceof DnDTab)
      {
         DnDTab tab = (DnDTab) event.getSource();
         setLineColumnStatus(tab);

         updateMenu();
      }
   }

   /**
    * Update the edit menu status according to the current visible tab
    * component.
    */
   public void updateMenu()
   {
      JTextArea editor = getCurrentEditor();

      // update file menu
      ((Action) m_actions.get(OPEN)).setEnabled(((DnDTab) editor).isLoadable());
      ((Action) m_actions.get(SAVEAS)).setEnabled(((DnDTab) editor).canUndo());
      ((Action) m_actions.get(SAVEALL)).setEnabled(((DnDTab) editor).isSaveable());

      // update view menu
      ((Action) m_actions.get(FORMAT)).setEnabled(((DnDTab) editor).getId() == SOURCE_HTML ||
      ((DnDTab) editor).getId() == MERGED_HTML);

      // update edit menu
      if (editor instanceof DnDTab)
      {
         DnDTab tab = (DnDTab) editor;
         ((PSAction) m_actions.get(UNDO)).setEnabled(tab.canUndo() &&
         editor.isEditable());
         ((PSAction) m_actions.get(REDO)).setEnabled(tab.canRedo() &&
         editor.isEditable());
      }

      String strSelected = editor.getSelectedText();
      ((PSAction) m_actions.get(COPY)).setEnabled(strSelected != null);

      if (strSelected == null || !editor.isEditable())
      {
         ((PSAction) m_actions.get(CUT)).setEnabled(false);
         ((PSAction) m_actions.get(DELETE)).setEnabled(false);
      }
      else
      {
         ((PSAction) m_actions.get(CUT)).setEnabled(true);
         ((PSAction) m_actions.get(DELETE)).setEnabled(true);
      }

      String strText = editor.getText();
      if (strText == null || strText.equals(""))
      {
         ((PSAction) m_actions.get(SELECT_ALL)).setEnabled(false);
         ((PSAction) m_actions.get(FIND)).setEnabled(false);
         ((PSAction) m_actions.get(FIND_NEXT)).setEnabled(false);
         ((PSAction) m_actions.get(REPLACE)).setEnabled(false);
         ((PSAction) m_actions.get(GOTO)).setEnabled(false);
      }
      else
      {
         ((PSAction) m_actions.get(SELECT_ALL)).setEnabled(true);
         ((PSAction) m_actions.get(FIND)).setEnabled(true);
         ((PSAction) m_actions.get(FIND_NEXT)).setEnabled(true);
         ((PSAction) m_actions.get(REPLACE)).setEnabled(true);
         ((PSAction) m_actions.get(GOTO)).setEnabled(true);
      }

      Clipboard cb = getToolkit().getSystemClipboard();
      boolean canPaste = cb.getContents(this) instanceof StringSelection;
      ((PSAction) m_actions.get(PASTE)).setEnabled(canPaste &&
      editor.isEditable());
   }

   /**
    * Update the line and column status for the provided tab.
    *
    * @param tab the tab to update for.
    */
   private void setLineColumnStatus(DnDTab tab)
   {
      try
      {
         int dotPos = tab.getCaretPosition();
         int lineOffset = tab.getLineOfOffset(dotPos);
         int lineStartOffset = tab.getLineStartOffset(lineOffset);

         int column = dotPos-lineStartOffset+1;
         int line = lineOffset+1;
         m_statusBar.setLineColumnText(line, column);
      }
      catch (BadLocationException e)
      {
         // this should never happen, so just print the stack trace
         e.printStackTrace();
      }
   }

   /**
    * Appends the current error stream to the error output text area and
    * resets the stream.
    */
   private void appendErrorOutput()
   {
      m_errorOut.append(m_err.toString());
      m_err.reset();
   }

   /**
    * @return an int that increments each time this method is called.
    **/
   private int getDefaultCounter()
   { return m_defaultCounter++; }
   private int m_defaultCounter = 1;

   private static final String BASE_TITLE = "Percussion Rhythmyx XSpLit";
   /**
    * Each time a file is opened, the directory is stored here so it can be used
    * to default the open dialog the next time it is accessed.
    **/
   private File m_lastOpenedDir = null;

   /**
    * The program resources. You must access this variable thru the {@link
    * #getRes getRes} method.
    */
   private static ResourceBundle m_res = null;
   /**
    * The dialog used to search for a provided string in the document set.
    */
   private static FindDialog m_findDialog = null;
   /**
    * The dialog used to search/replace for a provided string in the document
    * set.
    */
   private static ReplaceDialog m_replaceDialog = null;
   /**
    * The context menu valid for the selected tab.
    */
   private JPopupMenu m_contextMenu = null;

   /**
    * The splitter configuration used for this instance. Initialized during
    * construction, never <code>null</code> after that.
    */
   private static SplitterConfiguration m_config = null;

   /**
    * The postfix of the splitter configuation property entry name used for
    * all tabs. This will be prepended with the tab name to store the last
    * open/save file location used for that tab.
    */
   private final static String LAST_OPEN_DIR = "LastOpenDir";

   /**
    * The singleton instance of java help so that it is not garbage collected,
    * <code>null</code> until first call to <code>initializeHelp()</code> and
    * never <code>null</code> or modified after that.
    */
   private PSJavaHelp m_help = null;

   /**
    * The help topic id of this application, <code>null</code> until first call
    * to <code>initializeHelp()</code> and never <code>null</code>, empty or
    * modified after that.
    */
   private String m_helpTopicId = null;

   /**
    * The name of the parameter which provides helpset file.
    */
   private static final String HELPSETFILE = "helpset_file";

   /**
    * The name of the parameter which defines help topic id to be displayed.
    */
   private static final String HELPTOPICID = "helpid";

   /**
    * Flag indicating that this is a standalone app
    */
   private boolean m_isStandalone = false;

}
