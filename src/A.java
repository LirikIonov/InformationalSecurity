
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * CRC-32-IEEE 802.3 realisation
 *
 * x^26 + x^23 + x^22 + x^16 + x^12 + x^11 + x^10
 *  + x^8 + x^7 + x^5 + x^4 + x^2 + x + 1
 *
 * @source https://www.xilinx.com/support/documentation/application_notes/xapp209.pdf
 */
public class A {
    private int crc = 0xFFFFFFFF;
    private int poly = 0xEDB88320;

    private String crc32CustomRealisation(String filepath) throws IOException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(filepath));
        int symbol;
        while ((symbol = inputStream.read()) != -1) {
            update(symbol);
        }
        return Integer.toHexString(
                (int) (crc ^ 0xFFFFFFFFL)
        ).toUpperCase();
    }

    private void update(int symbol) {
        int temp = (crc ^ symbol) & 0xFF;
        for (int i = 0; i < 8; i++) {
            temp = ((temp & 1) == 1) ? (temp >>> 1) ^ poly : (temp >>> 1);
        }
        crc = (crc >>> 8) ^ temp;
    }

    void run() throws IOException  {
        System.out.println(crc32CustomRealisation("input.txt"));
    }

    public static void main(String[] args) throws IOException {
        new A().run();
    }
}