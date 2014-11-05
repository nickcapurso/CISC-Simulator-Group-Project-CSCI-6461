package edu.gwu.seas.csci;

import javax.swing.JFrame;
import javax.swing.JPanel;
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
	private static JButton cont, start, microstep, macrostep, runinput, enter,
			load, set_reg_mem, get_reg_mem, show_hide_dev;
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
	private static HashMap<String, JRadioButton[]> Registers; // map of
																// registers on
																// gui
	private HashMap<String, String> exec_command;

	/**
	 * Create the frame.
	 */
	public Computer_GUI(CPU cpu) {
		this.cpu = cpu;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		setBounds(100, 100, 1049, 547);
		contentPane.setBackground(SystemColor.menu);
		setContentPane(contentPane);
		init_exec_command_map();

		// Registers for getting and setting register and memory
		String[] registers = { "Select Register/Memory", "Memory", "R0", "R1",
				"R2", "R3", "X1", "X2", "X3", "PC", "IR", "CC", "MAR", "MDR",
				"MSR", "MFR", "OPCODE", "I", "R", "IX", "ADDR", "EA", "OP1",
				"OP2", "OP3", "OP4", "RESULT", "RESULT2", "RX", "RY", "AL",
				"LR", "COUNT" };
		contentPane
				.setLayout(new MigLayout(
						"",
						"[250px:n:250px][125px:n][125px:n][97px][12px][19px][176px]",
						"[30px][30px][30px][30px][30px][30px][30px][25px][25px][13px][25px][13px][25px][13px][25px][13px][47px][27px]"));

		textField = new JTextField();
		contentPane.add(textField, "cell 0 17,grow");
		textField.setColumns(10);

		terminal = new JTextArea();
		terminal.setEnabled(false);
		terminal.setLineWrap(true);
		DefaultCaret caret = (DefaultCaret) terminal.getCaret();
		JScrollPane scroll = new JScrollPane(terminal,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		contentPane.add(scroll, "cell 0 8 3 9, grow");

		/*
		 * JButtons for user interaction - Load: load a textfile through
		 * relative path - Start: start the loaded program (will run default on
		 * initialization) - btnStep: Take a Macro Step in the Program -
		 * btnStepInto: Take a Micro Step in the Program - btnContinue: Run the
		 * rest of the Program
		 */
		start = new JButton("Start");
		contentPane.add(start, "cell 3 16,growx,aligny top");
		start.addActionListener(this);

		load = new JButton("Load");
		contentPane.add(load, "cell 1 17,grow");

		macrostep = new JButton("Macro Step");
		contentPane.add(macrostep, "cell 3 8,alignx left,aligny top");
		macrostep.addActionListener(this);

		microstep = new JButton("Micro Step");
		contentPane.add(microstep, "cell 3 10,growx,aligny top");
		microstep.addActionListener(this);

		runinput = new JButton("Run Input");
		contentPane.add(runinput, "cell 2 17,grow");

		reset = new JButton("Reset");
		contentPane.add(reset, "cell 3 14,growx,aligny top");

		cont = new JButton("Continue");
		contentPane.add(cont, "cell 3 12,growx,aligny top");
		cont.addActionListener(this);

		enter = new JButton("Enter");
		contentPane.add(enter, "cell 3 17,growx,aligny center");

		/*
		 * Register Label Creation - If there is time should create in a loop
		 */
		JLabel r0_lbl = new JLabel("R0:");
		contentPane.add(r0_lbl, "cell 0 0,alignx left,growy");

		JLabel R1_lbl = new JLabel("R1:");
		contentPane.add(R1_lbl, "cell 0 1,alignx left,growy");

		JLabel R2_lbl = new JLabel("R2:");
		contentPane.add(R2_lbl, "cell 0 2,alignx left,growy");

		JLabel R3_lbl = new JLabel("R3:");
		contentPane.add(R3_lbl, "cell 0 3,alignx left,growy");

		JLabel X1_lbl = new JLabel("X1:");
		contentPane.add(X1_lbl, "cell 0 4,alignx left,growy");

		JLabel X2_lbl = new JLabel("X2:");
		contentPane.add(X2_lbl, "cell 0 5,alignx left,growy");

		JLabel X3_lbl = new JLabel("X3:");
		contentPane.add(X3_lbl, "cell 0 6,alignx left,growy");

		JLabel lblPc = new JLabel("PC:");
		contentPane.add(lblPc, "cell 3 0 3 1,alignx right,growy");

		lblMar = new JLabel("MAR:");
		contentPane.add(lblMar, "cell 3 1 3 1,alignx right,growy");

		lblMsr = new JLabel("MSR:");
		contentPane.add(lblMsr, "cell 3 2 3 1,alignx right,growy");

		lblMdr = new JLabel("MDR:");
		contentPane.add(lblMdr, "cell 3 3 3 1,alignx right,growy");

		lblMfr = new JLabel("MFR:");
		contentPane.add(lblMfr, "cell 3 4 3 1,alignx right,growy");

		JLabel lblCc = new JLabel("CC:");
		contentPane.add(lblCc, "cell 3 5 3 1,alignx right,growy");

		JLabel lblIr = new JLabel("OPCODE:");
		contentPane.add(lblIr, "cell 3 6 3 1,alignx right,aligny center");

		/*
		 * Jpanel for Setting Registers and Memory - Spinner: memory_address -
		 * spinner to determine memory location to update bit_value - value to
		 * update register/memory - Buttons: set_reg_mem - sets the
		 * register/memory address - ComboBox: register_list - list of
		 * registers/memory
		 */
		panel = new JPanel();
		panel.setBackground(Color.GRAY);
		contentPane.add(panel, "cell 5 8 2 5,grow");
		panel.setLayout(new MigLayout("",
				"[50:n:50px][50px:n,center][150:n][][150px:n:150px,center][]",
				"[][][]"));

		JLabel lblSetMemoryAnd = new JLabel("Set/Get Memory and Registers");
		panel.add(lblSetMemoryAnd, "cell 0 0");

		JLabel lblSet = new JLabel("Set - ");
		panel.add(lblSet, "cell 0 1,alignx center");

		register_list = new JComboBox(registers);
		panel.add(register_list, "cell 1 1 2 1,growx");

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

		JLabel lblAt = new JLabel("At -");
		panel.add(lblAt, "cell 0 2,alignx center");

		SpinnerModel model = new SpinnerNumberModel(0, 0, 2048, 1);
		memory_address = new JSpinner(model);
		memory_address.setSize(10, 50);
		panel.add(memory_address, "cell 1 2,growx");

		get_reg_mem = new JButton("Get");
		get_reg_mem.setEnabled(false);
		panel.add(get_reg_mem, "cell 5 2");

		show_hide_dev = new JButton("Hide Developer Console");
		contentPane.add(show_hide_dev, "cell 0 7 2 1,alignx left,aligny top");

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
					String location = "cell 0 " + j;
					contentPane.add(GPR[j][i], location);
				}
				if (j < XR.length) {
					XR[j][i] = new JRadioButton();
					XR[j][i].setEnabled(false);
					String location = "cell 0 " + (j + 4);
					contentPane.add(XR[j][i], location);
				}
				if (j == MR.length - 1 || j == MR.length - 2) {
					if (i < MFR.length || i < CC.length) {
						MR[j][i] = new JRadioButton();
						MR[j][i].setEnabled(false);
						String location = "cell 6 " + (j);
						contentPane.add(MR[j][i], location);
					}
				} else if (j == 0) {
					if (i < PC.length) {
						MR[j][i] = new JRadioButton();
						MR[j][i].setEnabled(false);
						String location = "cell 6 " + (j);
						contentPane.add(MR[j][i], location);
					}
				} else {
					MR[j][i] = new JRadioButton();
					MR[j][i].setEnabled(false);
					String location = "cell 6 " + (j);
					contentPane.add(MR[j][i], location);
				}
			}
		}

		JRadioButton[] IR = new JRadioButton[6];
		for (int i = 0; i < 6; i++) {
			IR[i] = new JRadioButton();
			IR[i].setEnabled(false);
			String location = "cell 6 6";
			contentPane.add(IR[i], location);
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

		// Action Listeners for complex logic
		register_list.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String value = (String) register_list.getSelectedItem();
				if (value == "Select Register/Memory") {
					set_reg_mem.setEnabled(false);
					get_reg_mem.setEnabled(false);
				} else if (value == "Memory") {
					panel.remove(bit_value_model);
					memory_address.setEnabled(true);
					bit_value = new SpinnerNumberModel(0, 0, Math.pow(2, 18), 1);
					bit_value_model = new JSpinner(bit_value);
					panel.add(bit_value_model, "cell 4 1,growx");
					set_reg_mem.setEnabled(true);
					get_reg_mem.setEnabled(true);
					panel.revalidate();
					panel.repaint();
				} else {
					panel.remove(bit_value_model);
					memory_address.setEnabled(false);
					bit_value = new SpinnerNumberModel(0, 0, Math.pow(2, cpu
							.getReg(value).getNumBits() - 1), 1);
					bit_value_model = new JSpinner(bit_value);
					panel.add(bit_value_model, "cell 4 1,growx");
					set_reg_mem.setEnabled(true);
					get_reg_mem.setEnabled(true);
					panel.revalidate();
					panel.repaint();

				}
			}
		});

		load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser chooser = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
							"TXT files", "txt");
					chooser.setFileFilter(filter);
					int returnVal = chooser.showOpenDialog(load);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						String fully_qualified_file_name = chooser
								.getSelectedFile().getAbsolutePath();
						fileloader = new InstructionLoader(
								fully_qualified_file_name, true);
						fileloader.load();
					} else {
						logger.debug("File failed to load or could not be found.");
					}
				} catch (Exception ex) { // Catch exception if any
					System.err.println("Error: " + ex.getMessage());
				}
				textField.setText("");
				// Needs to run through the FileLoader Instruction Parser to
				// work
				// properly
				start.setEnabled(true);
				cont.setEnabled(true);
				macrostep.setEnabled(true);
				microstep.setEnabled(true);
				runinput.setEnabled(true);
				load.setEnabled(true);
			}
		});

		get_reg_mem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((String) register_list.getSelectedItem() == "Memory") {
					Word word = Memory.getInstance().read(
							(Integer) memory_address.getValue());
					Computer_GUI.append_to_terminal(Utils
							.WordToString(word, 18) + "\n");
				} else {
					Register reg = cpu.getReg((String) register_list
							.getSelectedItem());
					Computer_GUI.append_to_terminal(Utils.WordToString(reg, 18)
							+ "\n");
				}
			}
		});

		set_reg_mem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((String) register_list.getSelectedItem() == "Memory") {
					int memory_address_value = (int) memory_address.getValue();
					int bitset_value = clean_spinner((double) bit_value
							.getValue());
					BitSet bitset = Utils.intToBitSet(bitset_value, 18);
					Word word = Utils.registerToWord(bitset, 18);
					Memory.getInstance().write(word, memory_address_value);
				} else {
					int int_value = clean_spinner((double) bit_value.getValue());
					BitSet reg_val = Utils.intToBitSet(int_value, 18);
					cpu.setReg((String) register_list.getSelectedItem(),
							reg_val, 18);
				}
			}
		});

		show_hide_dev.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				show_hide = !show_hide;
				for (int i = 0; i < MAR.length; i++) {
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
				String show_or_hide = show_hide ? "Hide Developers Console"
						: "Show Developers Console";
				show_hide_dev.setText(show_or_hide);
			}
		});

		enter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				cpu.input_buffer = textField.getText() + (char) 4;
				cpu.handleInterrupt(CPUConstants.INTERRUPT_IO);
				System.out.println(cpu.input_buffer);
				textField.setText("");
			}
		});

		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cpu.initializeProgramCounter(0);
				start.setEnabled(true);
				cont.setEnabled(true);
				macrostep.setEnabled(true);
				microstep.setEnabled(true);
				runinput.setEnabled(true);
				load.setEnabled(true);
			}
		});

		runinput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String user_input = textField.getText();
				cpu.executeInstruction(user_input);
				textField.setText("");
			}
		});
	}

	// Run execution commands
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton command = (JButton) e.getSource();
		cpu.executeInstruction(exec_command.get(command.getText()));
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

	public void init_exec_command_map() {
		exec_command = new HashMap<String, String>();
		exec_command.put("Start", "continue");
		exec_command.put("Continue", "continue");
		exec_command.put("Micro Step", "micro step");
		exec_command.put("Macro Step", "macro step");
		return;
	}
}
