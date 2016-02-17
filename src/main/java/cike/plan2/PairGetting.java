package cike.plan2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import cike.bean.Opw_Freq;
import cike.bean.ParseDependencies;
import cike.standfordparser.StandfordParser;
import cike.xmlhandler.XMLparser;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.Tree;


/**本类用于预处理，获取aspect和opinion的pairs，并且记录frequencies
 * @author Qixuan
 *
 */
public class PairGetting {

	//输出XML
	private static Document outdom;
	private static Element rootelement;
	
	
	
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
		System.out.println("We have---"+aspects.size()+"---aspects");
		
//		XMLparser xparser = new XMLparser("C:/Users/Qixuan/Desktop/pos/cam.xml");
		XMLparser xparser = new XMLparser("cam.xml");
		
		//用于获取potential pair的队列（计算frequency）
		ArrayList<HashMap<String, Integer>> as_opw_pairs = new ArrayList<HashMap<String, Integer>>();
		for(int a=0;a<17;a++){
			as_opw_pairs.add(new HashMap<String, Integer>());
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
					ArrayList<String> opinionHas = new ArrayList<String>();
					//遍历每个词，找到NN,JJ,RB
					for(int ni = 0;ni<sentLen;ni++){
						
						String tempword= treeLeaves.get(ni).nodeString().split(" ")[0].toLowerCase();
						if(fatherIs(ni, tree, "NN")){
							for(int idx = 0;idx<17;idx++){
								LinkedList<String> as = aspects.get(idx);
								if(as.contains(tempword))
									aspectHas.add(idx);
							}
						}
						else if((fatherIs(ni, tree, "JJ")&&!fatherIs(ni, tree, "JJR"))||fatherIs(ni, tree, "VB")){
							if(parseresult.idxGoverRelation(ni, "neg")!=-1)
								opinionHas.add("neg-"+tempword);
							else
								opinionHas.add(tempword);
						}
					}
					
					//配对aspect和opinion
					int alen = aspectHas.size();
					for(int ai=0;ai<alen;ai++){
						HashMap<String, Integer> pairs = as_opw_pairs.get(aspectHas.get(ai));
						for(String opw:opinionHas){
							if(pairs.containsKey(opw)){
								pairs.put(opw, pairs.get(opw)+1);
							}
							else
								pairs.put(opw, 1);
						}
					}
					
				}
				System.out.println("Review "+ridx+" of item "+i+" completed.");
				ridx++;
			}
			
			System.out.println("Item "+i+" completed.");
			PrintWriter out = new PrintWriter(new File("F:/Item-"+i+"-pairs-occur.txt"));
			for(int x=0;x<17;x++){
				out.println("Aspect "+x+":");
				HashMap<String, Integer> pairs = as_opw_pairs.get(x);
				Iterator<String> mapIt = pairs.keySet().iterator();
				ArrayList<Opw_Freq> pairList = new ArrayList<Opw_Freq>();
				while(mapIt.hasNext()){
					String word = mapIt.next();
					int freq = pairs.get(word);
					pairList.add(new Opw_Freq(word, freq));
				}
				Collections.sort(pairList);
				for(Opw_Freq pair: pairList){
					out.println(pair.opinionWord+" "+pair.frequency);
				}
				out.println("------------------");
			}
			out.close();
		}
		
		
		PrintWriter out = new PrintWriter(new File("F:/pairs-occur.txt"));
		for(int i=0;i<17;i++){
			out.println("Aspect "+i+":");
			HashMap<String, Integer> pairs = as_opw_pairs.get(i);
			Iterator<String> mapIt = pairs.keySet().iterator();
			ArrayList<Opw_Freq> pairList = new ArrayList<Opw_Freq>();
			while(mapIt.hasNext()){
				String word = mapIt.next();
				int freq = pairs.get(word);
				pairList.add(new Opw_Freq(word, freq));
			}
			Collections.sort(pairList);
			for(Opw_Freq pair: pairList){
				out.println(pair.opinionWord+" "+pair.frequency);
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
	
	
	
	
	
	
	/**输出生成XML文件
	 * @param doc 输出的XML文档对象
	 * @param outfilename 输出文件路径
	 */
	private static void outXML(Document doc, String outfilename) {
		XMLOutputter outputter = null;
		Format format = Format.getCompactFormat();
		format.setIndent("	");
		outputter = new XMLOutputter(format);

		try {
			outputter.output(doc, new FileOutputStream(outfilename));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
	
	
	/**
	 * 初始化一个新建XML文件所需组件
	 */
	private static void newDocument() {
		outdom = new Document();
		rootelement = new Element("Doc");
		Namespace ns = Namespace.getNamespace("other", "http://www.w3c.org");
		rootelement.addNamespaceDeclaration(ns); 
		outdom.setRootElement(rootelement);
	}
	
	
	/**给一段文本断句，这个方法其实并不好，因为会丢失.!?符号
	 * @param txt 被断句的文本
	 * @return 断完句子后的String[]
	 */
	private static String[] segmentation(String txt) {
		return txt.split("[.!?]");
	}
	
}
