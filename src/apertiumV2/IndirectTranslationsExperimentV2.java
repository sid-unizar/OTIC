package apertiumV2;

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

public class IndirectTranslationsExperimentV2 {
	
	final static String SEPARATOR = "\t";

	public void createTranslatablePairsWithScoreFile (String sparqlEndpoint, String sourceLanguage, String pivotLanguage, String targetLanguage, String outputFile){
		
		OneTimeInverseConsultationV2 otic = new OneTimeInverseConsultationV2();		
		ArrayList<TranslatablePair> translatablePairs = new ArrayList<TranslatablePair>();		
		String sourceLexicon = SPARQLSearchesV2.obtainLexiconFromLanguage(sourceLanguage);		
		String translationSetURI1 = SPARQLSearchesV2.obtainTranslationSetFromLanguages(sourceLanguage, pivotLanguage);
		String translationSetURI2 = SPARQLSearchesV2.obtainTranslationSetFromLanguages(pivotLanguage, targetLanguage);		
		// Create a new query to get all the source labels along with their associated lexical entries
		String queryString = 
			" PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>" + 
			" PREFIX lime: <http://www.w3.org/ns/lemon/lime#>" + 
			" SELECT DISTINCT ?sourceLabel ?lex_entry " + 
			" WHERE { " +
			//" GRAPH <http://linguistic.linkeddata.es/id/apertium-lexinfo/>" + 
			//"    {" + 
			" <" + sourceLexicon + "> lime:entry ?lex_entry ." + 
			"  ?lex_entry ontolex:lexicalForm ?form . " +
			"  ?form ontolex:writtenRep ?sourceLabel ." +
			//"}" +
			"}" +
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
				for (TranslatablePair tp : tps) {
					tp.print(le, pivotLanguage);
					tp.printToFile(writer, le, pivotLanguage);					
				}
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
		
		String langS = args[0];
		String langP = args[1];
		String langT = args[2];		
		
//		String langS = "es";
//		String langP = "eo";
//		String langT = "ca";
		
		String outputFile = "data/OTIC_" + langS + "-" + langP + "-" + langT + "_APv2.tsv";		
		
		//String sparqlEndpoint = "http://dbserver.acoli.cs.uni-frankfurt.de:5005/ds/query";
		String sparqlEndpoint = "http://localhost:8080/fuseki/ds/query";		
		
		IndirectTranslationsExperimentV2 myExperiment = new IndirectTranslationsExperimentV2();
		myExperiment.createTranslatablePairsWithScoreFile(sparqlEndpoint, langS , langP, langT, outputFile);	
		
	}
	


}
