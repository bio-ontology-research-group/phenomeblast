import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary

def onturi = "http://purl.obolibrary.org/obo/"
def reluri = "http://purl.obolibrary.org/obo/PHENOMENET_"


OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
OWLDataFactory fac = manager.getOWLDataFactory()

def po = fac.getOWLObjectProperty(IRI.create(reluri+"part-of"))
def hp = fac.getOWLObjectProperty(IRI.create(reluri+"has-part"))
def inheresin = fac.getOWLObjectProperty(IRI.create(reluri+"inheres-in"))
def hasquality = fac.getOWLObjectProperty(IRI.create(reluri+"has-quality"))
def qualifier = fac.getOWLObjectProperty(IRI.create(reluri+"qualifier"))
def towards = fac.getOWLObjectProperty(IRI.create(reluri+"towards"))
def pheneof = fac.getOWLObjectProperty(IRI.create(reluri+"phene-of"))

OWLOntology ont = manager.createOntology(IRI.create(onturi+System.currentTimeMillis()))


String strip(String a) {
  if (a.indexOf("!")>-1) {
    a = a.substring(0,a.indexOf("!")).trim()
  }
  a = a.replaceAll("_:","anon:")
  a = a.replaceAll(":", "_")
  a
}

OWLClass mc(String a, OWLDataFactory fac) {
  return fac.getOWLClass(IRI.create(onturi+a))
}

def pf = new File(args[0])

def list = []
def lines = []
def exp = new Expando()
exp.rels = []
exp.haspart = []
exp.inter = []

pf.eachLine { line ->
  if (!line.startsWith("!")) {
    lines << line
  }
  if (line=="[Term]") {
    if (lines.contains("[Term]")) { // need to convert the term
      lines.each {
	if (it.indexOf("id:")>-1) {
	  exp.id = strip(it.substring(3).trim())
	  if (exp.id.indexOf(":_")>-1) {
	    exp.anatomy = true
	  } else {
	    exp.anatomy = false
	  }
	}
      }
      if (!exp.anatomy) {
	lines.each {
	  if (it.indexOf("intersection_of:")>-1) {
	    exp.intersection = true
	  }
	  if (it.indexOf("union_of:")>-1) {
	    exp.intersection = false
	  }
	}
	lines.each {
	  if (it.indexOf("intersection_of: towards")>-1) {
	    exp.towards = strip(it.substring(25).trim())
	  } else
	  if (it.indexOf("intersection_of: qualifier")>-1) {
	    exp.qualifier = strip(it.substring(27).trim())
	  } else
	  if (it.indexOf("intersection_of: PATO:")>-1) {
	    exp.quality = strip(it.substring(17).trim())
	  } else
	  if (it.indexOf("intersection_of: inheres_in ")>-1) {
	    exp.inheresIn = strip(it.substring(28).trim())
	  } else
	  if (it.indexOf("intersection_of: inheres_in_part_of ")>-1) {
	    exp.inheresInPartOf = strip(it.substring(35).trim())
	  } else 
	  if (it.indexOf("intersection_of: has_part ")>-1) {
	    exp.haspart << strip(it.substring(26).trim())
	  } else 
	  if (it.indexOf("intersection_of: ")>-1) {
	    if (strip(it.substring(17).trim()).indexOf(" ")>-1) {
	      exp.rels << strip(it.substring(17).trim())
	    } else {
	      exp.inter << strip(it.substring(17).trim())
	    }
	  }
	  if (it.indexOf("union_of: ")>-1) {
	    if (strip(it.substring(10).trim()).indexOf(" ")>-1) {
	      exp.rels << strip(it.substring(10).trim())
	    } else {
	      exp.inter << strip(it.substring(10).trim())
	    }
	  }
	}
      }
    }
    lines = []
    if ((exp.id!=null) && (exp.id.size()>2)) {
      list << exp
    }
    exp = new Expando()
    exp.rels = []
    exp.haspart = []
    exp.inter = []
    lines << line
  }
}

