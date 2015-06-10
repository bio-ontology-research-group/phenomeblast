import groovy.util.*

def fout = new PrintWriter(new BufferedWriter(new FileWriter("flymap-gene-allele.txt")))

def flyfile = new File("allele_phenotypic_data_fb_2011_05.tsv")
def flyont = new File("flybase_controlled_vocabulary.obo")
def flyant = new File("fly_anatomy.obo")

def chadofile = new File("FBal.xml")

def entries = new XmlSlurper().parse(chadofile)

def map = [:]
entries.allele.each { allele ->
  def id = allele.id.text()
  allele.related_feature.each { rel ->
    if ((rel.relationship_type.text()=="alleleof") && (rel.type=="gene")) {
      map[id] = rel.id.text()
    }
  }
}

map.keySet().each {
  fout.println(it+"\t"+map[it])
}
fout.flush()
fout.close()
