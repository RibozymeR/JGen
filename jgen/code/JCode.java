package jgen.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import jgen.ConstantPool;
import jgen.JPoolEntry;

public class JCode{
	
	List<Instruction> code = new ArrayList<>();
	public int nstack, nlocals;
	public List<Integer> line_numbers = new ArrayList<>();
	boolean make_line_numbers;
	
	public byte[] getBytes(ConstantPool pool){
		Map<String, Integer> targets = new HashMap<>();
		List<Pair<Integer, String>> corrections = new ArrayList<>();
		List<Pair<Integer, String>> wcorrects = new ArrayList<>();
		
		byte[] buf = new byte[256];
		int index = 0;
		int max_local = 0;
		for(Instruction instr: code){
			if(instr.type == -1){
				targets.put(instr.arg, index);
				continue;
			}
			line_numbers.add(index);
			buf[index++] = instr.opcode;
			if(instr.type == 1){
				buf[index++] = (byte)instr.dat0;
			}
			else if(instr.type == 2 || instr.type == 4){
				//TODO add descriptor check (2->method, 4->field)
				int strd = instr.arg.indexOf('.');
				int strc = instr.arg.indexOf(':');
				int cindex = pool.getClass(instr.arg.substring(0, strd));
				int nnt = pool.getNameType(instr.arg.substring(strd + 1, strc), instr.arg.substring(strc + 1));
				pool.add(new JPoolEntry(instr.dat0, cindex >> 8, cindex, nnt >> 8, nnt));
				buf[index++] = (byte)((pool.size() - 1) >> 8);
				buf[index++] = (byte)(pool.size() - 1);
			}
			else if(instr.type == 3){
				corrections.add(new Pair<>(index, instr.arg));
				index += 2;
			}
			else if(instr.type == 5){
				int cindex = pool.getClass(instr.arg);
				buf[index++] = (byte)(cindex >> 8);
				buf[index++] = (byte)cindex;
			}
			else if(instr.type == 6){
				if((instr.dat0 >> 16) == 1) buf[index++] = (byte)(instr.dat0 >> 8);
				buf[index++] = (byte)instr.dat0;
			}
			else if(instr.type == 7){
				String arg = instr.arg;
				int ix = 0;
				if(arg.charAt(0) == '"') ix = pool.getStr(arg.substring(1, arg.length() - 1));
				else if(Character.isDigit(arg.charAt(0))) ix = arg.contains(".") ? pool.getFloat(Float.parseFloat(arg)) : pool.getInt(Integer.parseInt(arg));
				else ix = pool.getClass(arg);
				if(instr.dat0 > 0) buf[index++] = (byte)(ix >> 8);
				else if(ix >= 256) throw new IndexOutOfBoundsException("ldc does not have the index width necessary to use constant " + arg + ", use ldc_w instead");
				buf[index++] = (byte)ix;
			}
			else if(instr.type == 8){
				wcorrects.add(new Pair<>(index, instr.arg));
				index += 4;
			}
			else if(instr.type == 9){
				index = ((index - 1) & -4) + 4;
				String[] jumps = instr.arg.split(" ");
				wcorrects.add(new Pair<>(index, jumps[0]));
				index += 4;
				int l = jumps.length - 1;
				buf[index++] = (byte)(l >> 24);
				buf[index++] = (byte)(l >> 16);
				buf[index++] = (byte)(l >> 8);
				buf[index++] = (byte)l;
				List<Pair<Integer, String>> match_offset_pairs = new ArrayList<>();
				for(int i = 1; i <= l; ++i){
					int ix = jumps[i].indexOf(':');
					match_offset_pairs.add(new Pair<>(Integer.parseInt(jumps[i].substring(0, ix)), jumps[i].substring(ix)));
				}
				match_offset_pairs.sort((a, b) -> a.getKey().compareTo(b.getKey()));
				for(Pair<Integer, String> pair: match_offset_pairs){
					int match = pair.getKey();
					buf[index++] = (byte)(match >> 24);
					buf[index++] = (byte)(match >> 16);
					buf[index++] = (byte)(match >> 8);
					buf[index++] = (byte)match;
					wcorrects.add(new Pair<>(index, pair.getValue()));
					index += 4;
				}
			}
			else if(instr.type == 10){
				index = ((index - 1) & -4) + 4;
				String[] jumps = instr.arg.split(" ");
				wcorrects.add(new Pair<>(index, jumps[0]));
				index += 4;
				int low = Integer.parseInt(jumps[1]);
				buf[index++] = (byte)(low >> 24);
				buf[index++] = (byte)(low >> 16);
				buf[index++] = (byte)(low >> 8);
				buf[index++] = (byte)low;
				int high = Integer.parseInt(jumps[2]);
				buf[index++] = (byte)(high >> 24);
				buf[index++] = (byte)(high >> 16);
				buf[index++] = (byte)(high >> 8);
				buf[index++] = (byte)high;
				for(int i = 0; i < high - low + 1; ++i){
					wcorrects.add(new Pair<>(index, jumps[3 + i]));
					index += 4;
				}
			}
			else if(instr.type == 12){
				int cindex = pool.getClass(instr.arg);
				buf[index++] = (byte)(cindex >> 8);
				buf[index++] = (byte)cindex;
				buf[index++] = (byte)instr.dat0;
			}
			else if(instr.type == 13){
				String arg = instr.arg;
				int ix = arg.contains(".") ? pool.getLong(Double.doubleToRawLongBits(Double.parseDouble(arg)), 6) : pool.getLong(Long.parseLong(arg), 5);
				if(instr.dat0 > 0) buf[index++] = (byte)(ix >> 8);
				else if(ix >= 256) throw new IndexOutOfBoundsException("ldc does not have the index width necessary to use constant " + arg + ", use ldc_w instead");
				buf[index++] = (byte)ix;
			}
			else if(instr.type == 14){
				String[] args = instr.arg.split(" ");
				buf[index++] = (byte)Integer.parseUnsignedInt(args[0]);
				buf[index++] = Byte.parseByte(args[1]);
			}
			else if(instr.type == 15){
				buf[index++] = (byte)(Arrays.<String>asList("boolean", "char", "float", "double", "byte", "short", "int", "long").indexOf(instr.arg) + 4);
			}
			
			if(instr.opcode >= 0x15 && instr.opcode < 0x2e){
				int l = instr.opcode < 0x1a ? instr.dat0 : (instr.opcode - 0x1a) % 4;
				if(max_local < l) max_local = l;
			}
			else if(instr.opcode >= 0x36 && instr.opcode < 0x4f){
				int l = instr.opcode < 0x3b ? instr.dat0 : (instr.opcode - 0x3b) % 4;
				if(max_local < l) max_local = l;
			}
		}
		for(Pair<Integer, String> correct: corrections){
			String arg = correct.getValue();
			char fc = arg.charAt(0);
			int jump, ix = correct.getKey();
			if(fc == '+' || fc == '-') jump = Integer.parseInt(arg);
			else jump = targets.get(arg) - ix + 1;
			buf[ix] = (byte)(jump >> 8);
			buf[ix + 1] = (byte)jump;
		}
		for(Pair<Integer, String> wcorrect: wcorrects){
			String arg = wcorrect.getValue();
			char fc = arg.charAt(0);
			int jump, ix = wcorrect.getKey();
			if(fc == '+' || fc == '-') jump = Integer.parseInt(arg);
			else jump = targets.get(arg) - ix + 1;
			buf[ix] = (byte)(jump >> 24);
			buf[ix + 1] = (byte)(jump >> 16);
			buf[ix + 2] = (byte)(jump >> 8);
			buf[ix + 3] = (byte)jump;
		}
		nlocals = max_local + 1;
		return Arrays.copyOf(buf, index);
	}
	
	public static JCode merge(JCode a, JCode b){
		JCode merged = new JCode();
		merged.code.addAll(a.code);
		merged.code.addAll(b.code);
		return merged;
	}
	
	public void addLine(String line){
		if(line.charAt(0) == ':') code.add(new Instruction(0xca, -1, line.substring(1)));
		else code.add(JASM.assemble(line.trim()));
	}
	
	public void addLines(String... lines){
		for(String line: lines) addLine(line);
	}
	
	public void removeLine(){
		code.remove(code.size() - 1);
	}
	
	public void removeLines(int count){
		for(int i = 0; i < count; ++i) removeLine();
	}
	
	public boolean makesLineNumbers(){
		return make_line_numbers;
	}
	
	public void makeLineNumbers(boolean make_line_numbers){
		this.make_line_numbers = make_line_numbers;
	}
}