package cike.bean;

public class Opw_Freq implements Comparable<Opw_Freq>{

	public String opinionWord;
	public int frequency;
	
	public Opw_Freq(String ow, int fre){
		
		opinionWord = new String(ow);
		frequency = fre;
	}

	public int compareTo(Opw_Freq o) {
		if(this.frequency>o.frequency)
			return 1;
		else if(this.frequency<o.frequency)
			return -1;
		return 0;
	}
	
	
}
