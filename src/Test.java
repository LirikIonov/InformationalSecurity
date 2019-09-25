import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

public class Test {
    public static long checksumBufferedInputStream(String filepath) throws IOException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(filepath));
        CRC32 crc = new CRC32();
        int cnt;
        while ((cnt = inputStream.read()) != -1) {
            crc.update(cnt);
        }
        return crc.getValue();
    }

    public static void main(String[] args) throws IOException {
        System.out.println(checksumBufferedInputStream("input.txt"));
    }
}