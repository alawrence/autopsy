/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sleuthkit.autopsy.casemodule;

import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;
import org.sleuthkit.autopsy.coreutils.Log;

/**
 * Action to open the New Case wizard.
 */
public final class NewCaseWizardAction extends CallableSystemAction {

    private WizardDescriptor.Panel[] panels;

    @Override
    public void performAction() {
        Log.noteAction(this.getClass());


        // there's a case open
        if (Case.existsCurrentCase()) {
            // show the confirmation first to close the current case and open the "New Case" wizard panel
            String closeCurrentCase = "Do you want to save and close this case and proceed with the new case creation?";
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(closeCurrentCase, "Warning: Closing the Current Case", NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
            d.setValue(NotifyDescriptor.NO_OPTION);

            Object res = DialogDisplayer.getDefault().notify(d);
            if (res != null && res == DialogDescriptor.YES_OPTION) {
                try {
                    Case.getCurrentCase().closeCase(); // close the current case
                    newCaseAction(); // start the new case creation process
                } catch (Exception ex) {
                    Logger.getLogger(NewCaseWizardAction.class.getName()).log(Level.SEVERE, "Error closing case.", ex);
                }
            }
        } else {
            newCaseAction();
        }
    }

    /**
     * The method to perform new case creation
     */
    private void newCaseAction() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("New Case Information");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();


        boolean finished = wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION; // check if it finishes (it's not cancelled)
        boolean isCancelled = wizardDescriptor.getValue() == WizardDescriptor.CANCEL_OPTION; // check if the "Cancel" button is pressed

        // if the finish button is pressed (not cancelled)
        if (finished) {
            // nothing to do, the Add Image dialog will pop up on its own because we've just opened a case with no images
        }

        // if Cancel button is pressed
        if (isCancelled) {
            // if there's case opened, close the case
            if (Case.existsCurrentCase()) {
                // close the previous case if there's any
                CaseCloseAction closeCase = SystemAction.get(CaseCloseAction.class);
                closeCase.actionPerformed(null);
            }
        }
        panels = null; // reset the panel
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                        new NewCaseWizardPanel1()
                    };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    @Override
    public String getName() {
        return "New Case Wizard";
    }

    @Override
    public String iconResource() {
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
