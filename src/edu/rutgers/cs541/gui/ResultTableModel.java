package edu.rutgers.cs541.gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class ResultTableModel extends AbstractTableModel {
	private final ArrayList<String[]> data;
	private final String[] columnNames = {
		"id",
		"Time Elapsed",
		"# Tested",
		"Tuple Count"
	};

	public ResultTableModel() {
		this.data = new ArrayList<String[]>();
	}

	public ResultTableModel(ArrayList<String[]> data) {
		this.data = data;
	}

	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}

	@Override
	public int getRowCount() {
		return this.data.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		return this.data.get(row)[col];
	}

    @Override
	public String getColumnName(int col) {
        return this.columnNames[col];
    }

	public void addResult(String[] result) {
		this.data.add(result);
		this.fireTableDataChanged();
	}

	public String[] getResult(int row) {
		return this.data.get(row);
	}
}
