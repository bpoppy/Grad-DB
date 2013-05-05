package edu.rutgers.cs541.gui;

import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ResultsWindow extends BasePanel {

	private static final long serialVersionUID = 1050574277702365359L;

	public ResultsWindow(String q1, String q2, String s) {
		super();
		this.title = "Results";

        final JScrollPane query1 = EditorFactory.createSQLDisplay(q1);
        final JScrollPane query2 = EditorFactory.createSQLDisplay(q2);
        final JScrollPane schema = EditorFactory.createSQLDisplay(s);

        JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		wrapper.add(new JLabel("Schema Definition:"));
		wrapper.add(schema);

		JLabel queryLabel = new JLabel("Queries being compared:");
		wrapper.add(queryLabel);

		// Container for query editors
		JPanel queries = new JPanel(new GridLayout(1,2));
		queries.add(query1);
		queries.add(query2);
		wrapper.add(queries);

        this.add(wrapper);
	}

}
