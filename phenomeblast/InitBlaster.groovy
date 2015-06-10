
def infile = args[0] // contains phenotype list
def outfile = args[1] // contains phenotype list

def groovy = "groovy/bin/groovy" // /path/to/groovy
def cb = "cb/cb" // /path/to/cb

def tempFile = File.createTempFile("p-blaster-", ".owl" )
def tempFileCB = File.createTempFile("cb-blaster-", ".owl" )
def tempFileCEL = File.createTempFile("cel-blaster-", ".owl" )
def runFile = new File("tmp/phenome-BLASTer.sh")
runFile.delete()
runFile = new File("tmp/phenome-BLASTer.sh")

def fout = new PrintWriter(new BufferedWriter(new FileWriter(runFile)))

println "Temporary OWL file name: $tempFile"
println "Temporary OWL file name (for CB Reasoner): $tempFileCB"
println "Temporary OWL file name (for CEL Reasoner): $tempFileCEL"
println "Run file name: $runFile"
println ""
println "Input file name: $infile"
println "Output file name: $outfile"
println ""
println ""

fout.println "#!/bin/bash"
fout.println "echo \"Generating ontology file\""
fout.println "$groovy MakePhenomeBlaster -i $infile -o $tempFile"
fout.println "echo \"Converting to OWL Functional Syntax\""
fout.println "$groovy ToFunctionalSyntax $tempFile $tempFileCB"
fout.println "echo \"Classifying\""
fout.println "$cb -c -o $tempFileCEL $tempFileCB"
fout.println "echo \"Aligning\""
fout.println "$groovy GenerateForPhenomeBLAST $tempFileCEL $infile $outfile"

fout.close()