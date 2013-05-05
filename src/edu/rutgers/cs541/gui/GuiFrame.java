/**
 *
 */
package edu.rutgers.cs541.gui;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;

/**
 * @author bilalq
 *
 */
public class GuiFrame extends JFrame {

	/** Serialization number */
	private static final long serialVersionUID = -2646623987801324201L;
	/** Count of how many queries have been tried so far */
	public final AtomicInteger count = new AtomicInteger();
	/** Stack of windows to maintain back functionality */
	private final Stack<BasePanel> windowStack;
	/** Base title of application */
	private static final String BASE_TITLE = "Query Comparator";


	/**
	 * Constructor for the frame of the application. Sets the title,
	 * initializes the window stack, and adds a FormWindow.
	 */
	public GuiFrame() {
		super("Query Comparator");
		this.windowStack = new Stack<BasePanel>();
		BasePanel form = new FormWindow();
		this.windowStack.add(form);
		this.add(form);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


	/**
	 * Method for worker threads to POST updates to.
	 * @param wat
	 */
	public synchronized void updateResults(Object wat) {
		// TODO
		return;
	}

	/**
	 * Render the frame and display it for the first time.
	 *
	 */
	private void render() {
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	/**
	 * Redraw the frame to reflect updated content.
	 *
	 */
	public void redraw() {
		this.validate();
		this.repaint();
		this.pack();
		this.setVisible(true);
	}

	/**
	 * Returns the stack of windows
	 *
	 * @return the window stack
	 */
	public Stack<BasePanel> getWindowStack() {
		return this.windowStack;
	}

	/**
	 * Pushes a new window onto the stack and updates the gui.
	 *
	 * @param window
	 */
	public void pushWindow(BasePanel window) {
		this.setActiveWindow(this.windowStack.peek(), window);
		this.windowStack.push(window);
	}

	/**
	 * Pops a window from the stack and updates the gui.
	 */
	public void popWindow() {
		BasePanel prev = this.windowStack.pop();
		BasePanel next = this.windowStack.peek();
		this.setActiveWindow(prev, next);
	}

	/**
	 * Helper method to set the current active window.
	 *
	 * @param previous Last window
	 * @param next Current window
	 */
	private void setActiveWindow(BasePanel previous, BasePanel next) {
		this.add(next);
		this.remove(previous);
		String title = next.getTitle();
		if ( title != null && (! title.isEmpty()) ) {
	        title = BASE_TITLE + " | " + next.getTitle();
		} else {
			title = BASE_TITLE;
		}
        this.setTitle(title);
        next.triggerUpdates();
        this.redraw();
	}

	public static void main(String[] args) {
		GuiFrame frame = new GuiFrame();
		frame.render();
	}

}
