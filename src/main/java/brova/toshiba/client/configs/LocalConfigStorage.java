package brova.toshiba.client.configs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Локальное хранилище конфигов клиента. Лежит рядом с игрой, в папке toshiba/configs,
 * и никак не пересекается с облачными конфигами (CloudConfigSync).
 */
public final class LocalConfigStorage {

    private static final Pattern VALID_NAME = Pattern.compile("[\\p{L}\\p{N}_-]{1,64}");

    private static final String EXTENSION = ".json";

    private static final String LAST_POINTER = "last.txt";

    private final Path directory;

    public LocalConfigStorage() {
        this.directory = FabricLoader.getInstance().getGameDir().resolve("toshiba").resolve("configs");
    }

    public static boolean isValidName(String name) {
        return name != null && VALID_NAME.matcher(name).matches();
    }

    public Path getDirectory() {
        return this.directory;
    }

    public boolean exists(String name) {
        return isValidName(name) && Files.isRegularFile(this.resolve(name));
    }

    /** Сохраняет конфиг под именем. Возвращает false, если запись не удалась. */
    public boolean save(String name, String content) {
        if (!isValidName(name) || content == null) {
            return false;
        }
        try {
            Files.createDirectories(this.directory);
            Path target = this.resolve(name);
            Path temporary = this.directory.resolve(name + ".tmp");
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            this.writeLastPointer(name);
            return true;
        } catch (IOException failure) {
            System.err.println("[LocalConfigStorage] " + failure.getMessage());
            return false;
        }
    }

    /** Имя последнего сохранённого конфига или null. */
    public String getLastSavedName() {
        try {
            Path pointer = this.directory.resolve(LAST_POINTER);
            if (!Files.isRegularFile(pointer)) {
                return null;
            }
            String name = Files.readString(pointer, StandardCharsets.UTF_8).trim();
            return isValidName(name) ? name : null;
        } catch (IOException failure) {
            System.err.println("[LocalConfigStorage] " + failure.getMessage());
            return null;
        }
    }

    /** Содержимое последнего сохранённого конфига или null. */
    public String loadLastSaved() {
        String name = this.getLastSavedName();
        if (name == null) {
            return null;
        }
        String content = this.read(name);
        if (content == null) {
            this.clearLastPointer();
        }
        return content;
    }

    private void writeLastPointer(String name) {
        try {
            Files.createDirectories(this.directory);
            Files.writeString(this.directory.resolve(LAST_POINTER), name, StandardCharsets.UTF_8);
        } catch (IOException failure) {
            System.err.println("[LocalConfigStorage] " + failure.getMessage());
        }
    }

    private void clearLastPointer() {
        try {
            Files.deleteIfExists(this.directory.resolve(LAST_POINTER));
        } catch (IOException failure) {
            System.err.println("[LocalConfigStorage] " + failure.getMessage());
        }
    }

    /** Читает конфиг по имени или null, если его нет. */
    public String read(String name) {
        if (!isValidName(name)) {
            return null;
        }
        try {
            Path target = this.resolve(name);
            if (!Files.isRegularFile(target)) {
                return null;
            }
            return Files.readString(target, StandardCharsets.UTF_8);
        } catch (IOException failure) {
            System.err.println("[LocalConfigStorage] " + failure.getMessage());
            return null;
        }
    }

    public boolean delete(String name) {
        if (!isValidName(name)) {
            return false;
        }
        try {
            boolean deleted = Files.deleteIfExists(this.resolve(name));
            if (deleted && name.equals(this.getLastSavedName())) {
                this.clearLastPointer();
            }
            return deleted;
        } catch (IOException failure) {
            System.err.println("[LocalConfigStorage] " + failure.getMessage());
            return false;
        }
    }

    public List<String> list() {
        List<String> names = new ArrayList<>();
        if (!Files.isDirectory(this.directory)) {
            return names;
        }
        try (Stream<Path> stream = Files.list(this.directory)) {
            stream.filter(Files::isRegularFile).map(path -> path.getFileName().toString()).filter(name -> name.endsWith(EXTENSION))
                    .map(name -> name.substring(0, name.length() - EXTENSION.length())).sorted().forEach(names::add);
        } catch (IOException failure) {
            System.err.println("[LocalConfigStorage] " + failure.getMessage());
        }
        return names;
    }

    private Path resolve(String name) {
        return this.directory.resolve(name + EXTENSION);
    }
}
