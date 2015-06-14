import slib.sglib.model.impl.graph.elements.Edge
import org.openrdf.model.vocabulary.*
import slib.sglib.io.conf.GDataConf
import slib.sglib.io.loader.*
import slib.sglib.io.util.GFormat
import slib.sglib.model.graph.G
import slib.sglib.model.impl.graph.memory.GraphMemory
import slib.sglib.model.impl.repo.URIFactoryMemory
import slib.sglib.model.repo.URIFactory
import slib.sml.sm.core.engine.SM_Engine
import slib.sml.sm.core.metrics.ic.utils.*
import slib.sml.sm.core.utils.*
import slib.utils.ex.SLIB_Exception
import slib.sglib.io.loader.bio.obo.*
import org.openrdf.model.URI
import slib.sglib.algo.graph.extraction.rvf.instances.InstancesAccessor
import slib.sglib.algo.graph.extraction.rvf.instances.impl.InstanceAccessor_RDF_TYPE
import slib.sglib.algo.graph.utils.*
import slib.utils.impl.Timer

//def minpmi = new Double(opt."1")
//def minlmi = new Double(opt."2")

def URI = "http://phenomebrowser.net/smltest/"
URIFactory factory = URIFactoryMemory.getSingleton()
URI graph_uri = factory.createURI(URI)
factory.loadNamespacePrefix("GO", graph_uri.toString());
G graph = new GraphMemory(graph_uri)

GDataConf graphconf = new GDataConf(GFormat.OBO, "go.obo")

GraphLoaderGeneric.populate(graphconf, graph)

URI virtualRoot = factory.createURI("http://phenomebrowser.net/smltest/virtualRoot");
graph.addV(virtualRoot);
        
// We root the graphs using the virtual root as root
GAction rooting = new GAction(GActionType.REROOTING);
rooting.addParameter("root_uri", virtualRoot.stringValue());
GraphActionExecutor.applyAction(factory, rooting, graph);

// adding instances
new File("gene_association.ecocyc").splitEachLine("\t") { line ->
  if (!line[0].startsWith("!")) {
    def id = "UNIPROT:"+line[1]
    def iduri = factory.createURI(URI+id)
    def mp = line[4]?.trim()
    mp = mp?.replaceAll("GO:","")
    def mpuri = factory.createURI(URI+mp)
    if (graph.containsVertex(mpuri)) {
      if (mp && id && iduri && mpuri && mp.length()>0 && id.length()>0) {
	Edge e = new Edge(iduri, RDF.TYPE, mpuri);
	graph.addE(e)
      }
    } else {
      println "Mismatch: $mpuri"
    }
  }
}

ICconf icConf = new IC_Conf_Corpus("Resnik", SMConstants.FLAG_IC_ANNOT_RESNIK_1995)
SMconf smConfGroupwise = new SMconf("SimGIC", SMConstants.FLAG_SIM_GROUPWISE_DAG_GIC)
smConfGroupwise.setICconf(icConf)

SM_Engine engine = new SM_Engine(graph)

//println engine.getClasses()

println "Computing similarity..."
InstancesAccessor ia = new InstanceAccessor_RDF_TYPE(graph)

engine.getInstances().each { gene1 ->
  println "Processing $gene1..."
  Set set1 = ia.getDirectClass(gene1)
  engine.getInstances().each { gene2 ->
    Set set2 = ia.getDirectClass(gene2)
    def sim = engine.computeGroupwiseStandaloneSim(smConfGroupwise, set1, set2)
    println "$gene1\t$gene2\t$sim"
  }
}
