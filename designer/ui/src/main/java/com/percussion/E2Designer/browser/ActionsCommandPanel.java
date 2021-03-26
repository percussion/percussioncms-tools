/*******************************************************************************
 *
 * [ ActionsCommandPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionParameter;
import com.percussion.cms.objectstore.PSActionParameters;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The command panel lets the designer specify the actual command that is
 * executed with this menu.
 */
public class ActionsCommandPanel extends JPanel
{
   /**
    * Constructs the panel.
    *
    * @param catalogs a list of cataloges as <code>List</code>, not
    *    <code>null</code> or empty.
    */
   public ActionsCommandPanel(List catalogs)
   {
      if (catalogs == null || catalogs.isEmpty())
         throw new IllegalArgumentException("catalog list cannot be null");

      init(catalogs);
   }

   /**
    * Initializes the panel.
    *
    * @param catalogs a list of cataloges as <code>List</code>, not
    *    <code>null</code> or empty.
    */
   private void init(List catalogs)
   {
      //add data to the combo box
      m_comboVec.add(ms_res.getString("combo.ca"));
      m_nameUrlMap.put(ms_res.getString("combo.ca"), CONTENT_ASSEMBLER_EXAMPLE);
      m_comboVec.add(ms_res.getString("combo.ce"));
      m_nameUrlMap.put(ms_res.getString("combo.ce"), CONTENT_EDITOR_EXAMPLE);
      m_comboVec.add(ms_res.getString("combo.rh"));
      m_nameUrlMap.put(ms_res.getString("combo.rh"), RELATIONSHIP_HANDLER_EXAMPLE);

      setLayout(new BorderLayout());
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      setBorder(emptyBorder);

      JPanel topPanel = new JPanel();
      topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

      PSPropertyPanel comboPanel = new PSPropertyPanel();
      final JComboBox combo = new JComboBox(m_comboVec);

      char mn = ms_res.getString("combo.label.mn").charAt(0);
      comboPanel.addPropertyRow(ms_res.getString("combo.label"), 
                  new JComponent[] {combo}, combo, mn, null);

      JButton btn = new JButton(ms_res.getString("btn.generate"));
      btn.setMnemonic(ms_res.getString("btn.generate.mn").charAt(0));
      btn.setPreferredSize(new Dimension(100, 8));

      btn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            String example = (String) combo.getSelectedItem();
            m_urlPanel.setUrl((String) m_nameUrlMap.get(example), null);
         }
      });

      topPanel.add(comboPanel);
      topPanel.add(Box.createHorizontalStrut(20));
      topPanel.add(btn);
      topPanel.setBorder(emptyBorder);
      add(topPanel, BorderLayout.NORTH);

      List contextParameters = (ArrayList) catalogs.get(3);
      m_urlPanel = new PSUrlPanel(contextParameters.iterator(), true);

      add(m_urlPanel, BorderLayout.CENTER);
   }

   public boolean update(Object data, boolean isLoad)
   {
      PSAction action = null;
      if (data instanceof PSAction)
      {
         action = (PSAction) data;

         if (isLoad)
         {
            setUrl(action.getURL(), action.getParameters());
         }
         else
         {
            action.setURL(m_urlPanel.getUrlFile());

            PSActionParameters parameters = new PSActionParameters();
            parameters.fromMap(m_urlPanel.getParameters());

            PSActionParameters current = action.getParameters();

            // add any new parameters and update changed values
            Iterator params = parameters.iterator();
            while (params.hasNext())
            {
               PSActionParameter parameter = (PSActionParameter) params.next();
               current.add(parameter);
            }

            // remove deleted parameters
            List removeList = new ArrayList();
            params = current.iterator();
            while (params.hasNext())
            {
               PSActionParameter parameter = (PSActionParameter) params.next();
               if (!parameters.contains(parameter))
                  removeList.add(parameter);
            }
            for (int i=0; i<removeList.size(); i++)
               current.remove((PSActionParameter) removeList.get(i));
         }
      }

      return true;
   }

   /**
    * Get the full url string.
    *
    * @return the url string, never <code>null</code>, may be empty.
    */
   public String getUrl()
   {
      return m_urlPanel.getUrl();
   }

   /**
    * Set the url.
    *
    * @param url a url string, may be <code>null</code> or empty, can have
    *    parameters attached.
    * @param params a collection of url parameters, may be <code>null</code>
    *    or empty.
    */
   public void setUrl(String url, PSActionParameters params)
   {
      if (params == null)
         params = new PSActionParameters();

      m_urlPanel.setUrl(url, params.toMap());
   }

   /**
    * Get all url parameters.
    *
    * @return the url parameters, never <code>null</code>, may be empty.
    */
   public PSActionParameters getUrlParameters()
   {
      PSActionParameters parameters = new PSActionParameters();
      parameters.fromMap(m_urlPanel.getParameters());

      return parameters;
   }

   /**
    * Set the url parameters.
    *
    * @param parameters a collection or url parameters, may be
    *    <code>null</code> or empty.
    */
   public void setUrlParameters(PSActionParameters parameters)
   {
      if (parameters == null)
         parameters = new PSActionParameters();

      m_urlPanel.setParameters(parameters.toMap());
   }

   /**
    * Validates the data of this panel.
    *
    * @return <code>true</code> if all data is valid, <code>false</code>
    *    otherwise.
    */
   public boolean validateData()
   {
      return m_urlPanel.isValid(m_urlPanel.getUrl(), -1);
   }

   private Vector m_comboVec = new Vector();

   /**
    * The content editor example url.
    */
   private static final String CONTENT_EDITOR_EXAMPLE =
      "../rx_ceArticle/article.html" +
      "?sys_command=edit&$sys_contentid=$sys_contentid&sys_pageid=0&sys_view=sys_All" +
      "&sys_revision=$sys_revision";

   /**
    * The content assembler example url.
    */
   private static final String CONTENT_ASSEMBLER_EXAMPLE =
      "../casArticle/casArticle.html" +
      "?sys_contentid=$sys_contentid&sys_variantid=$sys_variantid" +
      "&sys_context=$sys_context&sys_revision=$sys_revision" +
      "&sys_authtype=$sys_authtype";

   /**
    * The relationship handler example url.
    */
   private static final String RELATIONSHIP_HANDLER_EXAMPLE =
      "../rx_ceArticle/article.html" +
      "?sys_command=relate/create&sys_contentid=$sys_contentid" +
      "&sys_revision=$sys_revision&sys_dependentid=$sys_dependentid";

   private Map m_nameUrlMap = new HashMap();

   /**
    * The url panel. Initialized in the ctor. Never <code>null</code>
    * after that.
    */
   private PSUrlPanel m_urlPanel = null;

   /**
    * Resource bundle for this class. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;
   static
   {
      ms_res = ResourceBundle.getBundle(ActionsCommandPanel.class.getName() +
         "Resources", Locale.getDefault());
   }

   // test code
   public static void main(String[] arg)
   {
      try
      {
         UIManager.setLookAndFeel((LookAndFeel) Class.forName(
            UIManager.getSystemLookAndFeelClassName()).newInstance());

         JFrame f = new JFrame("Test");
         Container contentPane = f.getContentPane();
         List contextParameters = new ArrayList();
         contextParameters.add("$test1");
         contextParameters.add("$test2");
         ActionsCommandPanel ac = new ActionsCommandPanel(contextParameters);

         String url = "../sys_cxSupport/contenteditorurls.html?foo=bar&test=value";
         PSActionParameters params = new PSActionParameters();
         params.add(new PSActionParameter("param1", "value1"));
         params.add(new PSActionParameter("param2", ""));
         params.add(new PSActionParameter("param3", null));
         System.out.println("Setting url: " + url + " params: " + params.toString());
         ac.setUrl(url, params);
         System.out.println("Getting url: " + ac.getUrl());

         contentPane.add(ac, BorderLayout.CENTER);
         f.addWindowListener(new WindowAdapter()
         {
            public void windowClosing(WindowEvent e)
            {
               System.exit(0);
            }
         });

         f.pack();
         f.setVisible(true);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
