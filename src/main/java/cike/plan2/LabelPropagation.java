package cike.plan2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import Jama.Matrix;
import cike.bean.Seed;
import cike.util.SeedHelper;

public class LabelPropagation {

	public static void main(String[] args) throws FileNotFoundException {
		
		
		//每个人review有自己的本地lexicon
		ArrayList<Seed> lexicon = new ArrayList<Seed>();
		SeedHelper.getseedlist();
		lexicon.addAll(SeedHelper.seedlist);
		
		Scanner synIn = new Scanner(new File("C:/Users/Qixuan/Desktop/label/SynWeight2.txt"));
		Scanner atnIn = new Scanner(new File("C:/Users/Qixuan/Desktop/label/AtnWeight2.txt"));
		Scanner coIn = new Scanner(new File("C:/Users/Qixuan/Desktop/label/CoWeight2.txt"));
		Scanner butIn = new Scanner(new File("C:/Users/Qixuan/Desktop/label/ButWeight2.txt"));
		
		Scanner opwIn = new Scanner(new File("C:/Users/Qixuan/Desktop/label/opinionWords.txt"));
		
		//对每个Aspect分开处理
		for(int i=0;i<17;i++){
			
			synIn.nextLine();
			synIn.nextLine();
			atnIn.nextLine();
			atnIn.nextLine();
			coIn.nextLine();
			coIn.nextLine();
			butIn.nextLine();
			butIn.nextLine();
			
			String opwLine = opwIn.nextLine();
			String[] opws = opwLine.split(" ");
			int n = opws.length;
			
			
			double[][] Y = new double[n][1];
			double[][] W = new double[n][n];
//			double[][] W1 = new double[n][n];
//			double[][] W2 = new double[n][n];
//			double[][] W3 = new double[n][n];
//			double[][] W4 = new double[n][n];
			
			ArrayList<Double> seed = new ArrayList<Double>();
			
			for(int j=0;j<n;j++){
				double polar = 0;
				if(opws[j].contains("neg-")){
					polar = -SeedHelper.getpolarity(opws[j].substring(4));
				}
				else
					polar = SeedHelper.getpolarity(opws[j]);
				
				seed.add(polar);
				
				if(polar==-1){
					Y[j][0]=-1;
//					Y[j][1]=0;
//					Y[j][2]=1;
				}
				else if(polar==1){
					Y[j][0]=1;
//					Y[j][1]=0;
//					Y[j][2]=0;
				}
				else {
					Y[j][0]=0;
//					Y[j][1]=1;
//					Y[j][2]=0;
				}
			}
			
			
			ArrayList<Double> count = new ArrayList<Double>();;
			for(int c=0;c<n;c++){
				count.add(0.0);
			}
			
			for(int j=0;j<n;j++){
				String synLine = synIn.nextLine();
				String atnLine = atnIn.nextLine();
				String coLine = coIn.nextLine();
				String butLine = butIn.nextLine();

				String[] syns = synLine.split(" ");
				String[] atns = atnLine.split(" ");
				String[] cos = coLine.split(" ");
				String[] buts = butLine.split(" ");
				
				
				double max = 0;
				for(int k=0;k<n;k++){
					if(Math.abs((Double.valueOf(cos[k])-Double.valueOf(buts[k]))) > max)
						max = Math.abs((Double.valueOf(cos[k])-Double.valueOf(buts[k])));
				}
				for(int k=0;k<n;k++){
					
					if(j==k)
						W[j][k] = 1;
					else{
						double temp = 0;
						temp += 2*(Double.valueOf(syns[k])-Double.valueOf(atns[k]));
						//为了使得co和but能够统一考虑（全部相加用vote方式决定采纳大众的意见）
						//但是又不应该影响同义词和反义词的作用
						if(max!=0)
							temp += (Double.valueOf(cos[k])-Double.valueOf(buts[k]))/max;
						W[j][k] = temp;
						
//						if(temp>0)  
//							W[j][k] = 1;
//						else if(temp<0)
//							W[j][k] = -1;
//						else
//							W[j][k] = 0;
					}
					
					count.set(j,  count.get(j)+Math.abs(W[j][k]));
				}
				for(int k=0;k<n;k++){
					if(count.get(j)!=0)
						W[j][k] = W[j][k]/(count.get(j));
					else
						W[j][k] = 0;
				}
			}
			
			PrintWriter outweight = new PrintWriter(new File("C:/Users/Qixuan/Desktop/label/weight_"+(i+1)+"_Aspect.txt"));
			for(int x=0;x<n;x++){
				for(int y=0;y<n;y++){
					outweight.print(W[x][y]+"\t");
				}
				outweight.println();
			}
			outweight.close();
			
			Matrix MY = new Matrix(Y);
		    Matrix MW = new Matrix(W);
		    double last = 0;
		    int time = 0;
		    
			while(time<100){
				Matrix temp = MY;
				MY = MW.times(MY);
				/*for(int j=0;j<n;j++){
					if(MY.get(j, 0)>=1)
						MY.set(j, 0, 1);
					else if(MY.get(j, 0)<=-1)
						MY.set(j, 0, -1);
//					double log = (logisticFunc(MY.get(j, 0))-0.5)*2;
//					MY.set(j, 0, log);
				}*/
				
				for(int j=0;j<n;j++){
					for(int k=0;k<1;k++){
						if(count.get(j) != 0)
							MY.set(j, k, MY.get(j, k)/count.get(j));
						else
							MY.set(j, k, 0);
					}
				}
				for(int j=0;j<n;j++){
					double polar = seed.get(j);
					
					if(polar==-1){
						MY.set(j, 0, -1);
//						MY.set(j, 1, 1);
//						MY.set(j, 2, 0);
					}
					else if(polar==1){
						MY.set(j, 0, 1);
//						MY.set(j, 1, 0);
//						MY.set(j, 2, 0);
					}
				}
		        double x = temp.minus(MY).norm1();
//		        System.out.println("difference: "+x);
		        if(x < 0.01){
		            break;
		        }
		        time++;
			}
			
			

			if(i==0){
				Scanner goldIn = new Scanner(new File("C:/Users/Qixuan/Desktop/label/standard_1.txt"));
				
				
				int a=0, b=0, c=0, d=0, e=0, f=0, g=0, h=0, j=0;
				
				for(int idx=0;idx<n;idx++){
					String line = goldIn.nextLine();
					int gold = (Integer.valueOf(line.split(" ")[1]));
					int result = 0;
					if(MY.get(idx, 0)>0.01)
						result = 1;
					else if(MY.get(idx, 0)<-0.01)
						result = -1;
					if(gold==1&&result==1)
						a++;
					else if(gold==1&&result==-1)
						b++;
					else if(gold==-1&&result==1)
						c++;
					else if(gold==-1&&result==-1)
						d++;
					else if(gold==1&&result==0)
						e++;
					else if(gold==-1&&result==0)
						f++;
					else if(gold==0&&result==0)
						j++;
					else if(gold==0&&result==1)
						g++;
					else if(gold==0&&result==-1)
						h++;
				}
				double precision = (a+d)*1.0/(a+b+c+d);
				double recall = (a+d)*1.0/(a+b+c+d+e+f);
				double f_measure = 2*precision*recall/(precision+recall);
				System.out.println("Precision: "+precision);
				System.out.println("Recall: "+recall);
				System.out.println("F-measure: "+f_measure);
			}
			
			
			
			
			PrintWriter out = new PrintWriter(new File("C:/Users/Qixuan/Desktop/label/Aspect_"+(i+1)+"_result.txt"));
			for(int j=0;j<n;j++){
				out.print(opws[j]+"\t\t\t");
				for(int k=0;k<1;k++){
					out.print(MY.get(j, k)+" ");
				}
				out.println();
			}
			out.close();
			System.out.println("get one.");
		}
		
		
	}
	
	
	
	

	public static double logisticFunc(double x){
		return ((1.0/(1+Math.exp(-x))));
	}
}















