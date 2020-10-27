/******************************************************************************
 *
 * [ PSFrameProvider.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import javax.swing.*;

/**
 * Provides a frame.
 * Used for embedding a frame root pane into SWT_AWT frame. 
 *
 * @author Andriy Palamarchuk
 */
public interface PSFrameProvider
{
    JInternalFrame getFrame();
}
