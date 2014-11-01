package edu.gwu.seas.csci;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.BitSet;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

import java.awt.SystemColor;
import java.awt.Color;

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
	private static JButton cont, start, microstep, macrostep, runinput, enter, load, set_reg_mem, get_reg_mem, show_hide_dev;
	private JRadioButton[] MAR, MSR, MFR, MDR;
	private JLabel lblMsr, lblMar, lblMfr, lblMdr;
	private Boolean show_hide = true;
	private JComboBox register_list;
	private SpinnerNumberModel bit_value;
	private JPanel panel;
	private JSpinner bit_value_model, memory_address;
	private JButton reset;
	private InstructionLoader fileloader = new InstructionLoader();
	private CPU cpu;
	private JLabel opcode_name;
	private static HashMap<String, JRadioButton[]> Registers; // map of
																// registers on
																// gui

	/**
	 * Create the frame.
	 */
	public Computer_GUI(CPU cpu) {
		this.cpu = cpu;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1112, 624);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.menu);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// Registers for getting and setting register and memory
		String[] registers = { "Select Register/Memory", "Memory", "R0", "R1",
				"R2", "R3", "X1", "X2", "X3", "PC", "IR", "CC", "MAR", "MDR",
				"MSR", "MFR", "OPCODE", "I", "R", "IX", "ADDR", "EA", "OP1",
				"OP2", "OP3", "OP4", "RESULT", "RESULT2", "RX", "RY", "AL",
				"LR", "COUNT" };

		/*
		 * Textfield and Textpane for I/O
		 */
		textField = new JTextField();
		textField.setBounds(10, 522, 179, 27);
		contentPane.add(textField);
		textField.setColumns(10);

		terminal = new JTextArea();
		terminal.setEnabled(false);
		terminal.setLineWrap(true);
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

		lblMar = new JLabel("MAR:");
		lblMar.setBounds(498, 41, 41, 30);
		contentPane.add(lblMar);

		lblMsr = new JLabel("MSR:");
		lblMsr.setBounds(498, 72, 41, 30);
		contentPane.add(lblMsr);

		lblMdr = new JLabel("MDR:");
		lblMdr.setBounds(498, 103, 41, 30);
		contentPane.add(lblMdr);

		lblMfr = new JLabel("MFR:");
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
		 * Jpanel for Setting Registers and Memory - Spinner: memory_address -
		 * spinner to determine memory location to update bit_value - value to
		 * update register/memory - Buttons: set_reg_mem - sets the
		 * register/memory address - ComboBox: register_list - list of
		 * registers/memory
		 */
		panel = new JPanel();
		panel.setBackground(Color.GRAY);
		panel.setBounds(535, 310, 531, 101);
		contentPane.add(panel);
		panel.setLayout(new MigLayout("", "[50:n:50px][50px:n,center][150:n][][150px:n:150px,center][]", "[][][]"));
		
		JLabel lblSetMemoryAnd = new JLabel("Set/Get Memory and Registers");
		panel.add(lblSetMemoryAnd, "cell 0 0");

		JLabel lblSet = new JLabel("Set - ");
		panel.add(lblSet, "cell 0 1,alignx center");

		register_list = new JComboBox(registers);
		panel.add(register_list, "cell 1 1 2 1,growx");
		register_list.addActionListener(this);

		JLabel lblTo = new JLabel("- to -");
		panel.add(lblTo, "cell 2 1");

		JLabel lblTo_1 = new JLabel("- to -");
		panel.add(lblTo_1, "cell 3 1");
		
		bit_value = new SpinnerNumberModel();
		bit_value_model = new JSpinner(bit_value);
		panel.add(bit_value_model, "cell 4 1,growx");
		
		set_reg_mem = new JButton("Set");
		panel.add(set_reg_mem, "cell 5 1");
		set_reg_mem.setEnabled(false);
		set_reg_mem.addActionListener(this);

		JLabel lblAt = new JLabel("At -");
		panel.add(lblAt, "cell 0 2,alignx center");

		SpinnerModel model = new SpinnerNumberModel(0, 0, 2048, 1);
		memory_address = new JSpinner(model);
		memory_address.setSize(10, 50);
		panel.add(memory_address, "cell 1 2,growx");

		get_reg_mem = new JButton("Get");
		get_reg_mem.setEnabled(false);
		panel.add(get_reg_mem, "cell 5 2");
		get_reg_mem.addActionListener(this);
		
		show_hide_dev = new JButton("Hide Developer Console");
		show_hide_dev.setBounds(12, 272, 225, 25);
		contentPane.add(show_hide_dev);
		show_hide_dev.addActionListener(this);
		

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
		MAR = new JRadioButton[18];
		MSR = new JRadioButton[18];
		MDR = new JRadioButton[18];
		MFR = new JRadioButton[4];
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
		Registers.put("CC", CC);

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
			try {
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"TXT files", "txt");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fileloader = new InstructionLoader(chooser
							.getSelectedFile().getName());
					fileloader.load();
				} else {
					logger.debug("File failed to load or could not be found.");
				}
			} catch (Exception ex) { // Catch exception if any
				System.err.println("Error: " + ex.getMessage());
			}
			textField.setText("");
			// Needs to run through the FileLoader Instruction Parser to work
			// properly
		} else if (e.getSource() == runinput) {
			String user_input = textField.getText();
			cpu.executeInstruction(user_input);
			textField.setText("");
		} else if (e.getSource() == reset) {
			cpu.initializeProgramCounter();
			start.setEnabled(true);
			cont.setEnabled(true);
			macrostep.setEnabled(true);
			microstep.setEnabled(true);
			runinput.setEnabled(true);
			load.setEnabled(true);
		} else if (e.getSource() == enter) {
			cpu.input_buffer = textField.getText() + (char) 4;
			cpu.handleInterrupt(CPUConstants.INTERRUPT_IO);
			System.out.println(cpu.input_buffer);
			textField.setText("");
		} else if (e.getSource() == register_list) {

			String value = (String) register_list.getSelectedItem();
			if (value == "Select Register/Memory") {
				set_reg_mem.setEnabled(false);
				get_reg_mem.setEnabled(false);
			} else if (value == "Memory") {
				panel.remove(bit_value_model);
				memory_address.setEnabled(true);
				bit_value = new SpinnerNumberModel(0, 0, Math.pow(2,  18), 1);
				bit_value_model = new JSpinner(bit_value);
				panel.add(bit_value_model, "cell 4 1,growx");
				set_reg_mem.setEnabled(true);
				get_reg_mem.setEnabled(true);
				panel.revalidate();
				panel.repaint();
			} else {
				panel.remove(bit_value_model);
				memory_address.setEnabled(false);
				bit_value = new SpinnerNumberModel(0, 0, Math.pow(2, cpu.getReg(value).getNumBits()-1), 1);
				bit_value_model = new JSpinner(bit_value);
				panel.add(bit_value_model, "cell 4 1,growx");
				set_reg_mem.setEnabled(true);
				get_reg_mem.setEnabled(true);
				panel.revalidate();
				panel.repaint();
				
			}

		} else if (e.getSource() == set_reg_mem) {
			if ((String) register_list.getSelectedItem() == "Memory") {
				int memory_address_value = (int) memory_address.getValue();
				int bitset_value = clean_spinner((double) bit_value.getValue());
				BitSet bitset = Utils.intToBitSet(bitset_value, 18);
				Word word = Utils.registerToWord(bitset, 18);
				Memory.getInstance().write(word, memory_address_value);
			} else {
				int int_value = clean_spinner((double) bit_value.getValue());
				BitSet reg_val = Utils.intToBitSet(int_value, 18);
				cpu.setReg((String) register_list.getSelectedItem(), reg_val, 18);
			}
		} else if (e.getSource() == get_reg_mem) {
			if ((String) register_list.getSelectedItem() == "Memory") {
				Word word = Memory.getInstance().read(
						(Integer) memory_address.getValue());
				Computer_GUI.append_to_terminal(Utils.WordToString(word, 18)
						+ "\n");
			} else {
				Register reg = cpu.getReg((String) register_list
						.getSelectedItem());
				Computer_GUI.append_to_terminal(Utils.WordToString(reg, 18));
			}
		} else if (e.getSource() == show_hide_dev) {
			show_hide = !show_hide;
			for (int i=0; i<MAR.length; i++) {
				if (i < 4) {
					MFR[i].setVisible(show_hide);
				}
				MAR[i].setVisible(show_hide);
				MDR[i].setVisible(show_hide);
				MSR[i].setVisible(show_hide);
			}
			lblMar.setVisible(show_hide);
			lblMsr.setVisible(show_hide);
			lblMdr.setVisible(show_hide);
			lblMfr.setVisible(show_hide);
			panel.setVisible(show_hide);
			String show_or_hide = show_hide ? "Hide Developers Console" : "Show Developers Console";
			show_hide_dev.setText(show_or_hide);
		}
	}
	
	private int clean_spinner(double value) {
		return (int) value;
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
		switch (Button) {
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
