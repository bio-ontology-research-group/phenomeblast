@Grab(group='com.github.sharispe', module='slib-sml', version='0.9.1')
@Grab(group='org.codehaus.gpars', module='gpars', version='1.1.0')

import java.net.*
import org.openrdf.model.vocabulary.*
import slib.sglib.io.loader.*
import slib.sml.sm.core.metrics.ic.utils.*
import slib.sml.sm.core.utils.*
import slib.sglib.io.loader.bio.obo.*
import org.openrdf.model.URI
import slib.graph.algo.extraction.rvf.instances.*
import slib.sglib.algo.graph.utils.*
import slib.utils.impl.Timer
import slib.graph.algo.extraction.utils.*
import slib.graph.model.graph.*
import slib.graph.model.repo.*
import slib.graph.model.impl.graph.memory.*
import slib.sml.sm.core.engine.*
import slib.graph.io.conf.*
import slib.graph.model.impl.graph.elements.*
import slib.graph.algo.extraction.rvf.instances.impl.*
import slib.graph.model.impl.repo.*
import slib.graph.io.util.*
import slib.graph.io.loader.*
import groovyx.gpars.GParsPool


def fout = new PrintWriter(new BufferedWriter(new FileWriter("phenosim-matrix.txt")))

def URI = "http://phenomebrowser.net/smltest/"
URIFactory factory = URIFactoryMemory.getSingleton()
URI graph_uri = factory.getURI(URI)
factory.loadNamespacePrefix("HP", graph_uri.toString())
G graph = new GraphMemory(graph_uri)

GDataConf graphconf = new GDataConf(GFormat.RDF_XML, "a-inferred.owl")
GraphLoaderGeneric.populate(graphconf, graph)

graph.removeE(RDF.TYPE)
URI virtualRoot = factory.getURI("http://phenomebrowser.net/smltest/virtualRoot");
graph.addV(virtualRoot);

// We root the graphs using the virtual root as root
GAction rooting = new GAction(GActionType.REROOTING);
rooting.addParameter("root_uri", virtualRoot.stringValue());
//GraphActionExecutor.applyAction(factory, rooting, graph);

//println graph.getE()

def mgimap = [:]
new File("diseasephenotypes.txt").splitEachLine("\t") { line ->
  def id = URLEncoder.encode(line[0])
  def iduri = factory.getURI(URI+id)
  def pheno = line[1]?.replaceAll(":","_")
  def phenouri = factory.getURI("http://purl.obolibrary.org/obo/"+pheno)
  Edge e = new Edge(iduri, RDF.TYPE, phenouri)
  graph.addE(e)
}
GraphActionExecutor.applyAction(factory, rooting, graph);

ICconf icConf = new IC_Conf_Corpus("ResnikIC", SMConstants.FLAG_IC_ANNOT_RESNIK_1995_NORMALIZED)
SMconf smConfPairwise = new SMconf("Resnik", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_RESNIK_1995 )
SMconf smConfGroupwise = new SMconf("BMA", SMConstants.FLAG_SIM_GROUPWISE_BMA)
smConfPairwise.setICconf(icConf)

InstancesAccessor ia = new InstanceAccessor_RDF_TYPE(graph)
SM_Engine engine = new SM_Engine(graph)

def id2gene = [:]
def map = [:].withDefault { new LinkedHashSet() }
new File("modelphenotypes.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def pheno = line[1]
  def gene = line[2]
  id2gene[id] = gene
  if (pheno.startsWith("HP") || pheno.startsWith("MP")) {
    pheno = pheno.replaceAll("HP:","http://purl.obolibrary.org/obo/HP_")
    pheno = pheno.replaceAll("MP:","http://purl.obolibrary.org/obo/MP_")
  }
  if (pheno.startsWith("http")) {
    def p = factory.getURI(pheno)
    if (graph.containsVertex(p)) {
      map[id].add(p)
    }
  }
}

//def set1 = hpomap["OMIM:101600"].collect { factory.getURI(it.replaceAll("HP:","http://purl.obolibrary.org/obo/HP_")) } as Set
GParsPool.withPool {
  map.eachParallel { id, phenoset ->
    println "Model: "+id
    engine.getInstances().each { dis ->
      def set2 = ia.getDirectClass(dis)
      if (set2) {
	fout.println("$id\t"+id2gene[id]+"\t$dis\t"+engine.compare(smConfGroupwise, smConfPairwise, phenoset, set2))
      }
    }
  }
}

fout.flush()
fout.close()
