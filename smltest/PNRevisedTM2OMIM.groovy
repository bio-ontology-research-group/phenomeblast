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

def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  m longOpt:'omim-file', 'OMIM disease definitions file', args:1, required:true
  a longOpt:'annotation-file', 'Text-mined phenotype file', args:1, required:true
  p longOpt:'positive-file', 'file with true positive associations', args:1, required:true
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

//def minpmi = new Double(opt."1")
//def minlmi = new Double(opt."2")

def mpfile = new File(opt.a)

def fout = new PrintWriter(new BufferedWriter(new FileWriter(opt.o)))

def URI = "http://phenomebrowser.net/smltest/"
URIFactory factory = URIFactoryMemory.getSingleton()
URI graph_uri = factory.createURI(URI)
//factory.loadNamespacePrefix("HP", graph_uri.toString())
G graph = new GraphMemory(graph_uri)

GDataConf graphconf = new GDataConf(GFormat.RDF_XML, "monarch/monarch-inferred.owl")
GraphLoaderGeneric.populate(graphconf, graph)

URI virtualRoot = factory.createURI("http://phenomebrowser.net/smltest/virtualRoot");
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

println "Reading eval dataset..."
def omimset = new LinkedHashSet()
def mgiset = new LinkedHashSet()
new File(opt.p).splitEachLine("\t") { line ->
  omimset.add(line[1])
  mgiset.add(line[0])
}

//println omim2pheno
// adding instances
mpfile.splitEachLine("\t") { line ->
  def id = line[0]
  if (id in mgiset) {
    def iduri = factory.createURI(URI+id)
    def mp = line[1]?.trim()
    mp = mp?.replaceAll(":","_")
    def mpuri = factory.createURI("http://purl.obolibrary.org/obo/"+mp)
    if (mp && id && iduri && mpuri && mp.length()>0 && id.length()>0) {
      Edge e = new Edge(iduri, RDF.TYPE, mpuri);
      graph.addE(e)
    }
  }
}

ICconf icConf = new IC_Conf_Corpus("Resnik", SMConstants.FLAG_IC_ANNOT_RESNIK_1995)
SMconf smConfGroupwise = new SMconf("SimGIC", SMConstants.FLAG_SIM_GROUPWISE_DAG_GIC)
smConfGroupwise.setICconf(icConf)

SM_Engine engine = new SM_Engine(graph)

// find DO->OMIM mappings
def doid2omim = [:].withDefault { new TreeSet() }
def oid = ""
new File("HumanDO.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    oid = line.substring(4).trim()
  }
  if (line.startsWith("xref: OMIM")) {
    def omim = line.substring(6).trim()
    doid2omim[oid].add(omim)
  }
}

def omim2pheno = [:].withDefault { new LinkedHashSet() }
new File(opt.m).splitEachLine("\t") { line ->
  def omim = line[0]
  def pheno = line[1].replaceAll(":","_").trim()
  def puri = factory.createURI("http://purl.obolibrary.org/obo/"+pheno)
  if (graph.containsVertex(puri)) {
    omim2pheno[omim].add(puri)
  }
}

println "Computing similarity..."
InstancesAccessor ia = new InstanceAccessor_RDF_TYPE(graph)

omim2pheno.each { omim, phenos2 ->
  if (phenos2.size() > 0) {
    println "Processing $omim..."
    engine.getInstances().each { mgi ->
      Set mgiSet = ia.getDirectClass(mgi)
      def sim = engine.computeGroupwiseStandaloneSim(smConfGroupwise, phenos2, mgiSet)
      fout.println("$omim\t$mgi\t$sim")
    }
  }
}
fout.flush()
fout.close()
