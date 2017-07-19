package jgen;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstantPool{

	List<JPoolEntry> pool = new ArrayList<>();
	
	public ConstantPool(){
		pool.add(null);	//entry 0
	}
	
	public int size(){
		return pool.size();
	}
	
	public void add(JPoolEntry entry){
		pool.add(entry);
	}
	
	public int getStr(String utf8){
		int ix = index(utf8);
		if(ix < 0){
			pool.add(new JPoolEntry(utf8));
			return pool.size() - 1;
		}
		else return ix;
	}
	
	public int getClass(String cname){
		int ix = indexClass(cname);
		if(ix < 0){
			ix = getStr(cname);
			pool.add(new JPoolEntry(7, ix >> 8, ix));
			return pool.size() - 1;
		}
		else return ix;
	}
	
	public String getString(int index){
		JPoolEntry s = pool.get(index);
		return new String(s.data, 2, s.data[0] << 8 | s.data[1], Charset.forName("UTF-8"));
	}
	
	public String getClass(int index){
		JPoolEntry c = pool.get(index);
		JPoolEntry s = pool.get(c.data[0] << 8 | c.data[1]);
		return new String(s.data, 2, s.data[0] << 8 | s.data[1], Charset.forName("UTF-8"));
	}
	
	public int getNameType(String name, String type){
		int ix = indexNT(name, type);
		if(ix < 0){
			int nix = getStr(name);
			int tix = getStr(type);
			pool.add(new JPoolEntry(12, nix >> 8, nix, tix >> 8, tix));
			return pool.size() - 1;
		}
		else return ix;
	}
	
	public int getInt(int n){
		byte[] data = new byte[] {(byte)(n >> 24), (byte)(n >> 16), (byte)(n >> 8), (byte)n};
		int ix = index(3, data);
		if(ix < 0){
			pool.add(new JPoolEntry(3, data));
			return pool.size() - 1;
		}
		else return ix;
	}
	
	public int getFloat(float f){
		int n = Float.floatToRawIntBits(f);
		byte[] data = new byte[] {(byte)(n >> 24), (byte)(n >> 16), (byte)(n >> 8), (byte)n};
		int ix = index(4, data);
		if(ix < 0){
			pool.add(new JPoolEntry(4, data));
			return pool.size() - 1;
		}
		else return ix;
	}
	
	public int getLong(long n, int tag){
		byte[] data = new byte[] {(byte)(n >> 56), (byte)(n >> 48), (byte)(n >> 40), (byte)(n >> 32), (byte)(n >> 24), (byte)(n >> 16), (byte)(n >> 8), (byte)n};
		int ix = index(tag, data);
		if(ix < 0){
			pool.add(new JPoolEntry(tag, data));
			pool.add(null);
			return pool.size() - 1;
		}
		else return ix;
	}
	
	public int index(String s){
		byte[] data = JGen.getStrData(s);
		for(int i = 1; i < pool.size(); ++i){
			JPoolEntry entry = pool.get(i);
			if(entry.tag == 1 && Arrays.equals(entry.data, data)) return i;
			continue;
		}
		return -1;
	}
	
	public int index(int tag, byte[] data){
		for(int i = 1; i < pool.size(); ++i){
			JPoolEntry entry = pool.get(i);
			if(entry.tag == tag && Arrays.equals(entry.data, data)) return i;
			continue;
		}
		return -1;
	}
	
	public int indexClass(String s){
		byte[] data = JGen.getStrData(s);
		for(int i = 1; i < pool.size(); ++i){
			JPoolEntry entry = pool.get(i);
			if(entry.tag != 7) continue;
			int ix = entry.data[0] << 8 | entry.data[1];
			entry = pool.get(ix);
			if(entry.tag == 1 && Arrays.equals(entry.data, data)) return i;
			continue;
		}
		return -1;
	}
	
	public int indexNT(String name, String type){
		byte[] ndata = JGen.getStrData(name);
		byte[] tdata = JGen.getStrData(type);
		for(int i = 1; i < pool.size(); ++i){
			JPoolEntry entry = pool.get(i);
			if(entry.tag != 12) continue;
			JPoolEntry nentry = pool.get(entry.data[0] << 8 | entry.data[1]);
			if(nentry.tag != 1 || !Arrays.equals(nentry.data, ndata)) continue;
			nentry = pool.get(entry.data[2] << 8 | entry.data[3]);
			if(nentry.tag == 1 && Arrays.equals(nentry.data, tdata)) return i;
			continue;
		}
		return -1;
	}
}