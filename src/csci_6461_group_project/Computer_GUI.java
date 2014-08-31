package csci_6461_group_project;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
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
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JTextPane textPane = new JTextPane();
		textPane.setEnabled(false);
		textPane.setBounds(10, 11, 325, 201);
		contentPane.add(textPane);
		
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
	}
}
