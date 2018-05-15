@Grab(group='com.github.sharispe', module='slib-sml', version='0.9.1')
@Grab(group='org.codehaus.gpars', module='gpars', version='1.2.0')

/*import slib.sglib.model.impl.graph.elements.Edge
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
*/
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
import slib.graph.algo.extraction.rvf.DescendantEngine;
import slib.graph.algo.accessor.GraphAccessor;
import groovyx.gpars.ParallelEnhancer
import groovyx.gpars.GParsPool

System.setProperty("jdk.xml.entityExpansionLimit", "0")
System.setProperty("jdk.xml.totalEntitySizeLimit", "0");

def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'input-file', 'disease definitions input file', args:1, required:true
  o longOpt:'output', 'output file', args:1, required:true
  //  "1" longOpt:'pmi', 'min PMI', args:1, required:true
  //  "2" longOpt:'lmi', 'min LMI', args:1, required:true
}

def opt = cli.parse(args)
if( !opt ) {
  //  cli.usage()
  return
}
if( opt.h ) {
    cli.usage()
    return
}

def mpfile = new File("/home/kafkass/Projects/phenomeblast/fixphenotypes/mousephenotypes.txt")
def disfile = new File(opt.i)

def fout = new PrintWriter(new BufferedWriter(new FileWriter(opt.o)))

String uri = "http://phenomebrowser.net/smltest/"

URIFactory factory = URIFactoryMemory.getSingleton()

URI graph_uri = factory.getURI(uri)
//factory.loadNamespacePrefix("HP", graph_uri.toString())
G graph = new GraphMemory(graph_uri)

GDataConf graphconf = new GDataConf(GFormat.RDF_XML, "/home/kafkass/Projects/Pathogen_Disease/a-inferred.owl")
GraphLoaderGeneric.populate(graphconf, graph)

URI virtualRoot = factory.getURI("http://purl.obolibrary.org/obo/virtualRoot");
graph.addV(virtualRoot);
        
// We root the graphs using the virtual root as root
GAction rooting = new GAction(GActionType.REROOTING);
rooting.addParameter("root_uri", virtualRoot.stringValue());
GraphActionExecutor.applyAction(factory, rooting, graph);

// remove all instances
Set removeE = new LinkedHashSet()
graph.getE().each { it ->
  String es = it.toString();
  if ( es.indexOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")>-1 ) {
    removeE.add( it );
  }
}               
removeE.each { graph.removeE(it) }

// adding instances
mpfile.splitEachLine("\t") { line ->
  def id = line[0]

    def iduri = factory.getURI(uri+id)
    def mp = line[1]?.trim()
    mp = mp?.replaceAll(":","_")
    def mpuri = factory.getURI("http://purl.obolibrary.org/obo/"+mp)
    if (mp && id && iduri && mpuri && mp.length()>0 && id.length()>0) {
      Edge e = new Edge(iduri, RDF.TYPE, mpuri);
      graph.addE(e)
    }
}

println "Reading disease file..."
def map = [:].withDefault { new LinkedHashSet() }
disfile.splitEachLine("\t") { line ->
  def doid = line[0]
  
    def pid = line[1]
    //    if (pmi > minpmi && lmi > minlmi) { // ******************* Adjust scores here!
    pid = pid?.replaceAll(":","_")?.trim()
    if (pid && pid.length()>0) {
      def piduri = factory.getURI("http://purl.obolibrary.org/obo/"+pid)
      if (graph.containsVertex(piduri)) {
	map[doid].add(piduri)
      }
    }
    //    }
  
}


ICconf icConf = new IC_Conf_Corpus("Resnik", SMConstants.FLAG_IC_ANNOT_RESNIK_1995)
SMconf smConfGroupwise = new SMconf("BMA", SMConstants.FLAG_SIM_GROUPWISE_BMA);
SMconf smConfPairwise = new SMconf("Resnik", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_RESNIK_1995 );

smConfPairwise.setICconf(icConf)

SM_Engine engine = new SM_Engine(graph)

println "Computing similarity..."
InstancesAccessor ia = new InstanceAccessor_RDF_TYPE(graph)

GParsPool.withPool {
  map.eachParallel { doid, phenos ->
    if (phenos.size() > 0) {
      println "Processing $doid..."
      engine.getInstances().each { mgi ->
       Set mgiSet = ia.getDirectClass(mgi)
    if (mgiSet.size()>0){//	println "mgiSet" + mgiSet
                        //     println "phenome" + phenos
	try {
	def sim = engine.compare(smConfGroupwise, smConfPairwise, phenos, mgiSet)
	fout.println("$doid\t$mgi\t$sim")
	} catch (Exception e) {}        
}
      }
  }
}
}
fout.flush()
fout.close()
