import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONException;
import org.json.JSONObject;

public class Twac {

    final static String HELP = "-help";
    final static String SIG_METHOD = "HMAC-SHA1";
    final static String OAUTH_VER = "1.0";
    final static Map<String, String> paramsMap = new TreeMap<>();
    final static int INDENT_FACTOR = 4;
    static String outputFile;
    static String httpMethod;
    static String oauthUrl;
    static String twConsKey;
    static String twPrivConsKey;
    static String oAccToken;
    static String oTokenSec;

    public static void main(String[] args) throws IOException {
        if (args == null || args.length < 1) {
            System.out.println("Please specify parameters or run with -help for help");
            return;
        }
        if (args[0].equals(HELP)) {
            help();
            return;
        }
        if (args[0] != null && args[1] != null && args[2] != null && args[3] != null && args[4] != null && args[5] != null && args[6] != null) {
            outputFile = args[0];
            httpMethod = args[1];
            oauthUrl = args[2];
            twConsKey = args[3];
            twPrivConsKey = args[4];
            oAccToken = args[5];
            oTokenSec = args[6];
        } else {
            System.out.println("Inconsistent parameters, run with -help for help");
            return;
        }

        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            handleException(e);
            return;
        }
        try {
            String signKey = URLEncoder.encode(twPrivConsKey, "UTF-8") + "&" + URLEncoder.encode((oTokenSec), "UTF-8");
            try {
                mac.init(new SecretKeySpec(signKey.getBytes(), mac.getAlgorithm()));
            } catch (InvalidKeyException e) {
                handleException(e);
                return;
            }
            SecureRandom random = new SecureRandom();
            byte[] nonce_array = new byte[32];
            random.nextBytes(nonce_array);
            for (int i = 0; i < nonce_array.length; i++) {
                if (nonce_array[i] < 0) {
                    nonce_array[i] = (byte) (nonce_array[i] * -1);
                }
                if (nonce_array[i] < 48) {
                    int next = random.nextInt(10);
                    nonce_array[i] = (byte) (48 + next);
                } else if (nonce_array[i] < 57 && nonce_array[i] > 48) {
                    int next = random.nextInt(26);
                    nonce_array[i] = (byte) (65 + next);
                } else if (nonce_array[i] < 97 && nonce_array[i] > 57) {
                    int next = random.nextInt(26);
                    nonce_array[i] = (byte) (97 + next);
                } else if (nonce_array[i] > 122) {
                    int next = random.nextInt(26);
                    nonce_array[i] = (byte) (122 - next);
                }
            }

            String nonce = new String(nonce_array);
            StringBuilder paramString = new StringBuilder();
            StringBuilder finParamString = new StringBuilder();
            finParamString.append(httpMethod).append("&");

            finParamString.append(URLEncoder.encode(oauthUrl, "UTF-8"));

            finParamString.append("&");
            paramsMap.put(URLEncoder.encode("oauth_consumer_key", "UTF-8"), URLEncoder.encode(twConsKey, "UTF-8"));
            paramsMap.put(URLEncoder.encode("oauth_nonce", "UTF-8"), URLEncoder.encode(nonce, "UTF-8"));
            paramsMap.put(URLEncoder.encode("oauth_signature_method", "UTF-8"), URLEncoder.encode(SIG_METHOD, "UTF-8"));
            paramsMap.put(URLEncoder.encode("oauth_token", "UTF-8"), URLEncoder.encode(oAccToken, "UTF-8"));
            paramsMap.put(URLEncoder.encode("oauth_version", "UTF-8"), URLEncoder.encode(OAUTH_VER, "UTF-8"));
            Long secondsEpoch = System.currentTimeMillis() / 1000;
            paramsMap.put(URLEncoder.encode("oauth_timestamp", "UTF-8"), URLEncoder.encode(secondsEpoch.toString(), "UTF-8"));

            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                paramString.append(entry.getKey());
                paramString.append("=");
                paramString.append(entry.getValue());
                paramString.append("&");
            }

