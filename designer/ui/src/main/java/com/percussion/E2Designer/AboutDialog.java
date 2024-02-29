/*[ AboutDialog.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.util.PSFormatVersion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

/**
 * About dialog displays the Percussion Logo, Rhythmyx version info and has a
 * clickable link to the percussion website.
 */
public class AboutDialog extends PSDialog
{

   /**
    * constructor that takes the parent frame.
    */
   AboutDialog(JFrame parent)
   {
      super(parent);
      getContentPane().setLayout(null);
      getContentPane().setBackground(Color.white);

      initDialog();

      setSize(m_width + 6, m_height + 32);

      center();
   }

   /**
    * internal for creating the controls and initializing the dialog.
    */
   private void initDialog()
   {
      PSFormatVersion version = new PSFormatVersion("com.percussion.E2Designer");
      JLabel labelVersion = new JLabel(version.getVersionString(),
                                     JLabel.CENTER);

      //create the hostname label
      String hostname = E2Designer.getDesignerConnection().getServer();
      String hostFormat = getResources().getString("serverName");
      String [] hostparams = { hostname };
      JLabel labelServer = new JLabel(MessageFormat.format( hostFormat, hostparams ),
                                       JLabel.CENTER);

      //create the server version label
      PSFormatVersion server = E2Designer.getDesignerConnection().getServerVersion();
      String versionFormat = getResources().getString("serverVersion");
      String [] versionparams = { server.getVersionString() };
      JLabel labelServerVersion = new JLabel(MessageFormat.format( versionFormat, versionparams ),
                                       JLabel.CENTER);

      JLabel labelCopyright = new JLabel(getResources().getString("copyright"),
                                       JLabel.CENTER);

      JTextArea thirdPartyCopyright =
         new JTextArea(getResources().getString("thirdPartyCopyright"));
      thirdPartyCopyright.setEditable(false);
      thirdPartyCopyright.setLineWrap(true);
      thirdPartyCopyright.setWrapStyleWord(true);
      thirdPartyCopyright.setFont(new Font(null, Font.PLAIN, 10));
      thirdPartyCopyright.setOpaque(false);
      JScrollPane thirdPartyCopyrightScroll = new JScrollPane(thirdPartyCopyright);
      //add the URL label
      Font defaultFont = labelVersion.getFont();
      Font urlFont = new Font(defaultFont.getFontName(),
                            defaultFont.getStyle(),
                            defaultFont.getSize() + 4);
      m_labelUrl = new JLabel(sPERCUSSION_URL, JLabel.CENTER);
      m_labelUrl.setFont(urlFont);
      int yPos = m_height / 3 + 20;
      m_labelUrl.setBounds(0, yPos, m_width, 30);
      m_labelUrl.setForeground(Color.red.darker());
      m_labelUrl.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            onMouseClick();
         }
         public void mouseEntered(MouseEvent e)
         {
            onMouseEnter();
         }
         public void mouseExited(MouseEvent e)
         {
            onMouseExit();
         }
    });
      getContentPane().add(m_labelUrl);

      //add the version label
      yPos += 25;
      labelVersion.setBounds(0, yPos, m_width, 30);
      getContentPane().add(labelVersion);

      //add the server label
      yPos += 25;
      labelServer.setBounds(0, yPos, m_width, 30);
      getContentPane().add(labelServer);

      //add the server version label
      yPos += 25;
      labelServerVersion.setBounds(0, yPos, m_width, 30);
      getContentPane().add(labelServerVersion);

      //add the copyright label
      yPos += 30;
      labelCopyright.setBounds(0, yPos, m_width, 30);
      getContentPane().add(labelCopyright);

      //add 3rd party copyright
      yPos += 35;
      thirdPartyCopyrightScroll.setBounds(30, yPos, m_width - 60, 60);
      getContentPane().add(thirdPartyCopyrightScroll);

      //add the image
      JLabel imageLabel = new JLabel(m_icon);
      imageLabel.setOpaque(false);
      imageLabel.setBounds(0, 0, m_width, m_height);
      getContentPane().add(imageLabel);
   }

   /**
    * Handler for mouse exiting the URL label for percussion. Sets the color to
    * darker shade of blue and mouse cursor to default cursor.
    */
   private void onMouseExit()
   {
      m_labelUrl.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      m_labelUrl.setForeground(Color.red.darker());
   }

   /**
    * Handler for when mouse is over the URL label for percussion. Sets the color to
    * blue and mouse cursor to hand cursor.
    */
   private void onMouseEnter()
   {
      m_labelUrl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      m_labelUrl.setForeground(Color.red.brighter());
   }

   /**
    * Handler for mouse click on the URL. Starts up the default browser and displays the
    * percussion web page.
    */
   private void onMouseClick()
      {
	   openInSystemBrowser(sPERCUSSION_URL);
   }

   private void openInSystemBrowser(String url) {
	if (Desktop.isDesktopSupported()) {
	     // Windows
	     try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException e) {
			// Not critical if error, just print stack and return
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	 } else {
	     // Ubuntu
	     Runtime runtime = Runtime.getRuntime();
	     try {
			runtime.exec("/usr/bin/firefox -new-window " + url);
		} catch (IOException e) {

			e.printStackTrace();
         }
      }
   }

   /**
    * For testing the dialog.
    */
  public static void main(String[] args)
  {
    final JFrame frame = new JFrame("Test About Dialog");
      frame.addWindowListener(new BasicWindowMonitor());
      try
    {
      String strLnFClass = UIManager.getSystemLookAndFeelClassName();
         LookAndFeel lnf = (LookAndFeel) Class.forName(strLnFClass).newInstance();
         UIManager.setLookAndFeel( lnf );

         JButton startButton = new JButton("Open Dialog");
         frame.getContentPane().add(startButton);
         startButton.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
          AboutDialog dialog = new AboutDialog(frame);
            dialog.setLocationRelativeTo(frame);
               dialog.setVisible(true);
            }
         });

         frame.setSize(400, 300);
         frame.setVisible(true);
     }
     catch (Exception e)
     {
      System.out.println(e);
     }
   }

   /**
    * the label for URL of Percussion website.
    */
   JLabel m_labelUrl = null;

   public static final String sPERCUSSION_URL = "https://percussionsupport.intsof.com";

  // about dialog size
  private ImageIcon m_icon = new ImageIcon(getClass().getResource(getResources().getString("gif_about")));
  private int m_width = m_icon.getIconWidth();
  private int m_height = m_icon.getIconHeight();
}
