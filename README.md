
Using the following command to install the new JAR (run in this directory):
$mvn install:install-file -Dfile=<JAR_file> -DpomFile=<pom_file> -DlocalRepositoryPath=.  -DcreateChecksum=true

Then push to maven-export branch.
