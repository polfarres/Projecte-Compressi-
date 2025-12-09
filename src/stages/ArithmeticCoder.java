package stages;

import image.Image;
import utils.BitReader;
import utils.BitWriter;

import java.util.ArrayList;
import java.util.List;

public class ArithmeticCoder {

    private static final int SYMBOL_BITS = 17;
    private static final int SYMBOLS = 1 << SYMBOL_BITS;

    private int offset = 32767;
    private int low = 0;
    private int high = 0xFFFFFFFF;
    private int underflow = 0;
    private int code = 0;

    public void encodeSymbol(int symbol, List<Integer> cumFreq, BitWriter bw) {
        long range = (long) (high & 0xFFFFFFFFL) - (low & 0xFFFFFFFFL) + 1;
        long totalFreq = cumFreq.get(cumFreq.size() - 1);

        long cumSymbol = cumFreq.get(symbol);
        long cumSymbolPlus1 = cumFreq.get(symbol + 1);

        high = (int) ((low & 0xFFFFFFFFL) + (range * cumSymbolPlus1) / totalFreq - 1);
        low = (int) ((low & 0xFFFFFFFFL) + (range * cumSymbol) / totalFreq);

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

    public void initializeDecoder(BitReader br) {
        this.low = 0;
        this.high = 0xFFFFFFFF;
        this.code = 0;
        for (int i = 0; i < 32; i++) {
            int b = br.readBit();
            code = (code << 1) | (b & 1);
        }
    }

    public int decodeSymbol(List<Integer> cumFreq, BitReader br) {
        long range = (long) (high & 0xFFFFFFFFL) - (low & 0xFFFFFFFFL) + 1;
        long totalFreq = cumFreq.get(cumFreq.size() - 1);

        long value = (((code & 0xFFFFFFFFL) - (low & 0xFFFFFFFFL) + 1) * totalFreq - 1) / range;

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

        while (true) {
            if (((high & 0x80000000) == (low & 0x80000000))) {
                low <<= 1; high <<= 1; high |= 1;
                int b = br.readBit();
                code = ((code << 1) | (b & 1));
            } else if (((low & 0x40000000) != 0) && ((high & 0x40000000) == 0)) {
                low &= 0x3FFFFFFF;
                high |= 0x40000000;
                low <<= 1; high <<= 1; high |= 1;
                int b = br.readBit();
                code = ((code ^ 0x40000000) << 1) | (b & 1);
            } else {
                break;
            }
        }
        return symbol;
    }

    public static List<Integer> computeCumFreq(final List<Integer> data) {
        List<Integer> freq = new ArrayList<>(SYMBOLS);
        for (int i = 0; i < SYMBOLS; i++) freq.add(0);

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
        java.util.List<Integer> symbols = getIntegers(image);
        java.util.List<Integer> cumFreq = ArithmeticCoder.computeCumFreq(symbols);

        for (int symbol : symbols) {
            encodeSymbol(symbol, cumFreq, bw);
        }
        finish(bw);
    } //✅

    public void DecodeImage(Image image, BitReader br) {
        image.img = new int[image.bands][image.height][image.width];

        List<Integer> cumFreq = new ArrayList<>();
        int currentSum = 0;
        cumFreq.add(0);

        for (int freq : image.frequencies) {
            currentSum += freq;
            cumFreq.add(currentSum);
        }

        int[][][] imgPredicted = new int[image.bands][image.height][image.width];
        java.util.List<Integer> symbols = new ArrayList<>();

        for (int b = 0; b < image.bands; b++) {
            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    symbols.add(decodeSymbol(cumFreq, br));
                }
            }
        }

        List<Integer> deNormalized = deNormalizeSymbols(symbols);

        for (int b = 0; b < image.bands; b++) {
            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    imgPredicted[b][y][x] = deNormalized.get(b * image.height * image.width + y * image.width + x);
                }
            }
        }

        image.img = imgPredicted;
    } //✅

    private List<Integer> deNormalizeSymbols(List<Integer> symbols) {
        List<Integer> deNormalized = new ArrayList<>();
        for (Integer symbol : symbols) {
            deNormalized.add(symbol - offset);
        }
        return deNormalized;
    } //✅

    private List<Integer> getIntegers(Image image) {
        List<Integer> symbols = new ArrayList<>();

        int maxSymbols = SYMBOLS;
        int[] freqHistogram = new int[maxSymbols];

        for (int b = 0; b < image.bands; b++) {
            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    int raw = image.img[b][y][x];
                    int val = raw + offset;        // Se normalizan los valores para tenerlos en positivo antes del Histograma

                    symbols.add(val);

                    if (val >= 0 && val < maxSymbols)
                        freqHistogram[val]++;
                }
            }
        }

        image.setCompressionHeaderData(Quantitzation.Q_STEP, freqHistogram);
        return symbols;
    } //✅


    public void encodeImageImproved(Image image, BitWriter bw) {
        java.util.List<Integer> symbols = getIntegers(image);
        java.util.List<Integer> cumFreq = ArithmeticCoder.computeCumFreq(symbols);

        for (int symbol : symbols) {
            encodeSymbol(symbol, cumFreq, bw);
        }
        finish(bw);
    }

    public void encodeSymbolImproved(int symbol, stages.AdaptiveFrequencyModel model, BitWriter bw) {
        long range = (long) (high & 0xFFFFFFFFL) - (low & 0xFFFFFFFFL) + 1;
        long totalFreq = model.getTotal();

        long cumSymbol = model.getLow(symbol);
        long cumSymbolPlus1 = model.getHigh(symbol);

        // Actualització de l'interval
        high = (int) ((low & 0xFFFFFFFFL) + (range * cumSymbolPlus1) / totalFreq - 1);
        low = (int) ((low & 0xFFFFFFFFL) + (range * cumSymbol) / totalFreq);

        // ... El bucle de renormalització (writeBit) és EXACTAMENT IGUAL al teu ...
        while (true) {
            // (Copia el teu codi de renormalització aquí: el de low/high i writeBit)
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

    // --- DECODER ---
    public int decodeSymbolImproved(stages.AdaptiveFrequencyModel model, BitReader br) {
        long range = (long) (high & 0xFFFFFFFFL) - (low & 0xFFFFFFFFL) + 1;
        long totalFreq = model.getTotal();

        long value = (((code & 0xFFFFFFFFL) - (low & 0xFFFFFFFFL) + 1) * totalFreq - 1) / range;

        // Preguntem al model quin símbol és
        int symbol = model.getSymbol(value);

        long cumSymbol = model.getLow(symbol);
        long cumSymbolPlus1 = model.getHigh(symbol);

        high = (int) ((low & 0xFFFFFFFFL) + (range * cumSymbolPlus1) / totalFreq - 1);
        low = (int) ((low & 0xFFFFFFFFL) + (range * cumSymbol) / totalFreq);

        // ... Bucle de renormalització (readBit) IGUAL al teu ...
        while (true) {
            // (Copia el teu codi de renormalització del decoder aquí)
            if (((high & 0x80000000) == (low & 0x80000000))) {
                low <<= 1; high <<= 1; high |= 1;
                int b = br.readBit();
                code = ((code << 1) | (b & 1));
            } else if (((low & 0x40000000) != 0) && ((high & 0x40000000) == 0)) {
                low &= 0x3FFFFFFF;
                high |= 0x40000000;
                low <<= 1; high <<= 1; high |= 1;
                int b = br.readBit();
                code = ((code ^ 0x40000000) << 1) | (b & 1);
            } else {
                break;
            }
        }
        return symbol;
    }

}
