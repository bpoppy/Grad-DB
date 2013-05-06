package edu.rutgers.cs541.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import jsyntaxpane.DefaultSyntaxKit;
import edu.rutgers.cs541.EntryPoint;
import edu.rutgers.cs541.InstanceTester;



public class FormWindow extends BasePanel {

	private static final long serialVersionUID = 2473544787468600537L;

	public FormWindow() {
		// Initialization
		super();
		this.title = "Home";
		DefaultSyntaxKit.initKit();

		// Wrapper
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		// Container for schema editor
		JPanel schemaSection = new JPanel();
		schemaSection.setLayout(new BoxLayout(schemaSection, BoxLayout.Y_AXIS));
		schemaSection.setPreferredSize(new Dimension(420, 270));
        final JScrollPane schemaEditor = EditorFactory.createSchemaEditor();
        final JButton schemaUploadButton = new JButton("Upload Schema");
        schemaSection.add(schemaEditor);
        schemaSection.add(schemaUploadButton);
        wrapper.add(schemaSection);
        wrapper.add(new JSeparator());

		// Container for query editors
		JPanel queries = new JPanel(new GridLayout(1,2));
		JPanel query1Container = new JPanel();
		query1Container.setLayout(new BoxLayout(query1Container, BoxLayout.Y_AXIS));
		JPanel query2Container = new JPanel();
		query2Container.setLayout(new BoxLayout(query2Container, BoxLayout.Y_AXIS));
		queries.add(query1Container);
		queries.add(query2Container);
		wrapper.add(queries);


		// Create query editors
        final JScrollPane query1 = EditorFactory.createSQLEditor();
        final JButton upload1Button = new JButton("Upload Query 1");
        final JScrollPane query2 = EditorFactory.createSQLEditor();
        final JButton upload2Button = new JButton("Upload Query 2");
        query1Container.add(query1);
        query1Container.add(upload1Button);
        query2Container.add(query2);
        query2Container.add(upload2Button);

        // Create buttons
        JPanel buttons = new JPanel();
		final JButton runButton = new JButton("Run");
		buttons.add(runButton);
		wrapper.add(buttons);



		// Add event listeners
		schemaUploadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				File chosenFile = chooser.getSelectedFile();
				if (chosenFile != null) {
					String text = EntryPoint.readFileOrDie(chosenFile.getAbsolutePath());
					JEditorPane editor = (JEditorPane) schemaEditor.getViewport().getView();
					editor.setText(text);
				}
			}
		});
		upload1Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				File chosenFile = chooser.getSelectedFile();
				if (chosenFile != null) {
					String text = EntryPoint.readFileOrDie(chosenFile.getAbsolutePath());
					JEditorPane editor = (JEditorPane) query1.getViewport().getView();
					editor.setText(text);
				}
			}
		});
		upload2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				File chosenFile = chooser.getSelectedFile();
				if (chosenFile != null) {
					String text = EntryPoint.readFileOrDie(chosenFile.getAbsolutePath());
					JEditorPane editor = (JEditorPane) query2.getViewport().getView();
					editor.setText(text);
				}
			}
		});
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				FormWindow self = FormWindow.this;
				JEditorPane q1 = (JEditorPane) query1.getViewport().getView();
				JEditorPane q2 = (JEditorPane) query2.getViewport().getView();
				JEditorPane schema = (JEditorPane) schemaEditor.getViewport().getView();
				String q1Text = q1.getText().trim();
				String q2Text = q2.getText().trim();
				String schemaText = schema.getText().trim();
				if (q1Text.isEmpty() || q2Text.isEmpty() || schemaText.isEmpty()) {
					return;
				}
				InstanceTester.active = true;
				self.open(new ResultsWindow(q1Text, q2Text, schemaText));
			}
		});


		this.add(wrapper);
	}

}
