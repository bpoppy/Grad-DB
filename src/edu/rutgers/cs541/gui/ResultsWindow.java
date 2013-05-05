package edu.rutgers.cs541.gui;

import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class ResultsWindow extends BasePanel {

	private static final long serialVersionUID = 1050574277702365359L;

	public ResultsWindow(String q1, String q2, String s) {
		// Initialization
		super();
		this.title = "Results";

		// Container
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));

		// Creating uneditable text areas
        final JScrollPane query1 = EditorFactory.createSQLDisplay(q1);
        final JScrollPane query2 = EditorFactory.createSQLDisplay(q2);
        final JScrollPane schema = EditorFactory.createSQLDisplay(s);

        // Text area container
        JPanel textAreas = new JPanel();
		textAreas.setLayout(new BoxLayout(textAreas, BoxLayout.Y_AXIS));
		wrapper.add(textAreas);

		// Schema definition
		textAreas.add(new JLabel("Schema Definition:"));
		textAreas.add(schema);

		// Container for query editors
		JLabel queryLabel = new JLabel("Queries being compared:");
		textAreas.add(queryLabel);
		JPanel queries = new JPanel(new GridLayout(1,2));
		queries.add(query1);
		queries.add(query2);
		textAreas.add(queries);


		final JTable resultsTable = new JTable(new ResultTableModel());
		resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane tableScroll = new JScrollPane(resultsTable);
		wrapper.add(tableScroll);

        this.add(wrapper);
	}

}
