package cike.plan2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.jdom.Element;

import cike.bean.Opw_Freq;
import cike.bean.ParseDependencies;
import cike.standfordparser.StandfordParser;
import cike.xmlhandler.XMLparser;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.Tree;

public class Co_AppearanceGetting {

	
	public static void main(String[] args) throws FileNotFoundException {
		
		ArrayList<LinkedList<String>> aspects = new ArrayList<LinkedList<String>>();
		Scanner as_in = new Scanner(new File("manual_aspects.txt"));
		while(as_in.hasNext()){
			LinkedList<String> tempAs = new LinkedList<String>();
			as_in.nextLine();
			String line = as_in.nextLine();
			String[] words = line.split(" ");
			for(String word: words){
				tempAs.add(word);
			}
			aspects.add(tempAs);
		}
		
		XMLparser xparser = new XMLparser("cam.xml");
		
		//用于获取potential pair的队列（计算frequency）
		ArrayList<ArrayList<String>> as_opw_co = new ArrayList<ArrayList<String>>();
		for(int a=0;a<17;a++){
			as_opw_co.add(new ArrayList<String>());
		}
		
		
		//每一件商品的集合
		List<Element> items = xparser.root.getChildren();
		//计算商品件数，遍历商品
		int length = items.size();
		for(int i=0;i<length;i++){
			List<Element> reviews = items.get(i).getChild("Reviews").getChildren("Review");
			
			int ridx=1;
			for(Element rev: reviews){
				
				
				//断句
				String cont = rev.getChildText("Content");
				String[] sentsarray = segmentation(cont);
				
				//遍历每个句子
				for(String tempsent: sentsarray){
					//防止异常句子
					if(tempsent.trim().length()==0){
						continue;
					}
//					//获取POS结果
//					ArrayList<TaggedWord> posresult = StandfordParser.sentencePOS(tempsent);
					//获取语法树
					Tree tree = StandfordParser.getParseTree(tempsent);
					//获取Parse结果(注意这里的序号和上面两个不同，是index-1类型)					
					ParseDependencies parseresult = new ParseDependencies(StandfordParser.sentenceparser(tempsent));
					List<Tree> treeLeaves = tree.getLeaves();
					int sentLen = treeLeaves.size();
					
					ArrayList<Integer> aspectHas = new ArrayList<Integer>();
					ArrayList<String> firstopinionHas = new ArrayList<String>();
					ArrayList<String> secondopinionHas = new ArrayList<String>();
					
					//遍历每个词，找到NN,JJ,RB
					//用but,however,though断开
					boolean half = false;
					for(int ni = 0;ni<sentLen;ni++){
						
						String tempword= treeLeaves.get(ni).nodeString().split(" ")[0].toLowerCase();
						
						if(tempword.equalsIgnoreCase("but")||tempword.equalsIgnoreCase("however")||tempword.equalsIgnoreCase("though")||tempword.equalsIgnoreCase("although"))
							half = true;
							
						if(fatherIs(ni, tree, "NN")){
							for(int idx = 0;idx<17;idx++){
								LinkedList<String> as = aspects.get(idx);
								if(as.contains(tempword))
									aspectHas.add(idx);
							}
						}
						else if(fatherIs(ni, tree, "JJ")||fatherIs(ni, tree, "VB")){
							if(parseresult.idxGoverRelation(ni, "neg")!=-1)
								if(half)
									secondopinionHas.add("neg-"+tempword);
								else
									firstopinionHas.add("neg-"+tempword);
							else
								if(half)
									secondopinionHas.add(tempword);
								else
									firstopinionHas.add(tempword);
						}
					}
					
					//配对aspect和opinion
					int alen = aspectHas.size();
					for(int ai=0;ai<alen;ai++){
						ArrayList<String> pairs = as_opw_co.get(aspectHas.get(ai));
						String wordlist = new String("");
						for(String opw:firstopinionHas){
							wordlist+= (opw+" ");
						}
						wordlist+=",, ";
						for(String opw:secondopinionHas){
							wordlist+= (opw+" ");
						}
						pairs.add(wordlist);
					}
					
				}
				System.out.println("Review "+ridx+" of item "+i+" completed.");
				ridx++;
			}
			
			System.out.println("Item "+i+" completed.");
			
			PrintWriter out = new PrintWriter(new File("Item-"+i+"-co_appearance.txt"));
			for(int x=0;x<17;x++){
				out.println("Aspect "+x+":");
				ArrayList<String> pairs = as_opw_co.get(x);
				for(String pair: pairs){
					out.println(pair);
				}
				out.println("------------------");
			}
			out.close();
		}
		
		
		PrintWriter out = new PrintWriter(new File("co_appearance.txt"));
		for(int x=0;x<17;x++){
			out.println("Aspect "+x+":");
			ArrayList<String> pairs = as_opw_co.get(x);
			for(String pair: pairs){
				out.println(pair);
			}
			out.println("------------------");
		}
		out.close();

		
	}
	
	
	private static boolean fatherIs(int idx, Tree tree, String tag){
		
		if(idx<0)
			return false;
		return tree.getLeaves().get(idx).parent(tree).nodeString().split(" ")[0].contains(tag);
	}
	
	private static boolean grandpaIs(int idx, Tree tree, String tag){

		if(idx<0)
			return false;
		return tree.getLeaves().get(idx).parent(tree).parent(tree).nodeString().split(" ")[0].equalsIgnoreCase(tag);
	}
	
	
	/**给一段文本断句，这个方法其实并不好，因为会丢失.!?符号
	 * @param txt 被断句的文本
	 * @return 断完句子后的String[]
	 */
	private static String[] segmentation(String txt) {
		return txt.split("[.!?]");
	}
}
