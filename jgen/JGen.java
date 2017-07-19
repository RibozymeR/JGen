package jgen;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JGen{
	
	public static final int ACC_PUBLIC		= 0x0001;
	public static final int ACC_PRIVATE		= 0x0002;
	public static final int ACC_PROTECTED	= 0x0004;
	public static final int ACC_STATIC		= 0x0008;
	public static final int ACC_FINAL		= 0x0010;
	/**Do not use for fields!*/
	public static final int ACC_ABSTRACT	= 0x0400;
	public static final int ACC_SYNTHETIC	= 0x1000;
	/**Do not use for methods!*/
	public static final int ACC_ENUM		= 0x4000;
	
	private static final char[] forbidden = new char[] {'.', ';', '[', '/'};
	
	private static final Map<Class<?>, Character> primitives = new HashMap<>();
	static{
		primitives.put(boolean.class, 'Z');
		primitives.put(byte.class, 'B');
		primitives.put(char.class, 'C');
		primitives.put(double.class, 'D');
		primitives.put(float.class, 'F');
		primitives.put(int.class, 'I');
		primitives.put(long.class, 'J');
		primitives.put(short.class, 'S');
	}
	
	private static boolean contains(String str, char... cs){
		Arrays.sort(cs);
		for(char c: str.toCharArray()){
			if(Arrays.binarySearch(cs, c) >= 0) return true;
		}
		return false;
	}
	
	public static boolean isUnqualName(String name, boolean method){
		if(name.isEmpty()) return false;
		if(name.equals("<init>") || name.equals("<clinit>")) return true;
		return !contains(name, forbidden) && !(method && contains(name, '<', '>'));
	}
	
	public static boolean isBinaryName(String name){
		if(name.contains("//")) return false;
		for(String s: name.split("/")){
			if(contains(s, forbidden)) return false;
		}
		return true;
	}
	
	public static String getBinaryName(Class<?> clazz){
		if(clazz.isArray() || clazz.isPrimitive()) throw new ClassCastException("array or primitive type" + clazz + " doesn't have a binary name");
		return clazz.getName().replace('.', '/');
	}
	
	public static String getDescriptor(Class<?> clazz){
		if(clazz.isArray()){
			return "[" +  getDescriptor(clazz.getComponentType());
		}
		if(clazz.isPrimitive()){
			return Character.toString(primitives.get(clazz));
		}
		return "L" + clazz.getName().replace('.', '/') + ";";
	}
	
	public static String getMethodDescriptor(Class<?> ret, Class<?>... args){
		StringBuilder desc = new StringBuilder();
		desc.append('(');
		for(Class<?> arg: args) desc.append(getDescriptor(arg));
		desc.append(')');
		if(ret == void.class) desc.append('V');
		else desc.append(getDescriptor(ret));
		return desc.toString();
	}
	
	public static void checkUnqual(String name, boolean method){
		if(!isUnqualName(name, method)) throw new IllegalArgumentException("\"" + name + "\" is not a valid unqualified name");
	}
	
	public static void checkBinary(String name){
		if(!isBinaryName(name)) throw new IllegalArgumentException("\"" + name + "\" is not a valid unqualified name");
	}
	
	public static byte[] getStrData(String s){
		byte[] str = s.getBytes(Charset.forName("UTF-8"));
		int l = str.length;
		byte[] data = new byte[l + 2];
		data[0] = (byte)(l >> 8);
		data[1] = (byte)l;
		System.arraycopy(str, 0, data, 2, l);
		return data;
	}
	
	public static void write(JClass clazz, String dir) throws IOException{
		write(clazz, Paths.get(dir));
	}
	
	public static void write(JClass clazz, Path dir) throws IOException{
		JGenWriter writer = new JGenWriter(clazz, new DataOutputStream(Files.newOutputStream(dir.resolve(clazz.this_class + ".class"))));
		writer.write();
		writer.close();
	}
	
	public static JClass read(String class_file) throws IOException{
		return read(Paths.get(class_file));
	}
	
	public static JClass read(Path class_file) throws IOException{
		JGenReader reader = new JGenReader(new DataInputStream(Files.newInputStream(class_file)));
		reader.read();
		reader.close();
		return reader.clazz;
	}
}