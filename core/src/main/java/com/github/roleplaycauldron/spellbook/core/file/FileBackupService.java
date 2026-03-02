package com.github.roleplaycauldron.spellbook.core.file;

import com.github.roleplaycauldron.spellbook.core.logger.WrappedLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service class responsible for managing file backups, including adding files
 * for backup, initiating the backup process, and managing backup retention.
 *
 * <p>
 * This class provides functionality to create backups of specified files by
 * compressing them into a ZIP archive and saving them in a dedicated backup directory.
 * Additional features include managing backup retention by automatically pruning
 * older backup files based on a configured limit.
 *
 * <p>
 * The operation of this service depends on the following configurations:
 * - A valid backup directory, where all backup archives are stored.
 * - Maximum backup count configuration to enforce the backup retention policy.
 * - Proper permissions to read/write files in the configured directories.
 */
public class FileBackupService {

    /**
     * The {@link WrappedLogger} instance.
     */
    private final WrappedLogger log;

    /**
     * The maximum number of backups.
     */
    private final int maxBackups;

    /**
     * The backup directory.
     */
    private final File backupDirectory;

    /**
     * The list of files to back up.
     */
    private final List<File> backupFiles;

    /**
     * Creates a new {@link FileBackupService} instance.
     *
     * @param log             the {@link WrappedLogger} instance.
     * @param pluginDirectory the {@link File} for the plugin directory.
     * @param backupDirectoryName the {@link String} name for the backup directory.
     * @param maxBackups      the maximum number of backups. If set to 0 or lower, no backups will be deleted.
     */
    public FileBackupService(final WrappedLogger log, final File pluginDirectory, final String backupDirectoryName, final int maxBackups) {
        this.log = log;
        this.maxBackups = maxBackups;
        this.backupDirectory = new File(pluginDirectory, backupDirectoryName);
        this.backupFiles = new ArrayList<>();

        if (backupDirectory.mkdirs()) {
            log.infoF("Created Backup Directory");
        }
    }

