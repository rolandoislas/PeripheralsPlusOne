package com.austinv11.peripheralsplusplus.mount;

import dan200.computercraft.api.filesystem.IWritableMount;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class DynamicMountWritable implements IWritableMount {
    private final File directory;
    private static final long MAX_SIZE = 1024 * 100;

    DynamicMountWritable(File directory) {
        this.directory = directory;
    }

    private File getFile(String path) {
        return new File(directory, path);
    }

    @Override
    public void makeDirectory(@Nonnull String path) throws IOException {
        File file = getFile(path);
        file.mkdir();
    }

    @Override
    public void delete(@Nonnull String path) throws IOException {
        File file = getFile(path);
        file.delete();
    }

    @Nonnull
    @Override
    public OutputStream openForWrite(@Nonnull String path) throws IOException {
        return new FileOutputStream(getFile(path));
    }

    @Nonnull
    @Override
    public OutputStream openForAppend(@Nonnull String path) throws IOException {
        return new FileOutputStream(getFile(path), true);
    }

    @Override
    public long getRemainingSpace() throws IOException {
        return MAX_SIZE - FileUtils.sizeOfDirectory(directory);
    }

    @Override
    public boolean exists(@Nonnull String path) throws IOException {
        return getFile(path).exists();
    }

    @Override
    public boolean isDirectory(@Nonnull String path) throws IOException {
        return getFile(path).isDirectory();
    }

    @Override
    public void list(@Nonnull String path, @Nonnull List<String> contents) throws IOException {
        File startPath = getFile(path);
        if (startPath.isFile())
            throw new IOException(path);
        String[] list = startPath.list();
        if (list != null)
            contents.addAll(Arrays.asList(list));
    }

    @Override
    public long getSize(@Nonnull String path) throws IOException {
        return getFile(path).getTotalSpace();
    }

    @Nonnull
    @Override
    public InputStream openForRead(@Nonnull String path) throws IOException {
        return new FileInputStream(getFile(path));
    }
}
