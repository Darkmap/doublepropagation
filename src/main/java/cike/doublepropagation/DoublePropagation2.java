package cike.doublepropagation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import cike.bean.Seed;
import cike.util.POShelper;
import cike.util.SeedHelper;
import cike.xmlhandler.XMLparser;

/**LiuBing ExpandDomain。。。。。那篇
 * @author Qixuan
 *
 */
public class DoublePropagation2 {

	public static void main(String[] args) {
		
		File posdir = new File("F:\\Dropbox\\Verb\\Data\\cam\\pos");	
		File[] files = posdir.listFiles();
		
		for(File file: files){
		
		
			
			//获取doc对象
			XMLparser xparser = new XMLparser(file.getAbsolutePath());
			
			//每一条review的集合
			List<Element> reviews = xparser.root.getChildren();
			
			/****************************************************************
			 * 处理每一个Item
			 ****************************************************************/
			
			int t = 1;
			
			
			//获取每一个review
			for(Element review: reviews){

				//做标记
				int flag = 0;
				
				//每个人review有自己的本地lexicon
				ArrayList<Seed> lexicon = new ArrayList<Seed>();
				lexicon.addAll(SeedHelper.seedlist);
				

				while(flag!=lexicon.size()){
					flag = lexicon.size();
					
					Element content = review.getChild("Content");
					List<Element> sents = content.getChildren();
					//获取每一个Sentence
					for(Element s: sents){
						//获取relation结果组父节点
						Element r = s.getChild("R");
						List<Element> relations = r.getChildren();
						
						
						//遍历关系，处理D->D->H<-D<-D这种的
						int len = relations.size();
						for(int i = 0;i<len;i++){
							Element e1 = relations.get(i);
							
							for(int j=i+1;j<len;j++){
								
								Element e2 = relations.get(j);
								
								if(e2.getAttributeValue("Rel").equals(e1.getAttributeValue("Rel"))){
									Element g1 = e1.getChild("G");
									Element g2 = e2.getChild("G");
									if(g1.getAttributeValue("Idx").equals(g2.getAttributeValue("Idx"))){
										Element d1 = e1.getChild("D");
										Element d2 = e2.getChild("D");
										String dstr1 = d1.getText();
										String dstr2 = d2.getText();
										double p1 = SeedHelper.getpolarity(dstr1, lexicon);
										double p2 = SeedHelper.getpolarity(dstr2, lexicon);
										
										if(p1!=0&&p2==0){
											if(POShelper.isJJorNN(d2)){
												if(d1.getAttributeValue("P") == null){
													d1.setAttribute("P", String.valueOf(p1));
												}
												if(d2.getAttributeValue("P") == null){
													d2.setAttribute("P", String.valueOf(p1));
												}
												lexicon.add(new Seed(dstr2, (double)(p1)));
											}//另外一个是NN或者JJ(且与另一个相同词性)
										}//有一个是F或者S集合的
										else if(p1==0&&p2!=0){
											if(POShelper.isJJorNN(d1)){
												if(d2.getAttributeValue("P") == null){
													d2.setAttribute("P", String.valueOf(p2));
												}
												if(d1.getAttributeValue("P") == null){
													d1.setAttribute("P", String.valueOf(p2));
												}
												lexicon.add(new Seed(dstr1, p2));
											}//另外一个是NN或者JJ(且与另一个相同词性)
										}//有一个是F或者S集合的
										else if(p1!=0&&p2!=0){
											if(d1.getAttributeValue("P") == null){
												d1.setAttribute("P", String.valueOf(p1));
											}
											if(d2.getAttributeValue("P") == null){
												d2.setAttribute("P", String.valueOf(p2));
											}
										}
										
									}//如果e1和e2的Governor一致								
								}//如果e1和e2关系同种
								
								else{
									Element g1 = e1.getChild("G");
									Element g2 = e2.getChild("G");
									if(g1.getAttributeValue("Idx").equals(g2.getAttributeValue("Idx"))){
										Element d1 = e1.getChild("D");
										Element d2 = e2.getChild("D");
										String dstr1 = d1.getText();
										String dstr2 = d2.getText();
										double p1 = SeedHelper.getpolarity(dstr1, lexicon);
										double p2 = SeedHelper.getpolarity(dstr2, lexicon);
										
										if(p1!=0&&p2==0){
											if((POShelper.isAdjective(d1)&&POShelper.isAdjective(d2))||(POShelper.isNoun(d1)&&POShelper.isNoun(d2))){
												if(d1.getAttributeValue("P") == null){
													d1.setAttribute("P", String.valueOf(p1));
												}
												if(d2.getAttributeValue("P") == null){
													d2.setAttribute("P", String.valueOf(p1));
												}
												lexicon.add(new Seed(dstr2, p1));
											}//另外一个是NN或者JJ(且与另一个相同词性)
										}//有一个是F或者S集合的
										else if(p1==0&&p2!=0){
											if((POShelper.isAdjective(d1)&&POShelper.isAdjective(d2))||(POShelper.isNoun(d1)&&POShelper.isNoun(d2))){
												if(d2.getAttributeValue("P") == null){
													d2.setAttribute("P", String.valueOf(p2));
												}
												if(d1.getAttributeValue("P") == null){
													d1.setAttribute("P", String.valueOf(p2));
												}
												lexicon.add(new Seed(dstr1, p2));
											}//另外一个是NN或者JJ(且与另一个相同词性)
										}//有一个是F或者S集合的
										else if(p1!=0&&p2!=0){
											if(d1.getAttributeValue("P") == null){
												d1.setAttribute("P", String.valueOf(p1));
											}
											if(d2.getAttributeValue("P") == null){
												d2.setAttribute("P", String.valueOf(p2));
											}
										}
										
									}//如果e1和e2的Governor一致
									
									
									
									//TODO 如果关系不同种，但是词性同种
								}
							}
						}
						
						//遍历关系，计算D->D->G这种的
						for(Element e: relations){
							String rel = e.getAttributeValue("Rel");
							if(rel.equals("amod")){
								Element g = e.getChild("G");
								String gov = g.getText();
//								String gc = g.getAttributeValue("C");
								Element d = e.getChild("D");
								String dep = d.getText();
//								String dc = d.getAttributeValue("C");
								
								double pg = SeedHelper.getpolarity(gov, lexicon);
								if(pg!=0){//假如lexicon里面有
									if(g.getAttributeValue("P") == null){
										g.setAttribute("P", String.valueOf(pg));
									}
								}
								double pd = SeedHelper.getpolarity(dep, lexicon);
								if(pd!=0){//假如lexicon里面有
									if(d.getAttributeValue("P") == null){
										d.setAttribute("P", String.valueOf(pd));
									}
								}
								if(pd==0 && pg!=0){
									if(g.getAttributeValue("P") == null){
										g.setAttribute("P", String.valueOf(pg));
									}
									d.setAttribute("P", String.valueOf(pg));
									lexicon.add(new Seed(dep, pg));
								}
								if(pd!=0 && pg==0){
									if(d.getAttributeValue("P") == null){
										d.setAttribute("P", String.valueOf(pd));
									}
									g.setAttribute("P", String.valueOf(pd));
									lexicon.add(new Seed(gov, pd));
								}
							}
							else if(rel.equals("nsubj")){
								Element g = e.getChild("G");
								String gov = g.getText();
								String gc = g.getAttributeValue("C");
								Element d = e.getChild("D");
								String dep = d.getText();
								String dc = d.getAttributeValue("C");
								
								//如果nsubj里面，governor是JJ,dependent是NN
								if(POShelper.isAdjective(gc)&&POShelper.isNoun(dc)){
									double pg = SeedHelper.getpolarity(gov, lexicon);
									if(pg!=0){//假如lexicon里面有
										if(g.getAttributeValue("P") == null){
											g.setAttribute("P", String.valueOf(pg));
										}
									}
									double pd = SeedHelper.getpolarity(dep, lexicon);
									if(pd!=0){//假如lexicon里面有
										if(d.getAttributeValue("P") == null){
											d.setAttribute("P", String.valueOf(pd));
										}
									}
									if(pd==0 && pg!=0){
										if(g.getAttributeValue("P") == null){
											g.setAttribute("P", String.valueOf(pg));
										}
										d.setAttribute("P", String.valueOf(pg));
										lexicon.add(new Seed(dep, pg));
									}
									if(pd!=0 && pg==0){
										if(d.getAttributeValue("P") == null){
											d.setAttribute("P", String.valueOf(pd));
										}
										g.setAttribute("P", String.valueOf(pd));
										lexicon.add(new Seed(gov, pd));
									}
								}
							}
							else if(rel.equals("npadvmod")){
								Element g = e.getChild("G");
								String gov = g.getText();
								String gc = g.getAttributeValue("C");
								Element d = e.getChild("D");
								String dep = d.getText();
								String dc = d.getAttributeValue("C");
								
								//如果npadvmod里面，governor是JJ,dependent是NN
								if(POShelper.isAdjective(gc)&&POShelper.isNoun(dc)){
									double pg = SeedHelper.getpolarity(gov, lexicon);
									if(pg!=0){//假如lexicon里面有
										if(g.getAttributeValue("P") == null){
											g.setAttribute("P", String.valueOf(pg));
										}
									}
									double pd = SeedHelper.getpolarity(dep, lexicon);
									if(pd!=0){//假如lexicon里面有
										if(d.getAttributeValue("P") == null){
											d.setAttribute("P", String.valueOf(pd));
										}
									}
									if(pd==0 && pg!=0){
										if(g.getAttributeValue("P") == null){
											g.setAttribute("P", String.valueOf(pg));
										}
										d.setAttribute("P", String.valueOf(pg));
										lexicon.add(new Seed(dep, pg));
									}
									if(pd!=0 && pg==0){
										if(d.getAttributeValue("P") == null){
											d.setAttribute("P", String.valueOf(pd));
										}
										g.setAttribute("P", String.valueOf(pd));
										lexicon.add(new Seed(gov, pd));
									}
								}
							}
							else if(rel.equals("conj_and")){
								Element g = e.getChild("G");
								String gov = g.getText();
								String gc = g.getAttributeValue("C");
								Element d = e.getChild("D");
								String dep = d.getText();
								String dc = d.getAttributeValue("C");
								
								if(!((POShelper.isAdjective(gc) &&POShelper.isAdjective(dc)) 
										||(POShelper.isNoun(gc) &&POShelper.isNoun(dc)) )) break;//退出本关系
								
								double pg = SeedHelper.getpolarity(gov, lexicon);
								if(pg!=0){//假如lexicon里面有
									if(g.getAttributeValue("P") == null){
										g.setAttribute("P", String.valueOf(pg));
									}
								}
								double pd = SeedHelper.getpolarity(dep, lexicon);
								if(pd!=0){//假如lexicon里面有
									if(d.getAttributeValue("P") == null){
										d.setAttribute("P", String.valueOf(pd));
									}
								}
								if(pd==0 && pg!=0){
									d.setAttribute("P", String.valueOf(pg));
									lexicon.add(new Seed(dep, pg));
								}
								if(pd!=0 && pg==0){
									g.setAttribute("P", String.valueOf(pd));
									lexicon.add(new Seed(gov, pd));
								}
							}
							//Verb部分
//							else if(rel.equals("dobj")){
//								Element g = e.getChild("G");
//								String gov = g.getText();
//								String gc = g.getAttributeValue("C");
//								Element d = e.getChild("D");
//								String dep = d.getText();
//								String dc = d.getAttributeValue("C");
//								
//								
//								int polarity = POShelper.getVerbPolarity(gov);
//								int pd = SeedHelper.getpolarity(dep, lexicon);
//								
//								if(polarity != 0){
//									if(pd == 0){
//										if(d.getAttributeValue("P") == null){
//											d.setAttribute("P", String.valueOf(polarity));
//										}
//										lexicon.add(new Seed(dep, polarity));
//									}
//									else if(pd == polarity){
//										if(d.getAttributeValue("P") == null){
//											d.setAttribute("P", String.valueOf(polarity));
//										}
//									}
//								}
//							}
							
							
							
						}//每一条关系
					}//每一个句子
					
					
					System.out.println("flag:\t"+flag+"\nlexicon.size():\t"+lexicon.size());
				}
				
			}//每一个review
			
			XMLOutputter outputter = null;
			Format format = Format.getCompactFormat();
			format.setIndent("	");
			outputter = new XMLOutputter(format);

			try {
				outputter.output(XMLparser.doc, new FileOutputStream("F:\\Dropbox\\Verb\\Data\\cam\\dp\\"+file.getName()+"_dp.xml"));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
	}
}
