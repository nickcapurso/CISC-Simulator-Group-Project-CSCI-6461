package edu.gwu.seas.csci;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JTextPane;
import javax.swing.JTextField;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class Computer_GUI extends JFrame {

	/**
	 * This class defines the UI used for the simulator
	 * Attributes include:
	 *  - Textpanel for output
	 *  - Radio Button for cpu cycles
	 *  - Textfiled for user input
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JLabel r0, r1, r2, r3;
	private JTextPane terminal;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Computer_GUI frame = new Computer_GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Computer_GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 530, 381);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 325, 201);
		contentPane.add(scrollPane);
		
		terminal = new JTextPane();
		scrollPane.setViewportView(terminal);
		terminal.setEnabled(false);
		
		textField = new JTextField();
		textField.setBounds(10, 223, 179, 27);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JButton btnNewButton = new JButton("Submit Command");
		btnNewButton.setBounds(199, 225, 136, 23);
		contentPane.add(btnNewButton);
		
		JRadioButton radioButton = new JRadioButton("");
		radioButton.setEnabled(false);
		radioButton.setBounds(341, 11, 20, 20);
		contentPane.add(radioButton);
		
		JLabel lblProcessing = new JLabel("Processing");
		lblProcessing.setBounds(365, 15, 80, 14);
		contentPane.add(lblProcessing);
		
		JLabel r0_label = new JLabel("R0:");
		r0_label.setBounds(347, 26, 34, 50);
		contentPane.add(r0_label);
		
		r0 = new JLabel("0");
		r0.setBounds(375, 26, 86, 50);
		contentPane.add(r0);
		
		JLabel r1_label = new JLabel("R1:");
		r1_label.setBounds(347, 52, 34, 50);
		contentPane.add(r1_label);
		
		r1 = new JLabel("0");
		r1.setBounds(375, 52, 86, 50);
		contentPane.add(r1);
		
		JLabel r2_label = new JLabel("R2:");
		r2_label.setBounds(347, 80, 34, 50);
		contentPane.add(r2_label);
		
		r2 = new JLabel("0");
		r2.setBounds(375, 80, 86, 50);
		contentPane.add(r2);
		
		JLabel r3_label = new JLabel("R3:");
		r3_label.setBounds(347, 104, 34, 50);
		contentPane.add(r3_label);
		
		r3 = new JLabel("0");
		r3.setBounds(375, 104, 86, 50);
		contentPane.add(r3);
		
	}
	
	//By giving a string value for register, and a value, registers can be monitored
	public void update_register(String register, String value) {
		switch (register) {
			case "r0":
				r0.setText(value);
				break;
			case "r1":
				r0.setText(value);
				break;
			case "r2":
				r0.setText(value);
				break;
			case "r3":
				r0.setText(value);
				break;
			default:
				StyledDocument document = (StyledDocument) terminal.getDocument();
				try {
					document.insertString(document.getLength(), "Error Updating Board", null);
				} catch (BadLocationException e) {
					
				}
				
		}
	}
	
	public void append_to_terminal(String value) {
		StyledDocument document = (StyledDocument) terminal.getDocument();
		try {
			document.insertString(document.getLength(), value, null);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
