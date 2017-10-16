package com.austinv11.peripheralsplusplus.mount;

import dan200.computercraft.api.filesystem.IMount;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamicMountExtra implements IMount {
    private final Map<String, File> files;

    DynamicMountExtra(Map<String, File> files) {
        this.files = files;
    }

    private File getFile(String path) throws IOException {
        if (files.containsKey(path))
            return files.get(path);
        throw new IOException(path);
    }

    @Override
    public boolean exists(@Nonnull String path) throws IOException {
        if (path.equals(""))
            return true;
        try {
            getFile(path);
            return true;
        }
        catch (IOException e) {
            List<String> contents = new ArrayList<>();
            String[] split = path.split("/");
            String previousPath = path.replace("/" + split[split.length - 1], "");
            if (previousPath.equals(path))
                previousPath = "";
            list(previousPath, contents);
            for (String item : contents)
                if (path.endsWith(item))
                    return true;
        }
        return false;
    }

    @Override
    public boolean isDirectory(@Nonnull String path) throws IOException {
        File file;
        try {
            file = getFile(path);
        }
        catch (IOException e) {
            return true;
        }
        return path.equals("") || file.isDirectory();
    }

    @Override
    public void list(@Nonnull String path, @Nonnull List<String> contents) throws IOException {
        for (Map.Entry<String, File> file : files.entrySet()) {
            if (file.getKey().startsWith(path)) {
                String name = file.getKey().replaceFirst(path, "");
                String[] nameSplit = name.split("/");
                // File
                if (nameSplit.length > 1 && nameSplit[0].equals("")) {
                    if (!nameSplit[1].equals("."))
                        contents.add(nameSplit[1]);
                }
                // Directory
                else
                    contents.add(nameSplit[0]);
            }
        }
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
