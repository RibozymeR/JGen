package jgen;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;

import jgen.code.JCode;

public class JGenReader implements Closeable{

	JClass clazz;
	DataInputStream in;
	
	JGenReader(DataInputStream in){
		this.in = in;
	}
	
	private int r2() throws IOException{
		return in.readUnsignedShort();
	}
	
	private int r4() throws IOException{
		return r2() << 16 | r2();
	}
	
	ConstantPool pool = new ConstantPool();
	
	public void readConstantPool() throws IOException{
		int pool_size = r2();
		for(int i = 0; i < pool_size; ++i){
			int tag = r2();
			JPoolEntry entry = new JPoolEntry(tag);
			switch(tag){
			case 1: entry.data = in.readUTF().getBytes(); break;
			case 7: case 8:
				entry.data = new byte[] {in.readByte(), in.readByte()}; break;
			case 3: case 4: case 9: case 10: case 11: case 12:
				entry.data = new byte[] {in.readByte(), in.readByte(), in.readByte(), in.readByte()}; break;
			case 5: case 6:
				entry.data = new byte[] {in.readByte(), in.readByte(), in.readByte(), in.readByte(), in.readByte(), in.readByte(), in.readByte(), in.readByte()}; break;
			}
			pool.add(entry);
			if(tag == 5 || tag == 6){
				pool.add(null);
				++i;
			}
		}
	}
	
	public void readFields() throws IOException{
		int count = r2();
		for(int i = 0; i < count; ++i){
			int access_flags = r2();
			JField field = new JField(pool.getString(r2()), pool.getString(r2()), access_flags);
			int acount = r2();
			for(int j = 0; j < acount; ++j){
				String aname = pool.getString(r2());
				int alen = r4();
				byte[] adata = new byte[alen];
				in.read(adata);
				if(aname.equals("ConstantValue")){
					if(alen != 2) throw new IOException("wrong length for argument ConstantValue in Field " + field.name);
					field.c_index = adata[0] << 8 | adata[1];
				}
			}
			clazz.addField(field);
		}
		for(JField field: clazz.fields){
			if(field.c_index < 1) continue;
			JPoolEntry constant = pool.pool.get(field.c_index);
			byte[] cdata = constant.data;
			int ctag = constant.tag;
			switch(ctag){
			case 8:
				field.constant_value = pool.getString(cdata[0] << 8 | cdata[1]); break;
			case 3: case 4:
				int n = cdata[0] << 24 | cdata[1] << 16 | cdata[2] << 8 | cdata[3];
				field.constant_value = ctag == 3 ? n : Float.intBitsToFloat(n); break;
			case 5: case 6:
				long l = cdata[0] << 56 | cdata[1] << 48 | cdata[2] << 40 | cdata[3] << 32 | cdata[4] << 24 | cdata[5] << 16 | cdata[6] << 8 | cdata[7];
				field.constant_value = ctag == 5 ? l : Double.longBitsToDouble(l); break;
			}
		}
	}
	
	public void readInterfaces() throws IOException{
		int size = r2();
		for(int i = 0; i < size; ++i) clazz.addInterface(pool.getClass(r2()));
	}
	
	public void readCode(JMethod method) throws IOException{
		JCode code = new JCode();
		code.nstack = r2();
		code.nlocals = r2();
		method.codebytes = new byte[r4()];
		in.read(method.codebytes);
		//TODO exceptions
		/*int exc_count = */r2();
		//TODO code attributes LocalVariableTable, LocalVariableTypeTable
		int acount = r2();
		for(int j = 0; j < acount; ++j){
			String aname = pool.getString(r2());
			int alen = r4();
			if(aname.equals("LineNumberTable")){
				code.makeLineNumbers(true);
				int lncount = r2();
				for(int i = 0; i < lncount; ++i){
					code.line_numbers.add(r2());
					/*line number = */r2();
				}
			}
			else in.skipBytes(alen);
		}
		//disassemble code
		method.setCode(code);
	}
	
	public void readMethods() throws IOException{
		int count = r2();
		for(int i = 0; i < count; ++i){
			int access_flags = r2();
			JMethod method = new JMethod(pool.getString(r2()), pool.getString(r2()), access_flags);
			//TODO method attribute Annotations
			int acount = r2();
			for(int j = 0; j < acount; ++j){
				String aname = pool.getString(r2());
				int alen = r4();
				if(aname.equals("Code")){
					if(alen != 2) throw new IOException("wrong length for attribute Code in method " + method.name);
					readCode(method);
				}
				else if(aname.equals("Exceptions")){
					int n = r2();
					for(int k = 0; k < n; ++k) method.addException(pool.getClass(r2()));
				}
				else in.skipBytes(alen);
			}
			clazz.addMethod(method);
		}
	}
	
	public void readAttributes() throws IOException{
		int count = r2();
		for(int i = 0; i < count; ++i){
			String name = pool.getString(r2());
			int length = r4();
			byte[] data = new byte[length];
			in.read(data);
			if(name.equals("SourceFile")){
				if(length != 2) throw new IOException("wrong length for attribute SourceFile");
				clazz.source_file = pool.getString(data[0] << 8 | data[1]);
			}
		}
	}
	
	public void read() throws IOException{
		if(in.readInt() != 0xcafebabe) throw new IOException("\"class\" file doesn't start with cafebabe. Good work, amateur!");
		int version = r4();
		if(version < 45) throw new IOException("illogical version number");
		readConstantPool();
		int access_flags = r2();
		this.clazz = new JClass(pool.getString(r2()), pool.getString(r2()), access_flags);
		readInterfaces();
		readFields();
		readMethods();
		readAttributes();
	}
	
	public void close() throws IOException{
		in.close();
	}
}