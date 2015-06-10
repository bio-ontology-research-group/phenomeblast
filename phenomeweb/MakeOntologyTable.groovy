new File("ontologies/").eachFile { file ->
  def id = ""
  file.eachLine { line ->
    if (line.startsWith("id: ")) {
      id = line.substring(4).trim()
    }
    if (line.startsWith("name: ")) {
      def name = line.substring(5)
      println "$id\t$name"
    }
  }
}