package edu.rutgers.cs541.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
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
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

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

		// Create table
		this.tableModel = new ResultTableModel();
		this.resultsTable = new JTable(this.tableModel);
		this.resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane tableScroll = new JScrollPane(this.resultsTable);
		wrapper.add(tableScroll);
		wrapper.setBorder(new EmptyBorder(25, 25, 25, 25));
		wrapper.setMinimumSize(new Dimension(740, 400));

		// Add wrapper to window
        this.add(wrapper);

		// Show & back buttons
        JPanel buttons = new JPanel();
        JButton backButton = new JButton("Back");
        JButton showButton = new JButton("Show");
        buttons.add(backButton);
        buttons.add(showButton);
        this.add(buttons);

        backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				ResultsWindow self = ResultsWindow.this;
				self.close();
			}
        });
        showButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
				ResultsWindow self = ResultsWindow.this;
				int row = resultsTable.getSelectedRow();
				String[] chosen = tableModel.getResult(row);
				JFrame result = new JFrame("Result " + chosen[0]);
				result.add(new SolutionWindow(chosen));
				result.pack();
				result.setLocationRelativeTo(null);
				result.setVisible(true);
        	}
        });

        EntryPoint.beginJudgment(q1, q2, s, this);
	}

	public void publishResult(String[] result) {
		this.tableModel.addResult(result);
	}

}
