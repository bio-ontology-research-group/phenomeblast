def infile = new File(args[0]) // xp file
def out1 = new PrintWriter(new BufferedWriter(new FileWriter(args[1]))) // write XP
def out2 = new PrintWriter(new BufferedWriter(new FileWriter(args[2]))) // write anatomy

def flag = false
infile.eachLine {
  if (it.indexOf("!!!! GO cross products")>-1) {
    flag = true
  }
  if (!flag) {
    out1.println(it)
  } else {
    out2.println(it)
  }
}
out1.println()
out2.println()

out1.flush()
out1.close()
out2.flush()
out2.close()