    /**
     * Adds a file to the backup process by copying it to the designated backup directory.
     * If the file does not exist, the method exits without performing any action.
     *
     * @param fileToBackup the file to be added to the backup. This file is copied to the backup directory.
     * @throws FileException if an issue occurs while copying the file to the backup directory.
     */
    public void addFileToBackup(final File fileToBackup) throws FileException {
        if (!fileToBackup.exists()) {
            return;
        }
        final File backupFile = new File(backupDirectory, fileToBackup.getName());
        try {
            Files.copy(fileToBackup.toPath(), new File(backupDirectory, fileToBackup.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            throw new FileException("Issue while copying file to backup directory.", e);
        }
        backupFiles.add(backupFile);
    }

    /**
     * Initiates the backup process by generating a backup file name based on the current date and time,
     * formatted as "yyyy-MM-dd_HH-mm-ss", and delegating the backup creation to the overloaded
     * {@link #startBackup(String backupFileName)} method.
     * <p>
     * The generated backup file will have a ".zip" extension and will include the current timestamp
     * in its name to prevent naming conflicts and ensure uniqueness.
     * <p>
     * Exception Handling:
     * - A {@link FileException} is thrown if an error occurs during the backup creation process.
     * <p>
     * Prerequisites:
     * - The `backupDirectory` must be set as the location for saving the generated backup file.
     * - The list of files to back up must already be populated, as managed by the {@link #addFileToBackup(File)} method.
     * <p>
     * @throws FileException: If an issue occurs during the backup process, such as problems with file I/O.
     */
    public void startBackup() throws FileException {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        final String currentDate = LocalDateTime.now().format(formatter);

        startBackup(String.format("%s_Backup.zip", currentDate));
    }

    /**
     * Initiates the backup process, creating a compressed archive of the specified files and managing backup retention.
     * <p>
     * This method performs the following steps:
     * 1. Checks if there are any files to back up. If none, the method exits without performing any action.
     * 2. Constructs the backup file name using the current date and time, formatted as "yyyy-MM-dd HH-mm-ss Backup.zip".
     * 3. Creates a ZIP archive of the files specified in the backup list.
     *    - If an I/O error occurs during this step, a {@link FileException} is thrown with the underlying cause.
     * 4. Logs a message upon successful creation of the backup file.
     * 5. Deletes files from the backup list after completion of the backup process.
     *    - If a file cannot be deleted, an error message is logged.
     * 6. Calls the {@link #manageBackupRetention()} method to manage old backups based on the configured retention policy.
     * <p>
     * Logging:
     * - Logs informational messages at the start and completion of the backup process.
     * - Logs errors if file deletions fail or an exception occurs during the backup creation.
     * <p>
     * Prerequisites:
     * - The `backupDirectory` must be configured as the location where backups will be stored.
     * - The `backupFiles` list must contain the files to be included in the backup archive.
     * - Proper permissions must be in place to read the files in `backupFiles` and write to the `backupDirectory`.
     * <p>
     * @param backupFileName the name of the backup file to be created. It must be a valid file name and cannot be null or empty.
     * @throws FileException if an I/O issue occurs during the creation of the backup file.
     */
    public void startBackup(String backupFileName) throws FileException {
        if (backupFiles.isEmpty()) {
            return;
        }
        log.infoF("Starting Backup Process...");

        if (!backupFileName.contains(".zip")) {
            backupFileName = String.format("%s.zip", backupFileName);
        }

        final File backupFile = new File(backupDirectory, backupFileName);

        try {
            zipFiles(backupFile);
            log.infoF("Successfully created Backup: " + backupFile.getAbsolutePath());
        } catch (final IOException e) {
            throw new FileException("An error occurred while creating the backup.", e);
        }

        backupFiles.forEach(oldFiles -> {
            if(!oldFiles.delete()) {
                log.errorF("An error occurred while deleting the backup file: " + oldFiles.getAbsolutePath());
            }
        });

        log.infoF("Backup process completed. Starting backup retention management...");
        manageBackupRetention();
    }

    /**
     * Manages the retention of backup files by ensuring the number of stored backups does not exceed a specified limit.
     * <p>
     * This method evaluates the number of existing backup files in the configured backup directory. If the number
     * of backups exceeds the configured maximum limit, the oldest backups are deleted to comply with the retention policy.
     * <p>
     * Backup retention is skipped in the following scenarios:
     * - The maximum number of backups is set to zero or fewer, effectively disabling retention management.
     * - The number of existing backups does not exceed the maximum permissible limit.
     * <p>
     * The process sorts the backup files by their last modified timestamp in ascending order, ensuring the oldest
     * files are removed first. If file deletion fails for any reason, an error message is logged.
     * <p>
     * Expected backup files must have the suffix "Backup.zip". Files not matching this criterion are ignored during
     * retention management.
     * <p>
     * Logging:
     * - Logs informational messages during the retention process, including when backups are skipped or deleted.
     * - Logs errors if the deletion of a backup file fails.
     * <p>
     * Prerequisites:
     * - The `backupDirectory` must be correctly set to the directory containing the backup files.
     * - The `maxBackups` must be configured to define the retention policy.
     */
    public void manageBackupRetention() {
        if (!(maxBackups <= 0)) {
            log.infoF("Either backups are disabled or the maximum number of backups is set to 0. Skipping backup retention management.");
            return;
        }

        final File[] backupFiles = backupDirectory.listFiles((dir, name) -> name.endsWith("Backup.zip"));
        if (backupFiles == null || backupFiles.length <= maxBackups) {
            return;
        }

        Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified));

        for (int i = 0; i < backupFiles.length - maxBackups; i++) {
            if (backupFiles[i].delete()) {
                log.infoF("Deleted backup: " + backupFiles[i].getName());
            } else {
                log.errorF("An error occurred during the deletion of the Backup: " + backupFiles[i].getName());
            }
        }
        log.infoF("Backup retention management completed.");
    }

    private void zipFiles(final File outputZipFile) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(outputZipFile.toPath()))) {
            for (final File file : backupFiles) {
                addToZip(file, zipOutputStream, "");
            }
        }
    }

    private void addToZip(final File file, final ZipOutputStream zipOutputStream, final String parentPath) throws IOException {
        final String zipEntryName = parentPath + file.getName();
        if (file.isDirectory()) {
            zipOutputStream.putNextEntry(new ZipEntry(zipEntryName + "/"));
            zipOutputStream.closeEntry();

            final File[] children = file.listFiles();
            if (children != null) {
                for (final File child : children) {
                    addToZip(child, zipOutputStream, zipEntryName + "/");
                }
            }
        } else {
            zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
            Files.copy(file.toPath(), zipOutputStream);
            zipOutputStream.closeEntry();
        }
    }
}
