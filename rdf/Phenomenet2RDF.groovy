import com.hp.hpl.jena.vocabulary.*
import org.apache.commons.cli.Option
import com.hp.hpl.jena.rdf.model.*

def pn    = "http://phenomebrowser.net/explore.php?id=";
def obo    = "http://purl.obolibrary.org/obo/";
def rn    = "http://phenomebrowser.net/rel#";
def cn    = "http://phenomebrowser.net/#";

Model model = null

model = ModelFactory.createDefaultModel()

new File("../phenomeweb/ontologies/").eachFile { file ->
  def id = ""
  file.eachLine { line ->
    if (line.startsWith("id: ")) {
      id = line.substring(4).trim().replaceAll(":","_")
    }
    if (line.startsWith("name: ")) {
      def name = line.substring(5)
      def res = model.createResource(obo+id)
      res.addProperty(RDFS.label, name)
    }
  }
}

model.write(new BufferedWriter(new FileWriter("ontologynames.rdf")))
model = ModelFactory.createDefaultModel()

new File("../phenomeweb/names/").eachFile { file ->
  file.splitEachLine("\t") { line ->
    if (line.size()<=2 && line[1] && line[1].size()>1 && line[0].indexOf(" ")==-1) {
      def res = model.createResource(pn+line[0])
      res.addProperty(RDFS.label, line[1].replaceAll("\\p{C}", "?"))
    } 
    /*else if (line[0].indexOf(" ")==-1) { // special treatment for mouse
      if (line[-1]=="allele") { // allele
	def res = model.createResource(pn+line[0])
	res.addProperty(RDFS.label, line[1])
      } else if (line[-1]=="genotype") { // genotype
	def res = model.createResource(pn+line[0])
	res.addProperty(RDFS.label, line[1]+" ("+line[2]+")")
      } else { // gene
	def res = model.createResource(pn+line[0])
	res.addProperty(RDFS.label, line[1]+" ("+line[2]+")")
      }
      } */
  }
}
model.write(new BufferedWriter(new FileWriter("phenotypenames.rdf")))

model = ModelFactory.createDefaultModel()
def hasAssertedPhenotype = model.createProperty(rn, "has-asserted-phenotype")

new File("../phenomeweb/phenotypes/").eachFile { file ->
  file.splitEachLine("\t") { line ->
    if (line[0].indexOf(" ")==-1 && line[1].indexOf(" ")==-1) {
      def res = model.createResource(pn+line[0])
      if (res) {
	def id = line[1].replaceAll(":","_")
	if (id!=null) {
	  res.addProperty(hasAssertedPhenotype, model.createResource(obo+id))
	}
      }
    }
  }
}
model.write(new BufferedWriter(new FileWriter("assertedpheno.rdf")))


model = ModelFactory.createDefaultModel()
def hasPhenotype = model.createProperty(rn, "has-phenotype")
def genotype = new File("../data/phenotypes.txt")
genotype.splitEachLine("\t") { line ->
  def id = line[0]
  def res = model.createResource(pn+id)
  if (id.indexOf(" ")==-1) {
    if (line.size()>1) {
      line[1..-1].each {
	it = it.replaceAll(">","").replaceAll("<","")
	res.addProperty(hasPhenotype, model.createResource(it))
      }
    }
  }
}

model.write(new BufferedWriter(new FileWriter("phenotype.rdf")))
