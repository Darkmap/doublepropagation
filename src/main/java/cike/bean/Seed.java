package cike.bean;

/**情感词对象
 * @author Qixuan
 *
 */
public class Seed {

	private String word;
	private double polarity;
	
	public Seed(String word, double polarity) {
		super();
		this.word = word;
		this.polarity = polarity;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public double getPolarity() {
		return polarity;
	}

	public void setPolarity(int polarity) {
		this.polarity = polarity;
	}
	
	
}
