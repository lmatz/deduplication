JAVAC= javac
sources =  *.java
#SHA.java Save.java Reference.java RemoteSave.java Stream.java Content.java CHandler.java CIterator.java ManageOneFile.java LocalSave.java ManageAllChunks.java MyDedup.java ManageAllFiles.java
classes = $(sources:.java=.class)

ifndef classpath
	export classpath=./AzureJar
endif

all: subblock $(classes) 

subblock:
	mkdir mydedup

%.class: %.java
	$(JAVAC) -cp .:$(classpath)/* $(sources) -d mydedup

clean :
	rm -f *.class
