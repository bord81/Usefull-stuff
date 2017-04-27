import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Serializer {

    //MODIFY THIS LINE
    private static final String targetFile = "your_entity.ser"; //Enter the filename for serialized class

    public static void serializeYourClass() {

        //MODIFY THIS LINE
        SomeEntityClass someEntityClass = new SomeEntityClass(); //Put your class information here

        File file = new File(targetFile);
        try {
            try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                objectOutputStream.writeObject(jsfTestCookie);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTargetFile() {
        return targetFile;
    }
}
