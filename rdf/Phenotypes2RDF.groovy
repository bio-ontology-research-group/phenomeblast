import com.hp.hpl.jena.rdf.model.*
import com.hp.hpl.jena.vocabulary.*

def fout = new PrintWriter(new BufferedWriter(new FileWriter(args[0])))

String faldons = "http://biohackathon.org/resource/faldo#"
String reluri = "http://phenomebrowser.net#"
String enturi = "http://phenomebrowser.net/explore.php?id="

Model model = ModelFactory.createDefaultModel()

new File("../phenotypenetwork/phenotypes/").eachFile { f ->
  f.splitEachLine("\t") { line ->
    def id = line[0]
    def pid = line[1]
    if (id!=null && pid!=null && id!="null" && pid!="null" && id.indexOf(" ")==-1 && pid.indexOf(" ")==-1) {
      def res1 = model.createResource("http://phenomebrowser.net/explore.php?id="+id)
      def res2 = model.createResource("http://purl.obolibrary.org/obo/"+pid.replaceAll(":","_"))
      def prop = model.createProperty(reluri, "has-phenotype")
      def stmt = model.createStatement(res1, prop, res2)
      model.add(stmt)
    }
  }
}

new File("geneinfo/MRK_List1.rpt").splitEachLine("\t") { line ->
  def id = line[0]
  if (id != "NULL" && id.indexOf(" ")==-1) {
    def res1 = model.createResource(enturi+id)
    def prop = model.createProperty(reluri, "on-chromosome")
    try {
      res1.addProperty(prop, new Integer(line[1]))
    } catch (Exception E) {}
    try {
      def start = new Integer(line[3])
      def end = new Integer(line[4])
      res1.addProperty(model.createProperty(reluri, "start"), start)
      res1.addProperty(model.createProperty(reluri, "end"), end)
    } catch (Exception E) {}
    def strand = line[5]
    if (strand == null) {
      strand = 0
    } else if (strand == "+") {
      strand = 1
    } else {
      strand = -1
    }
    res1.addProperty(model.createProperty(reluri, "strand"), strand)
  }
}

def id2hid = [:]
def hid2id = [:]
new File("geneinfo/HOM_AllOrganism.rpt").splitEachLine("\t") { line ->
  def hid = line[0]
  if (hid2id[hid] == null) {
    hid2id[hid] = new TreeSet()
  }
  def mgiid = line[5]
  def orgid = line[2]
  def omimid = line[7]
  if (orgid == "10090") {
    id2hid[mgiid] = hid
    hid2id[hid].add(mgiid)
  }
  if (omimid!=null) {
    id2hid["OMIM:"+omimid] = hid
    hid2id[hid].add("OMIM:"+omimid)
  }
}
hid2id.each { hid, ortho ->
  def mgi = null
  def omim = null
  ortho.each { o ->
    if (o.indexOf("MGI")>-1){
      mgi = o
    }
    if (o.indexOf("OMIM")>-1){
      omim = o
    }
  }
  if (mgi!=null && omim!=null) {
    def res1 = model.createResource(enturi+mgi)
    def res2 = model.createResource(enturi+omim)
    res1.addProperty(model.createProperty(reluri, "ortholog"), res2)
  }
}


new File("geneinfo/omim-genes-pos.txt").splitEachLine("\t") { line ->
  def id1 = line[0]
  def id2 = line[1]
  def res1 = model.createResource(enturi+id2)
  def res2 = model.createResource(enturi+id1)
  res1.addProperty(model.createProperty(reluri, "disease"), res2)
}
new File("geneinfo/mgi-gene-positive.txt").splitEachLine("\t") { line ->
  def id1 = line[0]
  def id2 = line[1]
  def res1 = model.createResource(enturi+id2)
  def res2 = model.createResource(enturi+id1)
  res1.addProperty(model.createProperty(reluri, "disease"), res2)
}


model.write(fout)
