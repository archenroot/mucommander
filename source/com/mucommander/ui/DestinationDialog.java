
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.text.Translator;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.ui.table.FileTable;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Abstract Dialog which displays an input field in order to enter a destination path.
 * This dialog is used by CopyDialog, MoveDialog, UnzipDialog and DownloadDialog.
 *
 * @author Maxence Bernard
 */
public abstract class DestinationDialog extends FocusDialog implements ActionListener {

	protected MainFrame mainFrame;
	protected FileSet files;
	
	protected JTextField pathField;
	protected JComboBox fileExistsActionComboBox;
	protected JButton okButton;
	protected JButton cancelButton;

	protected String errorDialogTitle = Translator.get("move_dialog.error_title");
	
	// Dialog size constraints
	protected final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    protected final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(360,10000);	
	

	/**
	 * Creates a new DestinationDialog.
	 *
	 * @param mainFrame the main frame this dialog is attached to.
	 */
	public DestinationDialog(MainFrame mainFrame, FileSet files) {
		super(mainFrame, null, mainFrame);
		this.mainFrame = mainFrame;
		this.files = files;
	}
	
	
	/**
	 * Creates a new DestinationDialog.
	 *
	 * @param mainFrame the main frame this dialog is attached to.
	 */
	public DestinationDialog(MainFrame mainFrame, FileSet files, String title, String labelText, String okText, String errorDialogTitle) {
		this(mainFrame, files);
		
		init(title, labelText, okText, errorDialogTitle);
	}
	
	
	protected void init(String title, String labelText, String okText, String errorDialogTitle) {
		this.errorDialogTitle = errorDialogTitle;

		setTitle(title);
		
		Container contentPane = getContentPane();
        YBoxPanel mainPanel = new YBoxPanel();
		
		JLabel label = new JLabel(labelText);
        mainPanel.add(label);

		// Create path textfield
		pathField = new JTextField();
        pathField.addActionListener(this);
		mainPanel.add(pathField);
		mainPanel.addSpace(10);

		// Path field will receive initial focus
		setInitialFocusComponent(pathField);		
		
		// OK / Cancel buttons panel
        okButton = new JButton(okText);
        cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okButton);

		// Checkbox that allows the user to choose the default action when a file exists
		mainPanel.add(new JLabel(Translator.get("destination_dialog.file_exists_action")));
		fileExistsActionComboBox = new JComboBox();
		fileExistsActionComboBox.addItem(Translator.get("ask"));
		String choicesText[] = FileExistsDialog.CHOICES_TEXT;
		int nbChoices = choicesText.length;
		for(int i=0; i<nbChoices; i++)
			fileExistsActionComboBox.addItem(choicesText[i]);
		mainPanel.add(fileExistsActionComboBox);
		mainPanel.addSpace(10);
		
        contentPane.add(mainPanel, BorderLayout.NORTH);
		
		// Set minimum/maximum dimension
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
	}


	protected void setTextField(String text) {
		pathField.setText(text);
        // Text is selected so that user can directly type and replace path
        pathField.setSelectionStart(0);
        pathField.setSelectionEnd(text.length());
	}


	protected void setTextField(String text, int selStart, int selEnd) {
		pathField.setText(text);
        // Text is selected so that user can directly type and replace path
        pathField.setSelectionStart(selStart);
        pathField.setSelectionEnd(selEnd);
	}
	
	
	/**
	 * Displays an error message.
	 */
	protected void showErrorDialog(String msg) {
		JOptionPane.showMessageDialog(mainFrame, msg, errorDialogTitle, JOptionPane.ERROR_MESSAGE);
	}


	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		dispose();
		
		// OK action
		if(source == okButton || source == pathField) {
			okPressed();
		}
	}

	
	/**
	 * This method is invoked when the OK button is pressed.
	 */
	private void okPressed() {
		String destPath = pathField.getText();

		// Resolves destination folder
		Object ret[] = mainFrame.resolvePath(destPath);
		// The path entered doesn't correspond to any existing folder
		if (ret==null || (files.size()>1 && ret[1]!=null)) {
			showErrorDialog(Translator.get("this_folder_does_not_exist", destPath));
			return;
		}

		AbstractFile destFolder = (AbstractFile)ret[0];
		String newName = (String)ret[1];
		
		// Retrieve default action when a file exists in destination, default choice
		// (if not specified by the user) is 'Ask'
		int defaultFileExistsAction = fileExistsActionComboBox.getSelectedIndex();
		if(defaultFileExistsAction==0)
			defaultFileExistsAction = FileExistsDialog.ASK_ACTION;
		else
			defaultFileExistsAction = FileExistsDialog.CHOICES_ACTIONS[defaultFileExistsAction-1];
		// We don't remember default action on purpose: we want the user to specify it each time,
		// it would be too dangerous otherwise.
		
		startJob(destFolder, newName, defaultFileExistsAction);
	}
	
	
	protected abstract void startJob(AbstractFile destFolder, String newName, int defaultFileExistsAction);
	
}
