package filesystem;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public record FilesystemModel(Set<Resource> contents, @Nullable Consumer<MyFilesystem> operation) {


	public FilesystemModel(Set<Resource> contents, @Nullable Consumer<MyFilesystem> operation) {
		this.contents = contents;
		this.operation = operation;
	}

	public static FilesystemModel empty() {
		return new FilesystemModel(Set.of(), null);
	}

	public FilesystemModel createRootFolder(String name) {
		Set<Resource> newContents = new LinkedHashSet<>(this.contents);
		newContents.add(new Resource("/" + name, true));
		Consumer<MyFilesystem> nextOperation = fs -> fs.createRootFolder(name);
		return new FilesystemModel(newContents, nextOperation);
	}

	public FilesystemModel createNestedFolder(String name, String parent) {
		Set<Resource> newContents = new LinkedHashSet<>(this.contents);
		newContents.add(new Resource(parent + name, true));
		Consumer<MyFilesystem> nextOperation = fs -> fs.createFolder(name, parent);
		return new FilesystemModel(newContents, nextOperation);
	}

	public void operateAndCompare(MyFilesystem fs) {
		if (operation != null) {
			operation.accept(fs);
		}
		compareWithFilesystem(fs);
	}

	private void compareWithFilesystem(MyFilesystem filesystem) {
		assertThat(filesystem.listFolders()).containsExactlyInAnyOrderElementsOf(folders());
		assertThat(filesystem.listFiles()).containsExactlyInAnyOrderElementsOf(files());
	}

	private Set<String> files() {
		return contents.stream()
				.filter(resource -> !resource.isFolder())
				.map(Resource::toString)
				.collect(Collectors.toSet());
	}

	private Set<String> folders() {
		return contents.stream()
				.filter(Resource::isFolder)
				.map(Resource::toString)
				.collect(Collectors.toSet());
	}

	public Set<String> allFolders() {
		return contents.stream()
				.filter(Resource::isFolder)
				.map(Resource::toString)
				.collect(Collectors.toSet());
	}

	public Set<String> allFiles() {
		return contents.stream()
				.filter(resource -> !resource.isFolder())
				.map(Resource::toString)
				.collect(Collectors.toSet());
	}

	public FilesystemModel doNothing() {
		return new FilesystemModel(contents, null);
	}

	public FilesystemModel createFile(String name, String parent) {
		Set<Resource> newContents = new LinkedHashSet<>(this.contents);
		newContents.add(new Resource(parent + name, false));
		Consumer<MyFilesystem> nextOperation = fs -> fs.createFile(name, parent);
		return new FilesystemModel(newContents, nextOperation);
	}

	private record Resource(String name, boolean isFolder) {
		@Override
		public String toString() {
			return isFolder ? name + "/" : name;
		}
	}
}
