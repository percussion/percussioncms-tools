/******************************************************************************
 *
 * [ PSDeploymentWizardDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSDescriptor;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSResources;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Base class for dialogs that are part of a deployment wizard manager steps.  
 * Provides implementation for the generic wizard step operations as well as 
 * basic <code>PSDialog</code> functionality. This dialog should never dispose
 * itself, it should be disposed by the wizard manager. The derived classes 
 * should finish constructing dialog framework before the constructor returns 
 * and use <code>init()</code> to intialize the dialog state from the descriptor
 * which is called by a call to {@link #onShow(PSDescriptor) }.
 */
public abstract class PSDeploymentWizardDialog extends PSDialog
{
  /**
   * Constructor this object using the supplied deployment server. Assumes this
   * dialog as not the last step of the dialog.
   * 
   * @param parent the parent window of this dialog, may be <code>null</code>.
   * @param step the dialog step which identifies this dialog in the wizard 
   * steps
   * @param sequence One of the <code>SEQUENCE_XXX</code> values.
   */
  public PSDeploymentWizardDialog(Frame parent, int step, int sequence)
  {
     super(parent, null);
     
     m_step = step;
     setSequence(sequence);
  }
  
  /**
   * Constructor this object using the supplied deployment server. 
   * 
   * @param parent the parent window of this dialog, may be <code>null</code>.
   * @param deploymentServer the deployment server which should be used to 
   * accomplish the task designated to this dialog, may not be <code>null</code> 
   * and must be connected.
   * @param step the dialog step which identifies this dialog in the wizard 
   * steps
   * @param sequence One of the <code>SEQUENCE_XXX</code> values.
   */
  public PSDeploymentWizardDialog(Frame parent, 
     PSDeploymentServer deploymentServer, int step, int sequence)
  {
     this(parent, step, sequence); 
     
     if(deploymentServer == null)
        throw new IllegalArgumentException("deploymentServer may not be null");
        
     if(!deploymentServer.isConnected())
        throw new IllegalArgumentException(
           "deploymentServer must be connected");
     
     m_deploymentServer = deploymentServer;
  }
  
  /**
   * Displays this dialog. Stores the descriptor and then delegates call to 
   * <code>init()</code> to refresh the UI and any other supporting state. 
   * 
   * @param data The descriptor for this dialog to restore its state from. May 
   * be <code>null</code>.
   * 
   * @throws IllegalArgumentException if data is <code>null</code>
   * @throws PSDeployException if an error happens initializing the dialog.
   */
  public void onShow(PSDescriptor data) throws PSDeployException
  {  
     m_descriptor = data;
     
     init();
     setVisible(true);
  }
  
  /**
   * Determines whether the supplied element is a dependency for any other 
   * package in the supplied import descriptor. Updates the parent packages 
   * list with the packages that depend on the supplied package/element.
   * 
   * @param descriptor the import descriptor to check, may not be <code>null
   * </code>
   * @param element the package to check, may not be <code>null</code>
   * @param parentPackages the list of packages (<code>String</code>) that 
   * depend on the supplied package, gets updated with the display identifiers 
   * of the packages.
   * 
   * @return <code>true</code> if it is a dependency of any other packages, 
   * otherwise <code>false</code>
   */
  protected static boolean isDependencyPackage(PSImportDescriptor descriptor, 
     PSDeployableElement element, Set parentPackages)
  {
     if(descriptor == null)
        throw new IllegalArgumentException("descriptor may not be null.");
   
     if(element == null)
        throw new IllegalArgumentException("element may not be null.");
        
     if(parentPackages == null)
        throw new IllegalArgumentException("parentPackages may not be null.");
              
     boolean isDependencyPackage = false;
     Iterator importPackages = descriptor.getImportPackageList().iterator();
     while(importPackages.hasNext())
     {
        PSImportPackage pkg = (PSImportPackage)importPackages.next();
        if(!pkg.getPackage().getKey().equals(element.getKey()) &&
           pkg.getPackage().containsDependency(element))
        {
           isDependencyPackage = true;
           parentPackages.add(pkg.getPackage().getDisplayIdentifier());
        }
     }

     return isDependencyPackage;
  }

  /**
   * Called by <code>onShow(PSDescriptor)</code> to display this dialog. 
   * Implementations left to derived classes, but {@link #getData()} should
   * be checked for previously entered data if the derived class may return a
   * non-<code>null</code> value from {@link #getDataToSave()}.
   * 
   * @throws IllegalStateException if the supplied descriptor is not valid for
   * the derived class dialog.
   * @throws PSDeployException if an error happens initializing the dialog.
   */
  protected abstract void init() throws PSDeployException;
  
  /**
   * Called to retrieve any state the dialog needs to save for future 
   * instantiations.
   *  
   * @return The data, may be <code>null</code>.  Contents are determined by the 
   * derived class.
   */
  public abstract Object getDataToSave();
  
