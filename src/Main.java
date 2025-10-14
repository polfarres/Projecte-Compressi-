import io.ChooseOperation;
import processor.ImageProcessor;

public class Main {
    public static void main(String[] args) {


        ChooseOperation option = new ChooseOperation();
        option.readOption();


        String inputPath = "resources/imatges";
        String outputPath = "resources/imatges/sortides";

        ImageProcessor processor = new ImageProcessor(inputPath, outputPath);
        processor.processAll();
    }
}
