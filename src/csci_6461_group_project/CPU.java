package csci_6461_group_project;

import java.util.BitSet;

/*Still need to add additional comments describing class!!!!!! */

public class CPU extends Computer {
	
	//4 General Purpose Registers(GPRs)
	private BitSet R0 = new BitSet(18);
	private BitSet R1 = new BitSet(18);
	private BitSet R2 = new BitSet(18);
	private BitSet R3 = new BitSet(18);
	
	//3 Index Registers
	private BitSet X1 = new BitSet(12);
	private BitSet X2 = new BitSet(12);
	private BitSet X3 = new BitSet(12);
	
	//Special registers
	private BitSet PC = new BitSet(12);
	private BitSet CC = new BitSet(4);
	private BitSet IR = new BitSet(18);
	private BitSet MAR = new BitSet(12);
	private BitSet MBR = new BitSet(18);
	private BitSet MSR = new BitSet(18);
	private BitSet MFR = new BitSet(4);
	
	//Other registers we deemed necessary to add
	
	/*NOTE: For this commit I did not implement reg methods as we discussed as a group. 
	Going to think more about how to best implement it tomorrow. However, after doing a little research it seems like having many
	getter/setter methods is an acceptable practice (even though it is overly verbose).  While creating a wrapper making use of a map is possible,
	the implementation is a little messier and makes it a little harder for CPU class users.  Also...learned that eclipse has wizard for
	auto-generating getters and setters...which makes creation and edits a lot easier. 
	*/
	
	//Getter and Setter methods for registers
	public BitSet getR0() {
		return R0;
	}
	public void setR0(BitSet r0) {
		R0 = r0;
	}
	public BitSet getR1() {
		return R1;
	}
	public void setR1(BitSet r1) {
		R1 = r1;
	}
	public BitSet getR2() {
		return R2;
	}
	public void setR2(BitSet r2) {
		R2 = r2;
	}
	public BitSet getR3() {
		return R3;
	}
	public void setR3(BitSet r3) {
		R3 = r3;
	}
	public BitSet getX1() {
		return X1;
	}
	public void setX1(BitSet x1) {
		X1 = x1;
	}
	public BitSet getX2() {
		return X2;
	}
	public void setX2(BitSet x2) {
		X2 = x2;
	}
	public BitSet getX3() {
		return X3;
	}
	public void setX3(BitSet x3) {
		X3 = x3;
	}
	public BitSet getPC() {
		return PC;
	}
	public void setPC(BitSet pC) {
		PC = pC;
	}
	public BitSet getCC() {
		return CC;
	}
	public void setCC(BitSet cC) {
		CC = cC;
	}
	public BitSet getIR() {
		return IR;
	}
	public void setIR(BitSet iR) {
		IR = iR;
	}
	public BitSet getMAR() {
		return MAR;
	}
	public void setMAR(BitSet mAR) {
		MAR = mAR;
	}
	public BitSet getMBR() {
		return MBR;
	}
	public void setMBR(BitSet mBR) {
		MBR = mBR;
	}
	public BitSet getMSR() {
		return MSR;
	}
	public void setMSR(BitSet mSR) {
		MSR = mSR;
	}
	public BitSet getMFR() {
		return MFR;
	}
	public void setMFR(BitSet mFR) {
		MFR = mFR;
	}
	
}
