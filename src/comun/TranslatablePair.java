package comun;

import java.io.PrintWriter;
import java.net.URI;

public class TranslatablePair{
	
	final static String SEPARATOR = "\t";		
	String sourceLabel;
	String sourceLexicalEntryURI;
	String targetLexicalEntryURI;
	String targetLabel;
	String posURI;
	double score;
	
	public TranslatablePair(String sourceLabel, String sourceLexicalEntry2,  String targetTranslation, String targetLabel, String pos, double score){		
		this.sourceLabel = sourceLabel;
		this.sourceLexicalEntryURI = sourceLexicalEntry2;
		this.targetLexicalEntryURI = targetTranslation;
		this.targetLabel = targetLabel;
		this.posURI = pos;
		this.score = score;	
	}
	
	public String getSourceLabel(){ return this.sourceLabel;}
	public String getTargetLabel(){ return this.targetLabel;}
	public String getSourceLexicalEntry(){ return this.sourceLexicalEntryURI;}
	public String getTargetLexicalEntry(){ return this.targetLexicalEntryURI;}
	public String getPos(){ return this.posURI;};
	public void setScore(Double score){ this.score = score;}
	public double getScore(){ return this.score;}
	public String getFragmentPos() { return URI.create(this.posURI).getFragment();}	
	
	public void print(){
		System.out.print(getSourceLabel() + SEPARATOR + getTargetLabel().substring(0, getTargetLabel().indexOf("@")) + SEPARATOR + getFragmentPos() + SEPARATOR + getScore());
		System.out.println();
	}
	
	public void print(String le, String pivotLanguage) {
		String posApertium = " ";
		if(le.contains("-")) {
			le = le.substring(le.indexOf("-")+1);
			if(le.contains("-")) 
				posApertium = le.substring(0, le.lastIndexOf("-"));
		}		
		
		if(getTargetLabel() != null || posURI != null)
			System.out.println(getSourceLabel() + SEPARATOR + getTargetLabel().substring(0, getTargetLabel().length()-3) + SEPARATOR + getFragmentPos() + SEPARATOR + posApertium + SEPARATOR + getScore() + SEPARATOR + pivotLanguage);	
	}
	public void printToFile(PrintWriter writer){
		writer.println(getSourceLabel() + SEPARATOR + getTargetLabel().substring(0, getTargetLabel().length()-3) + SEPARATOR + getFragmentPos() + SEPARATOR + getScore());	
		writer.flush();
	}
	
	public void printToFile(PrintWriter writer, String le, String pivotLanguage){
		String posApertium = " ";
		if(le.contains("-")) {
			le = le.substring(le.indexOf("-")+1);
			if(le.contains("-")) 
				posApertium = le.substring(0, le.lastIndexOf("-"));
		}		

		if(getTargetLabel() != null || posURI != null)
			writer.println(getSourceLabel() + SEPARATOR + getTargetLabel().substring(0, getTargetLabel().length()-3) + SEPARATOR + getFragmentPos() + SEPARATOR + posApertium + SEPARATOR + getScore() + SEPARATOR + pivotLanguage);	
			writer.flush();
	}
}