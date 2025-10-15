import io.ChooseOperation;
import processor.ImageProcessor;

public class Main {
    public static void main(String[] args) {


        String inputPath = "resources/imatges";
        String outputPath = "resources/imatges/sortides";

        ImageProcessor processor = new ImageProcessor(inputPath, outputPath);


        ChooseOperation option = new ChooseOperation();
        option.readOption();

        switch (option.getOption()) {
            case 1:
                processor.processReadAndWrite();
                break;
            case 2:
                processor.processEntropy("ORDER_0");
                break;
            case 3:
                processor.processEntropy("ORDER_1");
                break;
            default:
                System.out.print("Amore, t'has equivocat ");
                break;
        }


    }
}
