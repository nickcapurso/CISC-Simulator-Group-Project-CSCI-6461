package edu.gwu.seas.csci;

import java.util.BitSet;

import javax.swing.JFrame;

/**
 * Initializes system resources and runs programs.
 * 
 */
public class Computer {

    public static void main(String[] args) {
	// TODO Auto-generated method stub
    	//Should the GUI exist within the Computer class or exist on its own?
    	
    	//Define the GUI
    	FileLoader fileloader = new FileLoader();
    	CPU cpu = new CPU();
    	Computer_GUI frontpanel = new Computer_GUI(fileloader, cpu);
    	frontpanel.setSize(900,650);
    	frontpanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frontpanel.setVisible(true);
    	//Test for updating register
    	BitSet test = new BitSet(18);
    	System.out.println(test.size());
    	for(int i=0; i<18; i++) { 
    		if((i%2) == 0) test.set(i); 
    		} 
    	frontpanel.update_register("R0", test);
    }
}
