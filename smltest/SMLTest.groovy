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


def fout = new PrintWriter(new BufferedWriter(new FileWriter("michel.txt")))
def URI = "http://phenomebrowser.net/smltest/"
URIFactory factory = URIFactoryMemory.getSingleton()
URI graph_uri = factory.createURI(URI)
factory.loadNamespacePrefix("HP", graph_uri.toString())
G graph = new GraphMemory(graph_uri)

GDataConf graphconf = new GDataConf(GFormat.OBO, "human-phenotype-ontology.obo")
GraphLoaderGeneric.populate(graphconf, graph)


URI virtualRoot = factory.createURI("http://phenomebrowser.net/smltest/virtualRoot");
graph.addV(virtualRoot);
        
// We root the graphs using the virtual root as root
GAction rooting = new GAction(GActionType.REROOTING);
rooting.addParameter("root_uri", virtualRoot.stringValue());
GraphActionExecutor.applyAction(factory, rooting, graph);


ICconf icConf = new IC_Conf_Topo("Resnik", SMConstants.FLAG_ICI_SANCHEZ_2011_b)
SMconf smConfGroupwise = new SMconf("SimGIC", SMConstants.FLAG_DIST_PAIRWISE_DAG_NODE_JIANG_CONRATH_1997)
smConfGroupwise.setICconf(icConf)



SM_Engine engine = new SM_Engine(graph)

graph.getV().each { phenoset1 ->
  graph.getV().each { phenoset2 ->
    try {
      double sim = engine.computePairwiseSim(smConfGroupwise, phenoset1, phenoset2)
      def hp1 = phenoset1.toString().replaceAll("http://phenomebrowser.net/smltest/","HP:")
      def hp2 = phenoset2.toString().replaceAll("http://phenomebrowser.net/smltest/","HP:")
      fout.println("$hp1\t$hp2\t$sim")
    } catch (Exception E) {}
  }
}
fout.flush()
fout.close()
