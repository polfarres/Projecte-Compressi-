package stages;

import image.Image;
import utils.BitReader;
import utils.BitWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Traducció del codificador/decodificador aritmètic de C++ a Java (IAC).
 * Utilitza int (32 bits) per low, high, code i long (64 bits) per range i càlculs.
 */
public class ArithmeticCoder {

    // Configurem 17 bits per seguretat (suporta tot el rang de short: -32768 a 32767).
    // Si només tens dades de 8 bits (-255 a 255), funcionara igualment sense problemes,
    // simplement la taula de freqüències serà més gran del necessari (sense impacte real).
    private static final int SYMBOL_BITS = 17;
    private static final int SYMBOLS = 1 << SYMBOL_BITS; // 131072

    private int offset = 32767;
    private int low = 0;
    private int high = 0xFFFFFFFF;
    private int underflow = 0;
    private int code = 0;

    // =======================================================================
    // ENCODER
    // =======================================================================

    public void encodeSymbol(int symbol, List<Integer> cumFreq, BitWriter bw) {
        long range = (long) (high & 0xFFFFFFFFL) - (low & 0xFFFFFFFFL) + 1;
        long totalFreq = cumFreq.get(cumFreq.size() - 1);

        long cumSymbol = cumFreq.get(symbol);
        long cumSymbolPlus1 = cumFreq.get(symbol + 1);

        high = (int) ((low & 0xFFFFFFFFL) + (range * cumSymbolPlus1) / totalFreq - 1);
        low = (int) ((low & 0xFFFFFFFFL) + (range * cumSymbol) / totalFreq);

        // Renormalització
        while (true) {
            if (((high & 0x80000000) == (low & 0x80000000))) {
                int bit = (high >>> 31) & 1;
                bw.writeBit(bit);
                while (underflow > 0) {
                    bw.writeBit(bit ^ 1);
                    underflow--;
                }
                low <<= 1; high <<= 1; high |= 1;

            } else if (((low & 0x40000000) != 0) && ((high & 0x40000000) == 0)) {
                underflow++;
                low &= 0x3FFFFFFF;
                high |= 0x40000000;
                low <<= 1; high <<= 1; high |= 1;
            } else {
                break;
            }
        }
    }

    public void finish(BitWriter bw) {
        underflow++;
        if ((low & 0xFFFFFFFFL) < 0x40000000L) {
            bw.writeBit(0);
            while (underflow-- > 0) bw.writeBit(1);
        } else {
            bw.writeBit(1);
            while (underflow-- > 0) bw.writeBit(0);
        }
        bw.flush();
    }

    // =======================================================================
    // DECODER
    // =======================================================================

    public void initializeDecoder(BitReader br) {
        this.low = 0;
        this.high = 0xFFFFFFFF;
        this.code = 0;
        for (int i = 0; i < 32; i++) {
            code = (code << 1) | br.readBit();
        }
    }

    public int decodeSymbol(List<Integer> cumFreq, BitReader br) {
        long range = (long) (high & 0xFFFFFFFFL) - (low & 0xFFFFFFFFL) + 1;
        long totalFreq = cumFreq.get(cumFreq.size() - 1);

        long value = (((code & 0xFFFFFFFFL) - (low & 0xFFFFFFFFL) + 1) * totalFreq - 1) / range;

        // Cerca binària
        int symbol;
        int left = 0, right = SYMBOLS;
        while (left < right - 1) {
            int mid = (left + right) / 2;
            if ((cumFreq.get(mid) & 0xFFFFFFFFL) <= value) left = mid;
            else right = mid;
        }
        symbol = left;

        long cumSymbol = cumFreq.get(symbol);
        long cumSymbolPlus1 = cumFreq.get(symbol + 1);

        high = (int) ((low & 0xFFFFFFFFL) + (range * cumSymbolPlus1) / totalFreq - 1);
        low = (int) ((low & 0xFFFFFFFFL) + (range * cumSymbol) / totalFreq);

        // Renormalització
        while (true) {
            if (((high & 0x80000000) == (low & 0x80000000))) {
                low <<= 1; high <<= 1; high |= 1;
                code = (code << 1) | br.readBit();

            } else if (((low & 0x40000000) != 0) && ((high & 0x40000000) == 0)) {
                low &= 0x3FFFFFFF;
                high |= 0x40000000;
                low <<= 1; high <<= 1; high |= 1;
                code = ((code ^ 0x40000000) << 1) | br.readBit();
            } else {
                break;
            }
        }
        return symbol;
    }

    // =======================================================================
    // UTILS
    // =======================================================================

    public static List<Integer> computeCumFreq(final List<Integer> data) {
        List<Integer> freq = new ArrayList<>(SYMBOLS);
        for(int i = 0; i < SYMBOLS; i++) freq.add(0);

        for (int s : data) {
            if (s >= 0 && s < SYMBOLS) {
                freq.set(s, freq.get(s) + 1);
            }
        }

        List<Integer> cum = new ArrayList<>(SYMBOLS + 1);
        cum.add(0);
        int currentSum = 0;
        for (int i = 0; i < SYMBOLS; i++) {
            currentSum += freq.get(i);
            cum.add(currentSum);
        }
        return cum;
    }

    public void encodeImage(Image image, BitWriter bw) {

        // Extraiem els símbols de la imatge
        java.util.List<Integer> symbols = getIntegers(image);

        // Normalizamos la imagen (para que no tenga negativos si es signed)
        java.util.List<Integer> normalized = normalizeSymbols(symbols);

        // Calculem les freqüències acumulades
        java.util.List<Integer> cumFreq = ArithmeticCoder.computeCumFreq(normalized);

        for (int symbol : normalized) {
            encodeSymbol(symbol, cumFreq, bw);
        }
        finish(bw);
    }

    private List<Integer> normalizeSymbols(List<Integer> symbols) {

        List<Integer> normalized = new ArrayList<>();

        for (Integer symbol : symbols) {
            normalized.add(symbol + offset);
        }
        return normalized;
    }

    private List<Integer> deNormalizeSymbols(List<Integer> symbols) {

        List<Integer> deNormalized = new ArrayList<>();

        for (Integer symbol : symbols) {
            deNormalized.add(symbol - offset);
        }
        return deNormalized;
    }

    private static List<Integer> getIntegers(Image image) {
        List<Integer> symbols = new ArrayList<>();

        int maxSymbols = 131072;

        int[] freqHistogram = new int[maxSymbols];

        for (int b = 0; b < image.bands; b++) {
            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    int val = image.img[b][y][x];
                    symbols.add(val);

                    // Comptem freqüència sense por al desbordament
                    if (val >= 0 && val < maxSymbols) {
                        freqHistogram[val]++;
                    }
                }
            }
        }

        image.setCompressionHeaderData(Quantitzation.Q_STEP, freqHistogram); // Con el qstep default

        return symbols;
    }
}