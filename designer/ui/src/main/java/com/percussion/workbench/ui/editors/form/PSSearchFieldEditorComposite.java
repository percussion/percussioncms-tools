/******************************************************************************
*
* [ PSSearchFieldEditorPanel.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.PSMainFrameInitFlag;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.search.ui.PSFieldSelectionEditorDialog;
import com.percussion.search.ui.PSSearchFieldEditor;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.workbench.ui.FeatureSet;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.common.PSEditorUtil;
import com.percussion.workbench.ui.legacy.AwtSwtModalDialogBridge;
import com.percussion.guitools.IPSValueChangedListener;
import com.percussion.guitools.PSCalendarButton;
import com.percussion.guitools.PSCalendarDialog;
import com.percussion.guitools.PSCalendarField;
import com.percussion.guitools.PSValueChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * This scrolled composite encapsulates all the functionality
 * needed to embed the Swing based search query panel within an SWT
 * eclipse component. This composite registers itself to the editor that
 * it is enclosed in.
 */
public class PSSearchFieldEditorComposite extends ScrolledComposite
   implements IPSDesignerObjectUpdater
{
   

   /**
    * Ctor
    * @param parent the parent composite, should not be <code>null</code>.
    * @param search the search designer object, cannot be <code>null</code>.
    * @param requester the remote requester, should not be <code>null</code>
    * @param cataloger the field cataloger, cannot be <code>null</code>.
    * @param editor the parent editor, cannot be <code>null</code>.
    */
   public PSSearchFieldEditorComposite(Composite parent, PSSearch search,
      IPSRemoteRequester requester, PSContentEditorFieldCataloger cataloger, PSEditorBase editor)
   {
      super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
      if(cataloger == null)
         throw new IllegalArgumentException("cataloger cannot be null.");
      if(search == null)
         throw new IllegalArgumentException("search cannot be null.");
      if(editor == null)
         throw new IllegalArgumentException("editor cannot be null.");
      setExpandHorizontal(true);
      setExpandVertical(true);
      m_requester = requester;
      m_search = search;
      m_cataloger = cataloger;
      m_editor = editor;
      init();
      editor.registerControl(
         "Search Criteria",
         this,
         null);
   }
   
   /**
    * Launches the field selection editor dialog when this method is called.
    */
   public void onCustomize()
   {
      final AwtSwtModalDialogBridge bridge = 
         new AwtSwtModalDialogBridge(
            PSSearchFieldEditorComposite.this.getShell());
      SwingUtilities.invokeLater( new Runnable()
         {
         @SuppressWarnings("synthetic-access")
         public void run()
         {            
            PSFieldSelectionEditorDialog dlg =
               new PSFieldSelectionEditorDialog(
                  (Frame)null, m_search,
                  PSEditorUtil.getCEFieldCatalog(false), true);
            dlg.setUseExternalSearchEngine(FeatureSet.isFTSearchEnabled() && 
               m_search.useExternalSearch());
            dlg.addValueChangedListener(new SearchValueChangedListener(
               PSSearchFieldEditorComposite.this));
            bridge.registerModalSwingDialog(dlg);
            dlg.setVisible(true);
         }
         });
   }
   
   /**
    * Sets up the controls for this component.
    */
   private void init()
   {
      m_comp = new Composite(this, SWT.NONE | SWT.EMBEDDED);
      m_comp.setLayout(new FillLayout());
      Frame frame = SWT_AWT.new_Frame(m_comp);
      try
      {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         SwingUtilities.updateComponentTreeUI(frame);
      }
      catch (Exception e)
      {         
         e.printStackTrace();
      }
      m_editorPanel = new JPanel();
      m_editorPanel.setLayout(new GridLayout(1, 1));
      frame.add(m_editorPanel);
   }
   
     
   /**
    * Updates the search query editor panel by basicaly just replacing
    * it with a new one. Also adds the appropriate listeners.
    * @param initCall flag indicating that this was called from within
    * an init type method where we don't want a selection event to
    * be fired off.
    */
   protected void updateEditor(final boolean initCall)
   {      
      SwingUtilities.invokeLater(new Runnable()
         {
         @SuppressWarnings({"synthetic-access","serial"})
         public void run()
         {
            if(m_editorPanel.getComponentCount() > 0)
               m_editorPanel.removeAll();
            m_searchEditor = new PSSearchFieldEditor(
                  m_search.getFields(), m_requester, m_cataloger, true)
                  {
                     @Override
                     protected PSCalendarButton createCalendarButton(Date date)
                     {
                        return createCustomizedCalendarButton(null, date);
                     }

                     @Override
                     protected PSCalendarField createCalendarField()
                     {
                        return new PSCalendarField()
                        {
                           @Override
                           protected PSCalendarButton createCalendarButton(
                                 Frame frame, Date date)
                           {
                              return createCustomizedCalendarButton(frame, date);
                           }
                        };
                     }
                  };
            m_editorPanel.add(m_searchEditor);
            m_searchEditor.addValueChangedListener(new IPSValueChangedListener()
               {
                  public void valueChanged(@SuppressWarnings("unused") PSValueChangedEvent event)
                  {                     
                     Display display = PSSearchFieldEditorComposite.this.getDisplay();
                     display.asyncExec(new Runnable()
                        {
                           public void run()
                           {                  
                              PSSearchFieldEditorComposite.this.fireSelectionEvent();                  
                           }            
                        });
                     
                  }
               
               });
            
            Display display = PSSearchFieldEditorComposite.this.getDisplay();
            display.asyncExec(new Runnable()
               {
                  public void run()
                  {                  
                     PSSearchFieldEditorComposite.this.setContent(m_comp);
                     if(!initCall)
                        PSSearchFieldEditorComposite.this.fireSelectionEvent();
                  }            
               });            
            
         }
         });
      
   }

   /**
    * Creates calendar button registering the calendar dialog.
    */
   @SuppressWarnings("serial")
   private PSCalendarButton createCustomizedCalendarButton(final Frame owner, Date date)
   {
      return new PSCalendarButton(owner, date)
      {
         @Override
         protected PSCalendarDialog createDialog()
         {
            final PSCalendarDialog dlg = super.createDialog();
            if (PSMainFrameInitFlag.isMainFrameInitialized())
            {
               E2Designer.getApp().getMainFrame().registerDialog(dlg);
            }
            return dlg;
         }

         @Override
         public void actionPerformed(ActionEvent event)
         {
            super.actionPerformed(event);
         }
      };
   }

   /**
    * Retrieves the search field values from the control.
    * @return iterator of <code>PSSearchField</code> objects.
    * Never <code>null</code>, may be empty.
    */
   public Iterator<PSSearchField> getValues()
   {
      final List<PSSearchField> results = new ArrayList<PSSearchField>(); 
      if (m_searchEditor == null)
         return results.iterator();
      
      final Iterator fieldIterator = m_searchEditor.getFields();
      while(fieldIterator.hasNext())
      {
         results.add((PSSearchField) fieldIterator.next());
      }
      return results.iterator();
   }
   
   
   
   /**
    * Adds a selection listener.
    * @param listener cannot be <code>null</code>.
    */
   public void addSelectionListener(SelectionListener listener)   
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(!m_selectionListeners.contains(listener))
      {
         m_selectionListeners.add(listener);
      }
   }
   
   /**
    * Removes the specified selection listener.
    * @param listener cannot be <code>null</code>.
    */
   public void removeSelectionListener(SelectionListener listener)   
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(m_selectionListeners.contains(listener))
      {
         m_selectionListeners.remove(listener);
      }
   }
   
   /**
    * Called when a selection event occurs on the control or
    * any of its nested controls.
    */
   protected void fireSelectionEvent()
   {      
      org.eclipse.swt.widgets.Event e = new org.eclipse.swt.widgets.Event();
      e.item = this;
      e.widget = this;     
      SelectionEvent event = new SelectionEvent(e);
      for(SelectionListener listener : m_selectionListeners)
      {
        listener.widgetSelected(event);
        
      }
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(@SuppressWarnings("unused") Object designObject,
      Object control)
   {
      if(control == this)
      {
         m_search.setFields(getValues());
      }      
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   public void loadControlValues(@SuppressWarnings("unused") Object designObject)
   {
      updateEditor(true);      
   }
   
   
   
   class SearchValueChangedListener implements IPSValueChangedListener
   {
      SearchValueChangedListener(PSSearchFieldEditorComposite comp)
      {
         mi_comp = comp;
      }

     
      public void valueChanged(@SuppressWarnings("unused") PSValueChangedEvent e)
      {
         Display display = mi_comp.getDisplay();
         display.asyncExec(new Runnable()
            {
               @SuppressWarnings("synthetic-access")
               public void run()
               {                  
                  mi_comp.updateEditor(false);
               }            
            });
         
         
      }
      
      private PSSearchFieldEditorComposite mi_comp; 
      
   }
   
   private IPSRemoteRequester m_requester;
   private PSSearch m_search;
   private PSContentEditorFieldCataloger m_cataloger;
   private JPanel m_editorPanel;
   private Composite m_comp;
   private List<SelectionListener> m_selectionListeners = 
      new ArrayList<SelectionListener>();
   private PSSearchFieldEditor m_searchEditor;
   @SuppressWarnings("unused")
   private PSEditorBase m_editor;
   
   

}
