package twac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class Twac {

    final static String HELP_S = "-h";
    final static String HELP_L = "--help";
    final static String AUTH_S = "-a";
    final static String AUTH_L = "--authorize";
    final static String REQ_S = "-r";
    final static String REQ_L = "--request";
    final static String SIG_METHOD_J = "HmacSHA1";
    final static String SIG_METHOD_TW = "HMAC-SHA1";
    final static String OAUTH_VER = "1.0";
    final static String ENCODING = "UTF-8";
    final static String HTTP_POST = "POST";
    private final static String keys_file = "twac_data.aut";
    final static Map<String, String> paramsMap = new TreeMap<>();
    static String outputFile;
    static String httpMethod;
    static String callUrl;
    private static String twConsKey;
    private static String twPrivConsKey;
    static String oAToken;
    static String oATokenSec;
    static String oRToken;
    static String oRTokenSec;
    static String pin;

    public static void main(String[] args) {

        if (args == null || args.length < 1) {
            System.out.println("Please specify parameters or run with --help");
            return;
        }

        switch (args[0]) {
            case HELP_S:
            case HELP_L:
                help();
                break;
            case AUTH_S:
            case AUTH_L:
                if (args[1] != null && args[2] != null) {
                    twConsKey = args[1];
                    twPrivConsKey = args[2];
                    doAuthorization();
                } else {
                    inconsistentParams();
                }
                break;
            case REQ_S:
            case REQ_L:
                if (args[1] != null && args[2] != null && args[3] != null) {
                    outputFile = args[1];
                    httpMethod = args[2];
                    callUrl = args[3];
                    doApiRequest();
                } else {
                    inconsistentParams();
                }
                break;
        }
    }

    private static void doAuthorization() {
        if (TwitterCall.authorizeApp(twConsKey, twPrivConsKey)) {
            askForPin();
            if (TwitterCall.getTokens(twConsKey, twPrivConsKey)) {
                saveTokensToLocalFile();
                loginSuccessMessage();
            }
        }
    }

    private static void doApiRequest() {
        try {
            if (loadTokensFromLocalFile()) {
                if (TwitterCall.executeRequest(twConsKey, twPrivConsKey)) {
                    requestSuccess();
                }
            }
        } catch (IOException ex) {
            handleException(ex);
            return;
        }
    }

    private static void requestSuccess() {
        System.out.println("Request success!");
        System.out.println("The response was written to " + outputFile);
    }

    private static void loginSuccessMessage() {
        System.out.println("Authorization success!");
        System.out.println("Run twac -r with parameters to execute API calls.");
        System.out.println("If you wish to obtain new tokens or use another account, please use -a again.");
    }

    private static void inconsistentParams() {
        System.out.println("Inconsistent parameters, run with -h or --help to get more information");
    }

    private static void authFileMissing() {
        System.out.println("Authorization tokens missing, please run with -a first;");
    }

    private static void askForPin() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please copy and paste the following URL to the browser and after obtaining PIN code, type it here.\n");
        System.out.println("https://api.twitter.com/oauth/authorize?oauth_token=" + oAToken);
        System.out.println("PIN code from Twitter: ");
        pin = sc.nextLine();
    }

    private static boolean loadTokensFromLocalFile() throws FileNotFoundException, IOException {
        File file = new File(System.getProperty("user.dir") + File.separator + keys_file);
        if (!file.exists()) {
            authFileMissing();
            return false;
        }
        byte[] input;
        try (FileInputStream fis = new FileInputStream(file)) {
            input = new byte[fis.available()];
            fis.read(input);
        }
        String s = new String(input);
        String[] tokens = s.split(Pattern.quote("\n"));
        twConsKey = tokens[0];
        twPrivConsKey = tokens[1];
        oRToken = tokens[2];
        oRTokenSec = tokens[3];
        return true;
    }

    private static void saveTokensToLocalFile() {
        File file = new File(System.getProperty("user.dir") + File.separator + keys_file);
        try {
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
        } catch (IOException ex) {
            handleException(ex);
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(twConsKey.getBytes());
            fos.write("\n".getBytes());
            fos.write(twPrivConsKey.getBytes());
            fos.write("\n".getBytes());
            fos.write(oRToken.getBytes());
            fos.write("\n".getBytes());
            fos.write(oRTokenSec.getBytes());
            fos.flush();
        } catch (IOException e) {
            handleException(e);
        }
    }

    public static void help() {
        System.out.println("***\n");
        System.out.println("twac is used to get the response from one of Twitter API endpoints.");
        System.out.println("Options to run it are:");
        System.out.println("java -jar twac.jar <-a, --authorize> <consumer key> <consumer secret key>");
        System.out.println("java -jar twac.jar <-r, --request> <output file> <HTTP method> <HTTP endpoint with call parameters>");
        System.out.println("The program will output unformatted JSON file at your <output file> path overwriting it if it exists.\n");
        System.out.println("Run -a prior to -r, so that the program gets authentication to use API.");
        System.out.println("Warning: -a stores the API keys by overwriting twac_data.aut in the same directory where twac.jar is located.");
        System.out.println("***");
    }

    public static void handleException(Exception e) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n***");
            System.out.println("Program terminated with exception, do you want to see error message/stack trace?(y/n): ");
            String answer = sc.nextLine();
            if (answer.toLowerCase().equals("y")) {
                System.out.println(e.getMessage());
                StackTraceElement[] stes = e.getStackTrace();
                if (stes != null && stes.length > 0) {
                    for (StackTraceElement ste : stes) {
                        System.out.println(ste.toString());
                    }
                }
                break;
            } else if (answer.toLowerCase().equals("n")) {
                break;
            }
        }
    }
}
