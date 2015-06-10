import groovy.json.*

def name = ""
def id = ""
def id2name = [:]

def flist = ["ascomycete_phenotype.obo", "dicty_phenotypes.obo", "flybase_controlled_vocabulary.obo", "human-phenotype-ontology.obo", "mammalian_phenotype.obo", "worm_phenotype.obo"]
//def flist = ["human-phenotype-ontology.obo", "mammalian_phenotype.obo"]
flist = flist.collect { new File("ontologies/$it") }
def outlist = []
flist.each { ofile ->
  ofile.eachLine { line ->
    if (line.startsWith("id:")) {
      id = line.substring(3).trim()
    }
    if (line.startsWith("name:")) {
      name = line.substring(5).trim()
      id2name[id] = [:]
      id2name[id]["id"] = id
      id2name[id]["value"] = id
      id2name[id]["name"] = name
      id2name[id]["key"] = name
      id2name[id]["sort"] = "0"
      id2name[id]["hidden"] = id
      id2name[id]["suggestion"] = "$name (<tt>$id</tt>)"
      id2name[id]["suggestable"] = "true"
      id2name[id]["level"] = "0"
      outlist << id2name[id]
    }
  }
}

def builder = new JsonBuilder(outlist)

println builder.toPrettyString()