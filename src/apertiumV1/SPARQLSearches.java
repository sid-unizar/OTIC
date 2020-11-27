package apertiumV1;

import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class SPARQLSearches {

	//private static String sparqlEndpoint = "http://localhost:3030/ds/query"; 
	//private static String sparqlEndpoint = "http://linguistic.linkeddata.es/sparql/";
	private static String sparqlEndpoint = "http://localhost:8080/fuseki/data/query";

	public static String getSparqlEndpoint() {return sparqlEndpoint;};
	public static void setSparqlEndpoint(String endpoint) {SPARQLSearches.sparqlEndpoint = endpoint;};
	public SPARQLSearches (String endpoint){setSparqlEndpoint(endpoint);}	
	
	
	/**
	 * return the URI of the translation set of two given languages
	 * @param sourceLanguage
	 * @param targetLanguage
	 * @return
	 */
	public static String obtainTranslationSetFromLanguages(String sourceLanguage, String targetLanguage){		
		String translationSetURI = "";		
		// Create a new query
		String queryString = 
			" PREFIX lemon: <http://www.lemon-model.net/lemon#> " +
			" PREFIX tr: <http://purl.org/net/translation#> " +
			" PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> " +
		    " SELECT DISTINCT ?transSet " +
			" WHERE { " +
			"  ?transSet a tr:TranslationSet ; " + 
			"     lemon:language \"" + sourceLanguage + "\" ;" +
			"     lemon:language \"" + targetLanguage + "\" ." +
			"}";
		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.sparqlService(getSparqlEndpoint(), query);		
		ResultSet results = qe.execSelect();
		//Review results
		for ( ; results.hasNext() ; )    {		
			QuerySolution soln = results.nextSolution() ;
			translationSetURI = soln.get("transSet").toString();							
		}		
		// Important - free up resources used running the query
		qe.close();		
		return translationSetURI;		
	}
	
	/**
	 * returns the URI of the lexicon of a given language
	 * @param lang
	 * @return
	 */
	public static String obtainLexiconFromLanguage(String lang){		
		String lexiconURI = "";		
		//TODO this query should  be restricted to a specific graph (e.g., Apertium) 		
		// Create a new query
		String queryString = 
			" PREFIX lemon: <http://www.lemon-model.net/lemon#> " +
			" PREFIX tr: <http://purl.org/net/translation#> " +
			" PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> " +
		    " SELECT DISTINCT ?lexicon " +
			" WHERE { " +
			"  ?lexicon a lemon:Lexicon ; " + 
			"     lemon:language \"" + lang + "\" ."  +
			"}";
		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.sparqlService(getSparqlEndpoint(), query);		
		ResultSet results = qe.execSelect();
		//Review results
		for ( ; results.hasNext() ; )    {  		
			QuerySolution soln = results.nextSolution() ;
			lexiconURI = soln.get("lexicon").toString();							
		}		
		// Important - free up resources used running the query
		qe.close();		
		return lexiconURI;		
	}

	/**
	 * returns the URIs of the lexical entries that correspond to a direct translation of the given lexical entry in the given dictionary
	 * @param translationSetURI
	 * @param sourceLexicalEntryURI
	 * @return 
	 */
	public static  ArrayList<String> obtainDirectTranslationsFromLexicalEntry(String translationSetURI, String sourceLexicalEntryURI){		
			ArrayList<String> translationLexicalEntries = new ArrayList<String>();		
			// Create a new query
			String queryString = 
				" PREFIX lemon: <http://www.lemon-model.net/lemon#> " +
				" PREFIX tr: <http://purl.org/net/translation#> " +
				" PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> " +
			    " SELECT DISTINCT ?lex_entry_b " +
				" WHERE { " +
			//	"  ?form_a lemon:writtenRep \"" + sourceLabel + "\"@" + sourceLanguage + "." +
			//	"  ?lex_entry_a lemon:lexicalForm ?form_a . " +
				" <" + translationSetURI + "> tr:trans ?trans ." + 
				"  ?trans  tr:translationSense  ?sense_a . " +
				"  ?trans  tr:translationSense  ?sense_b . " +
				"  ?sense_a lemon:isSenseOf  <" + sourceLexicalEntryURI +"> ." +
				"  ?sense_b lemon:isSenseOf  ?lex_entry_b . " +
			//	"  ?lex_entry_b lemon:lexicalForm ?form_b . " +
			//	"  ?form_b lemon:writtenRep ?translated_written_rep . " +
			//	"  ?lex_entry_b  lexinfo:partOfSpeech ?pos . " +
				"  ?lexicon_b lemon:entry ?lex_entry_b . " +
				" MINUS {?lexicon_b lemon:entry <" + sourceLexicalEntryURI +">} . " +
				"}";
			Query query = QueryFactory.create(queryString);
			// Execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.sparqlService(getSparqlEndpoint(), query);			
			ResultSet results = qe.execSelect();
			//Review results
			for ( ; results.hasNext() ; ) {			
				QuerySolution soln = results.nextSolution() ;
				//extract lexical entries of direct translations
				String translationLexicalEntry = new String(soln.get("lex_entry_b").toString());				
				translationLexicalEntries.add(translationLexicalEntry);								
			}			
			// Important - free up resources used running the query
			qe.close();			
			return translationLexicalEntries;		
	}

	/**
	 * Returns the URIs of the lexical entries that correspond to a direct translation of the given lemma in the given dictionary and source language
	 * 
	 * @param translationSetURI
	 * @param sourceLemma
	 * @param sourceLanguage
	 * @return
	 */
	public static  ArrayList<String> obtainDirectTranslationsFromLemma(String translationSetURI, String sourceLemma, String sourceLanguage){		
		ArrayList<String> translationLexicalEntries = new ArrayList<String>();	
		// Create a new query
		String queryString = 
			" PREFIX lemon: <http://www.lemon-model.net/lemon#> " +
			" PREFIX tr: <http://purl.org/net/translation#> " +
			" PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> " +
		    " SELECT DISTINCT ?lex_entry_b " +
			" WHERE { " +
			"  ?form_a lemon:writtenRep \"" + sourceLemma + "\"@" + sourceLanguage + "." +
			"  ?lex_entry_a lemon:lexicalForm ?form_a . " +
			" <" + translationSetURI + "> tr:trans ?trans ." + 
			"  ?trans  tr:translationSense  ?sense_a . " +
			"  ?trans  tr:translationSense  ?sense_b . " +
			"  ?sense_a lemon:isSenseOf  ?lex_entry_a ." +
			"  ?sense_b lemon:isSenseOf  ?lex_entry_b . " +
		//	"  ?lex_entry_b lemon:lexicalForm ?form_b . " +
		//	"  ?form_b lemon:writtenRep ?translated_written_rep . " +
		//	"  ?lex_entry_b  lexinfo:partOfSpeech ?pos . " +
			"  ?lexicon_b lemon:entry ?lex_entry_b . " +
			" MINUS {?lexicon_b lemon:language \"" + sourceLanguage +"\"} . " +
			"}";
		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.sparqlService(getSparqlEndpoint(), query);		
		ResultSet results = qe.execSelect();
		//Review results
		for ( ; results.hasNext() ; )    {		
			QuerySolution soln = results.nextSolution() ;
			//extract lexical entries of direct translations
			String translationLexicalEntry = new String(soln.get("lex_entry_b").toString());			
			translationLexicalEntries.add(translationLexicalEntry);							
		}  		
		// Important - free up resources used running the query
		qe.close();		
		return translationLexicalEntries;	
	}


	/**
	 * returns the URIs of the lexical entries that correspond to a direct translation of the given lexical entry in the given dictionary
	 * @param translationSetURI
	 * @param sourceLexicalEntryURI
	 * @return
	 */
	public static  ArrayList<String> obtainLexicalEntriesFromLemma(String lexiconURI, String lemma, String sourceLanguage){		
			ArrayList<String> lexicalEntries = new ArrayList<String>();		
			// Create a new query
			String queryString = 
				" PREFIX lemon: <http://www.lemon-model.net/lemon#> " +
				" PREFIX tr: <http://purl.org/net/translation#> " +
				" PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> " +
			    " SELECT DISTINCT ?lex_entry " +
				" WHERE { " +
				"  ?form lemon:writtenRep \"" + lemma + "\"@" + sourceLanguage + "." +
				"  ?lex_entry lemon:lexicalForm ?form . " +
				" <" + lexiconURI + "> lemon:entry ?lex_entry ." +
				"}";
			Query query = QueryFactory.create(queryString);
			// Execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.sparqlService(getSparqlEndpoint(), query);			
			ResultSet results = qe.execSelect();
			//Review results
			for ( ; results.hasNext() ; ) {			
				QuerySolution soln = results.nextSolution() ;
				//extract lexical entries of direct translations
				String translationLexicalEntry = new String(soln.get("lex_entry").toString());				
				lexicalEntries.add(translationLexicalEntry);								
			}			
			// Important - free up resources used running the query
			qe.close();			
			return lexicalEntries;		
	}	
	
	//uncomment for testing
//	public static void main(String[] args) {
//		ArrayList<String> le_translations = SPARQLSearches.obtainDirectTranslationsFromLexicalEntry("http://linguistic.linkeddata.es/id/apertium/tranSetEN-CA", "http://linguistic.linkeddata.es/id/apertium/lexiconCA/jacobita-adj-ca");
//		for (String le: le_translations)
//			System.out.println(le.toString());	
//	}

}