  /**
   * Called to restore data previously saved by {@link #getDataToSave()} back to 
   * the dialog.
   * 
   * @param data The data to restore, may be <code>null</code>.
   */
  public void setData(Object data)
  {
     m_data = data;
  }
  
  /**
   * Gets any data set by {@link #setData(Object)}.
   * 
   * @return The data, may be <code>null</code>.
   */
  protected Object getData()
  {
     return m_data;
  }
  
  /**
   * Determines if the data entered by the user should be saved in the plan and
   * made available to other dialogs in the wizard.
   *  
   * @return <code>true</code> if settings should be updated, <code>false</code>
   * otherwise. 
   */
  protected boolean shouldUpdateUserSettings()
  {
     return m_updateUserSettings;
  }
  
  /**
   * Set if the data entered by the user should be saved in the plan and
   * made available to other dialogs in the wizard.
   * 
   * @param shouldUpdate <code>true</code> if settings should be updated, 
   * <code>false</code> otherwise. 
   */
  protected void setShouldUpdateUserSettings(boolean shouldUpdate)
  {
     m_updateUserSettings = shouldUpdate;
  }

  /**
   * Returns the descriptor as modified by the dialog, represents the dialog 
   * state.
   * 
   * @return A descriptor object containing this dialogs state, may be 
   * <code>null</code> if this dialog is not set with a descriptor.
   */                                                                                                                                                                                                                                                                              
  public PSDescriptor getDescriptor()
  {
     return m_descriptor;
  }

  /**
   * Confirms with user to cancel the wizard dialog and if user clicks 'yes' 
   * hides the dialog.
   */
  public void onCancel()
  {  
     int option = JOptionPane.showConfirmDialog(this, 
        ms_res.getString("cancelConfirmMsg"), 
        ms_res.getString("cancelConfirmTitle"), JOptionPane.YES_NO_OPTION, 
        JOptionPane.INFORMATION_MESSAGE);
        
     if(option == JOptionPane.YES_OPTION)
        setVisible(false);
  }
  
  /**
   * Overridden to delegate the method to <code>onNext()</code> because the 
   * base class handles the 'Enter' key event and calls this method. The 
   * default behavior for 'Enter' key in the dialog is to execute the default
   * button action if the focus is on an component that does not handle 'Enter'
   * key event.
   */
  public void onOk()
  {
     onNext();
  }

  /**
   * Sets the flag <code>m_isNext</code> to <code>true</code> and hides the 
   * dialog. Derived classes should override this method to validate and save 
   * the dialog state, and then call <code>super.onNext()</code> to hide the 
   * dialog.
   */
  public void onNext()
  {  
     m_isNext = true;
     m_isBack = false;
     m_updateUserSettings = true;
     setVisible(false);
  }

  /**
   * Sets the flag <code>m_isBack</code> to <code>true</code> and 
   * hides the dialog. Derived classes should override this method to call 
   * {@link #setShouldUpdateUserSettings(boolean)} unless the default of 
   * <code>false</code> should always be used, and then call 
   * <code>super.onBack()</code> to hide the dialog.  If settings will not be
   * updated, then derived classes should supply any dialog state to save in
   * {@link #getDataToSave()}. 
   */
  public void onBack()
  {
     m_isBack = true;
     m_isNext = false;
     setVisible(false);
  }
  
