package it.polito.tdp.yelp.model;

import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	
	private Graph<Business, DefaultWeightedEdge> grafo ;
	private List<Business> vertici ;
	private Map<String, Business> verticiIdMap ;

	
	public List<String> getAllCities() {
		YelpDao dao = new YelpDao() ;
		return dao.getAllCities() ;
	}
	
	public String creaGrafo(String city, Year anno) {
		this.grafo = new SimpleDirectedWeightedGraph<Business, DefaultWeightedEdge>(DefaultWeightedEdge.class) ;
		YelpDao dao = new YelpDao() ;
		this.vertici = dao.getBusinessByCityAndYear(city, anno) ;
		this.verticiIdMap = new HashMap<>() ;
		for(Business b : this.vertici)
			this.verticiIdMap.put(b.getBusinessId(), b) ;
		
		Graphs.addAllVertices(this.grafo, this.vertici) ;
		
		/*
		// IPOTESI "Giuseppe": calcolare la media recensioni mentre leggo i Business
		for(Business b1: this.vertici) {
			for(Business b2: this.vertici) {
				if(b1.getMediaRecensioni() < b2.getMediaRecensioni()) {
					Graphs.addEdge(this.grafo, b1, b2, b2.getMediaRecensioni()-b1.getMediaRecensioni()) ;
				}
			}
		}
		*/
		
		/*
		// IPOTESI "Giuseppe + Mappa": non modifico oggetto Business, ma creo
		// una mappa per ricordarmi le medie delle recensioni
		Map<Business, Double> medieRecensioni = new HashMap<>() ;
		// carica la mappa con il DAO
		for(Business b1: this.vertici) {
			for(Business b2: this.vertici) {
				if(medieRecensioni.get(b1) < medieRecensioni.get(b2)) {
					Graphs.addEdge(this.grafo, b1, b2, medieRecensioni.get(b2)-medieRecensioni.get(b1)) ;
				}
			}
		}
		*/
		
		// IPOTESI 3 : faccio calcolare gli archi al DB
		List<ArcoGrafo> archi = dao.calcolaArchi(city, anno) ;
		for(ArcoGrafo arco : archi) {
			Graphs.addEdge(this.grafo,
					this.verticiIdMap.get(arco.getBusinessId1()),
					this.verticiIdMap.get(arco.getBusinessId2()), 
					arco.getPeso()) ;
		}
		
		

		
		return String.format("Grafo creato con %d vertici e %d archi\n",
				this.grafo.vertexSet().size(),
				this.grafo.edgeSet().size()) ;
	}
	
	public Business getLocaleMigliore() {
		double max = 0.0 ;
		Business result = null ;
		
		for(Business b: this.grafo.vertexSet()) {
			double val = 0.0 ;
			for(DefaultWeightedEdge e: this.grafo.incomingEdgesOf(b))
				val += this.grafo.getEdgeWeight(e) ;
			for(DefaultWeightedEdge e: this.grafo.outgoingEdgesOf(b))
				val -= this.grafo.getEdgeWeight(e) ;
			
			if(val>max) {
				max = val ;
				result = b ;
			}
		}
		return result; 
	}
	
}
