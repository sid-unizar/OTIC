package apertiumV2;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import comun.TranslatablePair;

public class OneTimeInverseConsultationV2 {

	
	/**
	 *  Given a lexical entry (and its associated lemma) in a source language, it returns its possible translations into a target language through a given pivot language,
	 * along with their score, in an array of TranslatablePair objects. The scores are obtained based on the "One Time Single Consultation" algorithm
	 * @param sourceLemma
	 * @param sourceLexicalEntry
	 * @param sourceLanguage
	 * @param pivotLanguage
	 * @param targetLanguage
	 * @return translatable pairs
	 */
	public  ArrayList<TranslatablePair> obtainTranslationScoresFromLexicalEntry(String sourceLemma, String sourceLexicalEntry, String sourceLanguage, String pivotLanguage, String targetLanguage){
		ArrayList<TranslatablePair> translatablePairs = new ArrayList<TranslatablePair>();		
		String translationSetURI1 = SPARQLSearchesV2.obtainTranslationSetFromLanguages(sourceLanguage, pivotLanguage);
		String translationSetURI2 = SPARQLSearchesV2.obtainTranslationSetFromLanguages(pivotLanguage, targetLanguage);		
		translatablePairs = obtainTranslationScoresFromLexicalEntry(sourceLemma, sourceLexicalEntry, translationSetURI1, translationSetURI2);
		return translatablePairs;
	}
	
	/**
	 * Given a lemma in a source language, it returns their possible translations into a target language through a given pivot language,
	 * along with their score, in an array of TranslatablePair objects. The scores are obtained based on the "One Time Single Consultation" algorithm
	 * @param sourceLemma
	 * @param sourceLanguage
	 * @param pivotLanguage
	 * @param targetLanguage
	 * @return translatable pairs
	 */
	public  ArrayList<TranslatablePair> obtainTranslationScoresFromLemma(String sourceLemma, String sourceLanguage, String pivotLanguage, String targetLanguage){
		ArrayList<TranslatablePair> translatablePairs = new ArrayList<TranslatablePair>();		
		String translationSetURI1 = SPARQLSearchesV2.obtainTranslationSetFromLanguages(sourceLanguage, pivotLanguage);
		String translationSetURI2 = SPARQLSearchesV2.obtainTranslationSetFromLanguages(pivotLanguage, targetLanguage);		
		String sourceLexicon = SPARQLSearchesV2.obtainLexiconFromLanguage(sourceLanguage);			
		// Get Lexical entries associated to source label
		ArrayList<String> sourceLexicalEntries = SPARQLSearchesV2.obtainLexicalEntriesFromLemma(sourceLexicon, sourceLemma, sourceLanguage);		
		for (String sourceLexicalEntry:sourceLexicalEntries)
			translatablePairs.addAll(obtainTranslationScoresFromLexicalEntry(sourceLemma, sourceLexicalEntry, translationSetURI1, translationSetURI2));			
		return translatablePairs;
	}
	
