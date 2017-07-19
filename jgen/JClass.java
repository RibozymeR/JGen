package jgen;

import java.util.ArrayList;
import java.util.List;

import jgen.code.JCode;

public class JClass{
	
	public static final int ACC_SUPER		= 0x0020;
	public static final int ACC_INTERFACE	= 0x0200;
	public static final int ACC_ANNOTATION	= 0x2000;
	
	String this_class;
	String super_class;
	int access_flags;
	List<String> interfaces;
	List<JField> fields;
	List<JMethod> methods;
	String source_file;

	public static String getBinaryName(String name){
		return name.replace('.', '/');
	}
	
	public JClass(String name){
		this(name, Object.class);
	}
	
	public JClass(String name, Class<?> parent){
		this(name, parent, JGen.ACC_PUBLIC);
	}
	
	public JClass(String name, Class<?> parent, int flags){
		this(name, parent.getName().replace('.', '/'), flags);
	}
	
	public JClass(String name, String parent, int flags){
		JGen.checkBinary(name);
		JGen.checkBinary(parent);
		this_class = name;
		super_class = parent;
		access_flags = flags;
		interfaces = new ArrayList<>();
		fields = new ArrayList<>();
		methods = new ArrayList<>();
	}
	
	public void addInterface(Class<?> interf){
		if(!interf.isInterface()) throw new IllegalArgumentException("class \"" + interf.getName() + "\" is not an interface type");
		addInterface(interf.getName());
	}
	
	public void addInterface(String name){
		JGen.checkBinary(name);
		if(!interfaces.contains(name)) interfaces.add(name.replace('.', '/'));
	}
	
	public void addField(JField field){
		fields.add(field);
	}
	
	public void addField(String name, Class<?> type, int access_flags){
		fields.add(new JField(name, type, access_flags));
	}
	
	public void addField(String name, Class<?> type, int access_flags, Object constant_value){
		fields.add((new JField(name, type, access_flags)).setConstantValue(constant_value));
	}
	
	public void addMethod(JMethod method){
		methods.add(method);
	}
	
	/**The default constructor, including a call to the default constructor of the superclass.*/
	public JMethod getDefaultConstructor(int access_flags){
		JMethod ctor = JMethod.getConstructor(access_flags);
		JCode ctor_code = new JCode();
		ctor_code.addLine("aload_0");
		ctor_code.addLine("invokespecial " + super_class + ".<init>:()V");
		ctor_code.addLine("return");
		ctor_code.nstack = 1;
		ctor_code.nlocals = 1;
		ctor.code = ctor_code;
		return ctor;
	}
	
	public void setSourceFile(String source){
		this.source_file = source;
	}
}