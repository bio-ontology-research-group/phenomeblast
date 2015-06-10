MINCOOC = 5
MINOC = 5
MINSCORE = 0.1

for (MINOC = 0 ; MINOC <= 0 ; MINOC += 5) {
  for (MINSCORE = 0.95 ; MINSCORE <= 1 ; MINSCORE += 0.01) {
    MINCOOC = MINOC

    new File("../../pmcanalysis/results/medline-simple").splitEachLine("\t") { line ->
      def did = line[0]
      def pheno = line[2]
      def score = new Double(line[4])
      def cooc = new Double(line[7])
      def oc1 = new Double(line[5])
      def oc2 = new Double(line[6])
      if (cooc>MINCOOC && score > MINSCORE && oc1 > MINOC && oc2 > MINOC) {
	println "MEDLINE-$MINOC-$MINSCORE-$did\t$pheno"
      }
    }

    new File("../../pmcanalysis/results/medline-processed").splitEachLine("\t") { line ->
      def did = line[0]
      def pheno = line[2]
      def score = new Double(line[4])
      def cooc = new Double(line[7])
      def oc1 = new Double(line[5])
      def oc2 = new Double(line[6])
      if (cooc>MINCOOC && score > MINSCORE && oc1 > MINOC && oc2 > MINOC) {
	println "MEDLINEPROCESSED-$MINOC-$MINSCORE-$did\t$pheno"
      }
    }
    
    new File("../../pmcanalysis/results/pmc-simple").splitEachLine("\t") { line ->
      def did = line[0]
      def pheno = line[2]
      def score = new Double(line[4])
      def cooc = new Double(line[7])
      def oc1 = new Double(line[5])
      def oc2 = new Double(line[6])
      if (cooc>MINCOOC && score > MINSCORE && oc1 > MINOC && oc2 > MINOC) {
	println "PMC-$MINOC-$MINSCORE-$did\t$pheno"
      }
    }

  }
}


// new File("../../pmcanalysis/results/medline-drugs-full-processed").splitEachLine("\t") { line ->
//   def did = line[0]
//   def pheno = line[2]
//   def score = new Double(line[4])
//   def cooc = new Double(line[7])
//   def oc1 = new Double(line[5])
//   def oc2 = new Double(line[6])
//   if (cooc>MINCOOC && score > MINSCORE && oc1 > MINOC && oc2 > MINOC) {
//     println "MEDLINE$did\t$pheno"
//   }
// }

// new File("../../pmcanalysis/results/pmc-drugs-processed").splitEachLine("\t") { line ->
//   def did = line[0]
//   def pheno = line[2]
//   def score = new Double(line[4])
//   def cooc = new Double(line[7])
//   def oc1 = new Double(line[5])
//   def oc2 = new Double(line[6])
//   if (cooc>MINCOOC && score > MINSCORE && oc1 > MINOC && oc2 > MINOC) {
//     println "MEDLINE$did\t$pheno"
//   }
// }
