@Grapes([
          @Grab(group='org.semanticweb.elk', module='elk-owlapi', version='0.4.2'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='4.2.5'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='4.2.5'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='4.2.5'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-parsers', version='4.2.5'),
          @GrabConfig(systemClassLoader=true)
        ])

import org.semanticweb.owlapi.model.parameters.*
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerConfiguration
import org.semanticweb.elk.reasoner.config.*
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.owllink.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.search.*;
import org.semanticweb.owlapi.manchestersyntax.renderer.*;
import org.semanticweb.owlapi.reasoner.structural.*


def fout = new PrintWriter(new BufferedWriter(new FileWriter("fishies.txt")))

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()
def ontset = new TreeSet()
def o1 = manager.loadOntologyFromOntologyDocument(new File("mp.owl"))
ontset.add(o1)
ontset.add(manager.loadOntologyFromOntologyDocument(new File("hp.owl")))
ontset.add(manager.loadOntologyFromOntologyDocument(new File("ordo_orphanet.owl")))
ontset.add(manager.loadOntologyFromOntologyDocument(new File("doid.owl")))
OWLOntology ont = manager.createOntology(IRI.create("http://aber-owl.net/phenotype-input.owl"), ontset)

OWLOntology outont = manager.createOntology(IRI.create("http://aber-owl.net/phenotype.owl"))
def onturi = "http://aber-owl.net/phenotype.owl#"

OWLDataFactory fac = manager.getOWLDataFactory()
ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
StructuralReasonerFactory f1 = new StructuralReasonerFactory()
OWLReasoner reasoner = f1.createReasoner(ont,config)
//reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator()])
generator.fillOntology(fac, outont)

ont.getClassesInSignature(true).each { cl ->
  EntitySearcher.getAnnotationAssertionAxioms(cl, ont).each { ax ->
    manager.addAxiom(outont, ax)
  }
}

println "Processing uberon"
OWLOntology ont2 = manager.loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/uberon.owl"))
reasoner = f1.createReasoner(ont2, config)
generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredClassAssertionAxiomGenerator(), new InferredEquivalentClassAxiomGenerator(), new InferredSubClassAxiomGenerator()])
generator.fillOntology(fac, outont)
ont2.getTBoxAxioms(Imports.INCLUDED).each { ax ->
  if (! (ax.isOfType(AxiomType.DISJOINT_CLASSES))) {
    manager.addAxiom(outont, ax)
  }
}
ont2.getClassesInSignature(true).each { cl ->
  EntitySearcher.getAnnotationAssertionAxioms(cl, ont2).each { ax ->
    manager.addAxiom(outont, ax)
  }
}
println "Processing go"
ont2 = manager.loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/go.owl"))
reasoner = f1.createReasoner(ont2, config)
generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredClassAssertionAxiomGenerator(), new InferredEquivalentClassAxiomGenerator(), new InferredSubClassAxiomGenerator()])
generator.fillOntology(fac, outont)
ont2.getTBoxAxioms(Imports.INCLUDED).each { ax ->
  if (! (ax.isOfType(AxiomType.DISJOINT_CLASSES))) {
    manager.addAxiom(outont, ax)
  }
}
ont2.getClassesInSignature(true).each { cl ->
  EntitySearcher.getAnnotationAssertionAxioms(cl, ont2).each { ax ->
    manager.addAxiom(outont, ax)
  }
}
println "Processing bspo"
ont2 = manager.loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/bspo.owl"))
reasoner = f1.createReasoner(ont2, config)
generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredClassAssertionAxiomGenerator(), new InferredEquivalentClassAxiomGenerator(), new InferredSubClassAxiomGenerator()])
generator.fillOntology(fac, outont)
ont2.getTBoxAxioms(Imports.INCLUDED).each { ax ->
  if (! (ax.isOfType(AxiomType.DISJOINT_CLASSES))) {
    manager.addAxiom(outont, ax)
  }
}
ont2.getClassesInSignature(true).each { cl ->
  EntitySearcher.getAnnotationAssertionAxioms(cl, ont2).each { ax ->
    manager.addAxiom(outont, ax)
  }
}
println "Processing zfa"
ont2 = manager.loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/zfa.owl"))
reasoner = f1.createReasoner(ont2, config)
generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredClassAssertionAxiomGenerator(), new InferredEquivalentClassAxiomGenerator(), new InferredSubClassAxiomGenerator()])
generator.fillOntology(fac, outont)
ont2.getTBoxAxioms(Imports.INCLUDED).each { ax ->
  if (! (ax.isOfType(AxiomType.DISJOINT_CLASSES))) {
    manager.addAxiom(outont, ax)
  }
}
ont2.getClassesInSignature(true).each { cl ->
  EntitySearcher.getAnnotationAssertionAxioms(cl, ont2).each { ax ->
    manager.addAxiom(outont, ax)
  }
}
println "Processing pato"
ont2 = manager.loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/pato.owl"))
reasoner = f1.createReasoner(ont2, config)
generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredClassAssertionAxiomGenerator(), new InferredEquivalentClassAxiomGenerator(), new InferredSubClassAxiomGenerator()])
generator.fillOntology(fac, outont)
ont2.getTBoxAxioms(Imports.INCLUDED).each { ax ->
  if (! (ax.isOfType(AxiomType.DISJOINT_CLASSES))) {
    manager.addAxiom(outont, ax)
  }
}
ont2.getClassesInSignature(true).each { cl ->
  EntitySearcher.getAnnotationAssertionAxioms(cl, ont2).each { ax ->
    manager.addAxiom(outont, ax)
  }
}
println "Processing cl"
ont2 = manager.loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/cl-basic.owl"))
reasoner = f1.createReasoner(ont2, config)
generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredClassAssertionAxiomGenerator(), new InferredEquivalentClassAxiomGenerator(), new InferredSubClassAxiomGenerator()])
generator.fillOntology(fac, outont)
ont2.getTBoxAxioms(Imports.INCLUDED).each { ax ->
  if (! (ax.isOfType(AxiomType.DISJOINT_CLASSES))) {
    manager.addAxiom(outont, ax)
  }
}
ont2.getClassesInSignature(true).each { cl ->
  EntitySearcher.getAnnotationAssertionAxioms(cl, ont2).each { ax ->
    manager.addAxiom(outont, ax)
  }
}
println "Processing nbo"
ont2 = manager.loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/nbo.owl"))
reasoner = f1.createReasoner(ont2, config)
generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredClassAssertionAxiomGenerator(), new InferredEquivalentClassAxiomGenerator(), new InferredSubClassAxiomGenerator()])
generator.fillOntology(fac, outont)
ont2.getTBoxAxioms(Imports.INCLUDED).each { ax ->
  if (! (ax.isOfType(AxiomType.DISJOINT_CLASSES))) {
    manager.addAxiom(outont, ax)
  }
}
ont2.getClassesInSignature(true).each { cl ->
  EntitySearcher.getAnnotationAssertionAxioms(cl, ont2).each { ax ->
    manager.addAxiom(outont, ax)
  }
}
// println "Processing pr"
// ont2 = manager.loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/pr.owl"))
// reasoner = f1.createReasoner(ont2, config)
// generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredClassAssertionAxiomGenerator(), new InferredEquivalentClassAxiomGenerator(), new InferredSubClassAxiomGenerator()])
// generator.fillOntology(fac, outont)
// ont2.getTBoxAxioms(Imports.INCLUDED).each { ax ->
//   if (! (ax.isOfType(AxiomType.DISJOINT_CLASSES))) {
//     manager.addAxiom(outont, ax)
//   }
// }
// ont2.getClassesInSignature(true).each { cl ->
//   EntitySearcher.getAnnotationAssertionAxioms(cl, ont2).each { ax ->
//     manager.addAxiom(outont, ax)
//   }
// }




