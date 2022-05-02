package com.percussion.cx.javafx;

import com.percussion.cx.PSContentExplorerApplication;
import com.percussion.cx.PSSelection;
import com.percussion.cx.objectstore.PSMenuAction;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;

import static javafx.concurrent.Worker.State.FAILED;

public class PSSimpleSwingBrowser extends PSDesktopExplorerWindow
{

   private static final String CANNOT_READ_MESSAGE = "Screen reader cannot read embedded browser.  Select button to open in system browser.";

   static Logger log = Logger.getLogger(PSSimpleSwingBrowser.class);

   private static boolean isMac = System.getProperty("os.name").toLowerCase(Locale.US).startsWith("mac");
   
   private static final long serialVersionUID = 1L;

   protected static final String TINYMCE = "window.tinymce";
   protected static final String TINYMCE_EDITOR = TINYMCE+".EditorManager.activeEditor";

   private final JFXPanel jfxPanel = new JFXPanel();

   private final JPanel panel = new JPanel();

   private boolean windowLoaded = false;

   private final JLabel lblStatus = new JLabel();
   
   JPanel topBar = new JPanel(new BorderLayout(5, 0));

   private final JButton btnGo = new JButton("Open in System Browser to Read");
   private final JButton btnDone = new JButton("Done");
   private final JTextField txtURL = new JTextField();
   private final JProgressBar progressBar = new JProgressBar();

   
   public PSSimpleSwingBrowser()
   {
      super();
   }

   @Override
   public boolean validateOpen(String mi_actionurl, String mi_target, String mi_style, PSSelection selection,
         PSMenuAction action)
   {
      // Everything else we try to open with this;
      return true;
   }

   @Override
   public JFrame instanceOpen()
   {
      
      this.getAccessibleContext().setAccessibleName("Embedded Browser Window");
      this.getAccessibleContext().setAccessibleDescription(CANNOT_READ_MESSAGE);

         setClosed(false);
         if (this.windowLoaded)
         { 
            Platform.runLater( () -> {
               loadURL(this.mi_actionurl);
            });
         }
         else
         {

            webView = new WebView();
            webView.setContextMenuEnabled(false);
            //createContextMenu(webView);
            // Create object to allow javascript to call java
            PSSimpleSwingBrowser.this.engine = webView.getEngine();
            setJavaBridge();
            Platform.runLater( () -> {
               initComponents(this.mi_actionurl);
               setVisible(true);
            });
         }
      return this;
   }

