package edu.rutgers.cs541;

public class TestThread implements Runnable{

	private final String schemaFile;
	
	public TestThread(String schemaFile) {
		this.schemaFile = schemaFile;
	}
	
	public void run() {
		while(true) {
			new InstanceTester(schemaFile, EntryPoint.random.nextInt(EntryPoint.numColumns) + 1);
		}
	}

}
