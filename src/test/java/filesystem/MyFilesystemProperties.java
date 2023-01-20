package filesystem;

import net.jqwik.api.*;
import net.jqwik.api.state.*;
import org.jetbrains.annotations.NotNull;

class MyFilesystemProperties {

	@Property(tries = 1000)
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
		return Chain.startWith(FilesystemModel::empty)
				.withTransformation(addRootFolder())
				.withTransformation(addNestedFolder())
				.withTransformation(addFile())
				.improveShrinkingWith(ChangeDetector::forImmutables)
				.withMaxTransformations(20);

	}

	@NotNull
	private static Transformation<FilesystemModel> addRootFolder() {
		return modelSupplier -> {
			Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
			return names.map(name ->
				Transformer.transform("add root folder " + name, fs -> {
					if (fs.allFolders().contains("/" + name + "/")) {
						// If there is already a root folder with that name then do nothing.
						// This has better shrinking behaviour than accessing the model and filtering out duplicates.
						return fs.doNothing();
					}
					return fs.createRootFolder(name);
				})
			);
		};
	}

	@NotNull
	private static Transformation<FilesystemModel> addNestedFolder() {
		return Transformation
				.when((FilesystemModel model) -> model.allFolders().size() > 0)
				.provide(model -> {
					Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
					Arbitrary<String> parents = Arbitraries.of(model.allFolders());
					return Combinators.combine(names, parents).as((name, parent) ->
							Transformer.transform("add folder " + name + " in " + parent, fs -> {
								if (fs.allFolders().contains(parent + name + "/")) {
									// If there is already a nested folder with that name then do nothing.
									// This has better shrinking behaviour than accessing the model and filtering out duplicates.
									return fs.doNothing();
								}
								return fs.createNestedFolder(name, parent);
							}));
				});
	}

	@NotNull
	private static Transformation<FilesystemModel> addFile() {
		return Transformation
				.when((FilesystemModel model) -> model.allFolders().size() > 0)
				.provide(model -> {
					Arbitrary<String> fileNames = Arbitraries.strings().alpha()
							.ofMinLength(1).ofMaxLength(10);
					Arbitrary<String> parents = Arbitraries.of(model.allFolders());
					return Combinators.combine(fileNames, parents).as((fileName, parent) ->
							Transformer.transform("add file " + fileName + " in " + parent, fs -> {
								if (fs.allFiles().contains(parent + fileName)) {
									// If there is already a file with that name in this folder then do nothing.
									// This has better shrinking behaviour than accessing the model and filtering out duplicates.
									return fs.doNothing();
								}
								return fs.createFile(fileName, parent);
							}));
				});
	}

}
