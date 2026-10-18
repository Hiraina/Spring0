package mg.dto;

public class MapFile {
    private String fileName;
    private String extension;
    private byte[] bytesTable;

    public MapFile() {
    }

    public MapFile(String fileName, String extension, byte[] bytesTable) {
        this.fileName = fileName;
        this.extension = extension;
        this.bytesTable = bytesTable;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public byte[] getBytesTable() {
        return bytesTable;
    }

    public void setBytesTable(byte[] bytesTable) {
        this.bytesTable = bytesTable;
    }
}