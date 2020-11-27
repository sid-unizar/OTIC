package apertiumV2;

import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class SPARQLSearchesV2 {

	//private static String sparqlEndpoint = "http://dbserver.acoli.cs.uni-frankfurt.de:5005/ds/query";
	private static String sparqlEndpoint = "http://localhost:8080/fuseki/ds/query";

	public static String getSparqlEndpoint() {return sparqlEndpoint;};
	public static void setSparqlEndpoint(String endpoint) {SPARQLSearchesV2.sparqlEndpoint = endpoint;};
	public SPARQLSearchesV2 (String endpoint){setSparqlEndpoint(endpoint);}	
	
	
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
			"PREFIX vartrans: <http://www.w3.org/ns/lemon/vartrans#>" + 
			"PREFIX lime: <http://www.w3.org/ns/lemon/lime#>" + 
			"SELECT DISTINCT ?transSet" + 
			"\nWHERE {" + 
			//"GRAPH <http://linguistic.linkeddata.es/id/apertium-lexinfo/>" + 
			//"	{" + 
			"	?transSet a vartrans:TranslationSet ; \n" + 
			"			  lime:language \"" + sourceLanguage + "\" ;" + 
			"			  lime:language \"" + targetLanguage + "\" ." +
			//"	}" + 
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
			" PREFIX lime: <http://www.w3.org/ns/lemon/lime#>" + 
			" SELECT DISTINCT ?lexicon " +
			" WHERE { " +
			//" GRAPH <http://linguistic.linkeddata.es/id/apertium-lexinfo/>" + 
			//"     {" + 
			"  ?lexicon a lime:Lexicon ; " + 
			"     		lime:language \"" + lang + "\" ." +
			//"}"  +
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
			" PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>" + 
			" PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>" + 
			" PREFIX vartrans: <http://www.w3.org/ns/lemon/vartrans#>" + 
			" PREFIX lime: <http://www.w3.org/ns/lemon/lime#>" + 
		    " SELECT DISTINCT ?lex_entry_b " +
			" WHERE { " +											
			//" GRAPH <http://linguistic.linkeddata.es/id/apertium-lexinfo/>" + 
			//"    {" + 
			" <" + translationSetURI + "> vartrans:trans ?trans ." + 
			" {?trans vartrans:source ?sense_a;" + 
			"         vartrans:target ?sense_b}UNION" + 
			" {?trans vartrans:target ?sense_a;" + 
			"         vartrans:source ?sense_b}" + 
			" ?sense_a ontolex:isSenseOf  <" + sourceLexicalEntryURI +"> ." +
			" ?sense_b ontolex:isSenseOf  ?lex_entry_b . " +
			" ?lexicon_b lime:entry ?lex_entry_b . " +
			" MINUS {?lexicon_b lime:entry <" + sourceLexicalEntryURI +">} . " +
			//"}" +
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
			" PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>" + 
			" PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>" + 
			" PREFIX vartrans: <http://www.w3.org/ns/lemon/vartrans#>" + 
			" PREFIX lime: <http://www.w3.org/ns/lemon/lime#>" + 
			" SELECT DISTINCT ?lex_entry_b " +
			" WHERE {" + 
			//" GRAPH <http://linguistic.linkeddata.es/id/apertium-lexinfo/>" +
			//"    {" +
			" ?form_a ontolex:writtenRep \"" + sourceLemma + "\"@" + sourceLanguage + "." +
			" ?lex_entry_a ontolex:lexicalForm ?form_a . " +
			" <" + translationSetURI + "> vartrans:trans ?trans ." + 
			" {?trans vartrans:source ?sense_a;" + 
			"         vartrans:target ?sense_b}UNION" + 
			" {?trans vartrans:target ?sense_a;" + 
			"         vartrans:source ?sense_b}" + 
			" ?sense_a ontolex:isSenseOf  ?lex_entry_a ." +
			" ?sense_b ontolex:isSenseOf  ?lex_entry_b . " +
			" ?lexicon_b lime:entry ?lex_entry_b . " +
			" MINUS {?lexicon_b lime:language \"" + sourceLanguage +"\"} . " +
			//"}" +
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
				" PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>" + 
				" PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>" + 
				" PREFIX vartrans: <http://www.w3.org/ns/lemon/vartrans#>" + 
				" PREFIX lime: <http://www.w3.org/ns/lemon/lime#>" + 
			    " SELECT DISTINCT ?lex_entry " +			
				" WHERE { " +
				//" GRAPH <http://linguistic.linkeddata.es/id/apertium-lexinfo/>" +
				//"    {" +
				"  ?form ontolex:writtenRep \"" + lemma + "\"@" + sourceLanguage + "." +
				"  ?lex_entry ontolex:lexicalForm ?form . " +
				" <" + lexiconURI + "> lime:entry ?lex_entry ." +
				//"}" +
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
	

}
