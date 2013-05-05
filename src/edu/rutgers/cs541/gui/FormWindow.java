package edu.rutgers.cs541.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jsyntaxpane.DefaultSyntaxKit;



public class FormWindow extends BasePanel {

	private static final long serialVersionUID = 2473544787468600537L;
	private File schema;

	public FormWindow() {
		// Initialization
		super();
		this.title = "Home";
		this.schema = null;
		DefaultSyntaxKit.initKit();

		// Wrapper
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		// Container for query editors
		JPanel queries = new JPanel(new GridLayout(1,2));
		wrapper.add(queries);


		// Create query editors
        final JScrollPane query1 = EditorFactory.createSQLEditor();
        final JScrollPane query2 = EditorFactory.createSQLEditor();
        queries.add(query1);
        queries.add(query2);

        // Create buttons
        JPanel buttons = new JPanel();
        final JButton uploadButton = new JButton("Upload Schema");
        final JLabel uploadedFileName = new JLabel("                ");
        uploadedFileName.setForeground(Color.GRAY);
        buttons.add(uploadButton);
        buttons.add(uploadedFileName);
		final JButton runButton = new JButton("Run");
		runButton.setEnabled(false);
		buttons.add(runButton);
		wrapper.add(buttons);



		// Add event listeners
		uploadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				FormWindow self = FormWindow.this;
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				File chosenFile = chooser.getSelectedFile();
				if (chosenFile != null) {
					String fileName = chosenFile.getName();
					uploadedFileName.setText(fileName);
					self.setSchema(chosenFile);
					runButton.setEnabled(true);
				}
			}
		});
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				FormWindow self = FormWindow.this;
				JEditorPane q1 = (JEditorPane) query1.getViewport().getView();
				JEditorPane q2 = (JEditorPane) query2.getViewport().getView();
				String q1Text = q1.getText().trim();
				String q2Text = q2.getText().trim();
				self.open(new ResultsWindow(q1Text, q2Text, self.getSchema()));
			}
		});


		this.add(wrapper);
	}

	public File getSchema() {
		return this.schema;
	}

	public void setSchema(File other) {
		this.schema = other;
	}
}
