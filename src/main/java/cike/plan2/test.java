package cike.plan2;

public class test {

	public static void main(String[] args) {
		String a = "wqx n,i mei ,, wqx ni ge";
		String[] aa = a.split(",,");
		
		for(String t : aa){
			System.out.println(t);
		}
	}
}
