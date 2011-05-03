/**
 * 
 */
package eu.scape_project.pc.qa.bitwiser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * @author anj
 *
 */
public class Entropy {

    /*

    Apply various randomness tests to a stream of bytes

         by John Walker  --  September 1996
              http://www.fourmilab.ch/

     */

    boolean binary = false;     /* Treat input as a bitstream */

    long ccount[] = new long[256],       /* Bins to count occurrences of values */
    totalc = 0;        /* Total bytes counted */
    double prob[] = new double[256];       /* Probabilities per bin for entropy */

    /*  RT_LOG2  --  Calculate log to the base 2  */

    static double rt_log2(double x)
    {
        return Math.log(x)/Math.log(2.0);
    }

    static int MONTEN = 6;             /* Bytes used as Monte Carlo
                    co-ordinates.  This should be no more
                    bits than the mantissa of your
                                        "double" floating point type. */

    int mp;
    boolean sccfirst;
    long monte[] = new long[MONTEN];
    long inmont, mcount;
    double cexp, incirc, montex, montey, montepi,
    scc, sccun, sccu0, scclast, scct1, scct2, scct3,
    ent, chisq, datasum, mean;

    /*  RT_INIT  --  Initialise random test counters.  */

    public Entropy() { }

    void rt_init(boolean binmode)
    {
        int i;

        binary = binmode;          /* Set binary / byte mode */

        /* Initialise for calculations */

        ent = 0.0;             /* Clear entropy accumulator */
        chisq = 0.0;           /* Clear Chi-Square */
        datasum = 0.0;         /* Clear sum of bytes for arithmetic mean */

        mp = 0;            /* Reset Monte Carlo accumulator pointer */
        mcount = 0;            /* Clear Monte Carlo tries */
        inmont = 0;            /* Clear Monte Carlo inside count */
        incirc = 65535.0 * 65535.0;/* In-circle distance for Monte Carlo */

        sccfirst = true;           /* Mark first time for serial correlation */
        scct1 = scct2 = scct3 = 0.0; /* Clear serial correlation terms */

        incirc = Math.pow(Math.pow(256.0, (double) (MONTEN / 2)) - 1, 2.0);

        for (i = 0; i < 256; i++) {
            ccount[i] = 0;
        }
        totalc = 0;
    }

    /*  RT_ADD  --  Add one or more bytes to accumulation.  */

    void rt_add(int[] ocb, int bufl)
    {
        int oc, c, bean;

        while ( bufl-- > 0 ) {
            bean = 0;
            oc = ocb[bufl];

            do {
                if (binary) {
                    c = ((oc & 0x80)!=0?1:0);
                } else {
                    c = oc;
                }
                ccount[c]++;        /* Update counter for this bin */
                totalc++;

                /* Update inside / outside circle counts for Monte Carlo
        computation of PI */

                if (bean == 0) {
                    monte[mp++] = oc;       /* Save character for Monte Carlo */
                    if (mp >= MONTEN) {     /* Calculate every MONTEN character */
                        int mj;

                        mp = 0;
                        mcount++;
                        montex = montey = 0;
                        for (mj = 0; mj < MONTEN / 2; mj++) {
                            montex = (montex * 256.0) + monte[mj];
                            montey = (montey * 256.0) + monte[(MONTEN / 2) + mj];
                        }
                        if ((montex * montex + montey *  montey) <= incirc) {
                            inmont++;
                        }
                    }
                }

                /* Update calculation of serial correlation coefficient */

                sccun = c;
                if (sccfirst) {
                    sccfirst = false;
                    scclast = 0;
                    sccu0 = sccun;
                } else {
                    scct1 = scct1 + scclast * sccun;
                }
                scct2 = scct2 + sccun;
                scct3 = scct3 + (sccun * sccun);
                scclast = sccun;
                oc <<= 1;
            } while (binary && (++bean < 8));
        }
    }

    /*  RT_END  --  Complete calculation and return results.  */

