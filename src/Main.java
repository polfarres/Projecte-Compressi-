import io.ChooseOperation;


public class Main {
    public static void main(String[] args) {

        ChooseOperation option = new ChooseOperation();

        while(!option.isFinished()) {

            option.readOption();
            option.ExecuteCommand();
        }

    }
}
