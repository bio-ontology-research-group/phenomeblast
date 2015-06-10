/* removes those lines in which field 0 and 1 are identify */

new File(args[0]).splitEachLine("\t") {
  if (it[0]!=it[1]) {
    println it[0]+"\t"+it[1]+"\t"+it[2]
  }
}