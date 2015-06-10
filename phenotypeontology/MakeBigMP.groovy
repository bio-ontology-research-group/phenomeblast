def fout1 = new PrintWriter(new FileWriter("mp-extended-xp.obo"))
def fout2 = new PrintWriter(new FileWriter("mp-extended.obo"))

def entities = new TreeSet()

def findEntities = { line ->
  if (line.indexOf("!")>1) {
    if (line.indexOf("inheres_in")>-1) {
      line = line.replaceAll("intersection_of: ","")
      def ent = line.substring(line.indexOf(" ")).trim()
      ent = ent.substring(0,ent.indexOf(" ")).trim()
      entities.add(ent)
    }
  }
}

new File("obo/mp-xp.obo").eachLine (findEntities)
new File("obo/hp-xp.obo").eachLine (findEntities)
new File("obo/fp-xp.obo").eachLine (findEntities)
new File("obo/worm_phenotype_xp.obo").eachLine (findEntities)
new File("obo/yp-xp.obo").eachLine (findEntities)
new File("obo/dicty-xp.obo").eachLine (findEntities)

new File("fish/fishphenotypes.txt").splitEachLine("\t") { line ->
  entities.add(line[1])
}

def t2i = [:]
def term = ""
def e2uberon = [:]
new File("obo/uberon.obo").eachLine { line ->
  if (line.startsWith("id: ")) {
    term = line.substring(4).trim()
    t2i[term] = new TreeSet()
  }
  if (line.startsWith("xref: ")) {
    def xref = line.substring(6).trim()
    t2i[term].add(xref)
    e2uberon[xref] = term
  }
}

def done = new TreeSet()
entities.each { e ->
  if (e2uberon[e]!=null) {
    done.add(e2uberon[e])
  }
}
def counter = 5000000
fout1.println("")
fout2.println("")
done.each { ub ->
  fout1.println("[Term]\nid: MP:"+counter)
  fout1.println("intersection_of: PATO:0000001")
  fout1.println("intersection_of: inheres_in $ub\n")
  fout2.println("[Term]\nid: MP:"+counter)
  fout2.println("name: abnormality of $ub\n")
  counter +=1
}
entities.each { e ->
  if (e.startsWith("GO:") || e.startsWith("CL:")) {
    fout1.println("[Term]\nid: MP:"+counter)
    fout1.println("intersection_of: PATO:0000001")
    fout1.println("intersection_of: inheres_in $e\n")
    fout2.println("[Term]\nid: MP:"+counter)
    fout2.println("name: abnormality of $e\n")
    counter +=1
  }
}
fout1.flush()
fout2.flush()
fout1.close()
fout2.close()
