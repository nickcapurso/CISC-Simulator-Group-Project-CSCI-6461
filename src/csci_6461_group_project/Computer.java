package csci_6461_group_project;

import javax.swing.JFrame;

/**
 * Initializes system resources and runs programs.
 * 
 * @author Nicholas Capurso
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
    }
}
