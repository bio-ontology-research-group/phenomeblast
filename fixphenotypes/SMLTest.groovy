@Grab(group='com.github.sharispe', module='slib-sml', version='0.9.1')

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

def fout = new PrintWriter(new BufferedWriter(new FileWriter("results.txt")))

def hpomap = [:].withDefault { new TreeSet() }

new File("phenotype_annotation.tab").splitEachLine("\t") { line ->
  def id = line[5]
  def hp = line[4]
  hpomap[id].add(hp)
}
println hpomap["OMIM:101600"]

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
GraphActionExecutor.applyAction(factory, rooting, graph);

//println graph.getE()

def mgimap = [:]
new File("MGI_PhenoGenoMP.rpt").splitEachLine("\t") { line ->
  def id = URLEncoder.encode(line[0]+" "+line[2])
  def iduri = factory.getURI(URI+id)
  def go = line[3]?.replaceAll("MP:","")
  def gouri = factory.getURI("http://purl.obolibrary.org/obo/MP_"+go)
  def name = line[0]
  mgimap[iduri] = name
  Edge e = new Edge(iduri, RDF.TYPE, gouri)
  graph.addE(e)
}

ICconf icConf = new IC_Conf_Corpus("Resnik", SMConstants.FLAG_IC_ANNOT_RESNIK_1995_NORMALIZED)
SMconf smConfGroupwise = new SMconf("SimGIC", SMConstants.FLAG_SIM_GROUPWISE_DAG_GIC)
smConfGroupwise.setICconf(icConf)

InstancesAccessor ia = new InstanceAccessor_RDF_TYPE(graph)

SM_Engine engine = new SM_Engine(graph)
def set1 = hpomap["OMIM:101600"].collect { factory.getURI(it.replaceAll("HP:","http://purl.obolibrary.org/obo/HP_")) } as Set
println "Patient phenotypes: "+set1
engine.getInstances().each { mgi ->
  def set2 = ia.getDirectClass(mgi)
  //  println "Set 2: "+set2
  if (set2) {
    fout.println(mgimap[mgi]+"\t"+engine.compare(smConfGroupwise, set1, set2))
  }
}

fout.flush()
fout.close()
