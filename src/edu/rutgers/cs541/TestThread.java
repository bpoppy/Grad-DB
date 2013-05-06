package edu.rutgers.cs541;

public class TestThread implements Runnable{

	private final String schemaFile;

	public TestThread(String schemaFile) {
		this.schemaFile = schemaFile;
	}

	public void run() {
		while(InstanceTester.active && InstanceTester.minRows.get() > 1) {
			//System.out.println(EntryPoint.numColumns);
			new InstanceTester(this.schemaFile, EntryPoint.random.nextInt(EntryPoint.numColumns) + 1, Math.min(InstanceTester.minRows.get(), 1000));
		}
	}

}
