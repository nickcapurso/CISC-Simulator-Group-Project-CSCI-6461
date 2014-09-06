package edu.gwu.seas.csci;

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
    	Computer_GUI frontpanel = new Computer_GUI();
    	frontpanel.setSize(500,300);
    	frontpanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frontpanel.setVisible(true);
    	//added method update_register to monitor values currently stored in registers
    	frontpanel.update_register("r0", "5");
    	//added method to log processes to terminal
		for (int i=0; i<100; i++) {
			frontpanel.append_to_terminal("This is line " + i + "\n");
		}
    	
    }
}
