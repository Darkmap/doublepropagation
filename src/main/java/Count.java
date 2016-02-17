import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;


public class Count {

	public static void main(String[] args) throws FileNotFoundException {
		
		
		Scanner goldIn = new Scanner(new File("C:\\Users\\Qixuan\\Desktop\\label\\gold\\standard_6_full.txt"));
		HashMap<String, Integer> goldMap = new HashMap<String, Integer>();
		while(goldIn.hasNext()){
			
			String line = goldIn.nextLine();
			if(line.length()>0){
				String[] lines = line.split(" ");
				goldMap.put(lines[0], Integer.valueOf(lines[1]));
			}
		}
		goldIn.close();
		
		int a=0, b=0, c=0, d=0, e=0, f=0, g=0, h=0, j=0;
		
		Scanner resultIn = new Scanner(new File("C:\\Users\\Qixuan\\Desktop\\label\\Aspect_6_result.txt"));
		while(resultIn.hasNext()){
			
			String line = resultIn.nextLine();
			if(line.length()>0){
				String[] lines = line.split("			");
//				System.out.println(line);
				String word = lines[0];
				double polarity = Double.valueOf(lines[1]);
				int result = 0;
				if(polarity>0.01)
					result = 1;
				else if(polarity<-0.01)
					result = -1;
				
				int gold = 0;
				if(!goldMap.containsKey(word))
//					System.out.println(word);
					;
				else
					gold = goldMap.get(word);
				
				if(gold==1&&result==1)
					a++;
				else if(gold==1&&result==-1){
					System.out.println("b:"+word);
					b++;
				}
				else if(gold==-1&&result==1){
					System.out.println("c:"+word);
					c++;
				}
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
			
		}
		
		
		double precision = (a+d)*1.0/(a+b+c+d);
		double recall = (a+d)*1.0/(a+b+c+d+e+f);
		double f_measure = 2*precision*recall/(precision+recall);
		System.out.println("Precision: "+precision);
		System.out.println("Recall: "+recall);
		System.out.println("F-measure: "+f_measure);

		resultIn.close();
	}
}