   private void initComponents(String mi_actionurl)
   {
      
      createScene();

      addWindowListener(new WindowAdapter()
      {
         @Override
         public void windowClosing(WindowEvent e)
         {
            log.debug("window closing" + e);
            Platform.runLater(new Runnable() {
               @Override
               public void run() {
                  try {
                     Object shouldClose = PSSimpleSwingBrowser.this.engine.executeScript("checkBeforeClose()");
                     if(Boolean.TRUE.equals(shouldClose)){
                        managerClose();
                     }
                  } catch (Exception e) {
                     //This dialog might not be Form related, thus will not have this method
                     managerClose();
                  }
               }
            });
         }
      });

      btnGo.addActionListener(e -> {
         URL url = PSBrowserUtils.toURL(mi_actionurl);
         PSBrowserUtils.openWebpage(url);
      });

      btnDone.addActionListener(e -> {
         SwingUtilities.invokeLater( () ->
            PSContentExplorerApplication.getApplet().refresh("Selected"));
            this.closeDceWindow();
      });

      //txtURL.addActionListener(al);
      SwingUtilities.invokeLater( () -> {
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());

      JButton openInBrowser = new JButton("Open in Browser");
      openInBrowser.requestFocusInWindow();
      openInBrowser.getAccessibleContext().setAccessibleDescription(CANNOT_READ_MESSAGE);
      openInBrowser.addActionListener(e -> {
         log.debug("Open In browser pressed" + mi_actionurl);
         URL url = PSBrowserUtils.toURL(mi_actionurl);
         PSBrowserUtils.openWebpage(url);
      });

      progressBar.setPreferredSize(new Dimension(150, 18));
      progressBar.setStringPainted(true);

      topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
      //topBar.add(txtURL, BorderLayout.CENTER);

      topBar.add(btnGo, BorderLayout.CENTER);
      topBar.add(btnDone, BorderLayout.EAST);
      topBar.setName("Accessibilty menu");
      topBar.getAccessibleContext().setAccessibleName("Accessibilty menu");
      topBar.setVisible(false);

      JPanel statusBar = new JPanel(new BorderLayout(5, 0));
      statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
      statusBar.add(lblStatus, BorderLayout.CENTER);
      statusBar.add(progressBar, BorderLayout.EAST);



      panel.add(topBar, BorderLayout.NORTH);

      this.jfxPanel.setSize(new Dimension(this.browserProps.getWidth(), this.browserProps.getHeight()));
      this.jfxPanel.setPreferredSize(new Dimension(this.browserProps.getWidth(), this.browserProps.getHeight()));


      panel.add(jfxPanel, BorderLayout.CENTER);
      panel.add(statusBar, BorderLayout.SOUTH);


      getContentPane().add(panel);


      setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
      validate();
      pack();
      repaint();

      });

   }

   public void loadURL(final String url)
   {

         String tmp = toURL(url);

         if (tmp == null)
         {
            tmp = toURL("http://" + url);
         }

         if (PSSimpleSwingBrowser.this.mi_actionurl != url)
            PSSimpleSwingBrowser.this.mi_actionurl = tmp;

         PSSimpleSwingBrowser.this.engine.load(tmp);

   }

   private static String toURL(String str)
   {
      try
      {
         return new URL(str).toExternalForm();
      }
      catch (MalformedURLException exception)
      {
         return null;
      }
   }

 private void createScene()
   {
         String userAgent = PSSimpleSwingBrowser.this.engine.getUserAgent();
         userAgent += " PercussionDCE/0.0.0";
         PSSimpleSwingBrowser.this.engine.setUserAgent(userAgent);
         log.debug("User agent set to "+userAgent);

         PSSimpleSwingBrowser.this.engine.titleProperty().addListener(
               (obs1, oldValue1, newValue1) ->
               {
                  setJavaBridge();
                  Platform.runLater(() -> PSSimpleSwingBrowser.this.setTitle(newValue1));
               });


         PSSimpleSwingBrowser.this.engine.locationProperty().addListener(
               (obs1, oldValue1, newValue1) ->
               {
                  log.debug("locationProperty:"+obs1+":"+oldValue1+":"+newValue1);
                  mi_actionurl = newValue1;
               });

         PSSimpleSwingBrowser.this.engine.documentProperty().addListener(
               (obs1, oldValue1, newValue1) ->
               {
                  log.debug("DocumentProperty:"+obs1+":"+oldValue1+":"+newValue1);
                  setJavaBridge();

               });

         PSSimpleSwingBrowser.this.engine.setOnResized(new EventHandler<WebEvent<Rectangle2D>>() {

            @Override
            public void handle(WebEvent<Rectangle2D> event)
            {
              log.debug("js size" +event.getData().getHeight()+ ":"+event.getData().getWidth());
              PSSimpleSwingBrowser.this.setSize((int)event.getData().getWidth(),(int)event.getData().getHeight());
            }

         });


         engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(final WebEvent<String> event) {
                setJavaBridge();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        lblStatus.setText(event.getData());
                    }
                });
            }
        });


      // Although Keyboard paste event is firing and being picked up by TinyMCE,
      // it
      // seems that each additionl copy to clipboard is appending to the
      // DataTransfer.items
      // and DataTransfer.types objects. This is causing previous html copied to
      // be pasted
      // in when text has more recently been copied. Instead we will capture the
      // paste key
      // event and will call the paste event ourselves and prevent the key form
      // being passed down
      webView.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>()
      {

         @Override
         public void handle(KeyEvent event)
         {
            String op = null;

            switch (event.getCode())
            {
               case V :
                  op = "paste";
                  break;
               // calling copy does not work as element required to be in
               // plcase.
               // regular handler works for copy. cut event will use dummy event
               // to set our object.
               // case C:
               // op = "copy";
               // break;
               case X :
                  op = "cut";
                  break;
            }

            // shortcut handles ctrl on windows and cmd on osx
            if (op != null)
            {

               if ((isMac && event.isMetaDown()) || event.isControlDown())
               {
               Object tinymce = engine.executeScript(TINYMCE);
               if (tinymce != null && tinymce instanceof JSObject )
               {
                     log.debug("Found tinymce");
                  Object focusedEditor = engine.executeScript(TINYMCE + ".EditorManager.focusedEditor");
                  if (focusedEditor != null && focusedEditor instanceof JSObject)
                  {
                        log.debug("Found focused tinymce");
                        // Only paste if we are actually focused on the editor
                        // body not another tinymce location
                        Boolean active = (Boolean) engine.executeScript(
                              "document.activeElement.contentDocument == tinymce.activeEditor.contentDocument");
                     if (active)
                     {
                           log.debug("contentDocument is active in tinymce - sending " + op);
                        //prevent key from being handled by tinymce

                        event.consume();
                        // fire fake clipboard event;

                        engine.executeScript(
                                 TINYMCE_EDITOR + ".fire('" + op + "',window.java.getClipboardDataEvent());");
                        }
                     }
                  }
               }
            }
         }
      });


      final ContextMenu contextMenu = createContextMenu(webView);

      webView.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
      {
         public void handle(MouseEvent event)
         {
            if (event.getButton() == MouseButton.SECONDARY)
            {
               Object tinyMCE = (Object)engine.executeScript(TINYMCE);
               boolean hasTinyMCE = !(tinyMCE == null || tinyMCE.toString().equals("undefined"));

               if (hasTinyMCE)
               {
                  log.debug("Page uses tinymce checking context area");
                  JSObject jsObject = (JSObject) engine
                        .executeScript(TINYMCE_EDITOR + ".getContentAreaContainer().getBoundingClientRect()");

                  int x = ((Number) jsObject.getMember("left")).intValue();
                  int y = ((Number) jsObject.getMember("top")).intValue();
                  int bottom = ((Number) jsObject.getMember("bottom")).intValue();
                  int right = ((Number) jsObject.getMember("right")).intValue();

                  if (event.getX() > x && event.getX() < right && event.getY() > y && event.getY() <= bottom)
                  {

                     y = (int) (event.getY() - y) - 10;
                     x = (int) (event.getX() - x) - 10;
                     log.debug("Initiating TinyMCE context menu");
                     engine.executeScript(TINYMCE_EDITOR + ".selection.placeCaretAt(" + x + "," + y + ");");
                     String script = "var evt = document.createEvent('MouseEvents');evt.initMouseEvent('contextmenu', true, true, window, 1, 0, 0, "
                           + x + ", " + y + ", false, false, false, false, 2, null);" + TINYMCE_EDITOR
                           + ".getDoc().documentElement.dispatchEvent(evt);";
                     engine.executeScript(script);

                  }
                  else
                  {
                     Platform.runLater(() -> {
                        contextMenu.show(webView, event.getScreenX(), event.getScreenY());
                     });
                     log.debug("Initiating TinyMCE JavaFx context menu");

                  }
                  event.consume();
               }
               else
               {
                  contextMenu.show(webView, event.getScreenX(), event.getScreenY());
               }
            }
            else contextMenu.hide();
         };
      });

         engine.locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        txtURL.setText(newValue);
                    }
                });
            }
        });

         engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setValue(newValue.intValue());
                    }
                });
            }
        });

         PSSimpleSwingBrowser.this.engine.setOnVisibilityChanged(e1 -> {
            if (!e1.getData())
            {
               log.debug("window.close called");
            }
         });

         PSSimpleSwingBrowser.this.engine.setConfirmHandler(message -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");

            alert.setContentText(message);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK)
            {
               return true;
            }

            return false;

         });

         PSSimpleSwingBrowser.this.engine.setOnAlert(event -> {


            String data = event.getData();
            log.debug("Alert: " + data);
            if(data.equals("undefined")){
               log.warn("Data Undefined returned.");
               return;
            }
            if (data.equals("WebKitTrigger"))
            {
               log.warn("Found WebKitTrigger alert from older version of Server.");
            }
         });


         PSSimpleSwingBrowser.this.engine.getLoadWorker().stateProperty().addListener((obs2, oldValue2, newValue2) -> {
            log.debug("state changed from " + oldValue2.name() + " to " + newValue2.name());

            if (oldValue2 == Worker.State.SCHEDULED && newValue2 == Worker.State.RUNNING)
            {
               setJavaBridge();
            }
            if (newValue2 == Worker.State.SUCCEEDED )
            {
               String location = engine.getLocation();
               log.debug("Loaded url "+location+ "to window "+this.target);

               PSContentExplorerApplication.getApplet().refresh("Selected");


               checkAndShowAppletWarning(PSSimpleSwingBrowser.this.engine);

               return;
            }

         });

         PSSimpleSwingBrowser.this.engine.getLoadWorker()
               .exceptionProperty()
               .addListener(
                     (o, old, value) -> {
                        if (PSSimpleSwingBrowser.this.engine.getLoadWorker().getState() == FAILED)
                        {
                        
                              JOptionPane.showMessageDialog(PSSimpleSwingBrowser.this.panel, value != null ? PSSimpleSwingBrowser.this.engine.getLocation() + "\n"
                                    + value.getMessage() : PSSimpleSwingBrowser.this.engine.getLocation() + "\nUnexpected error.",
                                    "Loading error...", JOptionPane.ERROR_MESSAGE);
         
                        }
                     });

         // Disable javafx pouphandler we are overriding window.open.
         this.engine.setCreatePopupHandler(popupFeatures -> null);

         Scene scene = new Scene(webView);

         PSSimpleSwingBrowser.this.jfxPanel.setScene(scene);

         Platform.runLater(() -> 
            PSSimpleSwingBrowser.this.engine.load(PSSimpleSwingBrowser.this.mi_actionurl)
         );


   }

   
   private ContextMenu createContextMenu(WebView webView) {
      ContextMenu contextMenu = new ContextMenu();
      MenuItem reload = new MenuItem("Reload");
      reload.setOnAction(e -> webView.getEngine().reload());
      MenuItem copyLink = new MenuItem("Copy Link to Clipboard");
      final CheckMenuItem debugMenuItem = new CheckMenuItem("Debug");
      debugMenuItem.setSelected(firebug.get());
      
      debugMenuItem.setOnAction(e -> 
      {
         Platform.runLater(() ->
         {
            firebug.set(debugMenuItem.isSelected());
          
            if (firebug.get())
               showFirebug();
            else
               webView.getEngine().reload();
            
         });
      });
      
      copyLink.setOnAction(e -> {
         final Clipboard clipboard = Clipboard.getSystemClipboard();
         final ClipboardContent content = new ClipboardContent();
         content.putString(mi_actionurl);
         clipboard.setContent(content);
      });
      MenuItem openInBrowser = new MenuItem("Open In Browser");
      openInBrowser.setOnAction(e ->
      {
         URL url = PSBrowserUtils.toURL(mi_actionurl);
         if (url!=null)
            PSBrowserUtils.openWebpage(url);
      });
      
      contextMenu.getItems().addAll(reload, copyLink, openInBrowser, debugMenuItem);

      return contextMenu;
   }
   
   /**
    * Check and show applet warning
    *
    * @param engine
    */
   private void checkAndShowAppletWarning(WebEngine engine)
   {
      JSObject editliveSections = (JSObject) webView
            .getEngine()
            .executeScript(
                  "if(window.jQuery){ jQuery(\".editableEditLiveSection\").map(function(){return this.id;}).get();}else{new Object();}");

      if (editliveSections != null && !editliveSections.getSlot(0).equals("undefined"))
      {

         int i = 0;
         StringBuilder typeString = new StringBuilder();
         while (editliveSections.getSlot(i) != null && !editliveSections.getSlot(i).equals("undefined"))
         {
            if (i > 0)
               typeString.append(", ");
            typeString.append((String) editliveSections.getSlot(i++));
         }
         log.debug("types" + typeString);

         String url = engine.getLocation();
         String type = StringUtils.substringBetween(url, "/psx_ce", "/");
         String id = StringUtils.substringBetween(url, "&sys_contentid=", "&");
         String user = PSContentExplorerApplication.getApplet().getUserInfo().getUserName();
         Alert alert = new Alert(AlertType.WARNING);
         alert.initModality(Modality.APPLICATION_MODAL);
         alert.setTitle("Warning");
         alert.setHeaderText("EditLive Field Type Not Supported in Desktop Content Explorer");
         alert.setContentText("The EditLive field type used in this editor requires Java Applet support.  \r\nThe application needs to be reconfigured to use TinyMCE Javascript control to be able to edit in Desktop Content Explorer");

         Label label = new Label(
               "The following text has been copied to your clipboard and can be sent to you Content Managment development team");

         TextArea textArea = new TextArea(
               "----\r\nUser '"
                     + user
                     + "' tried to edit item # "
                     + id
                     + " but the content type still includes fields that use Java Applets.\r\n\r\nContent Type: "
                     + type
                     + "\r\nAffected Fields: "
                     + typeString
                     + "\r\nurl="
                     + url
                     + "\r\n\r\nPercussion recommends that the CMS Web Developer update the fields on this content type to allow "
                     + user + " to edit the item with the Desktop Content Explorer. \r\n----");

         final Clipboard clipboard = Clipboard.getSystemClipboard();
         final ClipboardContent content = new ClipboardContent();
         content.putString(textArea.getText());
         clipboard.setContent(content);

         textArea.setEditable(false);
         textArea.setWrapText(true);

         textArea.setMaxWidth(Double.MAX_VALUE);
         textArea.setMaxHeight(Double.MAX_VALUE);
         GridPane.setVgrow(textArea, Priority.ALWAYS);
         GridPane.setHgrow(textArea, Priority.ALWAYS);

         GridPane expContent = new GridPane();
         expContent.setMaxWidth(Double.MAX_VALUE);
         expContent.add(label, 0, 0);
         expContent.add(textArea, 0, 1);

         // Set expandable Exception into the dialog pane.
         alert.getDialogPane().setExpandableContent(expContent);
         alert.getDialogPane().setExpanded(true);
         alert.showAndWait();
         managerClose();
      }
   }


   public void frameWindowStateChanged(WindowEvent e)
   {
      // minimized
      if ((e.getNewState() & Frame.ICONIFIED) == Frame.ICONIFIED)
      {
         // log.debug("minimized");
      }
      // maximized
      else if ((e.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH)
      {
         Rectangle r = e.getComponent().getBounds();

         Dimension dim = new Dimension();
         dim.setSize((int) r.getWidth() - 15, (int) r.getHeight() - 15);

         this.jfxPanel.setPreferredSize(dim);

      }
   }
  
   public AccessibleContext getAccessibleContext() {
      if (accessibleContext == null) {
          accessibleContext = new PSAccessibleJFrame();
      }
      return accessibleContext;
  }

  protected class PSAccessibleJFrame extends AccessibleJFrame {
      @Override
      public String getAccessibleName()
      {
         if (!topBar.isVisible())
         {
            topBar.setVisible(true);
            btnGo.requestFocusInWindow();
            jfxPanel.setFocusable(false);
         }
         return super.getAccessibleName();
      }
  }
   
}
