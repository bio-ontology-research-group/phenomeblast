new File("MGI_Geno_Disease.rpt").splitEachLine("\t") { line ->
  line[2].split("\\|").each { mgi ->
    line[-1].split(",").each { 
      def omim = "OMIM:"+it
      println "$omim\t$mgi"
    }
  }
}