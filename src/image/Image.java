package image;

import io.RawImageReader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class Image {
    public int width;
    public int height;
    public int bands;
    public int bitsPerSample;
    public boolean signed;
    public boolean bigEndian;
    public int[][][] img;
    public String name;
    public String imagePath;

    // --- Camps per a la compressió ---
    public int qStep;
    public int[] frequencies;  // ARRAY D'ENTERS

    public Image(int width, int height, int bands, int bitsPerSample,
                 boolean signed, boolean bigEndian, String inputImage){
        this.width = width;
        this.height = height;
        this.bands = bands;
        this.bitsPerSample = bitsPerSample;
        this.signed = signed;
        this.bigEndian = bigEndian;
        this.name = (inputImage != null) ? new File(inputImage).getName() : null;
        this.imagePath = (inputImage != null) ? new File(inputImage).getParent() : null;


        try {
            short[][][] src = RawImageReader.readRaw(inputImage, width, height, bands, bitsPerSample,
                    signed, bigEndian);

            int d1 = src.length;
            int d2 = src[0].length;
            int d3 = src[0][0].length;

            this.img = new int[d1][d2][d3];

            for (int i = 0; i < d1; i++) {
                for (int j = 0; j < d2; j++) {
                    for (int k = 0; k < d3; k++) {
                        img[i][j][k] = src[i][j][k];   // automatic widening conversion
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error llegint la imatge raw: " + inputImage);
        }
    } //✅

    public void printInfo() {
        System.out.println("Image Info:");
        System.out.println("Width: " + width);
        System.out.println("Height: " + height);
        System.out.println("Bands: " + bands);
        System.out.println("Bits per Sample: " + bitsPerSample);
        System.out.println("Signed: " + signed);
        System.out.println("Big Endian: " + bigEndian);
    } //✅

    public void setCompressionHeaderData(int qStep, int[] frequencies) {
        this.qStep = qStep;
        this.frequencies = frequencies;
    }

    public void writeHeader(DataOutputStream dos) throws IOException {
        dos.writeInt(width);
        dos.writeInt(height);
        dos.writeInt(bands);
        dos.writeInt(bitsPerSample);
        dos.writeBoolean(signed);
        dos.writeBoolean(bigEndian);

        dos.writeInt(qStep);

        // --- CORRECCIÓ CRÍTICA ---
        // Fem servir writeInt (4 bytes) perquè les freqüències poden ser grans (fins a 2M)
        if (frequencies != null) {
            dos.writeInt(frequencies.length);
            for (int freq : frequencies) {
                dos.writeInt(freq); // writeInt, NO writeShort
            }
        } else {
            dos.writeInt(0);
        }
    }

    /*
    public static Image readHeader(DataInputStream dis) throws IOException {
        int width = dis.readInt();
        int height = dis.readInt();
        int bands = dis.readInt();
        int bitsPerSample = dis.readInt();
        boolean signed = dis.readBoolean();
        boolean bigEndian = dis.readBoolean();

        Image config = new Image(width, height, bands, bitsPerSample, signed, bigEndian);

        config.qStep = dis.readInt();

        // --- CORRECCIÓ CRÍTICA ---
        // Fem servir readInt (4 bytes)
        int freqLength = dis.readInt();
        if (freqLength > 0) {
            config.frequencies = new int[freqLength];
            for (int i = 0; i < freqLength; i++) {
                config.frequencies[i] = dis.readInt(); // readInt, NO readShort
            }
        }

        return config;
    }

     */
}