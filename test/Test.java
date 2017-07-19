package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.lang.reflect.Method;
import java.nio.file.Paths;

import jgen.JClass;
import jgen.JGen;
import jgen.JMethod;
import jgen.code.JCode;

public class Test{
	
	static void javap() throws IOException{
		Process p = Runtime.getRuntime().exec("javap -v -p -c Gener.class");
		BufferedReader output = new BufferedReader(new InputStreamReader(new SequenceInputStream(p.getInputStream(), p.getErrorStream())));
		output.lines().forEach(System.out::println);
		output.close();
	}
	
	static void generate() throws IOException{
		JClass test = new JClass("Gener");
		
		test.addField("field", int.class, JGen.ACC_PUBLIC, 5);
		test.addField("str", String.class, JGen.ACC_PRIVATE, "Hello World!");
		
		JMethod run = new JMethod("abs", JGen.ACC_PUBLIC, int.class, int.class);
		JCode run_code = new JCode();
		run_code.addLines(
				"iload_1",
				"dup",
				"ifge ret",
				"ineg",
			":ret",
				"ireturn");
		run_code.nstack = 2;
		run.setCode(run_code);
		run.addException(IOException.class);
		test.addMethod(run);
		
		JMethod ctor = test.getDefaultConstructor(JGen.ACC_PUBLIC);
		ctor.getCode().makeLineNumbers(true);
		test.addMethod(ctor);
		
		JGen.write(test, Paths.get(""));
	}
	
	public static void main(String[] args) throws IOException, Exception{
		generate();
		javap();
		
		Class<?> gener_clazz = Class.forName("Gener");
		Method abs = gener_clazz.getMethod("abs", int.class);
		Object gener = gener_clazz.newInstance();
		System.out.println(abs.invoke(gener, 10));
	}
}