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
//			FilesystemModel model = modelSupplier.get();
			Arbitrary<String> names = Arbitraries.strings()
					.alpha().ofMinLength(1).ofMaxLength(10)
					.injectDuplicates(0.8);
//					.filter(name -> noSuchRootFolderIn(name, model));
			return names.map(name -> {
				return Transformer.transform("add root folder " + name, fs -> {
					return fs.createRootFolder(name);
				});
			});
		};
		return Chain.startWith(FilesystemModel::empty)
				.withTransformation(addRootFolder)
				.improveShrinkingWith(ChangeDetector::forImmutables)
				.withMaxTransformations(10);

	}

	private static boolean noSuchRootFolderIn(String name, FilesystemModel model) {
		return model.allFolders().stream()
				.noneMatch(folder -> folder.equals("/" + name + "/"));
	}


}
