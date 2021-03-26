/******************************************************************************
 *
 * [ PSRxPerspective.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.workbench.ui.views.PSContentDesignView;
import com.percussion.workbench.ui.views.PSDatabaseExplorerView;
import com.percussion.workbench.ui.views.PSFileExplorerView;
import com.percussion.workbench.ui.views.PSHelpView;
import com.percussion.workbench.ui.views.PSObjectSorterView;
import com.percussion.workbench.ui.views.PSProblemsView;
import com.percussion.workbench.ui.views.PSSystemDesignView;
import com.percussion.workbench.ui.views.PSXmlServerDesignView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

public class PSRxPerspective implements IPerspectiveFactory
{
   /**
    * The id of the WST snippets view.
    */
   private static final String VIEW_SNIPPET = 
      "org.eclipse.wst.common.snippets.internal.ui.SnippetsView";

   /**
    * The id of Eclipse's properties view.
    */
   private static final String VIEW_PROPERTIES = 
      "org.eclipse.ui.views.PropertySheet";

   public void createInitialLayout(IPageLayout layout)
   {
      String editorArea = layout.getEditorArea();
      layout.setEditorAreaVisible(true);
      layout.setFixed(false);

      IFolderLayout left = layout.createFolder("topLeft", IPageLayout.LEFT,
            0.2f, editorArea);
      left.addView(PSContentDesignView.ID);
      left.addView(PSSystemDesignView.ID);
      left.addView(PSFileExplorerView.ID);

      // TODO these views should not be shown by default on production
      // see documentation whether these should be shown on upgrade
      // show them now for testing
      left.addView(PSXmlServerDesignView.ID);
      left.addView(PSDatabaseExplorerView.ID);

      IPlaceholderFolderLayout bottom = layout.createPlaceholderFolder(
            "bottom", IPageLayout.BOTTOM, 0.75f, editorArea);
      bottom.addPlaceholder(PSObjectSorterView.ID);
      bottom.addPlaceholder(PSHelpView.ID);
      bottom.addPlaceholder(PSProblemsView.ID);

      IFolderLayout bottomleft = layout.createFolder("bottomLeft",
            IPageLayout.BOTTOM, 0.7f, "topLeft");
      bottomleft.addView(VIEW_PROPERTIES);
   }

}
