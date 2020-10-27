/******************************************************************************
 *
 * [ PSTemplateOutputWithPublishComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.editors.form.PSPublishWhenHelper;
import com.percussion.workbench.ui.editors.form.PSPublishWhenHelper.PublishWhenChoice;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * Adds functionality to manage publishing selection depending on format.
 * Specify {@link PSPublishWhenHelper} right after creation,
 * so it can be managed.
 * @author Andriy Palamarchuk
 */
public class PSTemplateOutputWithPublishComposite
      extends PSTemplateOutputComposite
{
   public PSTemplateOutputWithPublishComposite(Composite parent, int style,
         boolean narrow, boolean horizontal, boolean showDbOutput,
         boolean showAssemblers, boolean showLegacyGlobalTemplateUsage)
   {
      super(parent, style, narrow, horizontal, showDbOutput, showAssemblers,
            showLegacyGlobalTemplateUsage);
   }

   /**
    * Publishing selection helper. Can be <code>null</code> before initialized.
    * After initialization is always not-<code>null</code>.
    */
   public PSPublishWhenHelper getPublishWhenHelper()
   {
      return m_publishWhenHelper;
   }

   // see base
   @Override
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      super.loadControlValues(template);
      outputFormatSelectionChanged();
   }

   /**
    * Sets publishing selection helper to manage.
    */
   public void setPublishWhenHelper(PSPublishWhenHelper publishWhenHelper)
   {
      assert publishWhenHelper != null;
      assert m_publishWhenHelper == null : "Should be initialized only once";    //$NON-NLS-1$
      m_publishWhenHelper = publishWhenHelper;
      getFormatRadio().addSelectionListener(new SelectionAdapter()
            {
               @Override
               public void widgetDefaultSelected(
                     @SuppressWarnings("unused") SelectionEvent e)              //$NON-NLS-1$
               {
                  outputFormatSelectionChanged();
               }

               @Override
               public void widgetSelected(
                     @SuppressWarnings("unused") SelectionEvent e)              //$NON-NLS-1$
               {
                  outputFormatSelectionChanged();
               }
            });
   }

   // see base
   @Override
   protected void assemblerComboSelectionChanged()
   {
      super.assemblerComboSelectionChanged();
      outputFormatSelectionChanged();
   }

   /**
    * Is called whenever output format selection changes.
    * Contains actual logic to manager the publishing when control.
    */
   private void outputFormatSelectionChanged()
   {
      if (m_publishWhenHelper == null)
      {
         return;
      }
      if (getSelectedFormatChoice().equals(FormatChoice.SNIPPET))
      {
         m_publishWhenHelper.getRadio().setEnabledButtons(false,
               PublishWhenChoice.ALWAYS.ordinal(),
               PublishWhenChoice.DEFAULT.ordinal());
         m_publishWhenHelper.getRadio().setSelection(
               PublishWhenChoice.NEVER.ordinal());
      }
      // if switch from snippet to non-snippet
      else if (m_lastFormatChoice.equals(FormatChoice.SNIPPET))
      {
         m_publishWhenHelper.getRadio().setEnabledButtons(true,
               PublishWhenChoice.ALWAYS.ordinal(),
               PublishWhenChoice.DEFAULT.ordinal(),
               PublishWhenChoice.NEVER.ordinal());
         m_publishWhenHelper.getRadio().setSelection(
               PublishWhenChoice.DEFAULT.ordinal());
      }
      m_lastFormatChoice = getSelectedFormatChoice();
   }

   /**
    * Format choice selected last time.
    */
   private FormatChoice m_lastFormatChoice = FormatChoice.PAGE;

   private PSPublishWhenHelper m_publishWhenHelper;
}
