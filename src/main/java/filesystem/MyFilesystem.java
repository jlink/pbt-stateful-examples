package filesystem;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MyFilesystem {

	private Set<FilesystemResource> files = new HashSet<>();
	private Set<FilesystemResource> folders = new HashSet<>();

	public void createRootFolder(String name) {
		createFolder(name, null);
	}

	public void createFolder(String name, @Nullable String parentFolder) {
		FilesystemResource parent = findFolder(parentFolder);
		FilesystemResource newResource = new FilesystemResource(folderName(name), parent);
		if (folders.contains(newResource) || files.contains(newResource)) {
			String message = "Resource %s already exists".formatted(newResource);
			throw new IllegalArgumentException(message);
		}
		folders.add(newResource);
	}

	public void createFile(String name, String parentFolder) {
		if (parentFolder == null) {
			throw new IllegalArgumentException("File must have a parent folder");
		}

		FilesystemResource parent = findFolder(parentFolder);
		FilesystemResource newResource = new FilesystemResource(name, parent);

		// This is the bug since only file names and not full paths are checked for uniqueness:
		if (files.stream().anyMatch(file -> file.name().equals(name))) {
			String message = "File %s already exists".formatted(name);
			throw new IllegalArgumentException(message);
		}

		// This would be the correct check for existing files:
		// if (files.contains(newResource)) {
		// 	String message = "A file %s already exists".formatted(newResource);
		// 	throw new IllegalArgumentException(message);
		// }

		if (folders.contains(newResource)) {
			String message = "A folder %s already exists".formatted(newResource);
			throw new IllegalArgumentException(message);
		}
		files.add(newResource);
	}

	private FilesystemResource findFolder(String folder) {
		if (folder == null) {
			return null;
		}
		String folderName = folder.endsWith("/") ? folder : folderName(folder);
		return folders.stream()
				.filter(f -> f.fullName().equals(folderName))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Folder not found: " + folderName));
	}

	private String folderName(String folder) {
		return folder + "/";
	}

	public Set<String> listFolders() {
		return folders.stream().map(FilesystemResource::fullName).collect(Collectors.toSet());
	}

	public Set<String> listFiles() {
		return files.stream().map(FilesystemResource::fullName).collect(Collectors.toSet());
	}

	private record FilesystemResource(String name, @Nullable FilesystemResource parent) {
		public String fullName() {
			if (parent == null) {
				return "/" + name;
			}
			return parent.fullName() + name;
		}
	}
}
