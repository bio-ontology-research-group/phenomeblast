def results = new TreeSet()
new File("phenotypes/").eachFile { file ->
  file.splitEachLine("\t") { line ->
    results.add(line[0]+"\t"+line[1])
  }
}
results.each { 
  println it
}
