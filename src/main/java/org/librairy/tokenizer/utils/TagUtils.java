package org.librairy.tokenizer.utils;

import java.util.*;
import java.util.Map.Entry;

public class TagUtils {

	public static String cleanCornerCases(String rawAnnotations) {
		rawAnnotations = rawAnnotations.replaceAll("et_al.", "");
		rawAnnotations = rawAnnotations.replaceAll("et_al", "");

		return rawAnnotations;
	}
	
	public static List<Tag> rankAnnotations(String[] annotations, String type, double correction) {
		List<Tag> result = new ArrayList<Tag>();

		if (annotations.length==0)return result;
	    Hashtable<String, ArrayList<String>> tokensCount = new Hashtable<String, ArrayList<String>>();
	    for (String token : annotations){
	    	if (!containsKeyTable(tokensCount, token)){
	    		ArrayList<String> listWords = new ArrayList<String>();
	    		listWords.add(token);
	    		tokensCount.put(token, listWords);
	 
	    	}
	    }
	    
	    ArrayList<Entry<?, ArrayList<String>>> l = new ArrayList<Entry<?, ArrayList<String>>>(tokensCount.entrySet());
	    Collections.sort(l, new Comparator<Entry<?, ArrayList<String>>>(){

	      public int compare(Entry<?, ArrayList<String>> o1, Entry<?, ArrayList<String>> o2) {
	         return Integer.compare(o2.getValue().size(), o1.getValue().size());
	     }});


	    int maxFreq = l.get(0).getValue().size();
	    for (Entry<?, ArrayList<String>> word : l){
	    	//System.out.println(type +": "+word.getKey() + "  " + word.getValue());
	    	Tag t = new Tag();
	    	t.setScore((double)word.getValue().size()/maxFreq * correction);
	    	t.setSurface(maxLengthSurface (word.getValue()));
	    	t.setType(type);
	    	result.add(t);
	    }
	    
	    return result;
	}

	private static String maxLengthSurface(ArrayList<String> value) {
		String maxSurface =value.get(0);
		int maxSurfaceLength = value.get(0).length();
		for (String compound : value){
			if (compound.length() > maxSurfaceLength){
				maxSurface = compound;
				maxSurfaceLength = compound.length();
			}
		}
		return maxSurface ;
	}

	private static boolean containsKeyTable(Hashtable<String, ArrayList<String>> tokensCount, String token) {
		
		Iterator<Entry<String, ArrayList<String>>> iterator = tokensCount.entrySet().iterator();
		while (iterator.hasNext()){
			Entry<String, ArrayList<String>> entry = iterator.next();
			
			if (LetterPairSimilarity.compareStrings(entry.getKey(), token)>0.7){
				entry.getValue().add(token);
				return true;
			}
		}
		
			
		return false;
	}

}
