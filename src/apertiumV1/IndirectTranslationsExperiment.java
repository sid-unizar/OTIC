package apertiumV1;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
/*
 * This class computes "One Time Inverse Consultation" algorithm to obtain translation pairs between two unconnected languages and prints those translations to a .tsv file
 */

import comun.TranslatablePair;

public class IndirectTranslationsExperiment {
	
	final static String SEPARATOR = "\t";

	public void createTranslatablePairsWithScoreFile (String sparqlEndpoint, String sourceLanguage, String pivotLanguage, String targetLanguage, String outputFile){
		
		OneTimeInverseConsultation otic = new OneTimeInverseConsultation();		
		ArrayList<TranslatablePair> translatablePairs = new ArrayList<TranslatablePair>();		
		String sourceLexicon;		
		if(sourceLanguage == "en") sourceLexicon = "http://linguistic.linkeddata.es/id/apertium/lexiconEN";
		else sourceLexicon = SPARQLSearches.obtainLexiconFromLanguage(sourceLanguage);		
		String translationSetURI1 = SPARQLSearches.obtainTranslationSetFromLanguages(sourceLanguage, pivotLanguage);
		String translationSetURI2 = SPARQLSearches.obtainTranslationSetFromLanguages(pivotLanguage, targetLanguage);		
		// Create a new query to get all the source labels along with their associated lexical entries
		String queryString = 
			" PREFIX lemon: <http://www.lemon-model.net/lemon#> " +
			" PREFIX tr: <http://purl.org/net/translation#> " +
			" PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> " +
		    " SELECT DISTINCT ?sourceLabel ?lex_entry " + 
			" WHERE { " +
			" <" + sourceLexicon + "> lemon:entry ?lex_entry ." + 
			"  ?lex_entry lemon:lexicalForm ?form . " +
			"  ?form lemon:writtenRep ?sourceLabel ." +
			"} " + 
			" ORDER BY ?sourceLabel";
		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);		
		ResultSet results = qe.execSelect();	
		try {			
			PrintWriter writer = new PrintWriter(outputFile, "UTF-8");	
			//Review results
			for ( ; results.hasNext() ; )    {		
				QuerySolution soln = results.nextSolution() ;
				String sourceLabel = soln.get("sourceLabel").toString();
				String le = soln.get("lex_entry").toString();			
				ArrayList<TranslatablePair> tps = otic.obtainTranslationScoresFromLexicalEntry(sourceLabel.substring(0, sourceLabel.indexOf("@")), le, translationSetURI1, translationSetURI2);	
				for (TranslatablePair tp : tps)
					tp.printToFile(writer);	
				translatablePairs.addAll(tps);		
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		qe.close();	
	
	}		
	
	public static void main(String[] args) {		
//		String langS = args[0];
//		String langP = args[1];
//		String langT = args[2];		
		String langS = "es";
		String langP = "eo";
		String langT = "ca";
		
		String outputFile = "data/OTIC_" + langS + "-" + langP + "-" + langT + "_APv1.tsv";		
		//String sparqlEndpoint = "http://linguistic.linkeddata.es/sparql/";		
		String sparqlEndpoint = "http://localhost:8080/fuseki/data/query";
		
		IndirectTranslationsExperiment myExperiment = new IndirectTranslationsExperiment();
		myExperiment.createTranslatablePairsWithScoreFile(sparqlEndpoint, langS , langP, langT, outputFile);	
		
	}

}
