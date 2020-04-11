import static java.util.logging.Level.INFO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DuplicateFinder {
    private final static Logger LOGGER = Logger.getLogger(DuplicateFinder.class.getName());
    private static final int SAMPLE_SIZE = 4000;
    private static final String SHA_512 = "SHA-512";
    private static final int SAMPLES = 3;

    private Map<Integer, FileInfo> visitedSize = new HashMap<>();
    private Map<String, FileInfo> visitedContents = new HashMap<>();
    private Deque<Path> stack = new ArrayDeque<>();
    private List<FilePaths> duplicates = new ArrayList<>();

    public List<FilePaths> findDuplicateFiles(Path startingDirectory) {
        stack.push(startingDirectory);

        while (!stack.isEmpty()) {
            Path currentPath = stack.pop();
            File currentFile = new File(currentPath.toString());

            if (currentFile.isDirectory()) {
                for (File file : currentFile.listFiles()) {
                    stack.push(file.toPath());
                }
                continue;
            }
            examineFile(currentPath, currentFile);

        }
        return duplicates;
    }

    private void examineFile(Path currentPath, File currentFile) {
        int sizeHash = hashedSize(currentPath);
        FileInfo fileInfo = new FileInfo(currentFile.lastModified(), currentPath);

        if (visitedSize.containsKey(sizeHash)) {
            String fileHash = getHash(currentPath);
            if (visitedContents.containsKey(fileHash) && areFilesExactlyIdentical(visitedContents.get(fileHash).getPath(), currentPath)) {
                saveDuplicate(currentPath, fileHash, currentFile);
            } else {
                visitedContents.put(getHash(currentPath), fileInfo);
            }
        } else {
            visitedContents.put(getHash(currentPath), fileInfo);
        }
        visitedSize.put(sizeHash, fileInfo);
    }

    private void saveDuplicate(Path currentPath, String fileHash, File currentFile) {
        long currentModifiedTime = currentFile.lastModified();

        FileInfo fileInfo = visitedContents.get(fileHash);
        long modifiedTime = fileInfo.getModifiedTime();
        Path path = fileInfo.getPath();

        if (currentModifiedTime > modifiedTime) {
            duplicates.add(new FilePaths(currentPath, path));
        } else {
            duplicates.add(new FilePaths(path, currentPath));
            visitedContents.put(fileHash, new FileInfo(currentModifiedTime, currentPath));
        }
    }

    private String getHash(Path currentPath) {
        String fileHash;
        try {
            fileHash = hashedContents(currentPath);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        return fileHash;
    }

    private int hashedSize(Path path) {
        final Long totalBytes = new File(path.toString()).length();
        return totalBytes.hashCode();
    }

    private String hashedContents(Path path) throws IOException, NoSuchAlgorithmException {
        final long totalBytes = new File(path.toString()).length();

        try (InputStream inputStream = new FileInputStream(path.toString())) {
            MessageDigest digest = MessageDigest.getInstance(SHA_512);
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);

            if (totalBytes < SAMPLE_SIZE * SAMPLES) {
                byte[] bytes = new byte[(int) totalBytes];
                digestInputStream.read(bytes);
            } else {
                byte[] bytes = new byte[SAMPLE_SIZE * SAMPLES];
                long numBytesBetweenSamples = (totalBytes - SAMPLE_SIZE * SAMPLES) / 2;

                for (int n = 0; n < SAMPLES; n++) {
                    digestInputStream.read(bytes, n * SAMPLE_SIZE, SAMPLE_SIZE);
                    digestInputStream.skip(numBytesBetweenSamples);
                }
            }
            return new BigInteger(1, digest.digest()).toString(16);
        }
    }

    private boolean areFilesExactlyIdentical(Path originalPath, Path duplicatePath) {
        try {
            return areAllContentsEqual(originalPath, duplicatePath);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.log(INFO,
                    "Error whilst checking contents for '{0}' and '{1}' are exactly same. Error: {2}",
                    new Object[]{originalPath.toString(), duplicatePath.toString(), e.getMessage()});
            return false;
        }
    }

    private boolean areAllContentsEqual(Path originalPath, Path duplicatePath) throws IOException {
        BufferedReader ogReader = Files.newBufferedReader(originalPath);
        BufferedReader dupReader = Files.newBufferedReader(duplicatePath);

        String ogLine = ogReader.readLine();
        String dupLine = dupReader.readLine();

        boolean same = true;

        while (ogLine != null && dupLine != null) {
            if (!ogLine.equals(dupLine)) {
                same = false;
                break;
            }
            ogLine = ogReader.readLine();
            dupLine = dupReader.readLine();
        }

        ogReader.close();
        dupReader.close();
        return same;
    }
}