list.each { term ->
  term.haspart.each {
    if ((it.indexOf("MA:")>-1) || (it.indexOf("UBERON:")>-1) || (it.indexOf("FMA:")>-1) || (it.indexOf("GO:")>-1)) {
      term.isAnatomy = true
    }
  }
  if ((term.quality == "PATO_0000001") && (term.inheresInPartOf == null)) {
    term.inheresInPartOf = term.inheresIn
    term.inheresIn = null
  }
}

list.each { term ->
  if (term.isAnatomy) {
    
  } else {
    def cl3 = fac.getOWLThing()
    if (term.inheresIn!=null) {
      cl3 = fac.getOWLClass(IRI.create(onturi+term.inheresIn))
    }
    if (term.inheresInPartOf!=null && term.inheresIn!=null) {
      cl3 = fac.getOWLObjectIntersectionOf(
	cl3, fac.getOWLObjectSomeValuesFrom(
	  po, fac.getOWLClass(IRI.create(onturi+term.inheresInPartOf))))
    } else if (term.inheresInPartOf!=null) {
      cl3 = fac.getOWLObjectSomeValuesFrom(
	po, fac.getOWLClass(IRI.create(onturi+term.inheresInPartOf)))
    }
    term.rels.each {
      def rn = it.substring(0,it.indexOf(" ")).trim()
      def te = strip(it.substring(it.indexOf(" ")).trim())
      def op = fac.getOWLObjectProperty(IRI.create(reluri+rn))
      def tcl = fac.getOWLObjectSomeValuesFrom(op, fac.getOWLClass(IRI.create(onturi+te)))
      cl3 = fac.getOWLObjectIntersectionOf(cl3, tcl)
    }

    term.haspart.each { // intersect the has-parts for phenotypes
      cl3 = fac.getOWLObjectIntersectionOf(cl3, fac.getOWLClass(IRI.create(onturi+strip(it).trim())))
    }

    if (term.intersection) {
      term.inter.each {
	cl3 = fac.getOWLObjectIntersectionOf(cl3, fac.getOWLClass(IRI.create(onturi+it)))
      }
    } else {
      def unions = new TreeSet()
      term.inter.each {
	unions << fac.getOWLClass(IRI.create(onturi+it))
      }
      cl3 = fac.getOWLObjectUnionOf(unions)
    }

    def qcl = fac.getOWLThing()
    if (term.quality!=null) {
      qcl = fac.getOWLClass(IRI.create(onturi+term.quality))
    }
    if ((term.qualifier!=null) && (term.quality!="abnormal")) {
      qcl = fac.getOWLObjectIntersectionOf(
	qcl, fac.getOWLObjectSomeValuesFrom(
	  qualifier, fac.getOWLClass(IRI.create(onturi+term.qualifier))))
    }
    if (term.towards!=null) {
      qcl = fac.getOWLObjectIntersectionOf(
	qcl, fac.getOWLObjectSomeValuesFrom(
	  towards, fac.getOWLClass(IRI.create(onturi+term.towards))))
    }

    def cl1 = null
    if (term.towards||term.qualifier||term.quality||(term.rels.size()>0)) {
      cl1 = fac.getOWLObjectSomeValuesFrom(
	hp, fac.getOWLObjectIntersectionOf(
	  cl3, fac.getOWLObjectSomeValuesFrom(
	    hasquality,qcl)))
    } else {
      cl1 = cl3
    }

    if ((term.quality!=null) && ((term.quality.indexOf("HP")>-1)||(term.quality.indexOf("MP")>-1))) {
      if ((term.inheresIn!=null) || (term.inheresInPartOf!=null)) {
	cl1 = fac.getOWLObjectSomeValuesFrom(
	  hp, fac.getOWLObjectIntersectionOf(
	    cl3, qcl))
      }
    }

    if (term.haspart.size()>1) {
      cl1 = cl3
    }
    if (term.id!=null) {
      def res = fac.getOWLClass(IRI.create(onturi+term.id))
      def ax = fac.getOWLEquivalentClassesAxiom(res, cl1)
      manager.addAxiom(ont, ax)
    }
    
  }
  //  println "${it.id}: has-part some (${it.inheresIn} and has-quality some (${it.quality} and qualifier some ${it.qualifier}))"
}

manager.saveOntology(ont, IRI.create("file:"+new File(args[1]).getCanonicalFile()))

