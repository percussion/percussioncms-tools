/******************************************************************************
 *
 * [ PSSelectCommunitiesComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.List;

import static com.percussion.workbench.ui.IPSUiConstants.LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_HSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_VSPACE_OFFSET;

/**
 * Communities selection control. Allows user to pick list of communities from
 * available ones.
 *
 * @author Andriy Palamarchuk
 */
public class PSSelectCommunitiesComposite extends Composite
{
   /**
    * Creates new communities selection control.
    */
   public PSSelectCommunitiesComposite(Composite parent, int style)
   {
      super(parent, style);
      setLayout(new FormLayout());
      
      final Label filterLabel = createFilterLabel();
      m_communitiesControl = createCommunitiesLists(filterLabel);
      m_filterText = createFilterText(filterLabel);
      
      m_filterText.addModifyListener(new ModifyListener()
         {
            @SuppressWarnings("synthetic-access")
            public void modifyText(@SuppressWarnings("unused") ModifyEvent e)
            {               
               m_communitiesControl.filterAvailableList(
                  m_filterText.getText());   
            }         
         });
      setTabList(new Control[]{m_communitiesControl,m_filterText});
   }

   /**
    * Creates slots list control.
    */
   private PSSlushBucket createCommunitiesLists(final Label filterLabel)
   {
      final PSSlushBucket communitiesLists = new PSSlushBucket(this, SWT.NONE, 
            getMessage(
               "PSSelectCommunitiesComposite.label.available.communities") + ':',  //$NON-NLS-1$
            getVisibleLabel() + ':', //$NON-NLS-1$
            new PSReferenceLabelProvider());
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(0);
         formData.right = new FormAttachment(100);
         formData.top = new FormAttachment(0);
         formData.bottom = 
            new FormAttachment(filterLabel,
                  - LABEL_HSPACE_OFFSET - LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET,
                  SWT.TOP);
         communitiesLists.setLayoutData(formData);
      }
      communitiesLists.setValues(getCommunities(), null);
      return communitiesLists;
   }

   /**
    * Visible communities list label text.
    * To be used for error messages.
    */
   public String getVisibleLabel()
   {
      return getMessage(
         "PSSelectCommunitiesComposite.label.visible.communities");
   }
   
   /**
    * @return the filter text control, never <code>null</code>.
    */
   public Text getFilterTextControl()
   {
      return m_filterText;
   }

   /**
    * Filter label.
    */
   private Label createFilterLabel()
   {
      final Label filterLabel = new Label(this, SWT.NONE);
      filterLabel.setText(
            getMessage("common.label.filter") + ':'); //$NON-NLS-1$
      {
         final FormData formData = new FormData();      
         formData.left = new FormAttachment(0);
         formData.bottom = new FormAttachment(100, -LABEL_VSPACE_OFFSET);
         filterLabel.setLayoutData(formData);
      }
      return filterLabel;
   }

   /**
    * Creates filter text control.
    */
   private Text createFilterText(final Label filterLabel)
   {
      final Text text = new Text(this, SWT.BORDER);
      {
         final FormData formData = new FormData();      
         formData.right = new FormAttachment(ONE_THIRD_NUMERATOR, 0);
         formData.top = new FormAttachment(filterLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         formData.left = new FormAttachment(filterLabel, LABEL_HSPACE_OFFSET, SWT.DEFAULT);
         text.setLayoutData(formData);
      }
      return text;
   }

   /**
    * Available communities.
    */
   List<IPSReference> getCommunities()
   {
      try
      {         
         return PSCoreUtils.catalog(PSObjectTypes.COMMUNITY, false);
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            null,
            null,
            null,
            e);
         return null;
      }
   }
   
   /**
    * Gets underlying PSSlushBucket control.
    */
   public PSSlushBucket getSlushControl()
   {
      return m_communitiesControl;
   }
   
   /**
    * Utility method to get message.
    */
   private String getMessage(final String key)
   {
      return PSMessages.getString(key);
   }

   /**
    * Numerator specifying 1/3 of default denominator (100).
    */
   private static final int ONE_THIRD_NUMERATOR = 33;

   /**
    * The double list to select communities.
    */
   PSSlushBucket m_communitiesControl;
   
   /**
    * Filter for available communities list.
    */
   private Text m_filterText;
}
