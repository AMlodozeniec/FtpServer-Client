JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	FtpServ.java \
	FtpCli.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
