package it.polito.tdp.yelp.model;

import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.yelp.db.ArcoGrafo;
import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	
	private Graph<Business, DefaultWeightedEdge> grafo;
	private List<Business> vertici;
	private Map<String, Business> verticiIdMap;
	
	public List<String> getAllCities(){
		YelpDao dao = new YelpDao();
		return dao.getAllCity();
	}
	
	public String creaGrafo(String citta, Year anno) {
		this.grafo = new SimpleDirectedWeightedGraph<Business, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		YelpDao dao = new YelpDao();
		this.vertici = dao.getBusinessByCityAndYear(citta, anno);
		this.verticiIdMap = new HashMap<>();
		for(Business b : this.vertici) {
			this.verticiIdMap.put(b.getBusinessId(), b);
		}
		
		Graphs.addAllVertices(this.grafo, this.vertici);
		
		//IPOTESI 3 : faccio calcolare gli archi al DB
		List<ArcoGrafo> archi = dao.calcolaArchi(citta, anno);
		for(ArcoGrafo arco : archi) {
			Graphs.addEdge(this.grafo, this.verticiIdMap.get(arco.getBusinessId1()),
					this.verticiIdMap.get(arco.getBusinessId2()), arco.getPeso());
		}
		
		
		
		return String.format("Grafo creato con %d vertici e %d archi\n", 
				this.grafo.vertexSet().size(),
				this.grafo.edgeSet().size());
	}
	
	public Business getLocaleMigliore() {
		
		double max = 0.0;
		Business result = null;
		
		for(Business b : this.grafo.vertexSet()) {
			double val = 0.0;
			//Archi entranti
			for(DefaultWeightedEdge e : this.grafo.incomingEdgesOf(b)) {
				val += this.grafo.getEdgeWeight(e);
			}
			//Archi uscenti
			for(DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(b)) {
				val -= this.grafo.getEdgeWeight(e);
			}
			
			if(val>max) {
				max = val;
				result = b;
			}
			
		}
		
		return result;
	}
	
}
