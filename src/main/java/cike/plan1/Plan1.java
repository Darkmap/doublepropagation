package cike.plan1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import cike.bean.ParseDependencies;
import cike.bean.Seed;
import cike.standfordparser.StandfordParser;
import cike.util.SeedHelper;
import cike.xmlhandler.XMLparser;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;

public class Plan1 {

	//输出XML
	private static Document outdom;
	private static Element rootelement;
	
	public static void main(String[] args) {
		
		XMLparser xparser = new XMLparser("C:/Users/Qixuan/Desktop/pos/cam.xml");
		
		

		//获取lexicon
		ArrayList<Seed> lexicon = new ArrayList<Seed>();
		SeedHelper.getseedlist();
		lexicon.addAll(SeedHelper.seedlist);
		
		ArrayList<Seed> advLexicon = new ArrayList<Seed>();
		Scanner input;
		try {
			input = new Scanner(new File("RB.txt"));
			while(input.hasNext()){
				String[] seeds = input.nextLine().split(" ");
				advLexicon.add(new Seed(seeds[0], Double.valueOf(seeds[1])));
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		
		//每一件商品的集合
		List<Element> items = xparser.root.getChildren();
		//计算商品件数
		int length = items.size();
		
		for(int i=0;i<length;i++){
			
			List<Element> reviews = items.get(i).getChild("Reviews").getChildren("Review");
			
			/*************************************************************/
			
			
			
			
			
			//大批Element袭来。。。
			Element review, docid, content, rating, s, p, r, w, e, g, d;
			int ridx=1;
			for(Element rev: reviews){
				
				
				//初始化文档
				newDocument();
				
				review = new Element("Review");
				
				//DocID 子节点添加
				String id = rev.getChildText("DocID");
				docid = new Element("DocID");
				docid.setText(id);
				review.addContent(docid);
				
				//Rating 子节点添加
				String rat = rev.getChildText("Rating");
				rating = new Element("Rating");
				rating.setText(rat);
				review.addContent(rating);
				
				//Content 子节点添加
				String cont = rev.getChildText("Content");
				content = new Element("Content");
				
				
				
				//content断句后的结果数组
				String[] sentsarray = segmentation(cont);
				//遍历每个句子
				for(String tempsent: sentsarray){
					
					//防止异常句子
					if(tempsent.trim().length()==0){
						continue;
					}
					
					//句子pos结果
					p = new Element("P");
					//句子relation结果
					r = new Element("R");
					
//					System.out.println("{"+tempsent+"}");
					s = new Element("S");
					
					//获取POS结果
					ArrayList<TaggedWord> posresult = StandfordParser.sentencePOS(tempsent);
					//获取语法树
					Tree tree = StandfordParser.getParseTree(tempsent);
					//获取Parse结果(注意这里的序号和上面两个不同，是index-1类型)					
					ParseDependencies parseresult = new ParseDependencies(StandfordParser.sentenceparser(tempsent));
					
					
					//每个词的得分记录
					ArrayList<Double> polarityList = new ArrayList<Double>();
					
					List<Tree> treeLeaves = tree.getLeaves();
					int sentLen = treeLeaves.size();
					
					//初始化得分记录
					for(int j=0;j<sentLen;j++){
						polarityList.add(0.0);
					}
				
					
					//第一遍遍历，获取opinion words情感强度
					for(int idx=0;idx<sentLen;idx++){
						double count = 0;
						
						
						if(fatherIs(idx, tree, "JJ")||fatherIs(idx, tree, "VB")){

							/***************************************************************
							 * 我们认为，只要是JJ或者是VB，则可以包含情感，就应该对其查Lexicon*
							 ***************************************************************/
							String opinionWord = treeLeaves.get(idx).nodeString().split(" ")[0];
							count = SeedHelper.getpolarity(opinionWord, lexicon);
							
							/***************************
							 *假如是JJS，则是2倍情感强度 *
							 ***************************/
							if(fatherIs(idx, tree, "JJS")){
								count*=2;
							}
							
							/*************************************************
							 * 如果有adverb advmod修饰JJ/NN，则有可能有强弱改变*
							 * 这里如果没有advmod修饰，则会得到序号为-1*********
							 * ***********************************************/
							int advIdx = parseresult.idxGoverRelation(idx, "advmod");
							
							if(advIdx>=0){
								String advWord = treeLeaves.get(advIdx).nodeString().split(" ")[0];
								for(Seed adv: advLexicon){
									if(adv.getWord().equalsIgnoreCase(advWord)){
										double polar = adv.getPolarity();
										polarityList.set(advIdx, polar);
										
										
										if(polar<0){
											if(count==0)
												count=polar;
											else
												count=polar*count;
										}
										else{
											count=polar*count;
										}
										
										break;
									}
								}
								
							}
							
							
							
							/***********************************************************
							 * 最终可以计算出每个情感词（JJ/NN）的修正得分（被副词影响的）*
							 ***********************************************************/
							polarityList.set(idx, count);
						}
						
						
					}
					
					
					List<TypedDependency> depRelList = parseresult.dependencies;
					for(TypedDependency dep : depRelList){
						
						if(dep.reln().toString().equals("nsubj")){
							String tempg = dep.gov().toString();
							//打断“home-7”这样的输出结果
							String[] garray = tempg.split("-");
							//为了处理“8‘，8''这样的输出”
							String temp = garray[garray.length-1];
							if(temp.contains("'")){
								temp = temp.split("'")[0];
							}
							int indexG = Integer.valueOf(temp)-1;
							
							String tempd = dep.dep().toString();
							//打断“home-7”这样的输出结果
							String[] darray = tempd.split("-");
							//为了处理“8‘，8''这样的输出”
							String temp2 = darray[darray.length-1];
							if(temp2.contains("'")){
								temp2 = temp2.split("'")[0];
							}
							int indexD = Integer.valueOf(temp2)-1;
							
							if(posresult.get(indexG).tag().contains("JJ")&&posresult.get(indexD).tag().contains("NN")){
								if(polarityList.get(indexG)!=0){
									polarityList.set(indexD, polarityList.get(indexD)+polarityList.get(indexG));
								}
							}
							
						}
						else if(dep.reln().toString().equals("amod")){
							String tempg = dep.gov().toString();
							//打断“home-7”这样的输出结果
							String[] garray = tempg.split("-");
							//为了处理“8‘，8''这样的输出”
							String temp = garray[garray.length-1];
							if(temp.contains("'")){
								temp = temp.split("'")[0];
							}
							int indexG = Integer.valueOf(temp)-1;
							
							String tempd = dep.dep().toString();
							//打断“home-7”这样的输出结果
							String[] darray = tempd.split("-");
							//为了处理“8‘，8''这样的输出”
							String temp2 = darray[darray.length-1];
							if(temp2.contains("'")){
								temp2 = temp2.split("'")[0];
							}
							int indexD = Integer.valueOf(temp2)-1;
							
							if(posresult.get(indexD).tag().contains("JJ")&&posresult.get(indexG).tag().contains("NN")){
								if(polarityList.get(indexD)!=0){
									polarityList.set(indexG, polarityList.get(indexG)+polarityList.get(indexD));
								}
							}
							
						}
						else if(dep.reln().toString().equals("dobj")){
							String tempg = dep.gov().toString();
							//打断“home-7”这样的输出结果
							String[] garray = tempg.split("-");
							//为了处理“8‘，8''这样的输出”
							String temp = garray[garray.length-1];
							if(temp.contains("'")){
								temp = temp.split("'")[0];
							}
							int indexG = Integer.valueOf(temp)-1;
							
							String tempd = dep.dep().toString();
							//打断“home-7”这样的输出结果
							String[] darray = tempd.split("-");
							//为了处理“8‘，8''这样的输出”
							String temp2 = darray[darray.length-1];
							if(temp2.contains("'")){
								temp2 = temp2.split("'")[0];
							}
							int indexD = Integer.valueOf(temp2)-1;
							
							if(posresult.get(indexD).tag().contains("NN")&&posresult.get(indexG).tag().contains("VB")){
								if(polarityList.get(indexG)!=0){
									polarityList.set(indexD, polarityList.get(indexD)+polarityList.get(indexG));
								}
							}
							
						}
					}
					
					
					/**
					 * 填充XML内容，方便对比试验效果
					 */
					//遍历POS结果，处理结果
					int k=0;
					for(TaggedWord word: posresult){
						w = new Element("W");
						
						w.setText(word.word());
						w.setAttribute("C", word.tag());
						if(polarityList.get(k)!=0){
							w.setAttribute("Score", polarityList.get(k).toString());
						}
						p.addContent(w);
						k++;
					}
					//遍历Parse结果，处理结果
					for(TypedDependency depen: parseresult.dependencies){
						//dependency关系实例
						e = new Element("E");
						
						//设置关系名称属性
						e.setAttribute("Rel", depen.reln().toString());
						//添加依赖词与被依赖词
						
						
						g = new Element("G");
						String tempg = depen.gov().toString();
						//打断“home-7”这样的输出结果
						String[] garray = tempg.split("-");
						g.setText(garray[0]);
						//为了处理“8‘，8''这样的输出”
						String temp = garray[garray.length-1];
						if(temp.contains("'")){
							temp = temp.split("'")[0];
						}
						int index = Integer.valueOf(temp);
						
						//通过index获取上一步得到的词性属性标注
						if(index!=0)
							g.setAttribute("C",posresult.get(index-1).tag());
						g.setAttribute("Idx",String.valueOf(index));
						e.addContent(g);
						
						
						d = new Element("D");
						String tempd = depen.dep().toString();
						//打断“home-7”这样的输出结果
						String[] darray = tempd.split("-");
						d.setText(darray[0]);
						//为了处理“8‘，8''这样的输出”
						String temp2 = darray[darray.length-1];
						if(temp2.contains("'")){
							temp2 = temp2.split("'")[0];
						}
						int index2 = Integer.valueOf(temp2);
						//通过index获取上一步得到的词性属性标注
						if(index2!=0)
							d.setAttribute("C",posresult.get(index2-1).tag());
						d.setAttribute("Idx",String.valueOf(index2));
						e.addContent(d);
						
						r.addContent(e);
					}
					s.addContent(p);
					s.addContent(r);
					content.addContent(s);
				}
				
				
				review.addContent(content);
				
				
				
				//添加review项（01层子节点）进入根目录
				rootelement.addContent(review);
				
				//输出
				outXML(outdom, "C:/Users/Qixuan/Desktop/pos/cam_item_"+i+"_review_"+ridx+".xml");
//				outXML(outdom, "cam_item_"+i+"_review_"+ridx+".xml");
				System.out.println("Review "+ridx+" completed.");
				ridx++;
				
			}

			
			
			System.out.println("Item"+i+"completed");
			
			
			
			
			/*************************************************************/
		}
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
