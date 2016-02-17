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
import edu.stanford.nlp.trees.TypedDependency;


/**本类用于预处理，获取aspect和opinion的pairs，并且记录frequencies
 * @author Qixuan
 *
 */
public class Co_Rel {

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
					//获取POS结果
					ArrayList<TaggedWord> posresult = StandfordParser.sentencePOS(tempsent);
//					//获取语法树
					Tree tree = StandfordParser.getParseTree(tempsent);
					//获取Parse结果(注意这里的序号和上面两个不同，是index-1类型)					
					ParseDependencies parseresult = new ParseDependencies(StandfordParser.sentenceparser(tempsent));
					List<Tree> treeLeaves = tree.getLeaves();
					int sentLen = treeLeaves.size();
					
					ArrayList<String> opinionHas = new ArrayList<String>();
					
					//遍历每个词，找到NN,JJ,RB
					for(int ni = 0;ni<sentLen;ni++){
						
						String tempword= treeLeaves.get(ni).nodeString().split(" ")[0].toLowerCase();
						if(fatherIs(ni, tree, "NN")){
							int asi = -1;
							for(int idx = 0;idx<17;idx++){
								LinkedList<String> as = aspects.get(idx);
								if(as.contains(tempword))
									asi = idx;
							}
							if(asi!=-1){
								//再找出同一句中修饰同一NN的JJ,VB
								for(TypedDependency rel:parseresult.dependencies){
									
									
									String tempg = rel.gov().toString();
									//打断“home-7”这样的输出结果
									String[] garray = tempg.split("-");
									//为了处理“8‘，8''这样的输出”
									String temp = garray[garray.length-1];
									if(temp.contains("'")){
										temp = temp.split("'")[0];
									}
									//所有者(ex: amod(NN->JJ))中的NN 的index
									int indexG = Integer.valueOf(temp)-1;
									String tempd = rel.dep().toString();
									//打断“home-7”这样的输出结果
									String[] darray = tempd.split("-");
									//为了处理“8‘，8''这样的输出”
									String temp2 = darray[darray.length-1];
									if(temp2.contains("'")){
										temp2 = temp2.split("'")[0];
									}
									//依赖者(ex: amod(NN->JJ))中的JJ 的index
									int indexD = Integer.valueOf(temp2)-1;
									
									if(indexD<0||indexG<0)
										continue;
										
									TaggedWord dep = posresult.get(indexD);
									TaggedWord gov = posresult.get(indexG);
									
									
									//如果是形容词修饰(NN->JJ)型amod
									if(rel.reln().toString().equals("amod")
											&&(dep.tag().contains("JJ")&&!dep.tag().contains("JJR"))
											&&gov.tag().contains("NN")
											&&indexG==ni){
										
										String adj = dep.word().toLowerCase();
										
										if(parseresult.idxGoverRelation(indexD, "neg")!=-1)
											adj ="neg-"+adj;
										
										opinionHas.add(adj);
										
									}
									else if(rel.reln().toString().equals("nsubj")
											&&dep.tag().contains("NN")
											&&(gov.tag().contains("JJ")&&!gov.tag().contains("JJR"))
											&&indexD==ni){
										
										String adj = gov.word().toLowerCase();
										
										if(parseresult.idxGoverRelation(indexG, "neg")!=-1)
											adj ="neg-"+adj;
										
										opinionHas.add(adj);
									}
									else if(rel.reln().toString().equals("dobj")
											&&dep.tag().contains("NN")
											&&gov.tag().contains("VB")
											&&indexD==ni){
										
										String verb = gov.word().toLowerCase();
										
										if(parseresult.idxGoverRelation(indexG, "neg")!=-1)
											verb ="neg-"+verb;
										
										opinionHas.add(verb);
									}
								}
								
								ArrayList<String> pairs = as_opw_co.get(asi);
								String wordlist = new String("");
								for(String opw:opinionHas){
									wordlist+= (opw+" ");
								}
								pairs.add(wordlist);
							}

						}
					}
					
				}
				System.out.println("Review "+ridx+" of item "+i+" completed.");
				ridx++;
			}
			
			System.out.println("Item "+i+" completed.");
			
			PrintWriter out = new PrintWriter(new File("C:\\Users\\Qixuan\\Desktop\\label\\Item-"+i+"-co_appearance_rel.txt"));
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
		
		PrintWriter out = new PrintWriter(new File("C:\\Users\\Qixuan\\Desktop\\label\\co_appearance_rel.txt"));
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
