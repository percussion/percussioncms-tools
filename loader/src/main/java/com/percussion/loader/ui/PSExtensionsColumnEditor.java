/*[ PSExtensionsColumnEditor.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.UTComponents.UTEditorComponent;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;


/**
 * Represents cell editor for the second column of <code>m_table</code>
 * 'Mime Type Map' table. The cell editor is a panel with a a text field and
 * a button.
 */
public class PSExtensionsColumnEditor extends UTEditorComponent
{
   /**
    * Create the cell editor.
    *
    * @param table the table for which to create the editor, may not be
    *    <code>null</code>.
    * @throws IllegalArgumentException if the supplied table is
    *    <code>null</code>.
    */
   public PSExtensionsColumnEditor(JTable table)
   {
      if (table == null)
         throw new IllegalArgumentException("Table cannot be null");

      m_table = table;
      init();
   }

   /**
    * Parses a comma delimited string and returns them as a list.
    *
    * @param str, comma dilimited string, should not be <code>null</code> or
    *    empty.
    * @return list of strings, may be <code>null</code>.
    * @throws IllegalArgumentException if the spplied string is
    *    <code>null</code> or empty.
    */
   public static List getExtList(String str)
   {
      List list = null;
      if (str == null || str.trim().length() == 0)
    //     throw new IllegalArgumentException("str cannot be null or empty");
         return list;
      StringTokenizer tk = new StringTokenizer(str, ",");

      if (tk.hasMoreTokens())
         list = new ArrayList();
      while (tk.hasMoreTokens())
      {
         list.add(tk.nextToken().trim());
      }
      return list;
   }

   /**
    * Initialize the editor with a text field and a button.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
         getClass().getName() + "Resources", Locale.getDefault() );

      m_extMdl = new PSSingleColumnModel(PSSingleColumnModel.EXTENSIONS);
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      m_txtFld = new JTextField();
      JButton editExtbuttons = new JButton();
      /**
       * Brings up 'Extensions' dialog for editing the extensions.
       */
      editExtbuttons.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            List list = getExtList(m_txtFld.getText());
            if (list != null && !list.isEmpty())
               m_extMdl.setData(list);
            getExtDialog(m_extMdl).setVisible(true);

         }
      });
      add(m_txtFld);
      add(editExtbuttons);
   }

   /**
    * Creates 'Extensions' dialog with a single column table for editing
    * mime type.
    *
    * @param mdl, table model for the extensions table, should not be <code>
    * null</code>.
    *
    * @return dialog holding extensions table. Never <code>null</code>
    */
   private JDialog getExtDialog(final PSSingleColumnModel mdl)
   {
      if (m_extensionsDlg == null)
      {
         m_extensionsDlg = new JDialog();
         m_extensionsDlg.setModal(true);
         m_extensionsDlg.setTitle(PSContentLoaderResources.getResourceString(
            ms_res, "title.extensions"));

         JPanel p = new JPanel();
         p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5 ));
         p.setPreferredSize(new Dimension(300, 300));
         p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
         JScrollPane jsp = new JScrollPane(m_extTable);
         p.add(jsp);

         JPanel btnPane = new JPanel();
         btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.X_AXIS));

         final JButton ok = new JButton(
            PSContentLoaderResources.getResourceString(
            ms_res, "button.ok"));
         ok.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               stopExtTableEditing();
               List editedExtList =
                     ((PSSingleColumnModel)m_extTable.getModel()).getData();
               int sz = editedExtList.size();
               StringBuffer sbf = new StringBuffer();

               //form a comma delimited string.
               for(int k = 0; k < sz; k++)
               {
                  if (((String)editedExtList.get(k)).length() != 0)
                     sbf.append(editedExtList.get(k)).append(",");
               }

               int z = sbf.toString().lastIndexOf(",");
               String s = sbf.toString();
               if (z != -1)
                  s = sbf.toString().substring(0, z);

               int k = m_table.getSelectedRow();
               Iterator itr = m_listenerList.iterator();
               while (itr.hasNext())
               {
                  ActionListener listener = (ActionListener)itr.next();
                  listener.actionPerformed(e);
               }

               if (s != null)
                  m_table.getModel().setValueAt(s, k, 1);

               m_extensionsDlg.setVisible(false);
         }});

         JButton cancel = new JButton(
            PSContentLoaderResources.getResourceString(ms_res,
            "button.cancel"));
         cancel.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               stopExtTableEditing();
               m_extensionsDlg.setVisible(false);
         }});

         btnPane.add(Box.createHorizontalGlue());
         btnPane.add(ok);
         btnPane.add(Box.createRigidArea(new Dimension(10,0)));
         btnPane.add(cancel);
         p.add(Box.createRigidArea(new Dimension(0,10)));
         p.add(btnPane);
         m_extensionsDlg.getContentPane().add(p);
         PSContentDialog.center(m_extensionsDlg);
         m_extensionsDlg.pack();
      }
      m_extTable.setModel(m_extMdl);
      return m_extensionsDlg;
   }

   /**
    * Notifies the <code>m_extTable</code> to stop editing.
    */
   private void stopExtTableEditing()
   {
      if (m_extTable.isEditing())
      {
         m_extTable.getCellEditor(m_extTable.getEditingRow(),
            m_extTable.getEditingColumn()).stopCellEditing();
      }
   }

   //see interface for description
   public JComponent getEditorComponent()
   {
     return m_txtFld;
   }

   //see interface for description
   public  void addActionListener(ActionListener l)
   {
     m_listenerList.add(l);
   }

   /**
    * The table model used, initialized in ctor, never <code>null</code> after
    * that.
    */
   private PSSingleColumnModel m_extMdl;

   /**
    * The table used, initialized in ctor, never <code>null</code> after that.
    */
   private JTable m_table;

   /**
    * Resource bundle for this class. Initialized in the constructor.
    * It's not modified after that. May be <code>null</code> if it could not
    * load the resource properties file.
    */
   private static ResourceBundle ms_res;

  /**
    * List of listeners to the 'Extensions' column cell editor in
    * <code>m_table</code> which is invoked in the action listener of 'OK'
    * button in 'Extensions' dialog to stop editing. Never <code>null</code>
    * or modified.
    */
   private Vector m_listenerList = new Vector();

   /**
    * 'Extensions' dialog for editing. Initialised in {@link #getExtDialog(
    * ExtensionsTableModel)} and never <code>null</code> or modified.
    */
   private JDialog m_extensionsDlg;

   /**
    * Text field for cell editing. Initialised in the <code>init()</code>
    * and never <code>null</code> or modified after that.
    */
   private JTextField m_txtFld;

   /**
    * Represents 'Extensions' table. Never <code>null</code> or modified.
    */
   private JTable m_extTable = new JTable();
}