            paramString.deleteCharAt(paramString.length() - 1);
            finParamString.append(URLEncoder.encode(paramString.toString(), "UTF-8"));
            byte[] ouath_sig_arr = Base64.getUrlEncoder().encode(mac.doFinal(finParamString.toString().getBytes()));
            paramsMap.put(URLEncoder.encode("oauth_signature"), URLEncoder.encode(new String(ouath_sig_arr), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            handleException(ex);
            return;
        }
        StringBuilder authHeaderString = new StringBuilder();
        authHeaderString.append("OAuth ");
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            authHeaderString.append(entry.getKey());
            authHeaderString.append("=\"");
            authHeaderString.append(entry.getValue());
            authHeaderString.append("\", ");
        }
        String authHeader = authHeaderString.toString().trim();
        authHeader = authHeader.substring(0, authHeader.length() - 1);

        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(oauthUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(httpMethod);
            connection.setRequestProperty("Authorization", authHeader);
            connection.setRequestProperty("User-Agent", "YourApp");
            connection.setRequestProperty("Host", "api.twitter.com");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "*/*");
        } catch (MalformedURLException ex) {
            handleException(ex);
            return;
        } catch (ProtocolException ex) {
            handleException(ex);
            return;
        }
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            int inputStream;
            char[] buf = new char[8096];
            File file = new File(outputFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file);
            StringBuilder builder = new StringBuilder();
            while ((inputStream = in.read(buf)) != -1) {
                char[] end_buf = new char[inputStream];
                for (int i = 0; i < end_buf.length; i++) {
                    end_buf[i] = buf[i];
                }
                builder.append(end_buf);
            }
            boolean trim = false;
            if (builder.charAt(0) == '[' && builder.charAt(builder.length() - 1) == ']') {
                builder.deleteCharAt(0);
                builder.deleteCharAt(builder.length() - 1);
                trim = true;
            }
            String finString = builder.toString();
            try {
                JSONObject nObject = new JSONObject(finString);
                String jFinString = nObject.toString(INDENT_FACTOR);
                if (trim) {
                    builder = new StringBuilder(jFinString);
                    builder.insert(0, '[');
                    builder.append(']');
                    jFinString = builder.toString();
                }
                fw.write(jFinString);
            } catch (JSONException e) {
                fw.write(finString);
            }
            fw.flush();
            in.close();
            fw.close();
        } else {
            System.out.println("***");
            System.out.println("Respone NOT OK: " + responseCode + " " + connection.getResponseMessage());
            Map<String, List<String>> m = connection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : m.entrySet()) {
                System.out.print(entry.getKey() + ": ");
                for (String s : entry.getValue()) {
                    System.out.println(s);
                }
            }
            connection.disconnect();
            return;
        }
        connection.disconnect();
        successMessage();
    }

    public static void successMessage() {
        System.out.println("Operation success!");
    }

    public static void help() {
        System.out.println("***\n");
        System.out.println("twac is used to get the response from one of Twitter API endpoints using provided consumer key and access token.");
        System.out.println("twac generates proper HTTP request by using secret key and token for HMAC-SHA1 signature.");
        System.out.println("Results are saved to the provided filepath.");
        System.out.println("Syntax is: java -jar twac.jar <Output file> <HTTP method> <HTTP endpoint> <Consumer key> <Consumer secret key> <OAuth access token> <OAuth token secret>");
        System.out.println("<Output file> - path to your local file.");
        System.out.println("<HTTP method> - usually POST or GET.");
        System.out.println("<HTTP endpoint> - Twitter resource URL.");
        System.out.println("<Consumer key> and <Consumer secret key> - your app credentials.");
        System.out.println("<OAuth access token> and <OAuth token secret> - test tokens for your app.\n");
        System.out.println("***");
    }

    public static void handleException(Exception e) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("\n***");
            System.out.println("Program terminated with exception, do you want to see error message/stack trace?(y/n): ");
            String answer = reader.readLine();
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