println "Building ontology..."

def map = [:]

String formatClassNames(String s) {
  s=s.replace("<http://purl.obolibrary.org/obo/","")
  s=s.replace(">","")
  s=s.replace("_",":")
  s
}
def id2name = [:]
outont.getClassesInSignature(true).each { cl ->
  EntitySearcher.getAnnotationObjects(cl, outont, fac.getRDFSLabel()).each { lab ->
    if (lab.getValue() instanceof OWLLiteral) {
      def labs = (OWLLiteral) lab.getValue()
      id2name[cl] = labs.getLiteral()
    }
  }
}
def id2class = [:] // maps a name to an OWLClass
outont.getClassesInSignature(true).each {
  def aa = it.toString()
  aa = formatClassNames(aa)
  if (id2class[aa] != null) {
  } else {
    id2class[aa] = it
  }
}

def addAnno = {resource, prop, cont ->
  //  OWLAnnotation anno = fac.getOWLAnnotation(fac.getOWLAnnotationProperty(prop.getIRI()), fac.getOWLLiteral(cont))
  def axiom = fac.getOWLAnnotationAssertionAxiom(fac.getOWLAnnotationProperty(prop.getIRI()), resource.getIRI(), cont)
  manager.addAxiom(outont,axiom)
}

def R = { String s ->
  if (s == "part-of") {
    fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050"))
  } else if (s == "has-part") {
    fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000051"))
  } else {
    fac.getOWLObjectProperty(IRI.create("http://aber-owl.net/#"+s))
  }
}

