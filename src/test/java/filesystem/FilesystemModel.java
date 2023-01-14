package filesystem;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public record FilesystemModel(Set<Resource> contents, @Nullable Consumer<MyFilesystem> operation) {


	public FilesystemModel(Set<Resource> contents, @Nullable Consumer<MyFilesystem> operation) {
		this.contents = contents;
		if (operation == null) {
			this.operation = fs -> {};
		} else {
			this.operation = operation;
		}
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

	private record Resource(String name, boolean isFolder) {
		@Override
		public String toString() {
			return isFolder ? name + "/" : name;
		}
	}
}
