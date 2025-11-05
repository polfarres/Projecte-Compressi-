package processor;

/**
 * Implementa una predicció DPCM amb dades d'entrada de tipus short.
 * - Predictor: Píxel ESQUERRE (B)
 * - Excepció: Píxels de la Primera Columna utilitzen el píxel de DALT (A)
 * - Exepeció 2: El primer pixel la seva predicció és de 0
 * * NOTA: Les operacions de resta/suma es fan amb 'int' per evitar overflow.
 */
public class PredictorDPCM {
    public int[][][] aplicarPrediccio(short[][][] dades) {

        int numBandes = dades.length;
        int alçada = dades[0].length;
        int amplada = dades[0][0].length;

        int[][][] residu = new int[numBandes][alçada][amplada];

        for (int b = 0; b < numBandes; b++) {
            for (int y = 0; y < alçada; y++) {
                for (int x = 0; x < amplada; x++) {

                    int valorActual = dades[b][y][x];
                    int predictor;

                    // --- Lògica de Predicció ---

                    if (x == 0) {
                        if (y == 0) {
                            // Primer píxel de tots (0,0)
                            predictor = 0;
                        } else {
                            // Primera columna (x=0, y>0): Utilitzem DALT (A)
                            predictor = dades[b][y - 1][x];
                        }
                    } else {
                        // Cas General (x>0): Utilitzem ESQUERRA (B)
                        predictor = dades[b][y][x - 1];
                    }

                    // Càlcul de l'ERROR o RESIDU (el resultat és un int):
                    residu[b][y][x] = valorActual - predictor;
                }
            }
        }

        return residu;
    }

    public short[][][] reconstruirDades(int[][][] residu) {
        int numBandes = residu.length;
        int alçada = residu[0].length;
        int amplada = residu[0][0].length;


        short[][][] dadesReconstruides = new short[numBandes][alçada][amplada];

        for (int b = 0; b < numBandes; b++) {
            for (int y = 0; y < alçada; y++) {
                for (int x = 0; x < amplada; x++) {

                    int valorResidu = residu[b][y][x]; // int
                    int predictor; // int

                    // --- Lògica de Predicció (IDÈNTICA) ---

                    if (x == 0) {
                        if (y == 0) {
                            predictor = 0;
                        } else {
                            // Utilitzem el valor reconstruït de dalt (short, però s'ascendeix a int)
                            predictor = dadesReconstruides[b][y - 1][x];
                        }
                    } else {
                        // Utilitzem el valor reconstruït de l'esquerra (short, però s'ascendeix a int)
                        predictor = dadesReconstruides[b][y][x - 1];
                    }

                    // Càlcul de la RECONSTRUCCIÓ: Resultat és int
                    int valorReconstruït = valorResidu + predictor;

                    // Emmagatzemem el resultat, CASTEJANT-lo a short
                    dadesReconstruides[b][y][x] = (short) valorReconstruït;
                }
            }
        }

        return dadesReconstruides;
    }
}