    void rt_end()
    {
        int i;

        /* Complete calculation of serial correlation coefficient */

        scct1 = scct1 + scclast * sccu0;
        scct2 = scct2 * scct2;
        scc = totalc * scct3 - scct2;
        if (scc == 0.0) {
            scc = -100000;
        } else {
            scc = (totalc * scct1 - scct2) / scc;
        }

        /* Scan bins and calculate probability for each bin and
      Chi-Square distribution.  The probability will be reused
      in the entropy calculation below.  While we're at it,
      we sum of all the data which will be used to compute the
      mean. */

        cexp = totalc / (binary ? 2.0 : 256.0);  /* Expected count per bin */
        for (i = 0; i < (binary ? 2 : 256); i++) {
            double a = ccount[i] - cexp;;

            prob[i] = ((double) ccount[i]) / totalc;       
            chisq += (a * a) / cexp;
            datasum += ((double) i) * ccount[i];
        }

        /* Calculate entropy */

        for (i = 0; i < (binary ? 2 : 256); i++) {
            if (prob[i] > 0.0) {
                ent += prob[i] * rt_log2(1 / prob[i]);
            }
        }

        /* Calculate Monte Carlo value for PI from percentage of hits
      within the circle */

        montepi = 4.0 * (((double) inmont) / mcount);

        System.out.printf("0,File-%ss,Entropy,Chi-square,Mean,Monte-Carlo-Pi,Serial-Correlation\n",
                binary ? "bit" : "byte");
        System.out.printf("1,%d,%f,%f,%f,%f,%f\n",
                totalc, ent, chisq, datasum/totalc, montepi, scc);
        /* Return results through arguments */
        mean = datasum/totalc;

    }

    boolean isISOspace(byte x)  {return (Character.isWhitespace((char)x));}
    boolean isISOalpha(byte x)  {return (Character.isLetter((char)x));}
    boolean isISOupper(byte x)  {return (Character.isUpperCase((char)x));}
    boolean isISOlower(byte x)  {return (Character.isLowerCase((char)x));}
    boolean isISOprint(byte x)  {return (Character.isDefined((char)x) && !Character.isISOControl((char)x));}
    byte toISOupper(byte x) {return (byte)Character.toUpperCase((char)x);}
    byte toISOlower(byte x) {return (byte)Character.toLowerCase((char)x);}

