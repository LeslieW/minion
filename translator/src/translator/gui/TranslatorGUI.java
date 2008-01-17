package translator.gui;

import javax.swing.*;
import translator.TranslationSettings;

/**
 * This panel is the base panel for the graphical user interface of 
 * the translator. The GUI consists of 2 main panels:
 * one panel for modelling and observation of the translation process
 * and the other for selecting the target solvers and customising the
 * solver's features for the translation.
 * 
 * @author andrea
 *
 */

public class TranslatorGUI extends JPanel {

	static final long serialVersionUID = 99;
	
	ModellingPanel modellingPanel;
	SolverPanel solverPanel;
	TranslationSettings defaultSettings;
	JTabbedPane tabbedPanel;
	
	
	// ========== CONSTRUCTOR ===========================
	
	public TranslatorGUI() {
		this.defaultSettings = new TranslationSettings();
		int width = 1200;
		int height = 750;
		
		
		this.modellingPanel = new ModellingPanel(this.defaultSettings,
				                                 width,
				                                 height);
		this.solverPanel = new SolverPanel(this.defaultSettings,
										   width,
										   height);
		
		createTabbedPane();
	}
	
	
	// ============ METHODS =============================
	
	protected void createTabbedPane() {
		this.tabbedPanel = new JTabbedPane();
		tabbedPanel.add("Modelling", this.modellingPanel);
		tabbedPanel.add("Settings", this.solverPanel);
		this.add(tabbedPanel);
	}
}
