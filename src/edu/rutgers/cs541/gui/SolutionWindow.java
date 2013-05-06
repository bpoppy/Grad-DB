package edu.rutgers.cs541.gui;

import java.awt.Dimension;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.rutgers.cs541.EntryPoint;

public class SolutionWindow extends BasePanel {

	public SolutionWindow(String[] result) {
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		String filePath = "tmp" + File.separator + result[0] + ".sql";
		String data = EntryPoint.readFileOrDie(filePath);
		JScrollPane solution = EditorFactory.createSQLDisplay(data);
		solution.setPreferredSize(new Dimension(350, 200));
		wrapper.add(solution);

		this.add(wrapper);
	}

}
