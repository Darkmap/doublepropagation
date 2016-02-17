package cike.doublepropagation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import cike.standfordparser.StandfordParser;
import cike.xmlhandler.XMLparser;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.TypedDependency;

public class PreParseDocument {

	//输出XML
	private static Document outdom;
	private static Element rootelement;
	
	public static void main(String[] args) {
		XMLparser xparser = new XMLparser("cam.xml");
		
		//每一件商品的集合
		List<Element> items = xparser.root.getChildren();
		
		
		//TODO 要对其他item处理
//		//这里的reviews是review的列表
//		List<Element> reviews = items.get(0).getChild("Reviews").getChildren("Review");
		
		
		
		int length = items.size();
		for(int i=0;i<length;i++){
			
			List<Element> reviews = items.get(i).getChild("Reviews").getChildren("Review");
			
			/*************************************************************/
			//初始化文档
			newDocument();
			
			
			//大批Element袭来。。。
			Element review, docid, content, rating, s, p, r, w, e, g, d;
			int ridx=1;
			for(Element rev: reviews){
				
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
				
				//POS结果
				ArrayList<TaggedWord> posresult;
				//Parse结果
				List<TypedDependency> parseresult;
				
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
					posresult = StandfordParser.sentencePOS(tempsent);
					//遍历POS结果，处理结果
					for(TaggedWord word: posresult){
						w = new Element("W");
						
						w.setText(word.word());
						w.setAttribute("C", word.tag());
						
						p.addContent(w);
					}
					
					
					//获取Parse结果
					parseresult = StandfordParser.sentenceparser(tempsent);
					//遍历Parse结果，处理结果
					for(TypedDependency depen: parseresult){
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
				
				System.out.println("Review "+ridx+" completed.");
				ridx++;
				
			}

			
			
			//输出
			outXML(outdom, "cam_pos_"+i+".xml");
			
			System.out.println("Item"+i+"completed");
			
			
			
			
			/*************************************************************/
		}
		

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
		rootelement = new Element("Reviews");
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
