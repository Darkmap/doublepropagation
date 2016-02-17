package cike.bean;

import java.util.List;

import edu.stanford.nlp.trees.TypedDependency;

public class ParseDependencies {

	public List<TypedDependency> dependencies;
	
	public ParseDependencies(List<TypedDependency> dplist) {
		dependencies = dplist;
	}
	
	/**如果idx词作为relation中的统治者
	 * @param idx
	 * @param rel
	 */
	public int idxGoverRelation(int idx, String rel){
		
		for(TypedDependency depen: dependencies){
			
			//获取关系名称
			String dRel =  depen.reln().toString();
			
			String tempg = depen.gov().toString();
			//打断“home-7”这样的输出结果
			String[] garray = tempg.split("-");
			//为了处理“8‘，8''这样的输出”
			String temp = garray[garray.length-1];
			if(temp.contains("'")){
				temp = temp.split("'")[0];
			}
			//所有者(ex: amod(NN->JJ))中的NN 的index
			int indexG = Integer.valueOf(temp)-1;
			String tempd = depen.dep().toString();
			//打断“home-7”这样的输出结果
			String[] darray = tempd.split("-");
			//为了处理“8‘，8''这样的输出”
			String temp2 = darray[darray.length-1];
			if(temp2.contains("'")){
				temp2 = temp2.split("'")[0];
			}
			//依赖者(ex: amod(NN->JJ))中的JJ 的index
			int indexD = Integer.valueOf(temp2)-1;
			if(idx==indexG && rel.equalsIgnoreCase(dRel))
				return indexD;
			
		}
		return -1;
	}
	
	/**如果idx词作为relation中的依赖者
	 * @param idx
	 * @param rel
	 */
	public int idxDepRelation(int idx, String rel){
		
		for(TypedDependency depen: dependencies){
			
			//获取关系名称
			String dRel =  depen.reln().toString();
			
			String tempg = depen.gov().toString();
			//打断“home-7”这样的输出结果
			String[] garray = tempg.split("-");
			//为了处理“8‘，8''这样的输出”
			String temp = garray[garray.length-1];
			if(temp.contains("'")){
				temp = temp.split("'")[0];
			}
			//所有者(ex: amod(NN->JJ))中的NN 的index
			int indexG = Integer.valueOf(temp);
			
			String tempd = depen.dep().toString();
			//打断“home-7”这样的输出结果
			String[] darray = tempd.split("-");
			//为了处理“8‘，8''这样的输出”
			String temp2 = darray[darray.length-1];
			if(temp2.contains("'")){
				temp2 = temp2.split("'")[0];
			}
			//依赖者(ex: amod(NN->JJ))中的JJ 的index
			int indexD = Integer.valueOf(temp2);
			if(idx==indexD && rel.equalsIgnoreCase(dRel))
				return indexG;
		}
		return -1;
	}
	
	/**idxd和idxg之间存在rel关系
	 * @param idxd
	 * @param idxg
	 * @param rel
	 * @return
	 */
	public boolean hasRelation(int idxg, int idxd, String rel){ 
		
		for(TypedDependency depen: dependencies){
			
			//获取关系名称
			String dRel =  depen.reln().toString();
			
			
			String tempg = depen.gov().toString();
			//打断“home-7”这样的输出结果
			String[] garray = tempg.split("-");
			//为了处理“8‘，8''这样的输出”
			String temp = garray[garray.length-1];
			if(temp.contains("'")){
				temp = temp.split("'")[0];
			}
			//所有者(ex: amod(NN->JJ))中的NN 的index
			int indexG = Integer.valueOf(temp);
			
			String tempd = depen.dep().toString();
			//打断“home-7”这样的输出结果
			String[] darray = tempd.split("-");
			//为了处理“8‘，8''这样的输出”
			String temp2 = darray[darray.length-1];
			if(temp2.contains("'")){
				temp2 = temp2.split("'")[0];
			}
			//依赖者(ex: amod(NN->JJ))中的JJ 的index
			int indexD = Integer.valueOf(temp2);
			
			if(idxd==indexD && idxg==indexG && rel.equalsIgnoreCase(dRel))
				return true;
		}
		return false;
	}
}
