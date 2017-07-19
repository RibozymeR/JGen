package jgen;

import java.util.ArrayList;
import java.util.List;

import jgen.code.JCode;

public class JMethod{
	
	public static final int ACC_SYNCHRONIZED= 0x0020;
	public static final int ACC_BRIDGE		= 0x0040;
	public static final int ACC_VARARGS		= 0x0080;
	public static final int ACC_NATIVE		= 0x0100;
	public static final int ACC_STRICT		= 0x0800;
	
	int access_flags;
	String name, descriptor;
	int n_index, d_index;
	JCode code;
	byte[] codebytes;
	List<String> exceptions;
	List<Integer> exc_indices;
	
	public JMethod(String name, int access_flags, Class<?> ret_type, Class<?>... arg_types){
		this(name, JGen.getMethodDescriptor(ret_type, arg_types), access_flags);
	}
	
	public JMethod(String name, String descriptor, int access_flags){
		JGen.checkUnqual(name, true);
		this.name = name;
		this.descriptor = descriptor;
		this.access_flags = access_flags;
		exceptions = new ArrayList<>();
	}
	
	public static JMethod getConstructor(int access_flags, Class<?>... arg_types){
		return new JMethod("<init>", access_flags, void.class, arg_types);
	}
	
	public static JMethod getClassInit(int access_flags){
		return new JMethod("<clinit>", "()V", JGen.ACC_STATIC);
	}
	
	public void addException(Class<?> exception){
		if(!Exception.class.isAssignableFrom(exception)) throw new IllegalArgumentException("Class \"" + exception + "\" is not a subclass of Exception");
		addException(exception.getName().replace('.', '/'));
	}
	
	public void addException(String exception){
		JGen.checkBinary(exception);
		this.exceptions.add(exception);
	}
	
	public boolean hasCode(){
		return (access_flags & (ACC_NATIVE | JGen.ACC_ABSTRACT)) == 0;
	}
	
	public JCode getCode(){
		return code;
	}
	
	public void setCode(JCode code){
		this.code = code;
	}
	
	public String[] getExceptions(){
		return exceptions.toArray(new String[0]);
	}
}