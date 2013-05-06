package edu.rutgers.cs541.gui;

import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import edu.rutgers.cs541.EntryPoint;

public class ResultsWindow extends BasePanel {

	private static final long serialVersionUID = 1050574277702365359L;
	private final JTable resultsTable;
	private final ResultTableModel tableModel;

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


		this.tableModel = new ResultTableModel();
		this.resultsTable = new JTable(this.tableModel);
		this.resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane tableScroll = new JScrollPane(this.resultsTable);
		wrapper.add(tableScroll);
		wrapper.setBorder(new EmptyBorder(25, 25, 25, 25));

        this.add(wrapper);

        EntryPoint.beginJudgement(q1, q2, s, this);
	}

	public void publishResult(String[] result) {
		this.tableModel.addResult(result);
	}

}
