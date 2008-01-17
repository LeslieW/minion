package translator;

import javax.swing.*;
//import java.awt.*;
import translator.solver.*;

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
	TargetSolver defaultSolver;
	JTabbedPane tabbedPanel;
	
	
	// ========== CONSTRUCTOR ===========================
	
	public TranslatorGUI() {
		this.defaultSolver = new Minion();
		
		this.modellingPanel = new ModellingPanel();
		this.solverPanel = new SolverPanel(this.defaultSolver);
		
		createTabbedPane();
	}
	
	
	// ============ METHODS =============================
	
	protected void createTabbedPane() {
		this.tabbedPanel = new JTabbedPane();
		tabbedPanel.add("Modelling", this.modellingPanel);
		tabbedPanel.add("Solver", this.solverPanel);
		this.add(tabbedPanel);
	}
}
