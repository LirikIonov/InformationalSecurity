
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;


/**
 * CRC-32-IEEE 802.3 realisation
 * <p>
 * x^26 + x^23 + x^22 + x^16 + x^12 + x^11 + x^10
 * + x^8 + x^7 + x^5 + x^4 + x^2 + x + 1
 *
 * @source https://www.xilinx.com/support/documentation/application_notes/xapp209.pdf
 */
public class A {
    private BigInteger message = BigInteger.ZERO;
    private BigInteger poly = new BigInteger(
            new BigInteger(String.valueOf(0x04C11DB7), 16).toString(2)
    );

    private String crc32CustomRealisation(String filepath) throws IOException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(filepath));
        int symbol;
        while ((symbol = inputStream.read()) != -1) {
            String bytes = Integer.toBinaryString(symbol);
            BigInteger number = new BigInteger(bytes);
            message = append(number);
        }

        String str1 = message.toString(2);
        String str2 = poly.toString(16);

        String div = message.divide(poly).toString(10);
        String mod = message.mod(poly).toString(10);
        Polynomial divident = createPolynomial(message);
        Polynomial divider = createPolynomial(poly);
        Polynomial remainder = divident.divides(divider)[1];

        return new BigInteger(String.valueOf(remainder.toString2()), 2).toString(16);
    }

    BigInteger append(BigInteger number) {
        int ndigits = number.bitLength() * 3 / 10;
        BigInteger pow10 = BigInteger.TEN.pow(ndigits);
        while (pow10.compareTo(number) > 0) {
            pow10 = pow10.divide(BigInteger.TEN);
        }
        while (pow10.compareTo(number) <= 0) {
            pow10 = pow10.multiply(BigInteger.TEN);
        }
        return message.multiply(pow10).add(number);
    }

    public class Polynomial {
        private int[] coef;
        private int deg;

        public Polynomial(int a, int b) {
            coef = new int[b + 1];
            coef[b] = a;
            deg = degree();
        }


        public Polynomial(Polynomial p) {
            coef = new int[p.coef.length];
            for (int i = 0; i < p.coef.length; i++) {
                coef[i] = p.coef[i];
            }
            deg = p.degree();
        }

        public int degree() {
            int d = 0;
            for (int i = 0; i < coef.length; i++)
                if (coef[i] != 0) d = i;
            return d;
        }

        public Polynomial plus(Polynomial b) {
            Polynomial a = this;
            Polynomial c = new Polynomial(0, Math.max(a.deg, b.deg));
            for (int i = 0; i <= a.deg; i++) c.coef[i] += a.coef[i];
            for (int i = 0; i <= b.deg; i++) c.coef[i] += b.coef[i];
            c.deg = c.degree();
            return c;
        }


        public Polynomial minus(Polynomial b) {
            Polynomial a = this;
            Polynomial c = new Polynomial(0, Math.max(a.deg, b.deg));
            for (int i = 0; i <= a.deg; i++) c.coef[i] += a.coef[i];
            for (int i = 0; i <= b.deg; i++) c.coef[i] -= b.coef[i];
            c.deg = c.degree();
            return c;
        }


        public Polynomial times(Polynomial b) {
            Polynomial a = this;
            Polynomial c = new Polynomial(0, a.deg + b.deg);
            for (int i = 0; i <= a.deg; i++)
                for (int j = 0; j <= b.deg; j++)
                    c.coef[i + j] += (a.coef[i] * b.coef[j]);
            c.deg = c.degree();
            return c;
        }

        public int coeff() {
            return coeff(degree());
        }

        public int coeff(int degree) {
            if (degree > this.degree()) throw new RuntimeException("bad degree");
            return coef[degree];
        }


        public Polynomial[] divides(Polynomial b) {
            Polynomial q = new Polynomial(0, 0);
            Polynomial r = new Polynomial(this);
            while (!r.isZero() && r.degree() >= b.degree()) {
                int coef = r.coeff() / b.coeff();
                int deg = r.degree() - b.degree();
                Polynomial t = new Polynomial(coef, deg);
                q = q.plus(t);
                r = r.minus(t.times(b));
            }
            System.out.printf("(%s) / (%s): %s, %s \n", this, b, q, r);
            return new Polynomial[]{q, r};
        }

        public Polynomial compose(Polynomial b) {
            Polynomial a = this;
            Polynomial c = new Polynomial(0, 0);
            for (int i = a.deg; i >= 0; i--) {
                Polynomial term = new Polynomial(a.coef[i], 0);
                c = term.plus(b.times(c));
            }
            return c;
        }

        public boolean eq(Polynomial b) {
            Polynomial a = this;
            if (a.deg != b.deg) return false;
            for (int i = a.deg; i >= 0; i--)
                if (a.coef[i] != b.coef[i]) return false;
            return true;
        }


        public boolean isZero() {
            for (int i : coef) {
                if (i != 0) return false;
            }//end for
            return true;
        }


        public int evaluate(int x) {
            int p = 0;
            for (int i = deg; i >= 0; i--)
                p = coef[i] + (x * p);
            return p;
        }

        public Polynomial differentiate() {
            if (deg == 0) return new Polynomial(0, 0);
            Polynomial deriv = new Polynomial(0, deg - 1);
            deriv.deg = deg - 1;
            for (int i = 0; i < deg; i++)
                deriv.coef[i] = (i + 1) * coef[i + 1];
            return deriv;
        }

        public String toString() {
            if (deg == 0) return "" + coef[0];
            if (deg == 1) return coef[1] + "x + " + coef[0];
            String s = coef[deg] + "x^" + deg;
            for (int i = deg - 1; i >= 0; i--) {
                if (coef[i] == 0) {
                    continue;
                } else if (coef[i] > 0) {
                    s = s + " + " + (coef[i]);
                } else if (coef[i] < 0) s = s + " - " + (-coef[i]);
                if (i == 1) {
                    s = s + "x";
                } else if (i > 1) s = s + "x^" + i;
            }
            return s;
        }

        public String toString2() {
            String s = "1";
            for (int i = deg - 1; i >= 0; i--) {
                s += (coef[i] == 1) ? "1" : "0";
            }
            return s;
        }
    }

    void run() throws IOException {
        System.out.println(crc32CustomRealisation("input.txt"));
    }

    Polynomial createPolynomial(BigInteger base) {
        Polynomial result = new Polynomial(0, 0);
        String str = base.toString();
        for (int i = 0, len = str.length(); i < len; i++) {
            Polynomial temp = new Polynomial(1, len - 1 - i);
            if (str.charAt(i) == '1') {
                result = result.plus(temp);
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        new A().run();
    }
}