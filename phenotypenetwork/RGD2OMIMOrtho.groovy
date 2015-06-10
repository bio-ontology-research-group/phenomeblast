def mgi2rat = [:]

new File("RGD_ORTHOLOGS.txt").splitEachLine("\t") { line ->
  if (!line[0].startsWith("#")) {
    def rgd = "RGD:"+line[1]
    def mgi = line[10]
    mgi2rat[mgi] = rgd
  }
}

new File("omim-genes-positive.txt").splitEachLine("\t") { line ->
  def rgd = mgi2rat[line[1]]
  def omim = line[0]
  if (rgd) {
    println rgd+"\t"+omim
  }
}