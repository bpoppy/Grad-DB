package edu.rutgers.cs541;

public class TestThread implements Runnable{

	private final String schemaFile;
	private final int threadNum;

	public TestThread(String schemaFile, int threadNum) {
		this.schemaFile = schemaFile;
		this.threadNum = threadNum;
	}

	public void run() {
		while(InstanceTester.active && InstanceTester.minRows.get() > 1) {
			//System.out.println(EntryPoint.numColumns);
			if (threadNum != 0) {
				new InstanceTester(this.schemaFile, EntryPoint.random.nextInt(EntryPoint.numColumns) + 1, Math.min(InstanceTester.minRows.get(), 1000));
			} else {
				new InstanceTester(
					this.schemaFile,
					2,
					QueryProcessor.numTables
				);
			}
		}
	}

}
