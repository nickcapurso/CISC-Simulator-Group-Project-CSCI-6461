package edu.gwu.seas.csci;

import javax.swing.JFrame;

/**
 * Initializes system resources and runs programs.
 * 
 */
public class Computer {

	private CPU cpu = null;
	private Memory memory = Memory.getInstance();;
	private Computer_GUI gui = null;

	private Computer() {
		cpu = new CPU(memory);
		gui = new Computer_GUI(cpu, memory);
		gui.setSize(1000, 650);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setVisible(true);
	}

	public static void main(String[] args) {
		Computer computer = new Computer();
		computer.start();
	}

	private void start() {
		cpu.loadROM();
	}
}
