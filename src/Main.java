import io.ChooseOperation;


public class Main {
    public static void main(String[] args) {

        ChooseOperation option = new ChooseOperation();

        while(!option.isFinished()) {

            option.readOption();
            option.ExecuteCommand();
        }

    }


    //resources/imatges/n1_GRAY.ube8_1_2560_2048.raw
}
