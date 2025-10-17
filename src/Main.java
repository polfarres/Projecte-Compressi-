import io.ChooseOperation;

public class Main {
    static void main(String[] args) {

        ChooseOperation option = new ChooseOperation();
        option.readOption();


        String inputPath = "resources/imatges/entrades";
        String outputPath = "resources/imatges/sortides";

        option.ExecuteCommand(inputPath, outputPath);

    }
}
