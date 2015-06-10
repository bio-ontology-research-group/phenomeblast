new File(args[0]).splitEachLine("\t") {
  println it[1]+"\t"+it[0]
}