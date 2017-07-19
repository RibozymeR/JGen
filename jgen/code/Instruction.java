package jgen.code;

public class Instruction{
	byte opcode;
	int type;
	String arg;
	int dat0;
	public Instruction(int opcode, int type){
		this.opcode = (byte)opcode;
		this.type = type;
	}
	public Instruction(int opcode, int type, int dat0){
		this.opcode = (byte)opcode;
		this.type = type;
		this.dat0 = dat0;
	}
	public Instruction(int opcode, int type, String arg){
		this.opcode = (byte)opcode;
		this.type = type;
		this.arg = arg;
	}
	public Instruction(int opcode, int type, String arg, int dat0){
		this.opcode = (byte)opcode;
		this.type = type;
		this.arg = arg;
		this.dat0 = dat0;
	}
}