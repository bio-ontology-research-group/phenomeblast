import java.text.*
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

DecimalFormat df = new DecimalFormat("0.000");
new File(args[0]).splitEachLine(" ") { line ->
  def did = line[0]
  def name = doid2name[line[0]]
  def auc = new Double(line[1])
  def count = new Double(line[2])
  def pm = Math.sqrt(auc * (1-auc) / count)
  println "$name ({\\tt $did}) & \$"+df.format(auc)+" \\pm "+df.format(pm)+"\$ \\\\"
}