/* Generate merged flat file of model organism annotations */

@Grapes([
          @Grab(group='org.semanticweb.elk', module='elk-owlapi', version='0.4.2'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='4.1.0'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='4.1.0'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='4.1.0'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-parsers', version='4.1.0'),
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

// OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

// OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("a-inferred.owl"))

// OWLDataFactory fac = manager.getOWLDataFactory()
// ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
// OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
// ElkReasonerFactory f1 = new ElkReasonerFactory()
// OWLReasoner reasoner = f1.createReasoner(ont,config)
// reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

// def R = { String s ->
//   if (s == "part-of") {
//     fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050"))
//   } else if (s == "has-part") {
//     fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000051"))
//   } else {
//     fac.getOWLObjectProperty(IRI.create("http://aber-owl.net/#"+s))
//   }
// }

// def C = { String s ->
//   fac.getOWLClass(IRI.create(onturi+s))
// }

// def and = { cl1, cl2 ->
//   fac.getOWLObjectIntersectionOf(cl1,cl2)
// }
// def some = { r, cl ->
//   fac.getOWLObjectSomeValuesFrom(r,cl)
// }
// def equiv = { cl1, cl2 ->
//   fac.getOWLEquivalentClassesAxiom(cl1, cl2)
// }
// def subclass = { cl1, cl2 ->
//   fac.getOWLSubClassOfAxiom(cl1, cl2)
// }



def id2geneid = [:].withDefault { new TreeSet() }

def omimphenotypes = new TreeSet()
new File("omim/mim2gene.txt").splitEachLine("\t") { line ->
  if (line[1]=="gene") { // phenotype
    def id = "OMIM:"+line[0]
    omimphenotypes.add(id)
    id2geneid[id].add("ENTREZ:"+line[2])
    id2geneid[id].add(line[3])
    id2geneid[id].add(line[4])
  }
}

def map = [:].withDefault { new TreeSet() }
new File("diseasephenotypes/phenotype_annotation.tab").splitEachLine("\t") { line ->
  def id = line[0]+":"+line[1]
  if (id in omimphenotypes) {
    def pheno = line[4]
    map[id].add(pheno)
  }
}

/* Pinky and the Brain */
def id2markers = [:].withDefault { new TreeSet() }
new File("modelphenotypes/MGI_PhenoGenoMP.rpt").splitEachLine("\t") { line ->
  def id = "AllelicComposition: "+line[0]+"; Strain: "+line[2]
  def pheno = line[3]
  map[id].add(pheno)
  def markers = line[5].split(",")
  id2markers[id].addAll(markers)
}
new File("modelphenotypes/HMD_HumanPhenotype.rpt").splitEachLine("\t") { line ->
  def id = line[4]?.trim()
  id2geneid[id].add(line[0]?.trim())
  id2geneid[id].add("ENTREZ:"+(line[1]?.trim()))
}


// do the fish *blub* *blub*
new File("modelphenotypes/ortho_2015.12.16.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def hid = "ENTREZ:"+line[4]
  id2geneid[id].add(hid)
  id2geneid[id].add(line[3])
}

new File("fishies.txt").splitEachLine("\t") { line ->
  def id = line[18]
  def gid = line[2]
  id2geneid[id].add(gid)
  def pheno = line[-1]?.replaceAll(">","")?.replaceAll("<","")?.trim()
  map[id].add(pheno)
  id2markers[id].add(gid)
}

map.each { k, v ->
  def l = new TreeSet()
  id2markers[k]?.each {
    l.addAll(id2geneid[it])
  }
  v.each {
    if (l.size()>0) {
      println "$k\t$it\t"+l
    } else {
      println "$k\t$it\t"+id2geneid[k]
    }
  }
}
