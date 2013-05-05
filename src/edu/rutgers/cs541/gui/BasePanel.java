/**
 *
 */
package edu.rutgers.cs541.gui;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author bilalq
 *
 */
public abstract class BasePanel extends JPanel {

	/**
	 *
	 */
	protected static final long serialVersionUID = -5440571846700587662L;
	protected String title;

	/**
	 *
	 */
	public BasePanel() {
		super();
	}

	/**
	 * Open the specified window and push it onto the stack.
	 * @param window
	 */
	public void open(BasePanel window) {
		GuiFrame parent = this.getFrame();
		parent.pushWindow(window);
	}

	/**
	 * Close current window and pop from the stack.
	 */
	public void close() {
		GuiFrame parent = this.getFrame();
		parent.popWindow();
	}

	/**
	 * Get reference to parent frame.
	 * @return GuiView Root GUI frame of application
	 */
	public GuiFrame getFrame() {
		return (GuiFrame) SwingUtilities.getWindowAncestor(this);
	}

	/**
	 * Getter for panel title
	 * @return
	 */
    public String getTitle() {
        return this.title;
    }

    /**
     * Template method that is triggered when a panel is brought into focus.
     */
    public void triggerUpdates() {
    	return;
    }

}