    public void calculate( File file, boolean  counts, boolean fold, boolean binary, boolean terse  ) throws IOException {
        String samp = binary ? "bit" : "byte";
        //memset(ccount, 0, sizeof ccount);

        /* Initialise for calculations */
        System.out.println("Initialise...");
        rt_init(binary);

        /* Scan input file and count character occurrences */
        System.out.println("Scan file... "+file.length());
        int oc, totalc = 0, ccount[] = new int[256];
        int[] ocb = new int[1];
        DataInputStream fin = new DataInputStream( new FileInputStream(file) );
        try {
            while ( true ) {
                oc = fin.readUnsignedByte();

                if (fold && isISOalpha((byte)oc) && isISOupper((byte)oc)) {
                    oc = toISOlower((byte)oc);
                }
                ocb[0] = oc;
                totalc += binary ? 8 : 1;
                if (binary) {
                    int b;
                    int ob = ocb[0];

                    for (b = 0; b < 8; b++) {
                        ccount[ob & 1]++;
                        ob >>= 1;
                    }
                } else {
                    ccount[ocb[0]]++;         /* Update counter for this bin */
                }
                rt_add(ocb, 1);
            }
        } catch( EOFException e ) {
             // File ended, which is fine. Surely this should not be an Exception?!
        } finally {
            fin.close();
        }
        System.out.println("File read.");

        /* Complete calculation and return sequence metrics */
        double chip;
        rt_end();

        if (terse) {
            System.out.printf("0,File-%ss,Entropy,Chi-square,Mean,Monte-Carlo-Pi,Serial-Correlation\n",
                    binary ? "bit" : "byte");
            System.out.printf("1,%d,%f,%f,%f,%f,%f\n",
                    totalc, ent, chisq, mean, montepi, scc);
        }

        /* Calculate probability of observed distribution occurring from
           the results of the Chi-Square test */

        //chip = pochisq(chisq, (binary ? 1 : 255));
        // FIXME Is this a Cheat?
        chip = Math.sqrt(2.0 * chisq) - Math.sqrt(2.0 * (binary ? 1 : 255.0) - 1.0);
        double a = Math.abs(chip);
        for (int i = 9; i >= 0; i--) {
            if (chsqt[1][i] < a) {
                break;
            }
        }
        chip = (chip >= 0.0) ? chsqt[0][0] : 1.0 - chsqt[0][0];


        /* Print bin counts if requested */

        if (counts) {
            if (terse) {
                System.out.printf("2,Value,Occurrences,Fraction\n");
            } else {
                System.out.printf("Value Char Occurrences Fraction\n");
            }
            for (byte i = 0; i < (binary ? 2 : 256); i++) {
                if (terse) {
                    System.out.printf("3,%d,%d,%f\n", i,
                            ccount[i], ((double) ccount[i] / totalc));
                } else {
                    if (ccount[i] > 0) {
                        System.out.printf("%3d   %c   %10ld   %f\n", i,
                                /* The following expression shows ISO 8859-1
                  Latin1 characters and blanks out other codes.
                  The test for ISO space replaces the ISO
                  non-blanking space (0xA0) with a regular
                              ASCII space, guaranteeing it's rendered
                              properly even when the font doesn't contain
                  that character, which is the case with many
                  X fonts. */
                                (!isISOprint(i) || isISOspace(i)) ? ' ' : i,
                                        ccount[i], ((double) ccount[i] / totalc));
                    }
                }
            }
            if (!terse) {
                System.out.printf("\nTotal:    %10ld   %f\n\n", totalc, 1.0);
            }
        }

        /* Print calculated results */

        if (!terse) {
            System.out.printf("Entropy = %f bits per %s.\n", ent, samp);
            System.out.printf("\nOptimum compression would reduce the size\n");
            System.out.printf("of this %d %s file by %d percent.\n\n", totalc, samp,
                    (short) ((100 * ((binary ? 1 : 8) - ent) /
                            (binary ? 1.0 : 8.0))));
            System.out.printf(
                    "Chi square distribution for %d samples is %1.2f, and randomly\n",
                    totalc, chisq);
            if (chip < 0.0001) {
                System.out.printf("would exceed this value less than 0.01 percent of the times.\n\n");
            } else if (chip > 0.9999) {
                System.out.printf("would exceed this value more than than 99.99 percent of the times.\n\n");
            } else {
                System.out.printf("would exceed this value %1.2f percent of the times.\n\n",
                        chip * 100);
            }
            System.out.printf(
                    "Arithmetic mean value of data %ss is %1.4f (%.1f = random).\n",
                    samp, mean, binary ? 0.5 : 127.5);
            System.out.printf("Monte Carlo value for Pi is %1.9f (error %1.2f percent).\n",
                    montepi, 100.0 * (Math.abs(Math.PI - montepi) / Math.PI));
            System.out.printf("Serial correlation coefficient is ");
            if (scc >= -99999) {
                System.out.printf("%1.6f (totally uncorrelated = 0.0).\n", scc);
            } else {
                System.out.printf("undefined (all values equal!).\n");
            }
        }

    }

    /* For the chi-squared cheat? */
    double[][] chsqt= {
            {
                0.5,
                0.25,
                0.1,
                0.05,
                0.025,
                0.01,
                0.005,
                0.001,
                0.0005,
                0.0001
            },{
                0.0,
                0.6745,
                1.2816,
                1.6449,
                1.9600,
                2.3263,
                2.5758,
                3.0902,
                3.2905,
                3.7190
            }
    };
    
    public static void makeRandomFile( File file ) throws IOException {
    	Random rng = new XORShiftRandom(12321321l);
    	DataOutputStream dout = new DataOutputStream( new FileOutputStream( file ) );
    	for( int i = 0; i < 1000; i++ ) {
    		dout.writeDouble( rng.nextDouble() );
    	}
    	dout.flush();
    	dout.close();
    }
    /*
     * 
[22:04:43] [anj@lovely ../digitalpreservation]$ ent ~/temp.rnd 
Entropy = 7.448123 bits per byte.

Optimum compression would reduce the size
of this 8000 byte file by 6 percent.

Chi square distribution for 8000 samples is 32571.39, and randomly
would exceed this value less than 0.01 percent of the times.

Arithmetic mean value of data bytes is 129.6025 (127.5 = random).
Monte Carlo value for Pi is 2.961740435 (error 5.72 percent).
Serial correlation coefficient is -0.135723 (totally uncorrelated = 0.0).
     */
    
    public static void main(String [] args) throws IOException {
        File tempFile = new File("/Users/anj/temp.rnd");
        makeRandomFile(tempFile);
        Entropy ent = new Entropy();
        System.out.println("Starting entropy calc...");
        ent.calculate(tempFile, false, false, false, false);
    }
}
