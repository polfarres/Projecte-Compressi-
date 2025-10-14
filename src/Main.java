import io.ChooseOperation;
import processor.ImageProcessor;

public class Main {
    public static void main(String[] args) {


        String inputPath = "resources/imatges";
        String outputPath = "resources/imatges/sortides";

        ImageProcessor processor = new ImageProcessor(inputPath, outputPath);


        ChooseOperation option = new ChooseOperation();
        option.readOption();

        switch (option) {
            case 1:
                processor.processAll();
                break;
            case 2:
                break;
            case 3:
                break;
            default:
                System.out.print("Amore, t'has equivocat ");
                break;
        }


    }
}
