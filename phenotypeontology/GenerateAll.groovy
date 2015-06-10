/* groovy GenerateGeneric infile.owl Subclass Superclass */

import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.elk.owlapi.*
//import de.tudresden.inf.lat.cel.owlapi.*


def directory = "/tmp/mappings/"

void generateIt(PrintWriter fo, OWLOntology ont, OWLReasoner reasoner, String sub, String sup) {
  ont.getClassesInSignature().each { cl ->
    def clst = cl.toString()
    if (clst.indexOf(sub)>0) {
      def s = reasoner.getSuperClasses(cl, false).getFlattened()
      def t = reasoner.getEquivalentClasses(cl).getEntities()
      s.addAll(t)
      fo.print(clst+"\t")
      s.each {
	if (it.toString().indexOf(sup)>0) {
	  fo.print(it.toString()+"\t")
	}
      }
      fo.println("")
    }
  }
  fo.flush()
  fo.close()
}

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(args[0]))

OWLDataFactory fac = manager.getOWLDataFactory()

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasonerFactory fac1 = new ElkReasonerFactory()
//OWLReasoner reasoner = new CelReasoner(ont,config)
OWLReasoner reasoner = fac1.createReasoner(ont,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

def hp2mp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"hp2mp.txt")))
def mp2mp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"mp2mp.txt")))
def wp2mp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"wp2mp.txt")))
def yp2mp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"yp2mp.txt")))
def fp2mp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"fp2mp.txt")))
def zfin2mp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"zfin2mp.txt")))

def hp2wp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"hp2wp.txt")))
def hp2yp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"hp2yp.txt")))
def hp2hp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"hp2hp.txt")))
def hp2fp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"hp2fp.txt")))

def mp2hp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"mp2hp.txt")))
def mp2wp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"mp2wp.txt")))
def mp2yp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"mp2yp.txt")))
def mp2fp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"mp2fp.txt")))

def wp2hp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"wp2hp.txt")))
def wp2yp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"wp2yp.txt")))
def wp2wp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"wp2wp.txt")))
def wp2fp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"wp2fp.txt")))

def yp2hp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"yp2hp.txt")))
def yp2wp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"yp2wp.txt")))
def yp2yp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"yp2yp.txt")))
def yp2fp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"yp2fp.txt")))

def fp2hp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"fp2hp.txt")))
def fp2wp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"fp2wp.txt")))
def fp2yp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"fp2yp.txt")))
def fp2fp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"fp2fp.txt")))

def mgi2hp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"mgi2hp.txt")))
def mgi2mp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"mgi2mp.txt")))
def mgi2yp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"mgi2yp.txt")))
def mgi2wp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"mgi2wp.txt")))
def mgi2fp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"mgi2fp.txt")))
def mgi2omim = new PrintWriter(new BufferedWriter(new FileWriter(directory+"mgi2omim.txt")))

def zfin2hp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"zfin2hp.txt")))
def zfin2yp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"zfin2yp.txt")))
def zfin2wp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"zfin2wp.txt")))
def zfin2fp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"zfin2fp.txt")))
def zfin2omim = new PrintWriter(new BufferedWriter(new FileWriter(directory+"zfin2omim.txt")))

def omim2hp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"omim2hp.txt")))
def omim2mp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"omim2mp.txt")))
def omim2yp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"omim2yp.txt")))
def omim2wp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"omim2wp.txt")))
def omim2fp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"omim2fp.txt")))
def omim2zfin = new PrintWriter(new BufferedWriter(new FileWriter(directory+"omim2zfin.txt")))
def omim2mgi = new PrintWriter(new BufferedWriter(new FileWriter(directory+"omim2mgi.txt")))

def worm2hp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"worm2hp.txt")))
def worm2mp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"worm2mp.txt")))
def worm2yp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"worm2yp.txt")))
def worm2wp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"worm2wp.txt")))
def worm2fp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"worm2fp.txt")))

def fly2hp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"fly2hp.txt")))
def fly2mp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"fly2mp.txt")))
def fly2yp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"fly2yp.txt")))
def fly2wp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"fly2wp.txt")))
def fly2fp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"fly2fp.txt")))

def yeast2hp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"yeast2hp.txt")))
def yeast2mp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"yeast2mp.txt")))
def yeast2yp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"yeast2yp.txt")))
def yeast2wp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"yeast2wp.txt")))
def yeast2fp = new PrintWriter(new BufferedWriter(new FileWriter(directory+"yeast2fp.txt")))


generateIt(hp2mp, ont, reasoner, "HP_", "MP_")
generateIt(mp2mp, ont, reasoner, "MP_", "MP_")
generateIt(fp2mp, ont, reasoner, "FBcv_", "MP_")
generateIt(wp2mp, ont, reasoner, "WBPhenotype_", "MP_")
generateIt(yp2mp, ont, reasoner, "APO_", "MP_")
generateIt(zfin2mp, ont, reasoner, "ZDB-", "MP_")

