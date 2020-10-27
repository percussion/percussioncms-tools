/******************************************************************************
 *
 * [ PSKeywordChoiceListPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.common.PSKeywordCommonComposite;
import com.percussion.workbench.ui.editors.common.PSKeywordCommonComposite.Choice;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.List;

public class PSKeywordChoiceListPage extends PSWizardPageBase
   implements IPSUiConstants
{

   
   public PSKeywordChoiceListPage()
   {
      super(PSMessages.getString("PSKeywordPropertiesPage.pagename"), //$NON-NLS-1$
         PSMessages.getString("PSKeywordPropertiesPage.title"), null); //$NON-NLS-1$ 
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSKeyword keyword = (PSKeyword)designObject;
      if(control == m_comp.getChoicesTable().getTable())
      {
         final List<Choice> values = 
            m_comp.getChoicesTable().getValues();
         List choices = new ArrayList(keyword.getChoices());
         choices.clear();         
         int seq = 1;
         for(Choice choice : values)
         {
            PSKeywordChoice kc = choice.toKeywordChoice();
            kc.setSequence(seq++);
            choices.add(kc);
         }   
         keyword.setChoices(choices);
      }
      
   }

   public void createControl(Composite parent)
   {
      m_parent = parent;
      final Composite dummy = new Composite(parent, SWT.NONE)
      {
         @Override
         public Point computeSize(@SuppressWarnings("unused") int wHint, 
               @SuppressWarnings("unused") int hHint, 
               @SuppressWarnings("unused") boolean changed)
         {
            return new Point(300, 300);
         }
      };
      setControl(dummy);
   }
   
   /**
    * Besides default behavior on first call making the page visible
    * this method replaces dummy page control with real one. The control
    * is created here instead of {@link #createControl(Composite)} so
    * it can't affect size of the wizard dialog.
    * This is a workaround for problem described in Rx-06-10-0035
    * when Keyword dialog called from local menu is too wide.
    * This problem happens only in Eclipse 3.1, not in 3.2, so the workaround
    * can be removed when we switch Workbench to 3.2.
    *
    * @inheritDoc
    */
   @Override
   public void setVisible(boolean visible)
   {
      if (visible && !m_pageControlInitialized)
      {
         getControl().dispose();
         m_comp = new PSKeywordCommonComposite(m_parent, SWT.NONE, this);
         registerControl(
            "PSKeywordCommonComposite.label.choices",
            m_comp.getChoicesTable().getTable(),
            null);
         registerControlHelpOnly("PSKeywordCommonComposite.label.value",
            m_comp.getValueControl());
         setControl(m_comp);
         m_pageControlInitialized = true;
         m_parent.layout(true);
      }
      super.setVisible(visible);
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * getHelpHintKey(com.percussion.workbench.ui.util.PSControlInfo)
    */
   @Override
   protected String getHelpHintKey(PSControlInfo controlInfo)
   {
      if(m_helpHintKeyHelper == null)
      {
         m_helpHintKeyHelper = new PSHelpHintKeyHelper(new String[]
         {
            "PSKeywordCommonComposite.label.choices",
               "choices",
            "PSKeywordCommonComposite.label.value",
               "value_(box)",
            "PSKeywordCommonComposite.column.name.label",
               "label",
            "PSKeywordCommonComposite.column.name.value",
               "value_(table)",
            "PSKeywordCommonComposite.column.name.desc",
               "description"   
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;

   /**
    * Common keyword composite, initialized in {@link #createControl(Composite)}
    * , never <code>null</code> after that.
    */
   private PSKeywordCommonComposite m_comp;
   
   /**
    * The page control container. Initially <code>null</code>.
    * Contains value passed to {@link #createControl(Composite)}.
    * Not changed after that.
    */
   private Composite m_parent;
   
   /**
    * Used to delay page control creation until page is made visible.
    * If <code>true</code> real page control is already initialized.
    * Initially <code>false</code>.
    * Assigned <code>true</code> during control creation
    * in {@link #setVisible(boolean)}.
    */
   private boolean m_pageControlInitialized;
}
