package edu.gwu.seas.csci;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyledDocument;
import javax.swing.JTextPane;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.util.BitSet;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Computer_GUI extends JFrame implements ActionListener {

	/**
	 * This class defines the UI used for the simulator Attributes include: -
	 * Textpanel for output - Radio Button for cpu cycles - Textfiled for user
	 * input
	 */
	private static final long serialVersionUID = 1L;
	static final Logger logger = LogManager.getLogger(CPU.class.getName());
	private JPanel contentPane;
	private JTextField textField;
	private static JTextArea terminal;
	private static JButton cont, start, microstep, macrostep, runinput, enter, load;
	private JButton reset;
	private FileLoader fileloader = new FileLoader();
	private CPU cpu;
	private JLabel opcode_name;
	private static HashMap<String, JRadioButton[]> Registers; // map of
																// registers on
																// gui

	/**
	 * Create the frame.
	 */
	public Computer_GUI(CPU cpu1, Memory memory) {
		cpu = cpu1;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 954, 608);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		/*
		 * Textfield and Textpane for I/O
		 */
		textField = new JTextField();
		textField.setBounds(10, 522, 179, 27);
		contentPane.add(textField);
		textField.setColumns(10);

		terminal = new JTextArea();
		terminal.setEnabled(false);
		JScrollPane scroll = new JScrollPane (terminal, 
				   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		DefaultCaret caret = (DefaultCaret)terminal.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scroll.setBounds(12, 310, 402, 199);
		contentPane.add(scroll);

		/*
		 * JButtons for user interaction - Load: load a textfile through
		 * relative path - Start: start the loaded program (will run default on
		 * initialization) - btnStep: Take a Macro Step in the Program -
		 * btnStepInto: Take a Micro Step in the Program - btnContinue: Run the
		 * rest of the Program
		 */
		start = new JButton("Start");
		start.setBounds(426, 462, 97, 25);
		contentPane.add(start);
		start.addActionListener(this);

		load = new JButton("Load");
		load.setBounds(201, 523, 97, 25);
		contentPane.add(load);
		load.addActionListener(this);

		macrostep = new JButton("Macro Step");
		macrostep.setBounds(426, 310, 97, 25);
		contentPane.add(macrostep);
		macrostep.addActionListener(this);

		microstep = new JButton("Micro Step");
		microstep.setBounds(426, 348, 97, 25);
		contentPane.add(microstep);
		microstep.addActionListener(this);

		runinput = new JButton("Run Input");
		runinput.setBounds(310, 523, 97, 25);
		contentPane.add(runinput);
		runinput.addActionListener(this);

		reset = new JButton("Reset");
		reset.setBounds(426, 424, 97, 25);
		contentPane.add(reset);
		reset.addActionListener(this);

		cont = new JButton("Continue");
		cont.setBounds(426, 386, 97, 25);
		contentPane.add(cont);
		cont.addActionListener(this);
		
		enter = new JButton("Enter");
		enter.setBounds(426, 523, 97, 25);
		contentPane.add(enter);
		enter.addActionListener(this);

		/*
		 * Register Label Creation - If there is time should create in a loop
		 */
		JLabel r0_lbl = new JLabel("R0:");
		r0_lbl.setBounds(10, 10, 25, 30);
		contentPane.add(r0_lbl);

		JLabel R1_lbl = new JLabel("R1:");
		R1_lbl.setBounds(10, 41, 25, 30);
		contentPane.add(R1_lbl);

		JLabel R2_lbl = new JLabel("R2:");
		R2_lbl.setBounds(10, 72, 25, 30);
		contentPane.add(R2_lbl);

		JLabel R3_lbl = new JLabel("R3:");
		R3_lbl.setBounds(10, 103, 25, 30);
		contentPane.add(R3_lbl);

		JLabel X1_lbl = new JLabel("X1:");
		X1_lbl.setBounds(10, 134, 25, 30);
		contentPane.add(X1_lbl);

		JLabel X2_lbl = new JLabel("X2:");
		X2_lbl.setBounds(10, 165, 25, 30);
		contentPane.add(X2_lbl);

		JLabel X3_lbl = new JLabel("X3:");
		X3_lbl.setBounds(10, 196, 25, 30);
		contentPane.add(X3_lbl);

		JLabel lblPc = new JLabel("PC:");
		lblPc.setBounds(498, 10, 25, 30);
		contentPane.add(lblPc);

		JLabel lblMar = new JLabel("MAR:");
		lblMar.setBounds(498, 41, 41, 30);
		contentPane.add(lblMar);

		JLabel lblMsr = new JLabel("MSR:");
		lblMsr.setBounds(498, 72, 41, 30);
		contentPane.add(lblMsr);

		JLabel lblMdr = new JLabel("MDR:");
		lblMdr.setBounds(498, 103, 41, 30);
		contentPane.add(lblMdr);

		JLabel lblMfr = new JLabel("MFR:");
		lblMfr.setBounds(498, 134, 41, 30);
		contentPane.add(lblMfr);
		
		JLabel lblCc = new JLabel("CC:");
		lblCc.setBounds(498, 165, 41, 30);
		contentPane.add(lblCc);

		JLabel lblIr = new JLabel("OPCODE:");
		lblIr.setBounds(498, 202, 56, 16);
		contentPane.add(lblIr);

		opcode_name = new JLabel("");
		opcode_name.setBounds(730, 172, 56, 16);
		contentPane.add(opcode_name);

		/*
		 * Create a map of all registers used on GUI - Registers are stored as
		 * RadioButton [] to represent array of bitvalues - By setting certain
		 * RadioButtons to 0/1 we can visually represent values in Registers
		 */
		Registers = new HashMap<String, JRadioButton[]>();
		JRadioButton[] R0 = new JRadioButton[18];
		JRadioButton[] R1 = new JRadioButton[18];
		JRadioButton[] R2 = new JRadioButton[18];
		JRadioButton[] R3 = new JRadioButton[18];
		JRadioButton[][] GPR = { R0, R1, R2, R3 };
		JRadioButton[] X1 = new JRadioButton[18];
		JRadioButton[] X2 = new JRadioButton[18];
		JRadioButton[] X3 = new JRadioButton[18];
		JRadioButton[][] XR = { X1, X2, X3 };
		JRadioButton[] PC = new JRadioButton[12];
		JRadioButton[] MAR = new JRadioButton[18];
		JRadioButton[] MSR = new JRadioButton[18];
		JRadioButton[] MDR = new JRadioButton[18];
		JRadioButton[] MFR = new JRadioButton[4];
		JRadioButton[] CC = new JRadioButton[4];
		JRadioButton[][] MR = { PC, MAR, MSR, MDR, MFR, CC };

		// For loop to decrease RadioButton [] creation
		for (int j = 0; j < MR.length; j++) {
			for (int i = 0; i < R0.length; i++) {
				if (j < GPR.length) {
					GPR[j][i] = new JRadioButton();
					GPR[j][i].setEnabled(false);
					GPR[j][i].setBounds(35 + 24 * i, 15 + 31 * j, 20, 20);
					contentPane.add(GPR[j][i]);
				}
				if (j < XR.length) {
					XR[j][i] = new JRadioButton();
					XR[j][i].setEnabled(false);
					XR[j][i].setBounds(35 + 24 * i, 139 + 31 * j, 20, 20);
					contentPane.add(XR[j][i]);
				}
				if (j == MR.length - 1 || j == MR.length - 2) {
					if (i < MFR.length || i < CC.length) {
						MR[j][i] = new JRadioButton();
						MR[j][i].setEnabled(false);
						MR[j][i].setBounds(547 + 24 * i, 15 + 31 * j, 20, 20);
						contentPane.add(MR[j][i]);
					}
				} else if (j == 0) {
					if (i < PC.length) {
						MR[j][i] = new JRadioButton();
						MR[j][i].setEnabled(false);
						MR[j][i].setBounds(547 + 24 * i, 15 + 31 * j, 20, 20);
						contentPane.add(MR[j][i]);
					}
				} else {
					MR[j][i] = new JRadioButton();
					MR[j][i].setEnabled(false);
					MR[j][i].setBounds(547 + 24 * i, 15 + 31 * j, 20, 20);
					contentPane.add(MR[j][i]);
				}
			}
		}

		JRadioButton[] IR = new JRadioButton[6];
		for (int i = 0; i < 6; i++) {
			IR[i] = new JRadioButton();
			IR[i].setEnabled(false);
			IR[i].setBounds(547 + 24 * i, 201, 20, 20);
			contentPane.add(IR[i]);
		}
		/*
		 * Place registers in the map (Registers)
		 */
		Registers.put("R0", R0);
		Registers.put("R1", R1);
		Registers.put("R2", R2);
		Registers.put("R3", R3);
		Registers.put("X1", X1);
		Registers.put("X2", X2);
		Registers.put("X3", X3);
		Registers.put("PC", PC);
		Registers.put("MSR", MSR);
		Registers.put("MDR", MDR);
		Registers.put("MFR", MFR);
		Registers.put("MAR", MAR);
		Registers.put("IR", IR);
		Registers.put("CC",	CC);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == start || e.getSource() == cont) {
			cpu.executeInstruction("continue");
		} else if (e.getSource() == microstep) {
			cpu.executeInstruction("micro step");
		} else if (e.getSource() == macrostep) {
			cpu.executeInstruction("macro step");
		} else if (e.getSource() == load) {
			    JFileChooser chooser = new JFileChooser();
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "Text Files", "txt");
			    chooser.setFileFilter(filter);
			    int returnVal = chooser.showOpenDialog(this);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
					fileloader.Load_File("\\" + chooser.getSelectedFile().getName());
					try {
						fileloader.load();
					} catch (NullPointerException | IllegalArgumentException
							| ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			    } else {
			    	logger.debug("File not selected or could not be loaded");
			    }
			// Needs to run through the FileLoader Instruction Parser to work
			// properly
		} else if (e.getSource() == runinput) {
			String user_input = textField.getText();
			cpu.executeInstruction(user_input);
			textField.setText("");
		} else if (e.getSource() == reset) {
			cpu.startBootloader();
			start.setEnabled(true);
			cont.setEnabled(true);
			macrostep.setEnabled(true);
			microstep.setEnabled(true);
			runinput.setEnabled(true);
			load.setEnabled(true);
		} else if (e.getSource() == enter) {
			cpu.input_buffer = textField.getText();
			cpu.handleInterrupt(CPUConstants.INTERRUPT_IO);
			System.out.println(cpu.input_buffer);
			textField.setText("");
		}
	}

	// By giving a string value for register, and a value, registers can be
	// monitored
	public static void update_register(String register, BitSet value) {
		try {
			JRadioButton[] curr_reg = Registers.get(register);
			for (int i = 0; i < curr_reg.length; i++) {
				if (value.get(i)) {
					curr_reg[i].setSelected(true);
				} else {
					curr_reg[i].setSelected(false);
				}
			}
		} catch (Exception e) {
			/*
			 * StyledDocument document = (StyledDocument)
			 * terminal.getDocument(); try {
			 * document.insertString(document.getLength(),
			 * "Error Updating Board\n", null); } catch (BadLocationException
			 * e2) { }
			 */
		}
	}

	public static void disable_btns() {
		start.setEnabled(false);
		cont.setEnabled(false);
		macrostep.setEnabled(false);
		microstep.setEnabled(false);
	}
	
	public static void toggle_button(String Button, Boolean toggle) {
		switch(Button) {
		case "runinput":
			runinput.setEnabled(toggle);
			break;
			
		case "load":
			load.setEnabled(toggle);
			break;
		}
	}

	public static void append_to_terminal(String value) {
		terminal.append(value);
	}
}
