import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

/**
 * Packages Play native debug symbols for libraries that ship already stripped.
 *
 * <p>AGP only emits a {@code .so.sym} when {@code llvm-strip} changes the file. AndroidX CameraX,
 * DataStore, and Compose graphics libraries have a dynamic symbol table and no {@code .symtab}, so
 * stripping is a no-op and the App Bundle would otherwise contain native code with no
 * {@code BUNDLE-METADATA/com.android.tools.build.debugsymbols} entry.
 */
public abstract class PackagePlayNativeSymbolTablesTask extends DefaultTask {

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getMergedNativeLibs();

    @OutputDirectory
    public abstract DirectoryProperty getSymbolTables();

    @TaskAction
    public void packageSymbols() throws IOException {
        File outRoot = getSymbolTables().get().getAsFile();
        deleteRecursively(outRoot);
        if (!outRoot.mkdirs() && !outRoot.isDirectory()) {
            throw new IOException("Cannot create " + outRoot);
        }
        collect(getMergedNativeLibs().get().getAsFile(), outRoot);
    }

    private static void collect(File dir, File outRoot) throws IOException {
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                collect(child, outRoot);
                continue;
            }
            if (!child.getName().endsWith(".so") || elfHasSymtab(child)) {
                continue;
            }
            File dest = new File(outRoot, child.getParentFile().getName() + "/" + child.getName() + ".sym");
            if (!dest.getParentFile().isDirectory() && !dest.getParentFile().mkdirs()) {
                throw new IOException("Cannot create " + dest.getParentFile());
            }
            Files.copy(child.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** True when the ELF still has a full symbol table that AGP can extract itself. */
    static boolean elfHasSymtab(File file) throws IOException {
        byte[] data = Files.readAllBytes(file.toPath());
        if (data.length < 64 || data[0] != 0x7f || data[1] != 'E') {
            return false;
        }
        boolean elf64 = data[4] == 2;
        boolean little = data[5] == 1;
        long sectionOffset = elf64 ? u64(data, 40, little) : u32(data, 32, little);
        int sectionSize = elf64 ? u16(data, 58, little) : u16(data, 46, little);
        int sectionCount = elf64 ? u16(data, 60, little) : u16(data, 48, little);
        for (int index = 0; index < sectionCount; index++) {
            int header = (int) (sectionOffset + (long) index * sectionSize);
            if (header + 8 > data.length) {
                return false;
            }
            if (u32(data, header + 4, little) == 2L) {
                return true;
            }
        }
        return false;
    }

    private static int u16(byte[] data, int at, boolean little) {
        int b0 = data[at] & 0xff;
        int b1 = data[at + 1] & 0xff;
        return little ? (b0 | (b1 << 8)) : ((b0 << 8) | b1);
    }

    private static long u32(byte[] data, int at, boolean little) {
        long value = 0L;
        for (int i = 0; i < 4; i++) {
            int shift = little ? i * 8 : (3 - i) * 8;
            value |= (data[at + i] & 0xffL) << shift;
        }
        return value;
    }

    private static long u64(byte[] data, int at, boolean little) {
        long value = 0L;
        for (int i = 0; i < 8; i++) {
            int shift = little ? i * 8 : (7 - i) * 8;
            value |= (data[at + i] & 0xffL) << shift;
        }
        return value;
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
