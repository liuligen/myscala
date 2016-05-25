import AssemblyKeys._


assemblySettings

jarName in assembly := "stocktest.jar"


test in assembly := {}


mainClass in assembly := Some( "src.main.DownloadFileFromURL")


assemblyOption in packageDependency ~= { _.copy(appendContentHash = true) }
