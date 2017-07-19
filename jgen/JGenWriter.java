package jgen;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jgen.code.JCode;

public class JGenWriter implements Closeable{

	JClass clazz;
	DataOutputStream out;
	
	JGenWriter(JClass clazz, DataOutputStream out){
		this.clazz = clazz;
		this.out = out;
	}
	
	private void w2(int i) throws IOException{
		out.writeShort(i);
	}
	
	private void w4(int i) throws IOException{
		w2(i >> 16);
		w2(i);
	}
	
	ConstantPool pool = new ConstantPool();
	List<Integer> interf_indices = new ArrayList<>();
	int tc_index, sc_index, cv_index, mc_index, jg_index, sf_index, ln_index, ex_index;
	
	public void makeConstantPool(){
		tc_index = pool.getClass(clazz.this_class);
		sc_index = pool.getClass(clazz.super_class);
		
		for(String interf: clazz.interfaces) interf_indices.add(pool.getClass(interf));
		
		boolean use_constval = false;
		for(JField field: clazz.fields){
			field.n_index = pool.getStr(field.name);
			field.d_index = pool.getStr(field.descriptor);
			
			if(field.constant_value != null){
				Object constant = field.constant_value;
				int tag = 0;		int[] data = null;
				if(constant instanceof Integer){
					tag = 3;
					int n = ((Integer)constant).intValue();
					data = new int[] {n >> 24, n >> 16, n >> 8, n};
				}
				else if(constant instanceof Float){
					tag = 4;
					int n = Float.floatToRawIntBits((Float)constant);
					data = new int[] {n >> 24, n >> 16, n >> 8, n};
				}
				else if(constant instanceof Long || constant instanceof Double){
					tag = constant instanceof Long ? 5 : 6;
					long n = constant instanceof Long ? ((Long)constant).longValue() : Double.doubleToRawLongBits((Double)constant);
					int nh = (int)(n >> 32), nl = (int)n;
					data = new int[] {nh >> 24, nh >> 16, nh >> 8, nh, nl >> 24, nl >> 16, nl >> 8, nl};
				}
				else if(constant instanceof String){
					tag = 8;
					int ix = pool.getStr((String)constant);
					data = new int[] {ix >> 8, ix};
				}
				field.c_index = pool.size();
				pool.add(new JPoolEntry(tag, data));
				use_constval = true;
			}
		}	
		if(use_constval) cv_index = pool.getStr("ConstantValue");
		
		boolean use_line_numbers = false, use_exceptions = false;
		for(JMethod method: clazz.methods){
			method.n_index = pool.getStr(method.name);
			method.d_index = pool.getStr(method.descriptor);
			
			if(method.hasCode()){
				method.codebytes = method.code.getBytes(pool);
				if(method.code.makesLineNumbers()) use_line_numbers = true;
			}
			if(!method.exceptions.isEmpty()){
				method.exc_indices = new ArrayList<Integer>();
				for(String exc: method.exceptions) method.exc_indices.add(pool.getClass(exc));
				use_exceptions = true;
			}
		}
		mc_index = pool.getStr("Code");
		if(use_line_numbers) ln_index = pool.getStr("LineNumberTable");
		if(use_exceptions) ex_index = pool.getStr("Exceptions");
		
		if(clazz.source_file != null){
			pool.getStr("SourceFile");
			sf_index = pool.getStr(clazz.source_file);
		}
		
		jg_index = pool.getStr("JGen");
	}
	
	public void writeFields() throws IOException{
		w2(clazz.fields.size());
		for(JField field: clazz.fields){
			w2(field.access_flags);
			w2(field.n_index);
			w2(field.d_index);
			if(field.constant_value != null){
				w2(1);
				w2(cv_index);
				w4(2);
				w2(field.c_index);
			}
			else w2(0);
		}
	}
	
	public void writeInterfaces() throws IOException{
		w2(clazz.interfaces.size());
		for(int index: interf_indices) w2(index);
	}
	
	public void writeCode(JMethod method) throws IOException{
		JCode code = method.code;
		w2(mc_index);
		w4(12 + method.codebytes.length + (code.makesLineNumbers() ? 8 + 4 * code.line_numbers.size() : 0));
		w2(code.nstack);
		w2(code.nlocals);
		w4(method.codebytes.length);
		out.write(method.codebytes);
		//TODO exceptions handlers
		w2(0);
		//TODO code attributes LocalVariableTable, LocalVariableTypeTable
		if(code.makesLineNumbers()){
			w2(1);
			w2(ln_index);
			int l = code.line_numbers.size();
			w4(2 + 4 * l);
			w2(l);
			for(int i = 0; i < l; ++i){
				w2(code.line_numbers.get(i));
				w2(i);
			}
		}
		else w2(0);
	}
	
	public void writeMethods() throws IOException{
		w2(clazz.methods.size());
		for(JMethod method: clazz.methods){
			w2(method.access_flags);
			w2(method.n_index);
			w2(method.d_index);
			//TODO method attribute Annotations
			int l = method.exceptions.size();
			int count = (method.hasCode() ? 1 : 0) + (l > 0 ? 1 : 0);
			w2(count);
			if(method.hasCode()) writeCode(method);
			if(l > 0){
				w2(ex_index);
				w4(2 * l + 2);
				w2(l);
				for(int exc: method.exc_indices) w2(exc);
			}
		}
	}
	
	public void writeAttributes() throws IOException{
		int count = 1;
		if(clazz.source_file != null) ++count;
		w2(count);
		if(clazz.source_file != null){
			w2(pool.index("SourceFile"));
			w4(2);
			w2(sf_index);
		}
		{
			w2(jg_index);
			w4(17);
			out.write("Created with JGen".getBytes());
		}
	}
	
	public void write() throws IOException{
		out.writeInt(0xcafebabe);
		w4(50);				//version
		makeConstantPool();
		w2(pool.size());
		while(pool.pool.remove(null));
		for(JPoolEntry entry: pool.pool){
			out.write(entry.tag);
			out.write(entry.data);
		}
		w2(clazz.access_flags);
		w2(tc_index);		//this_class
		w2(sc_index);		//super_class
		writeInterfaces();
		writeFields();
		writeMethods();
		writeAttributes();
		out.flush();
	}
	
	public void close() throws IOException{
		out.close();
	}
}