/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.htmlConverter;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.percussion.util.PSFormatVersion;

/**
 * Taken from Rhythmyx and modified slightly.<p/>
 * About dialog displays the Percussion Logo, version info and has a
 * clickable link to the percussion website.
 */
public class AboutDialog extends JDialog
{

   /**
    * constructor that takes the parent frame.
    */
   AboutDialog(JFrame parent)
   {
      super(parent);
      loadResources();
      this.setResizable(false);
      getContentPane().setLayout(null);
      getContentPane().setBackground(Color.white);
      // 23 is the size of the title bar on Windows L&F
      setSize(m_width, m_height+23);
      initDialog();
      // center it
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = getSize();
      setLocation(( screenSize.width - size.width ) / 2,
            ( screenSize.height - size.height ) / 2 );

   }

   /**
    * internal for creating the controls and initializing the dialog.
    */
   private void initDialog()
   {
      PSFormatVersion version =
                   new PSFormatVersion("com.percussion.htmlConverter");
                JLabel labelVersion = new JLabel(version.getVersionString(), JLabel.CENTER);
      JLabel copyright = new JLabel(ms_res.getString("copyright"),
                                    JLabel.CENTER);

      //add the URL label
      Font defaultFont = labelVersion.getFont();
      Font urlFont = new Font(defaultFont.getFontName(),
                                          defaultFont.getStyle(),
                                          defaultFont.getSize() + 4);
      m_labelUrl = new JLabel(sPERCUSSION_URL, JLabel.CENTER);
      m_labelUrl.setFont(urlFont);
      FontMetrics fm = m_labelUrl.getFontMetrics( urlFont );
      /* The height of the white area at the bottom is 90 pixels. Divide the area
         into 3 rows. Put the version/copywrite in the last row. Put the URL centered
         between the first 2 rows. */
      final int WHITESPACE = 90;
      int yPos = (m_height - WHITESPACE) + WHITESPACE/3 - fm.getHeight()/2 ;
      m_labelUrl.setBounds(0, yPos, m_width, 30);
      m_labelUrl.setForeground(Color.red.darker());
      m_labelUrl.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            Rectangle rect = m_labelUrl.getBounds();
            e.translatePoint((int)rect.getX(), (int)rect.getY());
            onMouseClick(e.getPoint());
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

      yPos = (m_height - WHITESPACE) + WHITESPACE/2;
      labelVersion.setBounds(0, yPos, m_width, 30);
      getContentPane().add(labelVersion);

      yPos = (m_height - WHITESPACE) + 2*WHITESPACE/3;
      copyright.setBounds(0, yPos, m_width, 30);
      getContentPane().add(copyright);

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
   private void onMouseClick(Point p)
   {
      Rectangle rect = m_labelUrl.getBounds();
      if(rect != null)
      {
         if(rect.contains(p))
         {
            UTBrowserControl.displayURL(sPERCUSSION_URL);
         }
      }
   }
   
   /**
    * Get the program resources.
    */
   public static void loadResources()
   {
      /* load the resources first. this will throw an exception if we can't
      find them */
      if (ms_res == null)
         ms_res = ResourceBundle.getBundle( "com.percussion.htmlConverter.AboutDialog"
         + "Resources", Locale.getDefault());
   }

   /**
    * the label for URL of Percussion website.
    */
   JLabel m_labelUrl = null;

   public static final String sPERCUSSION_URL = "https://www.percussion.com";

   // about dialog size
   private ImageIcon m_icon = new ImageIcon(getClass().getResource(("images/aboutrhythmyxXsplit.gif")));
   private int m_width = m_icon.getIconWidth();
   private int m_height = m_icon.getIconHeight();
   
   /**
    * The resource bundle for this class, loaded once by the
    * ctor. Never <code>null</code> after that.
    */
   private static ResourceBundle ms_res; 

}
