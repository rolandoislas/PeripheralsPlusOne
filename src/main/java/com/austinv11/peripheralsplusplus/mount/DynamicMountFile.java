package com.austinv11.peripheralsplusplus.mount;

import dan200.computercraft.api.filesystem.IMount;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DynamicMountFile implements IMount {
    private final File filePath;

    public DynamicMountFile(File filePath) {
        this.filePath = filePath;
    }

    @Override
    public boolean exists(@Nonnull String path) throws IOException {
        return path.isEmpty();
    }

    @Override
    public boolean isDirectory(@Nonnull String path) throws IOException {
        return false;
    }

    @Override
    public void list(@Nonnull String path, @Nonnull List<String> contents) throws IOException {

    }

    @Override
    public long getSize(@Nonnull String path) throws IOException {
        return path.isEmpty() ? filePath.getTotalSpace() : 0;
    }

    @Nonnull
    @Override
    public InputStream openForRead(@Nonnull String path) throws IOException {
        if (path.isEmpty())
            return new FileInputStream(filePath);
        throw new IOException(path);
    }
}