def C = { String s ->
  fac.getOWLClass(IRI.create(onturi+s))
}

def and = { cl1, cl2 ->
  fac.getOWLObjectIntersectionOf(cl1,cl2)
}
def some = { r, cl ->
  fac.getOWLObjectSomeValuesFrom(r,cl)
}
def equiv = { cl1, cl2 ->
  fac.getOWLEquivalentClassesAxiom(cl1, cl2)
}
def subclass = { cl1, cl2 ->
  fac.getOWLSubClassOfAxiom(cl1, cl2)
}

ont.getClassesInSignature(true).each { cl ->
  if (cl.toString().indexOf("MP_")>-1 || cl.toString().indexOf("HP_")>-1) {    
    def q = []
    def e = []
    def e2 = []
    def po = []
    def ihp = []
    def modifier = []
    def occursin = []
    def haspart = []
    def during = []
    def hasquality = []
    def centralparticipant = []
    def resultsfrom = []
    EntitySearcher.getEquivalentClasses(cl, ont).each { cExpr -> // OWL Class Expression
      if (! cExpr.isClassExpressionLiteral()) {
	if (cExpr.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
	  def c = cExpr as OWLQuantifiedRestriction
	  if (c.getProperty()?.toString() == "<http://purl.obolibrary.org/obo/BFO_0000051>") {
	    c = c.getFiller().asConjunctSet()
	    c.each { conj ->
	      if (conj.isClassExpressionLiteral()) {
		q << conj
	      } else if (conj.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
		conj = conj as OWLQuantifiedRestriction
		if (conj.getProperty().toString() == "<http://purl.obolibrary.org/obo/RO_0002573>") { // modifier
		  modifier << conj.getFiller()
		} else if (conj.getProperty().toString() == "<http://purl.obolibrary.org/obo/RO_0000052>") { // inheres-in
		  e << conj.getFiller()
		} else if (conj.getProperty().toString() == "<http://purl.obolibrary.org/obo/RO_0002314>") { // inheres in part of (make part-of some E)
		  ihp << conj.getFiller()
		} else if (conj.getProperty().toString() == "<http://purl.obolibrary.org/obo/RO_0002503>") { // towards
		  e2 << conj.getFiller()
		} else if (conj.getProperty().toString() == "<http://purl.obolibrary.org/obo/mp/mp-equivalent-axioms-subq#has_quality>") { // has-quality (make quality of Q)
		  hasquality << conj.getFiller()
		} else if (conj.getProperty().toString() == "<http://purl.obolibrary.org/obo/mp/mp-equivalent-axioms-subq#exists_during>") { // exists-during (modified/intersect of E2)
		  during << conj.getFiller()
		} else if (conj.getProperty().toString() == "<http://purl.obolibrary.org/obo/BFO_0000051>") { // has-part: treat as intersection ( :( )
		  haspart << conj.getFiller()
		} else if (conj.getProperty().toString() == "<http://purl.obolibrary.org/obo/BFO_0000050>") { // part-of (E and part-of some X)
		  po << conj.getFiller()
		} else if (conj.getProperty().toString() == "<http://purl.obolibrary.org/obo/mp/mp-equivalent-axioms-subq#has_central_participant>") { // has-central-participant (?)
		  centralparticipant << conj.getFiller()
		} else if (conj.getProperty().toString() == "<http://purl.obolibrary.org/obo/mp/mp-equivalent-axioms-subq#results_from>") { // has-central-participant (?)
		  resultsfrom << conj.getFiller()
		} else if (conj.getProperty().toString() in ["<http://purl.obolibrary.org/obo/BFO_0000066>","<http://purl.obolibrary.org/obo/mp/mp-equivalent-axioms-subq#occurs_in>"]) { // occurs-in: make modifier to E (E occurs in ...)
		  occursin << conj.getFiller()
		} else {
		  println "Ignoring $cl: "+conj.getProperty()
		}
	      }
	    }
	  }
	}
      }
    }
    // def q = []
    // def e = []
    // def e2 = []
    // def po = []
    // def ihp = []
    // def modifier = []
    // def occursin = []
    // def haspart = []
    // def during = []
    // def hasquality = []
    // def centralparticipant = []

    if (cl == id2class["HP:0001714"]) { // fix for broken definition of ventricular hypertrophy
      q = [ id2class["PATO:0000584"] ]
    }

    Expando exp = new Expando()
    exp.cl = cl
    exp.e = e
    exp.e2 = e2
    exp.q = q
    exp.ihp = ihp
    exp.mod = modifier
    exp.occ = occursin
    exp.hp = haspart
    exp.during = during
    exp.hasquality = hasquality
    exp.centralparticipant = centralparticipant
    exp.resultsfrom = resultsfrom

    map[cl] = exp
    /*
    println "Class $cl:"
    println "\tE: $e"
    println "\tE2: $e2"
    println "\tQ: $q"
    println "\tIPO: $ihp"
    println "\tMod: $modifier"
    println "\tOcc: $occursin"
    println "\tHP: $haspart"
    println "\tduring: $during"
    println "\thas-quality: $hasquality"
    println "\tcentral-participant: $centralparticipant"
    println "\tresults-from: $resultsfrom"
    */
  }
}

