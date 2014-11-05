package edu.gwu.seas.csci;

import java.text.ParseException;

import javax.swing.JFrame;

/**
 * Initializes system resources and runs programs.
 * 
 */
public class Computer {

	private CPU cpu = null;
	private Computer_GUI gui = null;

	private Computer() throws NullPointerException, IllegalArgumentException,
			ParseException {
		cpu = CPU.getInstance();
		gui = new Computer_GUI(cpu);
		gui.setSize(1000, 650);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.pack();
		gui.setVisible(true);
	}

	public static void main(String[] args) {
		Computer computer;
		try {
			computer = new Computer();
			computer.cpu.loadROM(new InstructionLoader());
			computer.cpu.executeInstruction("continue");
		} catch (NullPointerException | IllegalArgumentException
				| ParseException e) {
			e.printStackTrace();
		}
	}
}
