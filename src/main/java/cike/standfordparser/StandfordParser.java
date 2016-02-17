package cike.standfordparser;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class StandfordParser {
	
	
	private static String grammar = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	private String[] options = { "-maxLength", "100", "-retainTmpSubcategories" };
	private static LexicalizedParser lp;
	private static TreebankLanguagePack tlp;
	private static GrammaticalStructureFactory gsf;
    
    
	static {
	    lp = LexicalizedParser.loadModel(grammar);
	    tlp = new PennTreebankLanguagePack();
	    gsf = tlp.grammaticalStructureFactory();
	    
	}
	
	public static void main(String[] args) {
		
		StandfordParser parser = new StandfordParser();
		parser.sentencePOS("The small camera is portable.");
	}
	

	
	
	public static ArrayList<TaggedWord> sentencePOS(String sent){
		Iterable<List<? extends HasWord>> sentences;

		Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory()
				.getTokenizer(new StringReader(sent));
		List<? extends HasWord> sentence = toke.tokenize();

		Tree parse = lp.apply(sentence);
		parse.pennPrint();
		ArrayList<TaggedWord> wordlist = parse.taggedYield();
//		for (TaggedWord tw : wordlist) {
////			System.out.println(tw);
////			System.out.println(tw.tag());
//			System.out.println(tw.word());
//			System.out.println("************************************");
//		}
		return wordlist;
	}
	
	/**为指定句子生成一个parse tree
	 * @param sent
	 * @return
	 */
	public static Tree getParseTree(String sent){
		Tree parse = lp.parse(sent);
		return parse;
	}
	
	public static List<TypedDependency> sentenceparser(String sent){


		Iterable<List<? extends HasWord>> sentences;

		Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory()
				.getTokenizer(new StringReader(sent));
		List<? extends HasWord> sentence = toke.tokenize();

		Tree parse = lp.apply(sentence);

		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed(true);
		
//		for(TypedDependency td: tdl){
////			System.out.println(td.reln()+":");
//			System.out.println(td.dep());
//			System.out.println(td.gov());
////			System.out.println("-------------");
////
//		}
		
		return tdl;
	}
	
	public static List<TypedDependency> sentenceparser(String sent, PrintWriter out){


		Iterable<List<? extends HasWord>> sentences;

		Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory()
				.getTokenizer(new StringReader(sent));
		List<? extends HasWord> sentence = toke.tokenize();

		Tree parse = lp.apply(sentence);

		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed(true);
		
//		for(TypedDependency td: tdl){
//			out.println(td.reln()+":");
//			out.println(td.dep());
//			out.println(td.gov());
//			out.println("-------------");
//		}
		
		return tdl;
	}
}
