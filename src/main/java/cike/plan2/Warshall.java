package cike.plan2;
import java.util.ArrayList;

public class Warshall {

	
	public static void main(String[] args) {
		
		ArrayList<ArrayList<Boolean>> matrix = new ArrayList<ArrayList<Boolean>>();
		for(int i=0;i<4;i++){
			ArrayList<Boolean> list = new ArrayList<Boolean>();
			for(int j=0;j<4;j++){
				list.add(false);
			}
			matrix.add(list);
		}
		
		matrix.get(0).set(1,true);
		matrix.get(1).set(3,true);
		matrix.get(3).set(0,true);
		matrix.get(3).set(2,true);
		
		Warshall.run(matrix);
		
		for(int i=0;i<4;i++){
			for(int j=0;j<4;j++){
				System.out.print(matrix.get(i).get(j)+" ");
			}
			System.out.println();
		}
	}
	
	
	
	
	
	static public ArrayList<ArrayList<Boolean>> run(ArrayList<ArrayList<Boolean>> matrix){
		
		ArrayList<ArrayList<Boolean>> newMartix = matrix;
		int n = newMartix.size();
		for(int k=0;k<n;k++){
			
			for(int i=0;i<n;i++){
				
				for(int j=0;j<n;j++){
					if(i==j)
						continue;
					newMartix.get(i).set(j, newMartix.get(i).get(j)||(newMartix.get(i).get(k))&&newMartix.get(k).get(j));
				}
			}
			
			
		}
		return newMartix;
	}
}
