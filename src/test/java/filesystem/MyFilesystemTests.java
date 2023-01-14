package filesystem;

import jdk.jfr.Enabled;
import net.jqwik.api.Example;

class MyFilesystemTests {

	@Example
	void test() {
		var fs = new MyFilesystem();

		fs.createRootFolder("A");
		fs.createRootFolder("B");

		fs.createFolder("C", "A");

		fs.createFile("a.txt", "A");
		fs.createFile("b.txt", "A");
		fs.createFile("c.txt", "C");

		// Triggers bug:
		// fs.createFile("b.txt", "B");

		System.out.println(fs.listFolders());
		System.out.println(fs.listFiles());
	}
}
