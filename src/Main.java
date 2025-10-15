import io.ChooseOperation;

public class Main {
    public static void main(String[] args) {

        ChooseOperation option = new ChooseOperation();
        option.readOption();


        String inputPath = "resources/imatges";
        String outputPath = "resources/imatges/sortides";

        option.ExecuteCommand(inputPath, outputPath);

    }
}
