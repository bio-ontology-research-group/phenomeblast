
// SPARQL 1.0 or 1.1 endpoint
def sparql = new Sparql(endpoint:"http://dbpedia.org/sparql")

def query = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 5"

def builder = new RDFBuilder(new BufferedWriter(new FileWriter("genotype.n3")))

def genotype = new File("../data/phenotypes.txt")

// model is a Jena Model
def model = builder.n3 {
    defaultNamespace "http://phenomebrowser.net/explore.php?id="
    namespace rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    genotype.splitEachLine("\t") { line ->
      def id = line[0]
      subject(id) {
	if (line.size()>1) {
	  line[1..-1].each {
	    property "has-phenotype":it
	  }
	}
	if (id.startsWith("STITCH")) {
	  property "rdf:type":"Drug"
	} else if (id.startsWith("OMIM")) {
	  property "rdf:type":"Disease"
	} else {
	  property "rdf:type":"Genotype"
	}
      }
    }
}


