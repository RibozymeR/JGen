package jgen.code;

import java.util.HashMap;
import java.util.Map;

public class JASM{
	
	//type << 8 | opcode
	//types:
	//	0	no bytes	
	//	1	index		i
	//	2	invoke		class.name:desc
	//	3	jump		+-number/target
	//	4	field		class.name:desc
	//	5	class		class
	//	6	ipush		n
	//	7	ldc			number/"string"/class -> add MethodType and MethodHandle loads
	//	8	wide jump	+-number/target
	//	c	multinewarray
	//	d	ldc2_w		number
	//	e	iinc		i, n
	//	f	newarray	boolean/char/float/double/byte/short/int/long
	private static Map<String, Integer> map = new HashMap<>();
	static{
		char[] p = {'i', 'l', 'f', 'd', 'a', 'b', 'c', 's'};
		int i, j;
		map.put("nop", 	0x000);
		map.put("aconst_null",	0x001);
		map.put("iconst_m1",	0x002);
		for(i = 0; i < 6; ++i)	map.put("iconst_" + i,	0x003 + i);
		map.put("lconst_0",	0x009);
		map.put("lconst_1", 0x00a);
		map.put("fconst_0",	0x00b);
		map.put("fconst_1", 0x00c);
		map.put("fconst_2",	0x00d);
		map.put("dconst_0",	0x00e);
		map.put("dconst_1", 0x00f);
		for(i = 0; i < 5; ++i) for(j = 0; j < 4; ++j)	map.put(p[i] + "load_" + j, 0x01a + 4 * i + j);
		for(i = 0; i < 8; ++i) map.put(p[i] + "aload", 0x02e + i);
		for(i = 0; i < 5; ++i) for(j = 0; j < 4; ++j)	map.put(p[i] + "store_" + j, 0x03b + 4 * i + j);
		for(i = 0; i < 8; ++i) map.put(p[i] + "astore", 0x04f + i);
		map.put("pop",		0x057);
		map.put("pop2",		0x058);
		map.put("dup",		0x059);
		map.put("dup_x1",	0x05a);
		map.put("dup_x2",	0x05b);
		map.put("dup2",		0x05c);
		map.put("dup2_x1",	0x05d);
		map.put("dup2_x2",	0x05e);
		map.put("swap",		0x05f);
		for(i = 0; i < 6; ++i){
			String s = "addsubmuldivremneg".substring(3 * i, 3 * i + 3);
			for(j = 0; j < 4; ++j) map.put(p[j] + s, 0x60 + 4 * i + j);
			s = "shl shr ushr and or xor".split(" ")[i];
			map.put('i' + s, 0x78 + 2 * i);
			map.put('l' + s, 0x79 + 2 * i);
		}
		j = 0x085;
		for(i = 0; i < 16; ++i) if(i % 5 != 0) map.put(p[i / 4] + "2" + p[i & 3], j++);
		for(i = 5; i < 8; ++i) map.put("i2" + p[i], 0x08c + i);
		map.put("lcmp",		0x094);
		map.put("fcmpl",	0x095);
		map.put("fcmpg",	0x096);
		map.put("dcmpl",	0x097);
		map.put("dcmpg",	0x098);
		for(i = 0; i < 5; ++i) map.put(p[i] + "return", 0x0ac + i);
		map.put("return",	0x0b1);
		map.put("arraylength",	0x0be);
		map.put("athrow",	0x0bf);
		map.put("monitorenter",	0x0c2);
		map.put("monitorexit",	0x0c3);
		
		for(i = 0; i < 5; ++i) map.put(p[i] + "load", 0x115 + i);
		for(i = 0; i < 5; ++i) map.put(p[i] + "store", 0x136 + i);
		map.put("ret", 0x1a9);
		
		map.put("invokevirtual",	0x2b6);
		map.put("invokespecial",	0x2b7);
		map.put("invokestatic",		0x2b8);
		map.put("invokeinterface",	0x2b9);
		
		map.put("ifeq",	0x399);
		map.put("ifne",	0x39a);
		map.put("iflt",	0x39b);
		map.put("ifge",	0x39c);
		map.put("ifgt",	0x39d);
		map.put("ifle",	0x39e);
		map.put("if_icmpeq",	0x39f);
		map.put("if_icmpne",	0x3a0);
		map.put("if_icmplt",	0x3a1);
		map.put("if_icmpge",	0x3a2);
		map.put("if_icmpgt",	0x3a3);
		map.put("if_icmple",	0x3a4);
		map.put("if_acmpeq",	0x3a5);
		map.put("if_acmpne",	0x3a6);
		map.put("goto",			0x3a7);
		map.put("jsr",			0x3a8);
		map.put("ifnull",		0x3c6);
		map.put("ifnonnull",	0x3c7);
		
		map.put("getstatic",	0x4b2);
		map.put("putstatic",	0x4b3);
		map.put("getfield",		0x4b4);
		map.put("putfield",		0x4b5);
		
		map.put("new",			0x5bb);
		map.put("anewarray",	0x5bd);
		map.put("checkcast",	0x5c0);
		map.put("instanceof",	0x5c1);
		
		map.put("bipush",	0x610);
		map.put("sipush",	0x611);
		
		map.put("ldc",		0x712);
		map.put("ldc_w",	0x713);
		
		map.put("goto_w",	0x8c8);
		map.put("jsr_w",	0x8c9);
		
		map.put("lookupswitch",	0x9ab);
		map.put("tableswitch", 0xaaa);
		
		map.put("multianewarray",	0xcc5);
		map.put("ldc2_w",	0xd14);
		map.put("iinc",		0xe84);
		map.put("newarray",	0xfbc);
	}
	
	public static Instruction assemble(String line){
		String[] tokens = line.split(" ");
		Integer op_wr = map.get(tokens[0]);
		if(op_wr == null) throw new IllegalArgumentException("line " + line + " is not a valid JVM command");
		int op = op_wr.intValue();
		int type = op >> 8;
		if(type == 0) return new Instruction(op, 0);
		else if(type == 1) return new Instruction(op, 1, (byte)Integer.parseUnsignedInt(tokens[1].substring(1)));
		else if(type == 2) return new Instruction(op, 2, tokens[1], op == 0x2b9 ? 11 : 10);
		else if(type == 3) return new Instruction(op, 3, tokens[1]);
		else if(type == 4) return new Instruction(op, 4, tokens[1], 9);
		else if(type == 5) return new Instruction(op, 5, tokens[1]);
		else if(type == 6) return new Instruction(op, 6, ((op - 0x610) << 16) + Integer.parseInt(tokens[1]));
		else if(type == 7) return new Instruction(op, 7, tokens[1], op - 0x712);
		else if(type == 8) return new Instruction(op, 8, tokens[1]);
		else if(type == 9) return new Instruction(op, 9, line.substring(line.indexOf(' ') + 1).replace(",", ""));
		else if(type == 10) return new Instruction(op, 10, line.substring(line.indexOf(' ') + 1).replace(",", ""));
		else if(type == 12) return new Instruction(op, 12, tokens[1].substring(0, tokens[1].length() - 1), Integer.parseInt(tokens[2]));
		else if(type == 13) return new Instruction(op, 13, tokens[1]);
		else if(type == 14) return new Instruction(op, 14, tokens[1].substring(0, tokens[1].length() - 1) + " " + tokens[2]);
		else if(type == 15) return new Instruction(op, 15, tokens[1].toLowerCase());
		throw new IllegalArgumentException("line " + line + " is not a valid JVM command");
	}
}