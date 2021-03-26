/******************************************************************************
 *
 * [ PSObjectSorterView.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.models.IPSModelListener;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.actions.PSMainActionGroup;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.help.IPSHelpProvider;
import com.percussion.workbench.ui.help.PSHelpManager;
import com.percussion.workbench.ui.layouts.IPSTableLayoutListener;
import com.percussion.workbench.ui.layouts.PSTableLayoutEvent;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySource;

import java.util.ArrayList;
import java.util.List;

/**
 * This view takes a list of <code>PSUiReference</code> objects and "walks
 * down the tree" flattening the views nodes and displays all of its objects.
 * @version 6.0
 * @created 03-Sep-2005 4:43:52 PM
 */
public class PSObjectSorterView extends ViewPart implements
      ISelectionListener, IPSHelpProvider
{
   /**
    * The view id as specified in the plugin.xml.
    */
   public static final String ID = PSObjectSorterView.class.getName();

   

   
   /**
    * Loads the view by flattening the list of ui refs and their
    * children.
    * @param refs list of ui references, cannot be <code>null</code>
    * but may be empty.
    */
   public void load(final List<PSUiReference> refs)
   {
      if(refs == null)
         throw new IllegalArgumentException("refs cannot be null."); //$NON-NLS-1$
      if(m_table == null)
         return;
      final List<PSUiReference> list = new ArrayList<PSUiReference>();
      final Display display = getSite().getShell().getDisplay();
      
      Job job = new Job(PSMessages.getString("PSObjectSorterView.job.loadObjectSorter"))    //$NON-NLS-1$
      {
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         @Override
         public  IStatus run(IProgressMonitor monitor)
         {
            try
            {
               monitor.beginTask(PSMessages.getString(
                  "PSObjectSorterView.loadingView.message"), refs.size()); //$NON-NLS-1$
               int count = 0;
               for(PSUiReference ref : refs)
               {            
                  addAllRefs(list, ref);
                  monitor.worked(++count);
               }
               display.syncExec(new Runnable()
                  {
                     
                     public void run()
                     {
                        m_table.setValues(list);
                        m_table.sortColumn(1, true);
                     }
                     
                  });
               
               if(list.isEmpty())
               {
                  display.syncExec(new Runnable()
                     {
                        
                        public void run()
                        {
                           m_label.setText(NO_OBJECTS_MSG);
                        }
                        
                     });
                  
               }
               else
               {
                  final StringBuilder msg = new StringBuilder();
                  final StringBuilder tip = new StringBuilder();
                  int len = refs.size();
                  for(int i = 0; i < len; i++)
                  {
                     String path = refs.get(i).getPath();
                     msg.append(path);
                     tip.append(path);
                     if(i < (len - 1))
                     {
                        msg.append(", "); //$NON-NLS-1$
                        tip.append("\n"); //$NON-NLS-1$
                     }
                  }
                  msg.append(" ("); //$NON-NLS-1$
                  msg.append(list.size());
                  msg.append(" ");
                  msg.append(OBJECTS_MSG);
                  msg.append(")"); //$NON-NLS-1$
                  tip.append("\n"); //$NON-NLS-1$
                  tip.append(list.size());
                  tip.append(" ");
                  tip.append(OBJECTS_MSG);                  
                  display.syncExec(new Runnable()
                     {
                        
                        public void run()
                        {
                           m_label.setText(msg.toString());
                           m_label.setToolTipText(tip.toString());
                        }
                        
                     });
                  
               }
               monitor.done();
               return Status.OK_STATUS;
            }
            catch (PSModelException e)
            {
               return new Status(IStatus.ERROR,
                  PSWorkbenchPlugin.getPluginId(), 0,
                  e.getLocalizedMessage(), e);
            }
         }
         
      };
      job.schedule();
      
      
   }
   
   /**
    * Recursive helper method to add the references and their children
    * to the flat list that will be displayed in the views table.
    * @param list assumed not <code>null</code>.
    * @param ref the <code>PSUiReference</code> to be added, assumed
    * not <code>null</code>.
    * @throws PSModelException upon any error.
    */
   private void addAllRefs(List<PSUiReference> list, PSUiReference ref)
      throws PSModelException
   {
      
      IPropertySource prop = (IPropertySource)ref.getAdapter(
         IPropertySource.class);
      if(prop != null && !ref.isFolder())
         list.add(ref);
      
      for(PSUiReference child : PSDesignObjectHierarchy.getInstance().
         getChildren(ref))
      {
         addAllRefs(list, child);
      }
   }
   
   /* 
    * @see org.eclipse.ui.IWorkbenchPart#createPartControl(
    * org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createPartControl(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
                  
      m_label = new Label(comp, SWT.NONE);
      m_label.setText(NO_OBJECTS_MSG);
      FormData formdata_1 = new FormData();
      formdata_1.top = new FormAttachment(0, 0);
      formdata_1.left = new FormAttachment(0, 0);
      formdata_1.right = new FormAttachment(100, 0);
      m_label.setLayoutData(formdata_1);      
     
      
      m_table = createTable(comp);
      FormData formdata_3 = new FormData();
      formdata_3.top = new FormAttachment(m_label, 5, SWT.BOTTOM);
      formdata_3.bottom = new FormAttachment(100, 0);
      formdata_3.right = new FormAttachment(100, 0);
      formdata_3.left = new FormAttachment(m_label, 0, SWT.LEFT);
      m_table.setLayoutData(formdata_3);
      
      initContextMenu();
      
      getSite().setSelectionProvider(m_table.getTableViewer());
      
      makeActions();
      
      m_table.getTableViewer().addDoubleClickListener(
         new IDoubleClickListener()
         {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public void doubleClick(DoubleClickEvent event)
            {
               m_actionGroup.handleDoubleClick(event);
            }
         });

      m_table.getTableViewer().addOpenListener(new IOpenListener()
         {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public void open(OpenEvent event)
            {
               m_actionGroup.handleOpen(event);
            }
         });
      addCoreListeners();
      m_helpManager = new PSHelpManager(this, comp);
   }
      
        

   /* 
    * @see org.eclipse.ui.IWorkbenchPart#setFocus()
    */
   @Override
   public void setFocus()
   {
      m_table.setFocus();      
   }

   /* 
    * @see org.eclipse.ui.ISelectionListener#selectionChanged(
    * org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
    */
   @SuppressWarnings("unused")
   public void selectionChanged(IWorkbenchPart part, ISelection selection)
   {
      // no-op      
   }
   
   /**
    * Creates the views table
    * @param parent the parent composite, cannot be <code>null</code>.
    * @return the newly created table, never <code.null</code>.
    */
   private PSSortableTable createTable(Composite parent)
   {
      IPSNewRowObjectProvider dummyObjectProvider = 
         new IPSNewRowObjectProvider()
         {

            public Object newInstance()
            {
               return null;
            }

            public boolean isEmpty(@SuppressWarnings("unused") Object obj)
            {
               return false;
            }
         
         };
         
         ITableLabelProvider labelProvider= new PSAbstractTableLabelProvider(){

            public String getColumnText(Object element, int columnIndex)
            {
               PSUiReference ref = (PSUiReference)element;
               switch(columnIndex)
               {
                  case 1:
                     return StringUtils.defaultString(ref.getName());
                  case 2:
                     return StringUtils.defaultString(ref.getLabel());
                  case 3:
                     return StringUtils.defaultString(ref.getDescription());
                  case 4:
                  {
                     String id = ref.getId() == null ? "" //$NON-NLS-1$
                        : String.valueOf(ref.getId().toString());
                     return id;
                  }
                  case 5:
                     return StringUtils.defaultString(ref.getPath());
                  case 6:
                     if(ref.getReference() != null)
                     {
                        return StringUtils.defaultString(
                           PSMessages.getString("common.objecttype." +  //$NON-NLS-1$
                              ref.getObjectType().toString()));
                     }
                  default:
                     return ""; //$NON-NLS-1$
               }
            }

            /* 
             * @see com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider#
             * getColumnImage(java.lang.Object, int)
             */
            @Override
            public Image getColumnImage(Object element, int columnIndex)
            {
               PSUiReference ref = (PSUiReference)element;
               if(ref == null)
                  return null;
               if(columnIndex == 0)
               {
                  if(ref.isFolder())
                     return PSUiUtils.getSharedImage(
                        ISharedImages.IMG_OBJ_FOLDER);
                  IPSDeclarativeNodeHandler handler = ref.getHandler();
                  if(handler == null)
                     return PSUiUtils.getSharedImage(
                        ISharedImages.IMG_OBJ_FILE);
                  return handler.getLabelImage(ref);
               }
               return null;
            }  
            
            
            };
            
         PSSortableTable table = new PSSortableTable(parent,
            labelProvider, dummyObjectProvider,
            SWT.SINGLE | SWT.FULL_SELECTION,
            PSSortableTable.HIDE_LINES |
            PSSortableTable.SURPRESS_MANUAL_SORT |
            PSSortableTable.SURPRESS_TABLE_CURSOR);
         final FormData formData = new FormData();
         formData.top = new FormAttachment(0, 5);
         formData.left = new FormAttachment(0, 225);
         table.setLayoutData(formData);
         
         table.addColumn(" ", PSSortableTable.NONE, //$NON-NLS-1$
            new ColumnWeightData(1, 25, true), null, SWT.LEFT);
         table.addColumn(PSMessages.getString("PSObjectSorterView.column.name.label"),
            PSSortableTable.IS_SORTABLE, //$NON-NLS-1$
            new ColumnWeightData(5, 80, true), null, SWT.LEFT);
         table.addColumn(PSMessages.getString("PSObjectSorterView.column.label.label"),
            PSSortableTable.IS_SORTABLE, //$NON-NLS-1$
            new ColumnWeightData(5, 80, true), null, SWT.LEFT);
         table.addColumn(PSMessages.getString("PSObjectSorterView.column.description.label"),
            PSSortableTable.IS_SORTABLE, //$NON-NLS-1$
            new ColumnWeightData(10, 100, true), null, SWT.LEFT);
         table.addColumn(PSMessages.getString("PSObjectSorterView.column.id.label"),
            PSSortableTable.IS_SORTABLE, //$NON-NLS-1$
            new ColumnWeightData(2, 50, true), null, SWT.LEFT);
         table.addColumn(PSMessages.getString("PSObjectSorterView.column.path.label"),
            PSSortableTable.IS_SORTABLE, //$NON-NLS-1$
            new ColumnWeightData(10, 100, true), null, SWT.LEFT);
         table.addColumn(PSMessages.getString("PSObjectSorterView.column.type.label"),
            PSSortableTable.IS_SORTABLE, //$NON-NLS-1$
            new ColumnWeightData(5, 80, true), null, SWT.LEFT);
         System.out.println(PSWorkbenchPlugin.getDefault().getStateLocation().toOSString());
         IDialogSettings settings = 
            PSUiUtils.getDialogSettings(this.getClass());
         IDialogSettings tableWidths = settings.getSection(
            TABLE_WIDTH_SETTINGS);
         
         if(tableWidths != null)
         {
            float[] ratios = new float[table.getColumnCount()];
            boolean error = false;
            for(int i = 0; i < ratios.length; i++)
            {
               try
               {
                  ratios[i] = tableWidths.getFloat(String.valueOf(i));
                  
               }
               catch (NumberFormatException e)
               {
                  PSWorkbenchPlugin.handleException(null,
                     null,
                     PSMessages.getString(
                        "PSObjectSorterView.error.missingColWidthValue"), //$NON-NLS-1$
                     e);
                  break;
               }
               
            }
            if(!error)
            {
               table.setColumnWidthRatios(ratios);
            }
         }
         
         // Add table layout listener so we know when the column ratios have 
         // been changed and we can then save the tables state info
         table.addTableLayoutListener(new IPSTableLayoutListener()
            {

               /* 
                * @see com.percussion.workbench.ui.layouts.IPSTableLayoutListener#
                * columnsResized(com.percussion.workbench.ui.layouts.PSTableLayoutEvent)
                */
               @SuppressWarnings("synthetic-access") //$NON-NLS-1$
               public void columnsResized(PSTableLayoutEvent event)
               {
                  saveViewState();                  
               }
            
            });
       
         return table;
   }
      
   
   /**
    * Persist the state of this view using the <code>PSDialogSettings</code>
    * object. Saves the tables width ratios.
    */
   private void saveViewState()
   {
      IDialogSettings settings = 
         PSUiUtils.getDialogSettings(this.getClass());
      float[] ratios = m_table.getColumnWidthRatios();
      if(ratios != null)
      {
         IDialogSettings tableWidths = settings.getSection(
            TABLE_WIDTH_SETTINGS);
         if(tableWidths == null)
            tableWidths = settings.addNewSection(TABLE_WIDTH_SETTINGS);
         for(int i = 0; i < ratios.length; i++)
         {
            tableWidths.put(String.valueOf(i), ratios[i]);
         }
      }
      
   }   
    

   /**
    * Called when the context menu is about to open. Adds all menu options
    * appropriate for the current selection.
    */
   protected void fillContextMenu(IMenuManager menu)
   {
      IStructuredSelection selection = (IStructuredSelection) m_table
            .getSelection();
      m_actionGroup.setContext(new ActionContext(selection));
      m_actionGroup.fillContextMenu(menu);
      //fixme - protect w/ property
      menu.add(new Action()
      {
         @Override
         public void run()
         {
            PSDesignObjectHierarchy.getInstance().dumpCache();
         }

         @Override
         public String getText()
         {
            return "Dump View's Design Object Cache"; //$NON-NLS-1$
         }
      });
   }
   
   /**
    * Creates the action group, which encapsulates all actions for the view.
    */
   protected void makeActions()
   {
      m_actionGroup = 
         new PSMainActionGroup(getViewSite(), m_table.getTableViewer());
   }
   
   /**
    * Initializes and registers the context menu.
    * 
    * @since 2.0
    */
   protected void initContextMenu()
   {
      MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
      menuMgr.setRemoveAllWhenShown(true);
      menuMgr.addMenuListener(new IMenuListener()
      {
         public void menuAboutToShow(IMenuManager manager)
         {
            fillContextMenu(manager);
         }
      });
      Menu menu = menuMgr.createContextMenu(m_table.getTable());
      m_table.getTable().setMenu(menu);
      getSite().registerContextMenu(menuMgr, m_table);
   }
   
   /**
    * Add listeners to track changes to objects in the other views and
    * editors so that the changes will be reflected in this view.
    */
   protected void addCoreListeners()
   {
      PSCoreFactory factory = PSCoreFactory.getInstance();
      factory.addListener(new IPSModelListener()
      {

         @SuppressWarnings({"synthetic-access","unchecked"})
         public void modelChanged(PSModelChangedEvent event)
         {
            IPSReference[] refs = event.getSource();
            PSModelChangedEvent.ModelEvents type = event.getEventType();
            List<PSUiReference> uiRefs = (List<PSUiReference>)m_table.getValues();
            List<IPSReference> rawRefs = uiRefsToRefs(uiRefs);
            if(type.equals(PSModelChangedEvent.ModelEvents.DELETED))
            {                 
               List<Integer> removeList = new ArrayList<Integer>();
               for(IPSReference ref : refs)
               {
                  int idx = rawRefs.indexOf(ref);
                  if(idx > -1)
                     removeList.add(idx);
               }
               if(!removeList.isEmpty())
               {
                  for(int item : removeList)
                     uiRefs.remove(item);
                  m_table.setValues(uiRefs);
               }
            }
            else if(type.equals(PSModelChangedEvent.ModelEvents.MODIFIED))
            {
               boolean foundChange = false;
               for(IPSReference ref : refs)
               {
                  int idx = rawRefs.indexOf(ref);
                  if(idx > -1)
                     foundChange = true;
               }
               if(foundChange)
               {                     
                  m_table.refreshTable();
               }
            }
            else if(type.equals(PSModelChangedEvent.ModelEvents.RENAMED))
            {
               boolean foundChange = false;
               PSObjectType oType = refs[0].getObjectType();
               for(int i = 0; i < rawRefs.size(); i++)
               {
                  IPSReference ref = rawRefs.get(i);
                  if(ref.getObjectType().equals(oType) && 
                     ref.getName().equals(refs[0].getName()))
                  {
                     foundChange = true;                       
                  }
               }
               if(foundChange)
               {
                 m_table.refreshTable();
               }
            }
         }
      
      }, 
      PSModelChangedEvent.ModelEvents.DELETED.getFlag() |
      PSModelChangedEvent.ModelEvents.MODIFIED.getFlag() |
      PSModelChangedEvent.ModelEvents.RENAMED.getFlag());
   }
   
   /**
    * Converts a list of <code>PSUiReference</code> objects to a list
    * of <code>IPSReference<code> objects.
    * @param refs assumed not <code>null</code>.
    * @return list of refs, never <code>null</code>, may be empty.
    */
   private List<IPSReference> uiRefsToRefs(List<PSUiReference> refs)
   {
      List<IPSReference> list = new ArrayList<IPSReference>();
      for(PSUiReference uiRef : refs)
      {
         list.add(uiRef.getReference());
      }
      return list;
   }
   
   /**
    * Base class implementation just returns name of the class. 
    * @see com.percussion.workbench.ui.help.IPSHelpProvider#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   public String getHelpKey(@SuppressWarnings("unused") Control control)
   {      
      return getClass().getName();
   }   
   
   
   /**
    * Contains all actions used by items in this table. 
    */
   private PSMainActionGroup m_actionGroup;
   
   /**
    * The help manager for the view
    */
   protected PSHelpManager m_helpManager;
      
   // Controls
   private Label m_label;
   private PSSortableTable m_table;
   
   private static final String NO_OBJECTS_MSG = 
      PSMessages.getString("PSObjectSorterView.noObjects.message"); //$NON-NLS-1$
   private static final String OBJECTS_MSG = 
      PSMessages.getString("PSObjectSorterView.objects.message");  //$NON-NLS-1$
   private static final String TABLE_WIDTH_SETTINGS = "tableWidths"; //$NON-NLS-1$
   
   

   

  

  

}
