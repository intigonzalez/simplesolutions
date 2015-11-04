package com.company;

import java.io.*;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Created by doctorant on 11/1/2015.
 */
public class Primes {

    /*
    * Known primes
    * */
    private int[] primes;

    /* maximum calculated primes */
    private static final long MAX_KNOWN_PRIME = 50000002;

    /*  size of a bin (the number line in split in bins) */
    private static final long BIN_SIZE = 1000000;

    /* count of bins  */
    private static final int BIN_COUNT = 1000001;

    /*bins, the element i contains pi((i+1)*BINSIZE) */
    private long[] bins = new long[BIN_COUNT];
    /*accumulated_bins, the element i contains pi((i+1)*BINSIZE) */
    private long[] accumulated_bins = new long[BIN_COUNT];


    /* calculated bins */
    private int known_bins = 0;


    public Primes() {
        primes = sieve((int)MAX_KNOWN_PRIME);
        int c = 0;
        while (primes[c] < BIN_SIZE) c++;

        accumulated_bins[known_bins] = c;
        bins [known_bins++] = c;
    }

    /*
     Calculate a bunch of initial primes
     */
    private static int[] sieve(int n) {
        BitSet flags = new BitSet(n);
        int[] primes = new int[(int) (2 * (n/Math.log(n)))];
        int c = 0;
        primes[c++] = 2;
        for (int i = 3 ; i <= n ; i+=2) {
            if (!flags.get(i)) {
                primes[c++] = i;
                flags.set(i);
                int tmp = n / i;
                if (tmp > i) tmp = i;
                for (int j = tmp*i ; j < n ; j+=i) {
                    flags.set(j);
                }
            }
        }
        int[] r = new int[c];
        System.arraycopy(primes, 0, r, 0, c);
        return r;
    }


    int cache = -1;
    long[] p_cache = null;

    public long getPrime(long n) {
        if (n < (long)primes.length)
            return primes[((int) n)];

        int i = 0;
        long s = 0;
        while (s <= n) {
            if (i >= known_bins) {
                pi((i+1)*BIN_SIZE);
            }
            s += bins[i];
            i++;
        }

        if (cache == (i-1)) {
            s -= p_cache.length;
            int ttt = ((int) (n - s));
            return p_cache[ttt];
        }

        long [] p = sieve2( (i-1)*BIN_SIZE );
        s -= p.length;
        int ttt = ((int) (n - s));

        cache = i - 1;
        p_cache = p;

        return p[ttt];
    }

    public long pi(long x) {

        if (x < BIN_SIZE) {
            int idx = Arrays.binarySearch(primes, 0, Math.min((int)BIN_SIZE, primes.length), (int)x);
            if (idx < 0) {
                idx = (-idx - 1);
            }
            else {
                idx = (idx + 1);
            }
            return idx;
        }

        long t = x / BIN_SIZE;

        long s = accumulated_bins[((int) Math.min(known_bins - 1, t - 1))];
        for (int i = known_bins ; i < t ; i++) {
            // now we know another bin
            long p = sieve3(i * BIN_SIZE);
            accumulated_bins[known_bins] = accumulated_bins[known_bins - 1] + p;
            bins[known_bins ++] = p;
            s += p;
        }

        long[] p = sieve2(t*BIN_SIZE);
        long idx = Arrays.binarySearch(p, x);
        if (idx < 0) {
            idx = (-idx - 1);
        }
        else {
            idx = (idx + 1);
        }
        return s + idx;

    }



    boolean[] flags2 = new boolean[(int)BIN_SIZE];
    private long[] sieve2(long base) {
        Arrays.fill(flags2, false);
        int c = 0;
        int t = (int) Math.sqrt(base + BIN_SIZE);
        long p;
        for (int idx = 1 ; (p = primes[idx]) <= t ; idx++) {
            long from = (base / p) * p;
            if (from < base) from += p;
            from = from - base;
            while (from < (BIN_SIZE)) {
                flags2[((int) from)] = true;
                from += p;
            }
        }

        for (int i = 1 ; i < BIN_SIZE ; i+=2) {
            if (!flags2[i]) {
                c++;
            }
        }

        long[] new_primes = new long[c];
        c = 0;
        for (int i = 1 ; i < BIN_SIZE ; i+=2) {
            if (!flags2[i]) {
                new_primes[c++] = (base + i);
            }
        }

        return new_primes;

    }


    private long sieve3(long base) {
        Arrays.fill(flags2, false);
        long c = 0;
        long t = (long) Math.sqrt(base + BIN_SIZE);
        long p;
        for (int idx = 1 ; (p = primes[idx]) <= t ; idx++) {
            long from = (base / p) * p;
            if (from < base) from += p;
            from = from - base;
            while (from < (BIN_SIZE)) {
                flags2[((int) from)] = true;
                from += p;
            }
        }

        for (int i = 1 ; i < BIN_SIZE ; i+=2) {
            if (!flags2[i]) {
                c++;
            }
        }

        return c;

    }

    public void writeTable(String s) {
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(s));

            pw.printf("%d %d\n", BIN_SIZE, known_bins);
            for (int i = 0 ; i < known_bins ; i++) {
                pw.printf("%d %d\n", bins[i], accumulated_bins[i]);
            }

            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void readTableIfExists(String s) {
        try {
            File file = new File(s);
            if (!file.exists()) return;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(s)));
            String line = br.readLine();
            String[] a = line.split(" ");
            int b_size = Integer.parseInt(a[0]);
            if (b_size != BIN_SIZE) throw  new RuntimeException("The file is not compatible with the configuration of this primes");
            known_bins = Integer.parseInt(a[1]);
            for (int i = 0 ; i < known_bins; i++) {
                line = br.readLine();
                a = line.split(" ");
                bins[i] = Long.parseLong(a[0]);
                accumulated_bins[i] = Long.parseLong(a[1]);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long divisors (long x, long i, long accumulated_r) {
        if (accumulated_r > 8) return 100;
        if (x <= (long)primes[primes.length - 1]) {
            int idx = Arrays.binarySearch(primes, (int)x);
            if (idx >= 0) return 2*accumulated_r;
        }
        long p = getPrime(i);
        int local_c = 0;
        long y = x;
        long mid_x = (long) Math.sqrt(x);
        // find prime factor
        while (y % p != 0 && p <= mid_x ) {
            if (y % p != 0) {
                i++;
                p = getPrime(i);
            }
        }
        if (y % p == 0) {
            while (y % p == 0) {
                y = y / p;
                local_c ++;
            }
            if (y == 1) {
                accumulated_r *= (local_c + 1);
            }
            else {
                accumulated_r *= (local_c + 1);
                return divisors(y, i+1, accumulated_r);
//                c *= (local_c + 1) * divisors(y, i + 1);
            }
        }
        else {
            // it is prime
            return 2*accumulated_r;
        }

        return accumulated_r;
    }

    public long divisors (long x) {
        return divisors(x, 0, 1);
    }
}
