package cike.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cike.bean.Seed;

public class SeedHelper {

	static public ArrayList<Seed> seedlist;
	
	public static void main(String[] args) {
	}
	
	
	/**获取seedlist，修改seed.txt文件可以更改seed列表
	 * @return
	 */
	static public ArrayList<Seed> getseedlist(){
		seedlist = new ArrayList<Seed>();
		
		Scanner seedscan;
		try {
			seedscan = new Scanner(new File("positive.txt"));
			while(seedscan.hasNext()){
				String temp = seedscan.nextLine();
				seedlist.add(new Seed(temp, 1));
			}
			seedscan.close();
			
			seedscan = new Scanner(new File("negative.txt"));
			while(seedscan.hasNext()){
				String temp = seedscan.nextLine();
				seedlist.add(new Seed(temp, -1));
			}
			seedscan.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	/**查询该词的极性
	 * @param query
	 * @return 有的话返回1/-1，没有的话返回0
	 */
	static public double getpolarity(String query){
		for(Seed s: seedlist){
			if(query.equalsIgnoreCase(s.getWord()))	return s.getPolarity();
		}
		return 0;
	}
	
	
	/**查询该词的极性,指定一个lexicon
	 * @param query 查询词
	 * @param lexicon 指定lexicon
	 * @return 有的话返回1/-1，没有的话返回0
	 */
	static public double getpolarity(String query, ArrayList<Seed> lexicon){
		for(Seed s: lexicon){
			if(query.equalsIgnoreCase(s.getWord()))	return s.getPolarity();
		}
		return 0;
	}
	
	
}
