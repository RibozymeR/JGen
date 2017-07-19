package jgen;

import java.util.Arrays;

public class JPoolEntry{

	public int tag;
	public byte[] data;
	
	public JPoolEntry(){
	}
	
	public JPoolEntry(int tag){
		this.tag = tag;
	}
	
	/**
	 * data should not be changed later!!
	 */
	public JPoolEntry(int tag, byte[] data){
		this.tag = tag;
		this.data = data;
	}
	
	/**
	 * automatic conversion from ints to byte array
	 */
	public JPoolEntry(int tag, int... data){
		this.tag = tag;
		this.data = new byte[data.length];
		for(int i = 0; i < data.length; ++i) this.data[i] = (byte)data[i];
	}
	
	public JPoolEntry(String s){
		this.tag = 1;
		this.data = JGen.getStrData(s);
	}
	
	public boolean equals(Object o){
		if(o == null || !(o instanceof JPoolEntry)) return false;
		JPoolEntry e = (JPoolEntry)o;
		return tag == e.tag && Arrays.equals(data, e.data);
	}
	
	public int hashCode(){
		return tag << 16 | tag | Arrays.hashCode(data);
	}
}