	/**
	 * Given a lexical entry (and its associated lemma) in a source language, it returns its possible translations into a target language through a given pivot language,
	 * along with their score, in an array of TranslatablePair objects. The scores are obtained based on the "One Time Single Consultation" algorithm
	 * 
	 * @param sourceLemma
	 * @param sourceLexicalEntry
	 * @param translationSetURI1 translation set from source language into pivot language
	 * @param translationSetURI2 translation set from pivot language into target language
	 * @return translatable pairs
	 */	
	public  ArrayList<TranslatablePair> obtainTranslationScoresFromLexicalEntry(String sourceLemma, String sourceLexicalEntry, String translationSetURI1, String translationSetURI2){
		ArrayList<TranslatablePair> translatablePairs = new ArrayList<TranslatablePair>(); // to collect the output
		Set<String> P1 = new HashSet<String>();  // to store translations in the pivot language (P1)
		Set<String> T = new HashSet<String>();  //  to store translations in the target language (T)
		
		// 1. For the source lexical entry, look up all translations in the pivot language (P1).			
		//get translations for the source lexical entry
		ArrayList<String> pivotTranslations = SPARQLSearchesV2.obtainDirectTranslationsFromLexicalEntry(translationSetURI1, sourceLexicalEntry.toString());		
		//populate set of pivot translations (P1)
		P1.addAll(pivotTranslations);

		// 2. For every pivot translation of every source lexical entry, look up its target translations (T).
		for (String pivotTranslation: pivotTranslations){			
			ArrayList<String> newTargetTranslations= SPARQLSearchesV2.obtainDirectTranslationsFromLexicalEntry(translationSetURI2, pivotTranslation.toString());
			newTargetTranslations.removeAll(T); //retains only those translations that are really new ones			
			//Create the array of translatable pairs leaving the scores empty for the moment
			for (String targetTranslation: newTargetTranslations){				
				//Get lemma and pos of the targetTraslation
				String pos = null;
				String targetLemma = null;
				String queryString = 
					" PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>" + 
					" PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> " +
				    " SELECT DISTINCT ?lemma ?pos " +
					" WHERE { " +
					//" GRAPH <http://linguistic.linkeddata.es/id/apertium-lexinfo/>" + 
					//" { " +
					" <" + targetTranslation + "> ontolex:lexicalForm ?form ;" + 
					"          lexinfo:partOfSpeech ?pos." +
					"  ?form ontolex:writtenRep ?lemma ." +
					//"}" +
					"}";
				Query query = QueryFactory.create(queryString);
				QueryExecution qe = QueryExecutionFactory.sparqlService(SPARQLSearchesV2.getSparqlEndpoint(), query);
				ResultSet results = qe.execSelect();
				for ( ; results.hasNext() ; )    {				
					QuerySolution soln = results.nextSolution() ;
					//extract lexical entries of direct translations
					targetLemma = soln.get("lemma").toString();
					pos = soln.get("pos").toString();
				}
				translatablePairs.add(new TranslatablePair(sourceLemma, sourceLexicalEntry, targetTranslation, targetLemma, pos, -1.0));				
				T.addAll(newTargetTranslations);
				qe.close();
			}
		}
			
		// 3. For every target translation, look up its pivot translations (P2)
		for (TranslatablePair ts:translatablePairs){	
				Set<String> P2 = new HashSet<String>();				
				P2.addAll(SPARQLSearchesV2.obtainDirectTranslationsFromLexicalEntry(translationSetURI2, ts.getTargetLexicalEntry().toString()));
				
				// 4. Measure how translations in P2 match those in P1. For each t in T, the more matches 
				// between P1 and P2, the better t is as a candidate translation of the original
				// Formula: 	score(t) = 2 ×(P1 ∩ P2)/P1+P2      
				Set<String> intersection = new HashSet<String>();
				intersection.addAll(P1);
				intersection.retainAll(P2);
				double score = 2.0 * intersection.size()/(P1.size() + P2.size());				
				// add all the information in the translation score object				
				ts.setScore(score);
				P2.clear();
		}		
		return translatablePairs;
	}
	
	public void printTranslationstoFile(String outputFile, ArrayList<TranslatablePair> translatablePairs) {
		try {
			PrintWriter writer = new PrintWriter(outputFile);			
			for (TranslatablePair ts: translatablePairs){
				ts.print();
				ts.printToFile(writer);
			}			
			writer.close();
		}catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
	}	
	
	//uncomment for testing
	public static void main(String[] args) { 
		String outputFile = "./data/OTIC_trans.tsv";
		OneTimeInverseConsultationV2 otic = new OneTimeInverseConsultationV2();
		ArrayList<TranslatablePair> translatablePairs = otic.obtainTranslationScoresFromLemma("dog", "en", "es", "fr");	
		otic.printTranslationstoFile(outputFile, translatablePairs);		
	}

}
