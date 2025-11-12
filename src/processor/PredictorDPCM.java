package processor;

/**
 * Implementa una predicció DPCM amb dades d'entrada de tipus short.
 * - Predictor: Píxel ESQUERRE (B)
 * - Excepció: Píxels de la Primera Columna utilitzen el píxel de DALT (A)
 * - Exepeció 2: El primer pixel la seva predicció és de 0
 * * NOTA: Les operacions de resta/suma es fan amb 'int' per evitar overflow.
 */
public class PredictorDPCM {

    private int mapSignedToUnsigned(int valor) {
        if (valor >= 0) {
            // 0 -> 0, 1 -> 2, 2 -> 4, ... (Parells)
            return 2 * valor;
        } else {
            // -1 -> 1, -2 -> 3, -3 -> 5, ... (Senars)
            return -2 * valor - 1;
        }
    }


    private int mapUnsignedToSigned(int valor) {
        if (valor % 2 == 0) {
            // Cas parell (origina valors >= 0)
            return valor / 2;
        } else {
            // Cas senar (origina valors < 0)
            return -(valor + 1) / 2;
        }
    }

    public int[][][] aplicarPrediccio(short[][][] dades) {

        int numBandes = dades.length;
        int alçada = dades[0].length;
        int amplada = dades[0][0].length;

        // Matriu de residus mapejats: ha de ser INT
        int[][][] residuMapejat = new int[numBandes][alçada][amplada];

        for (int b = 0; b < numBandes; b++) {
            for (int y = 0; y < alçada; y++) {
                for (int x = 0; x < amplada; x++) {

                    int valorActual = dades[b][y][x]; // int per seguretat
                    int predictor;

                    // --- Càlcul del Predictor (Idèntic a la teva lògica) ---
                    if (x == 0) {
                        if (y == 0) {
                            predictor = 0;
                        } else {
                            predictor = dades[b][y - 1][x];
                        }
                    } else {
                        predictor = dades[b][y][x - 1];
                    }

                    // 1. Càlcul del Residu Original (Z)
                    int residuOriginal = valorActual - predictor;

                    // 2. Aplicació del Mapeig Bijectiu (Z -> N)
                    residuMapejat[b][y][x] = mapSignedToUnsigned(residuOriginal);
                }
            }
        }

        return residuMapejat;
    }

    public short[][][] reconstruirDades(int[][][] matriuResidus) {

        int numBandes = matriuResidus.length;
        int alçada = matriuResidus[0].length;
        int amplada = matriuResidus[0][0].length;

        short[][][] dadesReconstruides = new short[numBandes][alçada][amplada];

        for (int b = 0; b < numBandes; b++) {
            for (int y = 0; y < alçada; y++) {
                for (int x = 0; x < amplada; x++) {

                    int residuMapejat = matriuResidus[b][y][x];

                    // 1. Aplicació del Mapeig Invers (N -> Z)
                    int residuOriginal = mapUnsignedToSigned(residuMapejat);

                    int predictor;

                    // --- Càlcul del Predictor (Idèntic a la teva lògica) ---
                    if (x == 0) {
                        if (y == 0) {
                            predictor = 0;
                        } else {
                            // Utilitzem el valor reconstruït (int)
                            predictor = dadesReconstruides[b][y - 1][x];
                        }
                    } else {
                        // Utilitzem el valor reconstruït (int)
                        predictor = dadesReconstruides[b][y][x - 1];
                    }

                    // 2. Càlcul de la Reconstrucció: Original = Residu + Predictor
                    int valorReconstruït = residuOriginal + predictor;

                    // 3. Emmagatzemar, amb CASTEIG (assumint que el valor final entra en un short)
                    dadesReconstruides[b][y][x] = (short) valorReconstruït;
                }
            }
        }

        return dadesReconstruides;
    }
}