  /**
    * Creates the panel with the supplied title and description. Keeps the
   * description indented to the title. Uses <code>BoxLayout</code> for the 
   * panel layout and sets white background color for the panel and the panel
   * is lowered. 
   * 
   * @param title The title for description of this dialog step, may not be 
   * <code>null</code> or empty.
    * @param description the array of descriptions with each element in a new 
   * line. If a description string in the array contains a new line ['\n']
   * character, then the following string will be in next line. May not be 
   * <code>null</code> or empty. The elements in the array may not be <code>
   * null</code> or empty.
   * 
   * @return the panel, never <code>null</code>
   * 
   * @throws IllegalArgumentException if any parameter is invalid.
   */
  protected JPanel createDescriptionPanel(String title, String[] description)
  {
     if(title == null || title.trim().length() == 0)
        throw new IllegalArgumentException("title may not be null or empty.");
        
     if(description == null || description.length == 0)
        throw new IllegalArgumentException(
           "description may not be null or empty.");
           
     for (int i = 0; i < description.length; i++) 
     {
        if(description[i] == null || description[i].trim().length() == 0)
           throw new IllegalArgumentException(
              "description element may not be null or empty.");         
     }
     
     JPanel descPanel = new JPanel();
     descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.Y_AXIS));
     descPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createBevelBorder(BevelBorder.LOWERED),
        BorderFactory.createEmptyBorder(10,10,10,10) ));
     descPanel.setBackground(Color.white);
     
     StringTokenizer st = new StringTokenizer(title, 
        System.getProperty("line.separator"));      
     while(st.hasMoreTokens())
     {
        descPanel.add(new JLabel(st.nextToken(), SwingConstants.LEFT));
     }
     descPanel.add(Box.createVerticalStrut(5));
     for (int i = 0; i < description.length; i++) 
     {
        st = new StringTokenizer(description[i], 
           System.getProperty("line.separator"));      
        while(st.hasMoreTokens())
        {
           JPanel labelBox = new JPanel();
           labelBox.setBackground(Color.white);
           labelBox.setLayout(new BoxLayout(labelBox, BoxLayout.X_AXIS));
           labelBox.setAlignmentX(LEFT_ALIGNMENT);
           labelBox.add(Box.createHorizontalStrut(50));
           labelBox.add(new JLabel(st.nextToken(), SwingConstants.LEFT));
           descPanel.add(labelBox);
        }
     }
     descPanel.setAlignmentX(LEFT_ALIGNMENT);
     return descPanel;
  }

  /**
   * Convenience method that calls 
   * {@link #createCommandPanel(boolean, JComponent[]) 
   * createCommandPanel(showHelp, null)}
   */
  protected JPanel createCommandPanel(boolean showHelp)
  {
     return createCommandPanel(showHelp, null);
  }
  
  /**
   * Overrides the base class to create a command panel with 'Next/Finish', 
   * 'Cancel' and 'Help' buttons. Sets actions on each button. The Next button
   * label will be 'Finish' if the dialog represents the last step of the 
   * wizard (specified in the constructor), otherwise 'Next'. This should be 
   * called from <code>initDialog()</code> and should be added to the bottom
   * end of the panel.
   * 
   * @param showHelp if <code>true</code> help button will be added to the 
   * panel, otherwise not.
   * @param extras Array of extra <code>Component</code>s to add to the command 
   * panel. These components are laid out left to right, aligned to the left 
   * side of the command panel.  Typically these are <code>JButton</code> 
   * objects.  May be <code>null</code>, may not have <code>null</code> entries.
   * 
   * @return the panel, never <code>null</code>
   */
  protected JPanel createCommandPanel(boolean showHelp, JComponent[] extras)
  {
     JButton backButton = new UTFixedButton(ms_res.getString("back"));
     backButton.setMnemonic(ms_res.getCharacter("mn_back"));
     backButton.addActionListener(new ActionListener() 
     {
        public void actionPerformed(ActionEvent e) 
        {
           onBack();
        }
     });
     backButton.setEnabled(!m_isFirst);
     
     String nextButtonLabel;
     if(m_isLast)
        nextButtonLabel = ms_res.getString("finish");
     else
        nextButtonLabel = ms_res.getString("next");  
        
     JButton nextButton = new UTFixedButton(nextButtonLabel);
     nextButton.setDefaultCapable(true);
     nextButton.setMnemonic(ms_res.getCharacter("mn_next"));
     getRootPane().setDefaultButton(nextButton);
     nextButton.addActionListener(new ActionListener() 
     {
        public void actionPerformed(ActionEvent e) 
        {
           onNext();
        }
     });
     
     JButton cancelButton = new UTFixedButton(ms_res.getString("cancel"));
     cancelButton.setMnemonic(ms_res.getCharacter("mn_cancel"));
     cancelButton.addActionListener(new ActionListener() 
     {
        public void actionPerformed(ActionEvent e) 
        {
           onCancel();
        }
     });
     
     JPanel box = new JPanel();
     box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
     box.add(Box.createHorizontalGlue());
     box.add(backButton);
     box.add(Box.createHorizontalStrut(5));
     box.add(nextButton);
     box.add(Box.createHorizontalStrut(5));
     box.add(cancelButton);
     if(showHelp)
     {
        JButton helpButton = new UTFixedButton(ms_res.getString("help"));
        helpButton.setMnemonic(ms_res.getCharacter("mn_help"));
        helpButton.addActionListener(new ActionListener() 
        {
           public void actionPerformed(ActionEvent e) 
           {
              onHelp();
           }
        });        
        box.add(Box.createHorizontalStrut(5));
        box.add(helpButton);
     }
     box.setAlignmentX(LEFT_ALIGNMENT);
     
     if (extras != null)
     {
        JPanel extraBox = new JPanel();
        extraBox.setLayout(new BoxLayout(extraBox, BoxLayout.X_AXIS));
        for (int i = 0; i < extras.length; i++)
        {
           if (!(extras[i] instanceof Component))
              throw new IllegalArgumentException(
                 "extras may only contain non-null Components");
           
           extraBox.add(extras[i]);
           if ((i+1) < extras.length)
              extraBox.add(Box.createHorizontalStrut(5));
        }
        
        extraBox.add(box);
        box = extraBox;
     }
     
     return box;
  }
  
  /**
   * Gets the wizard step this dialog representing.
   * 
   * @return the step.
   */
  public int getStep()
  {
     return m_step;
  }
  
  /**
   * Checks whether user exited this dialog by pressing 'Next' or 'Finish'.
   * 
   * @return <code>true</code> if the dialog was hidden by clicking Next.
   */
  public boolean isNext()
  {
     return m_isNext;
  }

  /**
   * Checks whether user exited this dialog by pressing 'Back'.
   * 
   * @return <code>true</code> if the dialog was hidden by clicking Back.
   */
  public boolean isBack()
  {
     return m_isBack;
  }

  /**
   * Validates the user entered values in the dialog and displays error 
   * messages if the validation fails. This base class implementation always 
   * returns <code>true</code>. The derived dialog classes should implement 
   * this method if they want to do any validation.
   * 
   * @return <code>false</code> if the validation fails, otherwise 
   * <code>true</code>
   */
  protected boolean validateData()
  {
     return true;
  }

  /**
   * Sets the sequence for this dialog
   * 
   * @param sequence Must be one of the <code>SEQUENCE_</code> values.
   */
  private void setSequence(int sequence)
  {
     if (sequence == SEQUENCE_FIRST)
     {
        m_isLast = false;
        m_isFirst = true;
     }
     else if (sequence == SEQUENCE_LAST)
     {
        m_isLast = true;
        m_isFirst = false;        
     }
     else if (sequence != SEQUENCE_MID)
     {
        throw new IllegalArgumentException("invalid sequence supplied");
     }
     else
     {
        m_isLast = false;
        m_isFirst = false;        
     }
  }
  
  /**
   * The deployment server (source/target) used to make requests to the server. 
   * Initialized in the constructor, may be <code>null</code> if it is not set.
   * If it is set, this instance is not <code>null</code> and is connected. 
   * Never modified after initialization.
   */
  protected PSDeploymentServer m_deploymentServer = null;   
  
  /**
   * The wizard step this dialog represents, initialized in the constructor and
   * never modified after that.
   */
  private int m_step;
  
  /**
   * The flag to indicate that this dialog represents the last step in the 
   * wizard or not. <code>true</code> to indicate it is the last step, <code>
   * false</code> otherwise.
   */
  private boolean m_isLast = false;

  /**
   * The flag to indicate that this dialog represents the fisrt step in the 
   * wizard or not. <code>true</code> to indicate it is the fisrt step, <code>
   * false</code> otherwise.
   */
  private boolean m_isFirst = false;
  
  /**
   * The descriptor which holds this dialog's state. Set in <code>onShow()
   * </code> and may be modified according to user actions. May be <code>null
   * </code>.
   */
  protected PSDescriptor m_descriptor = null;

  /**
   * The flag to indicate whether the dialog is hidden by clicking 'Next' or 
   * not. Initialized to <code>false</code> and set to <code>true</code> before
   * hiding the dialog in <code>onNext()</code>
   */
  private boolean m_isNext = false;
  
  /**
   * The flag to indicate whether the dialog is hidden by clicking 'Back' or 
   * not. Initialized to <code>false</code> and set to <code>true</code> before
   * hiding the dialog in <code>onBack()</code>
   */
  private boolean m_isBack = false;
  
  /**
   * Previously saved data for this dialog.  Used to store the data of the 
   * wizard dialog in case the user returns to the dialog after moving forward
   * or backward within the wizard. Set by calls to {@link #setData(Object)}, 
   * may be <code>null</code>.
   */
  private Object m_data = null;
  
  /**
   * Used to track if settings should be updated, initially <code>false</code>. 
   * See {@link #shouldUpdateUserSettings()} for details.
   */
  private boolean m_updateUserSettings = false;
  
  /**
   * The resource bundle containing the button labels and generic error 
   * messages used by all wizard dialogs, never <code>null</code>
   */
  protected static final PSResources ms_res = PSDeploymentClient.getResources();
  
  /**
   * The constant to indicate this dialog to be used as part of export wizard.
   */
  public static int TYPE_EXPORT = 1;   
  
  /**
   * The constant to indicate this dialog to be used as part of import wizard.
   */
  public static int TYPE_IMPORT = 2;
  
  /**
   * Constant to indicate this is the first dialog in the wizard.
   */
  public static int SEQUENCE_FIRST = 0;
  
  /**
   * Constant to indicate this is not the first or last dialog in the 
   * wizard.
   */
  public static int SEQUENCE_MID = 1;
  
  /**
   * Constant to indicate this is the last dialog in the wizard.
   */
  public static int SEQUENCE_LAST = 2;
}

