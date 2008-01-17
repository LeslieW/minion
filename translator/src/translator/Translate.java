package translator;

import javax.swing.JFrame;

/**
 * This is the started class. It can either evoke the GUI version or
 * the command-line version of the Essence' translator. The GUI version 
 * is default. Starting the translator with the "help" argument gives 
 * some information about the usage. 
 * 
 * @author andrea
 *
 */

public class Translate {

	public static final String GUI_NAME = "The Reformulator I";
	
	/**
	 * Start the GUI version or command-line version of the translator.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
	
		startGuiVersion();
		
	}


	/**
	 * Starts the GUI version of the Essence' translator
	 *
	 */
	
	private static void startGuiVersion() {
		JFrame frame = new JFrame(GUI_NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    //Add content to the window.
		frame.add(new translator.gui.TranslatorGUI());

	    //Display the window.
		frame.pack();
		frame.setVisible(true);
	}
}
