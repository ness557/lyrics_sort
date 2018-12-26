import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.*;
import static org.apache.commons.io.FilenameUtils.*;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.trim;

public class LrcSorter {

    public static void main(String[] args) {
        new LrcSorter().start(args[0], args[1]);
    }

    private void start(String pathToLrc, String pathToMusic) {

        File[] lrcFilesArray = new File(pathToLrc).listFiles();
        if(lrcFilesArray == null){
            System.out.println("No .lrc files found!");
            return;
        }

        // get all lrc files as list
        List<File> lrcFiles = Arrays.stream(lrcFilesArray)
                .filter(File::isFile)
                .filter(f -> getExtension(f.getName()).equals("lrc"))
                .collect(toList());

        File musicDirectory = new File(pathToMusic);

        Set<File> toDelete = checkFilesAndCopyLrcRecursively(lrcFiles, musicDirectory);

        toDelete.forEach(File::delete);
    }

    private Set<File> checkFilesAndCopyLrcRecursively(List<File> lrcFiles, File directory){

        Set<File> toDelete = new HashSet<>();

        File[] musicFiles = directory.listFiles();

        if(musicFiles != null) {
            Arrays.stream(musicFiles).filter(File::isFile).forEach(mf -> {
                lrcFiles.forEach(lf -> {

                    // get pure track name from lyrics
                    String lrcTrackName = lowerCase(trim(removeExtension(lf.getName()).split(" - ")[1])).replace(" ", "_");
                    // get pure track file name
                    String trackName = mf.getName().toLowerCase().replace(" ", "_");

                    // looking for coincidence
                    if (trackName.contains(lrcTrackName)) {

                        System.out.println("Found coincidence:");
                        System.out.println("\tMusic: " + mf.getAbsolutePath());
                        System.out.println("\tLyrics: " + lf.getAbsolutePath());

                        // get new lrc file path
                        String newLrcFile = directory.getAbsolutePath() + "\\" + removeExtension(mf.getName()) + ".lrc";
                        Path path = new File(newLrcFile).toPath();

                        try {
                            System.out.println("Copying...");
                            // copy lrc file
                            copy(lf.toPath(), path, REPLACE_EXISTING);
                            System.out.println("Copying finished!");
                            // add to delete set
                            toDelete.add(lf);
                        } catch (IOException e) {
                            System.out.println("Error with copying file: " + e.getMessage());
                        } finally {
                            System.out.println("\n");
                        }
                    }
                });
            });

            // doing the same recursively
            Arrays.stream(musicFiles)
                    .filter(File::isDirectory)
                    .forEach(dir -> toDelete.addAll(checkFilesAndCopyLrcRecursively(lrcFiles, dir)));
        }

        return toDelete;
    }
}
