package filesystem;

import net.jqwik.api.*;
import net.jqwik.api.state.*;

class MyFilesystemProperties {

	@Property(tries = 100)
	void checkFilesystemModelAgainstImplementation(
			@ForAll("filesystemTransformations") Chain<FilesystemModel> chain
	) {
		MyFilesystem fs = new MyFilesystem();
		for (FilesystemModel filesystemModel : chain) {
			filesystemModel.operateAndCompare(fs);
		}
		// System.out.println(chain.transformations());
		// System.out.println("Folders: " + fs.listFolders());
		// System.out.println("Files:   " + fs.listFiles());
	}

	@Provide
	ChainArbitrary<FilesystemModel> filesystemTransformations() {
		Transformation<FilesystemModel> addRootFolder = modelSupplier -> {
			Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
			return names.map(name -> {
				return Transformer.transform("add root folder " + name, fs -> {
					if (fs.allFolders().contains("/" + name + "/")) {
						// If there is already a root folder with that name then do nothing.
						// This has better shrinking behaviour than accessing the model and filtering out duplicates.
						return fs.doNothing();
					}
					return fs.createRootFolder(name);
				});
			});
		};
		return Chain.startWith(FilesystemModel::empty)
				.withTransformation(addRootFolder)
				.improveShrinkingWith(ChangeDetector::forImmutables)
				.withMaxTransformations(20);

	}

}
