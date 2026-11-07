package brova.toshiba.client.utils.neuro;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class NeuroRecordingFiles {

    public static void reset(String text) throws IOException {
        Path path = Paths.get(text);
        if (Files.exists(path)) {
            new File(text).delete();
        }
        Files.createFile(path);
    }

    public static boolean exists(String text) {
        return Files.exists(Paths.get(text));
    }

    private NeuroRecordingFiles() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
