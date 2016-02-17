package cike.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

public class WordNetTools {

	
	static public IDictionary dict;
	static public URL url;
	
	static {
		String wnhome = System . getenv ("WNHOME");
		String path = wnhome+File.separator+"dict";
		
		try {
			url = new URL("file", null , path );
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dict = new Dictionary (url);
		try {
			dict.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		
		
		String query = "big";
		for(String word: getSynonymsLes(query)){
			System.out.println(word);
		}
		System.out.println("-----------------");
		for(String word: getAntonyms2(query)){
			System.out.println(word);
		}
	}
	
	public static LinkedList<String> getSynonymsLes (String query){
		
		LinkedList<String> synList = new LinkedList<String>();
		
		IIndexWord idxWord = dict.getIndexWord (query, POS.ADJECTIVE );
		if(idxWord!=null){
			IWord word = dict.getWord(idxWord.getWordIDs().get(0));
			ISynset synset = word.getSynset();
			
			for(IWord w : synset.getWords ()){
				if(!synList.contains(w.getLemma())){
					synList.add(w.getLemma());
				}
			}
		}
		return synList;
	}
	
	public static LinkedList<String> getSynonyms (String query){
		
		LinkedList<String> synList = new LinkedList<String>();
		
		IIndexWord idxWord = dict.getIndexWord (query, POS.ADJECTIVE );
//		IIndexWord idxWord2 = dict.getIndexWord (query, POS.ADVERB );
		if(idxWord!=null){
			for(IWordID wordID: idxWord.getWordIDs()){
				IWord word = dict.getWord(wordID);
				ISynset synset = word.getSynset();
				
				for(IWord w : synset.getWords ()){
					if(!synList.contains(w.getLemma())){
						synList.add(w.getLemma());
//						System.out.println(w.getLemma());
					}
				}
			}
		}
//		if(idxWord2!=null){
//			for(IWordID wordID: idxWord2.getWordIDs()){
//				IWord word = dict.getWord(wordID);
//				ISynset synset = word.getSynset();
//				
//				for(IWord w : synset.getWords ()){
//					if(!synList.contains(w.getLemma())){
//						synList.add(w.getLemma());
////						System.out.println(w.getLemma());
//					}
//				}
//			}
//		}
		return synList;
	}
	
	public static LinkedList<String> getAntonyms (String query){
		
		LinkedList<String> antonList = new LinkedList<String>();
		
		
		IIndexWord idxWord = dict.getIndexWord (query, POS.ADJECTIVE );
//		IIndexWord idxWord2 = dict.getIndexWord (query, POS.ADVERB );
		if(idxWord!=null){
			for(IWordID wordID: idxWord.getWordIDs()){
				IWord word1 = dict.getWord(wordID);
				List<IWordID> list = word1.getRelatedWords(edu.mit.jwi.item.Pointer.ANTONYM);;
				
				for(IWordID wordID2: list){
					IWord word = dict.getWord(wordID2);
					ISynset synset = word.getSynset();
					
					for(IWord w : synset.getWords ()){
						if(!antonList.contains(w.getLemma())){
							antonList.add(w.getLemma());
//							System.out.println(w.getLemma());
						}
					}
				}
			}
		}
//		if(idxWord2!=null){
//			for(IWordID wordID: idxWord2.getWordIDs()){
//				IWord word1 = dict.getWord(wordID);
//				List<IWordID> list = word1.getRelatedWords(edu.mit.jwi.item.Pointer.ANTONYM);;
//				
//				for(IWordID wordID2: list){
//					IWord word = dict.getWord(wordID2);
//					ISynset synset = word.getSynset();
//					
//					for(IWord w : synset.getWords ()){
//						if(!antonList.contains(w.getLemma())){
//							antonList.add(w.getLemma());
////							System.out.println(w.getLemma());
//						}
//					}
//				}
//			}
//		}
		
		return antonList;
	}
	
	public static LinkedList<String> getAntonyms2 (String query){
		
		LinkedList<String> antonList = new LinkedList<String>();
		

		LinkedList<String> synList = getSynonymsLes(query);
		
		for(String syn: synList){
			for(String word: getAntonyms(syn)){
				if(!antonList.contains(word)){
					antonList.add(word);
				}
			}
		}
		
		return antonList;
	}
}














