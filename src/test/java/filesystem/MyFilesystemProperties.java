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
			filesystemModel.operation().accept(fs);
		}
		// System.out.println(chain.transformations());
		// System.out.println("Folders: " + fs.listFolders());
		// System.out.println("Files:   " + fs.listFiles());
	}

	@Provide
	ChainArbitrary<FilesystemModel> filesystemTransformations() {
		Transformation<FilesystemModel> addRootFolder = fsSupplier -> {
			Arbitrary<String> names = Arbitraries.strings()
					.alpha().ofMinLength(1).ofMaxLength(10)
					.injectDuplicates(0.5);
			return names.map(name -> {
				return Transformer.transform("add root folder " + name, fs -> {
					return fs.createRootFolder(name);
				});
			});
		};
		return Chain.startWith(FilesystemModel::empty)
				.withTransformation(addRootFolder)
				.improveShrinkingWith(() -> ChangeDetector.forImmutables())
				.withMaxTransformations(10);

	}


}
