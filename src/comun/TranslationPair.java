package comun;

import java.io.PrintWriter;
import java.net.URI;

public class TranslationPair {
	
		final static String SEPARATOR = "\t";		
		private String sourceLemma = null;
		private URI sourceLexicalEntry = null;
		private URI sourceSense = null;
		private URI translation = null;
		private URI targetSense = null;
		private URI targetLexicalEntry = null;
		private String targetLemma = null;
		private URI pos = null;
		private String sourceLanguage = null, targetLanguage = null; 
		
		TranslationPair (String sourceLemma, URI sourceLexicalEntry, URI sourceSense, URI translation,  URI targetSense, URI targetLexicalEntry, String targetLemma, URI pos, String sourceLanguage, String targetLanguage){
			this.sourceLemma = sourceLemma;
			this.sourceLexicalEntry = sourceLexicalEntry;
			this.sourceSense = sourceSense;
			this.translation = translation;
			this.targetSense = targetSense;
			this.targetLexicalEntry = targetLexicalEntry;
			this.targetLemma = targetLemma;
			this.pos = pos;
			this.sourceLanguage = sourceLanguage;
			this.targetLanguage = targetLanguage;
		}
	
		String getSourceLemma(){ return this.sourceLemma;}
		URI getSourceLexicaEntry(){ return this.sourceLexicalEntry;}
		URI getSourceSense(){ return this.sourceSense;}
		URI getTranslation(){ return this.translation;}
		URI getTargetSense(){ return this.targetSense;}
		URI getTargetLexicaEntry(){ return this.targetLexicalEntry;}
		String getTargetLemma(){ return this.targetLemma;}
		URI getPos(){ return this.pos;}
		String getLanguageSource(){ return this.sourceLanguage;}
		String getLanguageTarget(){ return this.targetLanguage;}
		
		public void print(){
			System.out.print(this.sourceLemma + " -> " + this.targetLemma + " " + this.pos.getFragment());
			System.out.println();
		}
		
		public void printToFile(PrintWriter writer){
			writer.print(this.sourceLemma + SEPARATOR + this.targetLemma + SEPARATOR + this.pos.getFragment() + SEPARATOR);
			writer.println();
		}
		

}
