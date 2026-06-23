package dev.barrikeit.util;

import dev.barrikeit.exception.UnexpectedException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class FileUtil {

  private static final Logger log = Logger.getLogger(FileUtil.class.getName());

  private FileUtil() {
    throw new IllegalStateException("FileUtil class");
  }

  public static File tempFile(String fileName, String extension) {
    try {
      String sanitized = sanitizeFileName(fileName);
      String timestamp = TimeUtil.formatDateTimeDownload(TimeUtil.offsetDateTimeNow());
      File temp = File.createTempFile("temp_" + sanitized + "_" + timestamp + "_", extension);
      temp.deleteOnExit();
      return temp;
    } catch (IOException e) {
      throw new UnexpectedException(
          "Failed to create temp file for '%s': %s", fileName, e.getMessage());
    }
  }

  public static File copyFile(File source) {
    String extension = getFileExtension(source.getName());
    String name = source.getName().replace(extension, "");
    File copy = tempFile(name, extension);
    try {
      Files.copy(source.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log.warning("Failed to copy file " + source.getName() + ": " + e.getMessage());
    }
    return copy;
  }

  public static void deleteFile(File file) {
    try {
      Path path = file.toPath();
      if (Files.deleteIfExists(path)) {
        log.info("File deleted: " + path.toAbsolutePath());
      } else {
        log.warning("File not found for deletion: " + path.toAbsolutePath());
      }
    } catch (IOException e) {
      log.warning("Failed to delete file " + file.getAbsolutePath() + ": " + e.getMessage());
    }
  }

  public static File zip(List<File> files, List<String> fileNames, String zipName) {
    if (files == null || files.isEmpty()) {
      throw new UnexpectedException("File list for zip cannot be empty");
    }

    File zipFile = tempFile(zipName, ".zip");

    try (FileOutputStream fos = new FileOutputStream(zipFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ZipOutputStream zos = new ZipOutputStream(bos)) {

      zos.setComment("Created by barrikeit-core FileUtil");

      for (int i = 0; i < files.size(); i++) {
        File file = files.get(i);
        String entryName =
            (fileNames != null && fileNames.size() == files.size())
                ? fileNames.get(i)
                : file.getName();

        ZipEntry entry = new ZipEntry(entryName);
        entry.setTime(System.currentTimeMillis());
        zos.putNextEntry(entry);

        try (FileInputStream fis = new FileInputStream(file)) {
          transfer(fis, zos);
        }
        zos.closeEntry();
      }

    } catch (IOException e) {
      log.warning("Failed to create zip '" + zipName + "': " + e.getMessage());
      throw new UnexpectedException("Failed to create zip file '%s': %s", zipName, e.getMessage());
    }

    return zipFile;
  }

  public static File unzip(InputStream zipInput, String originalName) {
    if (!originalName.endsWith(".zip")) {
      throw new UnexpectedException("Only .zip files are supported, got: %s", originalName);
    }

    try (BufferedInputStream bis = new BufferedInputStream(zipInput);
        ZipInputStream zis = new ZipInputStream(bis)) {

      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (!entry.isDirectory()) {
          String entryName = entry.getName();
          String ext = getFileExtension(entryName);
          String name = entryName.replace(ext, "");

          File dest = tempFile(name, ext);
          try (FileOutputStream fos = new FileOutputStream(dest)) {
            transfer(zis, fos);
          }
          return dest;
        }
        zis.closeEntry();
      }

    } catch (IOException e) {
      throw new UnexpectedException("Failed to unzip file '%s': %s", originalName, e.getMessage());
    }

    throw new UnexpectedException("ZIP file '%s' contains no valid file entries", originalName);
  }

  public static String getFileExtension(String fileName) {
    int index = fileName.lastIndexOf('.');
    return index > 0 ? fileName.substring(index) : "";
  }

  public static String sanitizeFileName(String fileName) {
    return Normalizer.normalize(fileName.trim(), Normalizer.Form.NFD)
        .replace(" ", "_")
        .replace(",", "")
        .replace("ñ", "ny")
        .replace("Ñ", "Ny")
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
        .replaceAll("[^\\p{ASCII}]", "");
  }

  private static void transfer(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[8192];
    int read;
    while ((read = in.read(buffer)) != -1) {
      out.write(buffer, 0, read);
    }
  }
}