def done = new HashSet()
def counter = 0

/* Do fishy mapping stuff */
def zfa2uberon = [:]
def oid = ""
new File("uberon_edit.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    oid = line.substring(3).trim()
    if (oid.indexOf("!")>-1) {
      oid = oid.substring(0,oid.indexOf("!"))?.replaceAll("!","")?.trim()
    }
  }
  if (line.startsWith("xref: ZFA")) {
    def xref = line.substring(5).trim()
    if (xref.indexOf("!")>-1) {
      xref = xref.substring(0,xref.indexOf("!"))?.replaceAll("!","")?.trim()
    }
    def cl1 = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+(oid.replaceAll(":","_"))))
    def cl2 = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+(xref.replaceAll(":","_"))))
    manager.addAxiom(outont, equiv(cl1, cl2))
    //    zfa2uberon[xref] = oid
  }
}

ManchesterOWLSyntaxOWLObjectRendererImpl rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl()
rendering.setShortFormProvider(new AnnotationValueShortFormProvider([fac.getRDFSLabel()], [:], manager))

def edoneq = [:].withDefault { new HashSet() }
def e2q2cl = [:].withDefault { new LinkedHashSet() }
new File("modelphenotypes/phenoGeneCleanData_fish.txt").splitEachLine("\t") { line ->
  def e = line[7]
  def q = line[9]?.replaceAll(":","_")
  def po = null
  def rel = null
  if (line[3] && line[5]) {
    po = line[3]
    rel = line[5]?.replaceAll(":","_")
  }
  e = e.replaceAll(":","_")
  po = po?.replaceAll(":","_")
  e = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+e))
  if (po && rel) {
    po = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+po))
    rel = fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/"+rel))
  }
  q = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+q))
  def str = "$rel$po$e$q"
  if (e && q) {
    def qq = and(id2class["PATO:0000001"], some(R("has-modifier"),fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000460"))))
    if (! (e in done) && (e.toString().indexOf("GO")>-1 || e.toString().indexOf("UBERON")>-1 || e.toString().indexOf("ZFA")>-1)) {
      manager.addAxiom(outont, equiv(C("PHENO:$counter"), some(R("has-part"),and(some(R("part-of"),e), some(R("has-quality"),qq)))))
      def an = fac.getOWLAnnotation(fac.getRDFSLabel(), fac.getOWLLiteral("Phenotypic abnormality of "+rendering.render(e).replace("\n"," ")))
      def axiom = fac.getOWLAnnotationAssertionAxiom(C("PHENO:$counter").getIRI(), an)
      manager.addAxiom(outont,axiom)
      done.add(e)
      counter += 1
    }
    if (po && rel) {
      e = and(po, some(rel, e))
      if (! (e in done) && (e.toString().indexOf("GO")>-1 || e.toString().indexOf("UBERON")>-1 || e.toString().indexOf("ZFA")>-1)) {
	manager.addAxiom(outont, equiv(C("PHENO:$counter"), some(R("has-part"),and(some(R("part-of"),e), some(R("has-quality"),qq)))))
	def an = fac.getOWLAnnotation(fac.getRDFSLabel(), fac.getOWLLiteral("Phenotypic abnormality of "+rendering.render(e).replace("\n"," ")))
	def axiom = fac.getOWLAnnotationAssertionAxiom(C("PHENO:$counter").getIRI(), an)
	manager.addAxiom(outont,axiom)
	done.add(e)
	counter += 1
      }
    }
    if (!e2q2cl[str] && q != id2class["PATO:0000001"]) {
      qq = and(q, some(R("has-modifier"),fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000460"))))
      edoneq[e].add(q)
      e2q2cl[str] = C("PHENO:$counter")
      manager.addAxiom(outont, equiv(C("PHENO:$counter"), some(R("has-part"),and(e, some(R("has-quality"),qq)))))
      def an = fac.getOWLAnnotation(fac.getRDFSLabel(), fac.getOWLLiteral(rendering.render(e).replace("\n"," ")+" "+rendering.render(q)))
      def axiom = fac.getOWLAnnotationAssertionAxiom(C("PHENO:$counter").getIRI(), an)
      manager.addAxiom(outont,axiom)
      q = and(q, some(R("has-modifier"),fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000460"))))
      counter += 1
    }
    def fcl = e2q2cl[str]
    line.each { fout.print(it+"\t") }
    fout.println(fcl)
  }
}


/* create structuring classes */
map.each { cl, exp ->
  exp.e.each { e ->
    if (e && !(e in done) && (e.toString().indexOf("GO")>-1 || e.toString().indexOf("UBERON")>-1 || e.toString().indexOf("ZFA")>-1)) {
      def qq = and(id2class["PATO:0000001"], some(R("has-modifier"),fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000460"))))
      manager.addAxiom(outont, equiv(C("PHENO:$counter"), some(R("has-part"),and(some(R("part-of"),e), some(R("has-quality"),qq)))))
      def an = fac.getOWLAnnotation(fac.getRDFSLabel(), fac.getOWLLiteral("Phenotypic abnormality of "+rendering.render(e).replace("\n"," ")))
      def axiom = fac.getOWLAnnotationAssertionAxiom(C("PHENO:$counter").getIRI(), an)
      manager.addAxiom(outont,axiom)
      counter += 1
      done.add(e)
    }
  }
}
map.each { cl, exp ->
  def temp = fac.getOWLThing()
  if (exp.hp.size()>0) {  // just intersect
    exp.hp.each { p ->
      temp = and(temp, p)
    }
    manager.addAxiom(outont, equiv(cl, temp))
  } else if (exp.e.size()==0 && exp.q.size()==0) {

  } else {
    def e = null
    // initialize e
    // intersect multiple entities, otherwise use the E field
    if (exp.e.size()>1) {
      e = and(exp.e[0],exp.e[1])
    } else if (exp.e.size() == 1) {
      e = exp.e[0]
    } else {
      e = fac.getOWLThing() // default entity, does no harm
    }
    
    // if ihp not empty, intersect e with "part-of some P"
    exp.ihp.each { p ->
      e = and(e, some(R("part-of"),p))
    }
    exp.resultsfrom.each { p ->
      e = and(e, some(R("results-from"),p))
    }
    exp.during.each { p ->
      e = and(e, some(R("during"), p))
    }
    exp.hasquality.each { p ->
      e = and(e, some(R("has-quality"), p))
    }
    exp.centralparticipant.each { p ->
      e = and(e, some(R("has-central-participant"), p))
    }
    exp.occ.each { p ->
      e = and(e, some(R("occurs-in"), p))
    }
    exp.e2.each { p ->
      e = and(e, some(R("towards"), p))
    }

    // Now do quality
    def q = id2class["PATO:0000001"]
    if (exp.q.size()>1) {
      q = and(exp.q[0], exp.q[1])
    } else if (exp.q.size()== 1) {
      q = exp.q[0]
    }

    /* if it's just a general abnormality (PATO:0000001), use "part-of some e" instead of just "e" */
    if (q == id2class["PATO:0000001"]) {
      e = some(R("part-of"), e)
    }
    // add the modifier abnormal
    exp.mod.each { p ->
      q = and(q, some(R("has-modifier"),p))
    }

    // Now add the axiom
    manager.addAxiom(outont, equiv(cl,some(R("has-part"),and(e, some(R("has-quality"),q)))))
  }
}

manager.addAxiom(outont, fac.getOWLTransitiveObjectPropertyAxiom(R("has-part")))
manager.addAxiom(outont, fac.getOWLTransitiveObjectPropertyAxiom(R("part-of")))
manager.addAxiom(outont, fac.getOWLReflexiveObjectPropertyAxiom(R("has-part")))
manager.addAxiom(outont, fac.getOWLReflexiveObjectPropertyAxiom(R("part-of")))

// occurs-in is sub-property of part-of
manager.addAxiom(outont, fac.getOWLSubObjectPropertyOfAxiom(R("occurs-in"), R("part-of")))

def q = and(id2class["PATO:0000001"], some(R("has-modifier"),id2class["PATO:0000460"]))

// add some more structuring classes
counter += 1
manager.addAxiom(outont, equiv(C("PHENO:$counter"), some(R("has-part"), and(some(R("part-of"), id2class["GO:0003674"]), some(R("has-quality"), q)))))
an = fac.getOWLAnnotation(fac.getRDFSLabel(), fac.getOWLLiteral("Phenotypic abnormality of molecular function"))
axiom = fac.getOWLAnnotationAssertionAxiom(C("PHENO:$counter").getIRI(), an)
manager.addAxiom(outont,axiom)
counter +=1
manager.addAxiom(outont, equiv(C("PHENO:$counter"), some(R("has-part"), and(some(R("part-of"), id2class["GO:0008150"]), some(R("has-quality"), q)))))
an = fac.getOWLAnnotation(fac.getRDFSLabel(), fac.getOWLLiteral("Phenotypic abnormality of biological process"))
axiom = fac.getOWLAnnotationAssertionAxiom(C("PHENO:$counter").getIRI(), an)
manager.addAxiom(outont,axiom)
counter +=1
manager.addAxiom(outont, equiv(C("PHENO:$counter"), some(R("has-part"), and(some(R("part-of"), id2class["UBERON:0000464"]), some(R("has-quality"), q)))))
an = fac.getOWLAnnotation(fac.getRDFSLabel(), fac.getOWLLiteral("Phenotypic abnormality of anatomical space"))
axiom = fac.getOWLAnnotationAssertionAxiom(C("PHENO:$counter").getIRI(), an)
manager.addAxiom(outont,axiom)

// top-level class defined through owl:Thing
manager.addAxiom(outont, equiv(id2class["HP:0000118"], some(R("has-part"), and(some(R("part-of"), fac.getOWLThing()), some(R("has-quality"), q)))))


an = fac.getOWLAnnotation(fac.getRDFSLabel(), fac.getOWLLiteral("has-part"))
axiom = fac.getOWLAnnotationAssertionAxiom(R("has-part").getIRI(), an)
manager.addAxiom(outont,axiom)
an = fac.getOWLAnnotation(fac.getRDFSLabel(), fac.getOWLLiteral("part-of"))
axiom = fac.getOWLAnnotationAssertionAxiom(R("part-of").getIRI(), an)
manager.addAxiom(outont,axiom)


/* Add mappings generated through third parties (AML, etc)*/
new File("mappings").eachFile { file ->
  file.splitEachLine("\t") { line ->
    if (!line[0].startsWith("#") && !line[0].startsWith("Source")) {
      def iri1 = fac.getOWLClass(IRI.create(line[0]))
      def iri2 = fac.getOWLClass(IRI.create(line[2]))
      def score = new Double(line[4])
      if (score >= 0.70) {
	manager.addAxiom(outont, equiv(iri1, iri2))
      }
    }
  }
}


// OWLImportsDeclaration importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/uberon.owl"))
// manager.applyChange(new AddImport(outont, importDecl1))

//importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/pato.owl"))
//manager.applyChange(new AddImport(outont, importDecl1))
//importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/go.owl"))
//manager.applyChange(new AddImport(outont, importDecl1))
importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/chebi.owl"))
manager.applyChange(new AddImport(outont, importDecl1))
//importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/pr.owl"))
//manager.applyChange(new AddImport(outont, importDecl1))
// importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/cl.owl"))
// manager.applyChange(new AddImport(outont, importDecl1))
//importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/nbo.owl"))
//manager.applyChange(new AddImport(outont, importDecl1))
importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/mpath.owl"))
manager.applyChange(new AddImport(outont, importDecl1))



manager.saveOntology(outont, IRI.create("file:/tmp/a-ordo-do.owl"))