generateIt(fp2wp, ont, reasoner, "FBcv_", "WBPhenotype_")
generateIt(fp2yp, ont, reasoner, "FBcv_", "APO_")
generateIt(fp2hp, ont, reasoner, "FBcv_", "HP_")
generateIt(fp2fp, ont, reasoner, "FBcv_", "FBcv_")

generateIt(hp2wp, ont, reasoner, "HP_", "WBPhenotype_")
generateIt(hp2yp, ont, reasoner, "HP_", "APO_")
generateIt(hp2hp, ont, reasoner, "HP_", "HP_")
generateIt(hp2fp, ont, reasoner, "HP_", "FBcv_")

generateIt(mp2hp, ont, reasoner, "MP_", "HP_")
generateIt(mp2wp, ont, reasoner, "MP_", "WBPhenotype_")
generateIt(mp2yp, ont, reasoner, "MP_", "APO_")
generateIt(mp2fp, ont, reasoner, "MP_", "FBcv_")

generateIt(wp2hp, ont, reasoner, "WBPhenotype_", "HP_")
generateIt(wp2yp, ont, reasoner, "WBPhenotype_", "APO_")
generateIt(wp2wp, ont, reasoner, "WBPhenotype_", "WBPhenotype_")
generateIt(wp2fp, ont, reasoner, "WBPhenotype_", "FBcv_")

generateIt(yp2hp, ont, reasoner, "APO_", "HP_")
generateIt(yp2wp, ont, reasoner, "APO_", "WBPhenotype_")
generateIt(yp2yp, ont, reasoner, "APO_", "APO_")
generateIt(yp2fp, ont, reasoner, "APO_", "FBcv_")

generateIt(omim2zfin, ont, reasoner, "OMIM:", "ZDB-")
generateIt(omim2mgi, ont, reasoner, "OMIM:", "MGI:")
generateIt(mgi2omim, ont, reasoner, "MGI:", "OMIM:")
generateIt(zfin2omim, ont, reasoner, "ZDB-", "OMIM:")

generateIt(mgi2hp, ont, reasoner, "MGI:", "HP_")
generateIt(mgi2mp, ont, reasoner, "MGI:", "MP_")
generateIt(mgi2wp, ont, reasoner, "MGI:", "WBPhenotype_")
generateIt(mgi2yp, ont, reasoner, "MGI:", "APO_")
generateIt(mgi2fp, ont, reasoner, "MGI:", "FBcv_")

generateIt(zfin2hp, ont, reasoner, "ZDB-", "HP_")
generateIt(zfin2wp, ont, reasoner, "ZDB-", "WBPhenotype_")
generateIt(zfin2yp, ont, reasoner, "ZDB-", "APO_")
generateIt(zfin2fp, ont, reasoner, "ZDB-", "FBcv_")

generateIt(omim2hp, ont, reasoner, "OMIM:", "HP_")
generateIt(omim2mp, ont, reasoner, "OMIM:", "MP_")
generateIt(omim2wp, ont, reasoner, "OMIM:", "WBPhenotype_")
generateIt(omim2yp, ont, reasoner, "OMIM:", "APO_")
generateIt(omim2fp, ont, reasoner, "OMIM:", "FBcv_")

generateIt(worm2hp, ont, reasoner, "WB", "HP_")
generateIt(worm2mp, ont, reasoner, "WB", "MP_")
generateIt(worm2yp, ont, reasoner, "WB", "APO_")
generateIt(worm2wp, ont, reasoner, "WB", "WBPhenotype_")
generateIt(worm2fp, ont, reasoner, "WB", "FBcv_")

generateIt(fly2hp, ont, reasoner, "FBal", "HP_")
generateIt(fly2mp, ont, reasoner, "FBal", "MP_")
generateIt(fly2yp, ont, reasoner, "FBal", "APO_")
generateIt(fly2wp, ont, reasoner, "FBal", "WBPhenotype_")
generateIt(fly2fp, ont, reasoner, "FBal", "FBcv_")

generateIt(yeast2hp, ont, reasoner, "S000", "HP_")
generateIt(yeast2mp, ont, reasoner, "S000", "MP_")
generateIt(yeast2yp, ont, reasoner, "S000", "APO_")
generateIt(yeast2wp, ont, reasoner, "S000", "WBPhenotype_")
generateIt(yeast2fp, ont, reasoner, "S000", "FBcv_")


reasoner.finalize()
//reasoner.getSubClasses(fac.getOWLClass(IRI.create("http://purl.org/obo/owlapi/phene#HP_0000118")), false).each { println it }

