import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.elk.owlapi.*

def CUTOFF = 0.005 // use 0.5% as level of significance

Integer icdshort(String icd) {
  String result = icd
  Integer resultInt = 0
  if (icd.indexOf(".")>-1) {
    result = result.substring(0,result.indexOf("."))
  }
  if (icd.indexOf("-")>-1) {
    result = result.substring(0,result.indexOf("-"))
  }
  try {
    resultInt = new Integer(result)
  } catch (Exception E) {}

  resultInt

}

def icdnum2class = [:]
(001..139).each { icdnum2class[it] = "INFECTIOUS AND PARASITIC DISEASES" }
(140..239).each { icdnum2class[it] = "NEOPLASMS" }
(240..279).each { icdnum2class[it] = "ENDOCRINE, NUTRITIONAL AND METABOLIC DISEASES, AND IMMUNITY DISORDERS" }
(280..289).each { icdnum2class[it] = "DISEASES OF THE BLOOD AND BLOOD-FORMING ORGANS" }
(290..319).each { icdnum2class[it] = "MENTAL DISORDERS" }
(320..389).each { icdnum2class[it] = "DISEASES OF THE NERVOUS SYSTEM AND SENSE ORGANS" }
(390..459).each { icdnum2class[it] = "DISEASES OF THE CIRCULATORY SYSTEM" }
(460..519).each { icdnum2class[it] = "DISEASES OF THE RESPIRATORY SYSTEM" }
(520..579).each { icdnum2class[it] = "DISEASES OF THE DIGESTIVE SYSTEM" }
(580..629).each { icdnum2class[it] = "DISEASES OF THE GENITOURINARY SYSTEM" }
(630..679).each { icdnum2class[it] = "COMPLICATIONS OF PREGNANCY, CHILDBIRTH, AND THE PUERPERIUM" }
(680..709).each { icdnum2class[it] = "DISEASES OF THE SKIN AND SUBCUTANEOUS TISSUE" }
(710..739).each { icdnum2class[it] = "DISEASES OF THE MUSCULOSKELETAL SYSTEM AND CONNECTIVE TISSUE" }
(740..759).each { icdnum2class[it] = "CONGENITAL ANOMALIES" }
(760..779).each { icdnum2class[it] = "CERTAIN CONDITIONS ORIGINATING IN THE PERINATAL PERIOD" }
(780..799).each { icdnum2class[it] = "SYMPTOMS, SIGNS, AND ILL-DEFINED CONDITIONS" }
(800..999).each { icdnum2class[it] = "INJURY AND POISONING" }
//println icdnum2class

def dotop = ["DOID:1287", "DOID:28", "DOID:77", "DOID:2914", "DOID:16", "DOID:17", "DOID:863", "DOID:15", "DOID:1579", "DOID:0060118", "DOID:18", "DOID:14566", "DOID:150", "DOID:0014667", "DOID:630", "DOID:0080015", "DOID:225", "DOID:104", "DOID:1564", "DOID:1398", "DOID:934", "DOID:0050117"]

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()
OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("HumanDO.obo"))
OWLDataFactory fac = manager.getOWLDataFactory()
OWLReasonerFactory reasonerFactory = null
ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasonerFactory fac1 = new ElkReasonerFactory()
OWLReasoner reasoner = fac1.createReasoner(ont)
reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)


def doid2name = [:]
def doid2icd = [:]
def id = ""
new File("HumanDO.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
  }
  if (line.startsWith("name:")) {
    doid2name[id] = line.substring(5).trim()
  }
  if (line.startsWith("xref: ICD9CM:")) {
    doid2icd[id] = line.substring(13).trim()
  }
}

/*
def dis2cluster = [:]
new File("cluster-index.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def disease = line[1].replaceAll("http://phenomebrowser.net/smltest/","")
  dis2cluster[disease] = id
}
*/

def list = []

new File(args[0]).splitEachLine("\t") { line ->
  list << new Double(line[2])
}

Integer index = Math.round(CUTOFF * list.size())
list = list.sort().reverse()
def limit = list[index]


//def map = [:].withDefault { [:] }
new File(args[0]).splitEachLine("\t") { line ->
  def val = new Double(line[2])
  def doid1 = line[0]
  def doid2 = line[1]
  doid1 = doid1.replaceAll("http://phenomebrowser.net/smltest/","")
  doid2 = doid2.replaceAll("http://phenomebrowser.net/smltest/","")
  if (val >= limit && doid1 != doid2) {
    def cl = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+doid1.replaceAll(":","_")))
    def cl2 = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+doid2.replaceAll(":","_")))
    def category = ""
    def category2 = ""
    reasoner.getSuperClasses(cl, false).getFlattened().each { sclass ->
      sclass = sclass.toString().replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll("_",":").replaceAll(">","")
      if (sclass in dotop) {
	category = category + doid2name[sclass]+" ; "
      }
    }
    reasoner.getSuperClasses(cl2, false).getFlattened().each { sclass ->
      sclass = sclass.toString().replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll("_",":").replaceAll(">","")
      if (sclass in dotop) {
	category2 = category2 + doid2name[sclass]+" ; "
      }
    }

    def icd1short = ""
    def icd2short = ""
    if (doid2icd[doid1] != null && doid2icd[doid2] != null) {
      icd1short = icdshort(doid2icd[doid1])
      icd2short = icdshort(doid2icd[doid2])
    }
    if (category.indexOf(";")>-1) {
      println doid1+"\t"+doid2name[doid1]+"\t"+doid2icd[doid1]+"\t"+icdnum2class[icd1short]+"\t"+category+"\t"+doid2+"\t"+doid2name[doid2]+"\t"+doid2icd[doid2]+"\t"+icdnum2class[icd2short]+"\t"+category2+"\t"+val
    }
  }
}
