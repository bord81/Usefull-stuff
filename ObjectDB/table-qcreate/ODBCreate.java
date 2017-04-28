import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Scanner;

public class ODBCreate {

    //MODIFY THIS LINE
    private static final String oDBfile = "test.odb"; //Enter the filename of your ODB, *.odb

    //The following declarations contain helper values for reflection mechanism, you can change or add other types, like Date, for example
    private static final Integer anInt = 1;
    private static final Long aLong = 1L;
    private static final Double aDouble = 1.0;
    private static final String aString = "1";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\\n");
        System.out.println("Serialize new class - 's', create new entries - 'e' or any other key to exit");
        String choice = scanner.next();
        switch (choice) {
            case "s":
                Serializer.serializeYourClass();
                break;
            case "e":
                System.out.println("Enter number of new lines: ");
                int lines = scanner.nextInt();
                Object entity_class = null;
                try (FileInputStream fileInputStream = new FileInputStream(Serializer.getTargetFile());
                     ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
                ) {
                    entity_class = objectInputStream.readObject();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(oDBfile);
                for (int i = 0; i < lines; i++) {
                    EntityManager entityManager = entityManagerFactory.createEntityManager();
                    entityManager.getTransaction().begin();
                    Field[] fields = entity_class.getClass().getDeclaredFields();
                    for (Field f : fields) {
                        f.setAccessible(true);
                        String s = f.getName();
                        System.out.println("Please enter the value for " + s);
                        String val = scanner.next();
                        Class<?> field_type = f.getType();
                        try {

                            //Update these checks for additional types, like Date, etc.
                            if (field_type.isInstance(aString)) {
                                f = setClassField(f, entity_class, val);
                            } else if (field_type.isPrimitive()) {
                                f = setClassField(f, entity_class, Long.parseLong(val)); //Change to Integer.parseInt, if you need
                            } else if (field_type.isInstance(aLong)) {
                                f = setClassField(f, entity_class, Long.valueOf(val));
                            } else if (field_type.isInstance(aDouble)) {
                                f = setClassField(f, entity_class, Double.valueOf(val));
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("Wrong number format, recording null instead");
                        }
                    }
                    entityManager.persist(entity_class);
                    entityManager.getTransaction().commit();
                    entityManager.close();
                }
                entityManagerFactory.close();
                break;
        }
        scanner.close();
    }

    private static Field setClassField(Field f, Object eclass, Object val) {
        try {
            f.set(eclass, val);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return f;
    }
}
