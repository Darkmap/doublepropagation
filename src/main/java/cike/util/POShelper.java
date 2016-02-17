package cike.util;

import org.jdom.Element;

public class POShelper {

	/** 判断一个POS是否为名词
	 * @param pos
	 * @return
	 */
	static public boolean isNoun(String pos){
		return pos.equals("NN")||pos.equals("NNS");
	}
	

	static public boolean isNoun(Element e){
		
		String pos =  e.getAttributeValue("C");
		
		return isNoun(pos);
	}
	
	
	/** 判断一个POS是否为形容词，我们只关心一般形式JJ和最高级JJS，比较级暂时忽略
	 * @param pos
	 * @return
	 */
	static public boolean isAdjective(String pos){
		return pos.equals("JJ")||pos.equals("JJS")||pos.equals("JJR");
	}
	
	static public boolean isAdjective(Element e){
		
		String pos =  e.getAttributeValue("C");
		
		return isAdjective(pos);
	}
	
	
	/** 判断一个D或者G词是否为形容词或者名词
	 * @param e
	 * @return
	 */
	static public boolean isJJorNN(Element e){
		
		String pos =  e.getAttributeValue("C");
		
		return isNoun(pos)||isAdjective(pos);
	}
	

	
	static public int getVerbPolarity(String verb) {
		if(verb.equalsIgnoreCase("love")
				||verb.equalsIgnoreCase("loved")
				||verb.equalsIgnoreCase("loves")
				||verb.equalsIgnoreCase("like")
				||verb.equalsIgnoreCase("likes")
				||verb.equalsIgnoreCase("liked"))
			return 1;
		else if(verb.equalsIgnoreCase("hate")
				||verb.equalsIgnoreCase("hated")
				||verb.equalsIgnoreCase("hates"))
			return -1;
		else
			return 0;
	}

}
