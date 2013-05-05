package edu.rutgers.cs541.gui;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

public class ResultsWindow extends BasePanel {

	private static final long serialVersionUID = 1050574277702365359L;

	public ResultsWindow(String q1, String q2, File schema) {
		super();
		this.title = "Results";

        final JScrollPane query1 = EditorFactory.createSQLDisplay(q1);
        final JScrollPane query2 = EditorFactory.createSQLDisplay(q2);

        JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		JLabel queryLabel = new JLabel("Queries being compared:");
		wrapper.add(queryLabel);

		// Container for query editors
		JPanel queries = new JPanel(new GridLayout(1,2));
		queries.add(query1);
		queries.add(query2);
		wrapper.add(queries);
		wrapper.add(new JSeparator());

        this.add(wrapper);
	}

}
