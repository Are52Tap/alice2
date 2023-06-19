cd Required
java -classpath ../target/alice2-2.6.0-SNAPSHOT.jar:../target/lib/* -Dpython.home=jython -Dpython.path=jython/Lib/alice -Xmx1024m -Dfile.encoding=UTF-8 edu.cmu.cs.stage3.alice.authoringtool.JAlice
