package jgen;

import java.util.HashMap;
import java.util.Map;

public class JField{
	
	public static final int ACC_VOLATILE	= 0x0040;
	public static final int ACC_TRANSIENT	= 0x0080;
	
	static final Map<Class<?>, String> wrappers = new HashMap<>();
	static{
		wrappers.put(Boolean.class, "Z");
		wrappers.put(Byte.class, "B");
		wrappers.put(Character.class, "C");
		wrappers.put(Short.class, "S");
		wrappers.put(Integer.class, "I");
		wrappers.put(Long.class, "J");
		wrappers.put(Float.class, "F");
		wrappers.put(Double.class, "D");
		wrappers.put(String.class, "Ljava/lang/String;");
	}
	
	int access_flags;
	String name, descriptor;
	int n_index, d_index;
	Object constant_value;
	int c_index;
	
	public JField(String name, Class<?> type, int access_flags){
		this(name, JGen.getDescriptor(type), access_flags);
	}
	
	public JField(String name, String descriptor, int access_flags){
		JGen.checkUnqual(name, false);
		this.name = name;
		this.descriptor = descriptor;
		this.access_flags = access_flags;
		constant_value = null;
	}
	
	public JField setConstantValue(Object o){
		if(descriptor.length() > 1 && !descriptor.equals("Ljava/lang/String;")){
			throw new IllegalArgumentException("descriptor of field \"" + name + "\" does not allow a constant value");
		}
		if(o != null){
			if(!(o instanceof Number) && !(o instanceof String)){
				throw new IllegalArgumentException("class \"" + o.getClass() + "\" does not allow use as constant value");
			}
			if(!wrappers.get(o.getClass()).equals(descriptor)){
				throw new IllegalArgumentException("descriptor \"" + descriptor + "\" of field \"" + name + "\" does not match argument type \"" + o.getClass() + "\"");
			}
			if(o instanceof Short || o instanceof Byte || o instanceof Character){
				o = new Integer(((Number)o).intValue());
			}
		}
		constant_value = o;
		return this;
	}
}