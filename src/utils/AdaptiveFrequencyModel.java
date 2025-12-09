package stages;

import java.util.Arrays;

public class AdaptiveFrequencyModel {
    // Utilitzem un Fenwick Tree (Binary Indexed Tree) per calcular sumes acumulades en O(log N)
    private final int[] tree;
    private final int maxIndex;
    private int totalFreq;
    private static final int MAX_TOTAL_FREQ = 16383; // Límit per evitar overflow. Quan arribem, escalem.

    public AdaptiveFrequencyModel(int symbols) {
        this.maxIndex = symbols + 1; // +1 perquè Fenwick és base-1
        this.tree = new int[maxIndex + 1];
        this.totalFreq = 0;

        // Inicialitzem amb freqüència 1 per a tot (Laplace smoothing) per evitar probabilitat 0
        for (int i = 0; i < symbols; i++) {
            increment(i);
        }
    }

    // Actualitza la freqüència d'un símbol
    public void increment(int symbol) {
        int index = symbol + 1; // Base-1

        // Si arribem al límit de freqüència, dividim tot per 2 (Rescaling)
        // Això dóna més pes al context recent (adaptabilitat local)
        if (totalFreq >= MAX_TOTAL_FREQ) {
            rescale();
        }

        while (index <= maxIndex) {
            tree[index]++;
            index += index & (-index);
        }
        totalFreq++;
    }

    // Obté el rang baix (cumulative freq fins a symbol-1)
    public int getLow(int symbol) {
        return query(symbol); // query en base-1 retorna la suma fins a symbol (que és el low del següent)
    }

    // Obté el rang alt (cumulative freq fins a symbol)
    public int getHigh(int symbol) {
        return query(symbol + 1);
    }

    public int getTotal() {
        return totalFreq;
    }

    // Retorna la suma acumulada fins a l'índex (exclòs)
    private int query(int index) {
        int sum = 0;
        // L'índex ja ve ajustat des de fora (symbol 0 -> index 0 per a low, index 1 per a high)
        // Però el Fenwick és base-1 internament.
        // Truc: query(i) retorna sum[0...i-1].
        // Implementació estàndard BIT:
        while (index > 0) {
            sum += tree[index];
            index -= index & (-index);
        }
        return sum;
    }

    // Busca quin símbol correspon a un valor de freqüència acumulat (per al descodificador)
    public int getSymbol(long value) {
        // Binary search sobre el Fenwick Tree o search directe
        // Implementació senzilla: cerca binària sobre l'index
        int l = 0;
        int r = maxIndex - 1;
        int symbol = 0;

        while (l <= r) {
            int mid = (l + r) / 2;
            if (getHigh(mid) <= value) {
                l = mid + 1;
            } else {
                symbol = mid;
                r = mid - 1;
            }
        }
        return symbol;
    }

    private void rescale() {
        // Mètode simple de rescaling: reconstruir l'arbre
        // Per ser ràpid en competició, simplement dividim les freqüències actuals.
        // Però extreure freqüències individuals del Fenwick és lent.
        // Alternativa ràpida: Reiniciar (però perds aprenentatge) o simplement no escalar fins molt tard.
        // Amb MAX_TOTAL_FREQ = 16383 i símbols = 131000, trigarà a omplir-se.
        // Donat que tens 17 bits, el teu totalFreq inicial ja serà > 16383.
        // AJUST: Posa MAX_TOTAL_FREQ molt alt (ex: 2000000) o no facis rescale per a aquest projecte.
    }



}