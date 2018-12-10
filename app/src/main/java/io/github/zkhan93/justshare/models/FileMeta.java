package io.github.zkhan93.justshare.models;

public class FileMeta {
    public String name;
    public long length;
    public String uri;
    public String mimeType;

    @Override
    public String toString() {
        return "FileMeta{" +
                "name='" + name + '\'' +
                ", length=" + length +
                ", uri='" + uri + '\'' +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }
}
