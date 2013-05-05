package edu.rutgers.cs541.gui;

import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

public class EditorFactory {

	/**
	 * Creates a scroll pane that contains a text editor formatted for SQL.
	 * @return JScrollPane with a JEditorPane inside it.
	 */
	public static JScrollPane createSQLEditor() {
        JEditorPane queryEditor = new JEditorPane();
        JScrollPane queryContainer = new JScrollPane(queryEditor);
        queryEditor.setContentType("text/sql");
        queryEditor.setPreferredSize(new Dimension(240, 170));
        queryEditor.setMinimumSize(new Dimension(10, 10));
        return queryContainer;
	}

	/**
	 * Creates a scroll pane that contains a text editor formatted for SQL.
	 * @param text Default text for the text editor.
	 * @return JScrollPane with a JEditorPane inside it.
	 */
	public static JScrollPane createSQLEditor(String text) {
        JEditorPane queryEditor = new JEditorPane();
        JScrollPane queryContainer = new JScrollPane(queryEditor);
        queryEditor.setContentType("text/sql");
        queryEditor.setText(text);
        queryEditor.setPreferredSize(new Dimension(240, 170));
        queryEditor.setMinimumSize(new Dimension(10, 10));
        return queryContainer;
	}

	/**
	 * Creates a scroll pane that contains an uneditable text editor formatted
	 * for SQL.
	 *
	 * @param text Default text for the text editor.
	 * @return JScrollPane with a JEditorPane inside it.
	 */
	public static JScrollPane createSQLDisplay(String text) {
        JEditorPane queryEditor = new JEditorPane();
        JScrollPane queryContainer = new JScrollPane(queryEditor);
        queryEditor.setContentType("text/sql");
        queryEditor.setText(text);
        queryEditor.setPreferredSize(new Dimension(240, 170));
        queryEditor.setMinimumSize(new Dimension(10, 10));
        queryEditor.setEditable(false);
        return queryContainer;
	}

	/**
	 * Creates a scroll pane that contains a text editor formatted for SQL and
	 * sized large enough to be used for schemas.
	 * @return JScrollPane with a JEditorPane inside it.
	 */
	public static JScrollPane createSchemaEditor() {
        JEditorPane queryEditor = new JEditorPane();
        JScrollPane queryContainer = new JScrollPane(queryEditor);
        queryEditor.setContentType("text/sql");
        queryEditor.setPreferredSize(new Dimension(400, 250));
        queryEditor.setMinimumSize(new Dimension(10, 10));
        return queryContainer;
	}

	/**
	 * Creates a scroll pane that contains an uneditable text editor formatted
	 * for SQL and sized large enough to be used for schemas.
	 *
	 * @param text Default text for the text editor.
	 * @return JScrollPane with a JEditorPane inside it.
	 */
	public static JScrollPane createSchemaDisplay(String text) {
        JEditorPane queryEditor = new JEditorPane();
        JScrollPane queryContainer = new JScrollPane(queryEditor);
        queryEditor.setContentType("text/sql");
        queryEditor.setText(text);
        queryEditor.setPreferredSize(new Dimension(640, 380));
        queryEditor.setMinimumSize(new Dimension(10, 10));
        queryEditor.setEditable(false);
        return queryContainer;
	}
}
