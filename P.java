import java.util.function.Function;
import java.math.BigInteger;

public class P {
    private static final BigInteger ZERO = new BigInteger("0");
    private static final BigInteger ONE =  BigInteger.ONE;
    private static final BigInteger TWO = new BigInteger("2");
    private static final BigInteger THREE = new BigInteger("3");

    public static BigInteger[] extendedEuclideanAlgorithm(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO)) {
            return new BigInteger[]{a, BigInteger.ONE, BigInteger.ZERO};
        }

        BigInteger u = BigInteger.ONE;
        BigInteger g = a;
        BigInteger x = BigInteger.ZERO;
        BigInteger y = b;

        while (!y.equals(BigInteger.ZERO)) {
            BigInteger t = g.mod(y);
            BigInteger q = g.subtract(t).divide(y);
            BigInteger s = u.subtract(q.multiply(x));

            u = x;
            g = y;
            x = s;
            y = t;
        }

        BigInteger v = (g.subtract(a.multiply(u))).divide(b);
        while (u.compareTo(BigInteger.ZERO) == -1) {
            u = u.add(b.divide(g));
            v = v.subtract(a.divide(g));
        }

        return new BigInteger[]{g, u, v};
    }

    private static Function<BigInteger, BigInteger> getF(BigInteger g, BigInteger a, BigInteger p) {
        BigInteger pMinus1 = p.subtract(ONE);
        BigInteger pDiv3 = p.divide(THREE);
        BigInteger pDiv3Times2 = pDiv3.multiply(TWO);

        return (x) -> {
            BigInteger moddedX = x.mod(pMinus1);
            if ( moddedX.compareTo(ZERO) > 0 && moddedX.compareTo(pDiv3) == -1) {
                return g.multiply(x).mod(p);
            }
            if ( moddedX.compareTo(pDiv3) > 0 && moddedX.compareTo(pDiv3Times2) == -1) {
                return x.pow(2).mod(p);
            }
            if ( moddedX.compareTo(pDiv3Times2) > 0 && moddedX.compareTo(p) == -1) {
                return a.multiply(x).mod(p);
            }
            return null;
        };
    }

    @FunctionalInterface
    interface Function2 <A, B, R> { 
        public R apply (A a, B b);
    }

    private static Function2<BigInteger, BigInteger, BigInteger> getAlpha(BigInteger g, BigInteger a, BigInteger p) {
        BigInteger pMinus1 = p.subtract(ONE);
        BigInteger pDiv3 = p.divide(THREE);
        BigInteger pDiv3Times2 = pDiv3.multiply(TWO);

        return (x, alpha) -> {
            BigInteger moddedX = x.mod(pMinus1);
            if ( moddedX.compareTo(ZERO) > 0 && moddedX.compareTo(pDiv3) == -1) {
                return alpha.add(ONE).mod(pMinus1);
            }
            if ( moddedX.compareTo(pDiv3) > 0 && moddedX.compareTo(pDiv3Times2) == -1) {
                return alpha.multiply(TWO).mod(pMinus1);
            }
            if ( moddedX.compareTo(pDiv3Times2) > 0 && moddedX.compareTo(p) == -1) {
                return alpha;
            }
            return null;
        };
    }

    private static Function2<BigInteger, BigInteger, BigInteger> getBeta(BigInteger g, BigInteger a, BigInteger p) {
        BigInteger pMinus1 = p.subtract(ONE);
        BigInteger pDiv3 = p.divide(THREE);
        BigInteger pDiv3Times2 = pDiv3.multiply(TWO);

        return (x, beta) -> {
            BigInteger moddedX = x.mod(pMinus1);
            if ( moddedX.compareTo(ZERO) > 0 && moddedX.compareTo(pDiv3) == -1) {
                return beta;
            }
            if ( moddedX.compareTo(pDiv3) > 0 && moddedX.compareTo(pDiv3Times2) == -1) {
                return beta.multiply(TWO).mod(pMinus1);
            }
            if ( moddedX.compareTo(pDiv3Times2) > 0 && moddedX.compareTo(p) == -1) {
                return beta.add(ONE).mod(pMinus1);
            }
            return null;
        };
    }

    public static BigInteger solve(BigInteger g, BigInteger a, BigInteger p) {
        BigInteger pMinus1 = p.subtract(ONE);
        Function<BigInteger, BigInteger> f = getF(g, a, p);
        Function2<BigInteger, BigInteger, BigInteger> fA = getAlpha(g, a, p);
        Function2<BigInteger, BigInteger, BigInteger> fB = getBeta(g, a, p);

        BigInteger xi = ONE;
        BigInteger alphai = ZERO;
        BigInteger betai = ZERO;
        BigInteger yi = ONE;
        BigInteger gammai = ZERO;
        BigInteger deltai = ZERO;

        alphai = fA.apply(xi, ZERO);
        betai = fB.apply(xi, ZERO);
        xi = f.apply(xi);

        gammai = fA.apply(yi, fA.apply(yi, ZERO));
        deltai = fB.apply(yi, fB.apply(yi, ZERO));
        yi = f.apply(f.apply(yi));

        while (!xi.equals(yi)) {
            alphai = fA.apply(xi, alphai);
            betai = fB.apply(xi, betai);
            xi = f.apply(xi);

            gammai = fA.apply(yi, gammai);
            deltai = fB.apply(yi, deltai);;
            yi = f.apply(yi);
            gammai = fA.apply(yi, gammai);
            deltai = fB.apply(yi, deltai);;
            yi = f.apply(yi);
        }

        BigInteger u = alphai.subtract(gammai).mod(pMinus1);
        BigInteger v = deltai.subtract(betai).mod(pMinus1);

        BigInteger [] extEuclid = extendedEuclideanAlgorithm(v, pMinus1);
        BigInteger d = extEuclid[0];


        if (d.compareTo(TWO) > 0) {
            BigInteger s = extEuclid[1];
            BigInteger w = s.multiply(u);

            // In practive 'd' is fairly small so this conversion should be fine
            int dInt = d.intValue();

            BigInteger m = w.divide(d);
            BigInteger n = pMinus1.divide(d);

            BigInteger possibleSolution;
            for (
                BigInteger k = ZERO;
                k.compareTo(d) < 0;
                k = k.add(ONE)
            ) {
                possibleSolution = m.add(k.multiply(n)).mod(pMinus1);
                if (g.modPow(possibleSolution, p).equals(a)) {
                    return possibleSolution;
                }
            }
        }

        throw new Error("No solution found");
    }

    private static final String TAB = "    ";

    public static void main(String[] args) {
        BigInteger g = new BigInteger("19");
        BigInteger a = new BigInteger("24717");
        BigInteger p = new BigInteger("48611");
        BigInteger result = solve(g, a, p);
        System.out.println("Example from book");
        System.out.println(TAB + result + "\n");

        System.out.println("4.40");

        g = new BigInteger("2");
        a = new BigInteger("2495");
        p = new BigInteger("5011");
        try {
            result = solve(g, a, p);
            System.out.println(TAB + "a) " + result);
        } catch (Error e) {
            System.out.println(TAB + "b) NO SOLUTION");
        }

        g = new BigInteger("17");
        a = new BigInteger("14226");
        p = new BigInteger("17959");
        try {
            result = solve(g, a, p);
            System.out.println(TAB + "b) " + result);
        } catch (Error e) {
            System.out.println(TAB + "b) NO SOLUTION");
        }

        g = new BigInteger("29");
        a = new BigInteger("5953042");
        p = new BigInteger("15239131");
        try {
            result = solve(g, a, p);
            System.out.println(TAB + "c) " + result);
        } catch (Error e) {
            System.out.println(TAB + "b) NO SOLUTION");
        }
    }
}
