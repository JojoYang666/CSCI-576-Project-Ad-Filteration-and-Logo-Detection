all:
	javac *.java
	
MyPlayer:
	javac MyPlayer.java

MyProgram:
	javac MyProgram.java

run: MyProgram
	java MyProgram ../../dataset/Videos/data_test1.rgb ../../dataset/Videos/data_test1.wav output.rgb output.wav
    
search: MyProgram
	date
	java MyProgram ../../dataset/Videos/data_test1.rgb ../../dataset/Videos/data_test1.wav output.rgb output.wav a
	date
    
video: run
	java MyPlayer output.rgb ../../dataset/Videos/data_test1.wav
	
clean:
	rm *.class