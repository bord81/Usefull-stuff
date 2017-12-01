package twac;

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
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import static twac.Twac.handleException;
import static twac.Twac.outputFile;

public class TwitterCall {

    private static final int GET_REQUEST_TOKENS = 0;
    private static final int GET_ACCESS_TOKENS = 1;
    private static final int AUTHORIZE_REQUEST = 2;

    private final static Map<String, String> paramsMap = new TreeMap<>();
    private final static String postOauth2ReqUrl = "https://api.twitter.com/oauth/request_token";
    private final static String postOauth2AccUrl = "https://api.twitter.com/oauth/access_token";
    private final static String oauthCallBack = "oob";

    public static boolean authorizeApp(String token, String secret) {
        return makeNetworkCall(GET_REQUEST_TOKENS, token, secret);
    }

    public static boolean getTokens(String token, String secret) {
        return makeNetworkCall(GET_ACCESS_TOKENS, token, secret);
    }

    public static boolean executeRequest(String token, String secret) {
        return makeNetworkCall(AUTHORIZE_REQUEST, token, secret);
    }

    private static boolean makeNetworkCall(int reqType, String token, String secret) {
        Mac mac = null;
        try {
            mac = Mac.getInstance(Twac.SIG_METHOD_J);
        } catch (NoSuchAlgorithmException e) {
            handleException(e);
            return false;
        }
        String signKey = null;
        try {
            if (reqType == GET_REQUEST_TOKENS) {
                signKey = URLEncoder.encode(secret, Twac.ENCODING) + "&";
            } else if (reqType == GET_ACCESS_TOKENS) {
                signKey = URLEncoder.encode(secret, Twac.ENCODING) + "&" + URLEncoder.encode(Twac.oATokenSec, Twac.ENCODING);
            } else {
                signKey = URLEncoder.encode(secret, Twac.ENCODING) + "&" + URLEncoder.encode(Twac.oRTokenSec, Twac.ENCODING);
            }

            try {
                mac.init(new SecretKeySpec(signKey.getBytes(), mac.getAlgorithm()));
            } catch (InvalidKeyException e) {
                handleException(e);
                return false;
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

            if (reqType == GET_REQUEST_TOKENS) {
                finParamString.append(Twac.HTTP_POST).append("&");
                finParamString.append(URLEncoder.encode(postOauth2ReqUrl, Twac.ENCODING));
                finParamString.append("&");
            } else if (reqType == GET_ACCESS_TOKENS) {
                finParamString.append(Twac.HTTP_POST).append("&");
                finParamString.append(URLEncoder.encode(postOauth2AccUrl, Twac.ENCODING));
                finParamString.append("&");
            } else if (reqType == AUTHORIZE_REQUEST) {
                finParamString.append(Twac.httpMethod).append("&");
                String[] urlParts = Twac.callUrl.split(Pattern.quote("?"));
                finParamString.append(URLEncoder.encode(urlParts[0], Twac.ENCODING));
                finParamString.append("&");
                if (Twac.callUrl.contains("?")) {
                    String[] q_params = urlParts[1].split(Pattern.quote("&"));
                    for (int i = 0; i < q_params.length; i++) {
                        paramsMap.put(URLEncoder.encode(q_params[i].split("=")[0], Twac.ENCODING), URLEncoder.encode(q_params[i].split("=")[1], Twac.ENCODING));
                    }
                }
            }
            paramsMap.put(URLEncoder.encode("oauth_consumer_key", Twac.ENCODING), URLEncoder.encode(token, Twac.ENCODING));
            paramsMap.put(URLEncoder.encode("oauth_nonce", Twac.ENCODING), URLEncoder.encode(nonce, Twac.ENCODING));
            paramsMap.put(URLEncoder.encode("oauth_signature_method", Twac.ENCODING), URLEncoder.encode(Twac.SIG_METHOD_TW, Twac.ENCODING));
            paramsMap.put(URLEncoder.encode("oauth_version", Twac.ENCODING), URLEncoder.encode(Twac.OAUTH_VER, Twac.ENCODING));
            if (reqType == GET_REQUEST_TOKENS) {
                paramsMap.put(URLEncoder.encode("oauth_callback", Twac.ENCODING), URLEncoder.encode(oauthCallBack, Twac.ENCODING));
            } else if (reqType == GET_ACCESS_TOKENS) {
                paramsMap.put(URLEncoder.encode("oauth_token", Twac.ENCODING), URLEncoder.encode(Twac.oAToken, Twac.ENCODING));
            } else {
                paramsMap.put(URLEncoder.encode("oauth_token", Twac.ENCODING), URLEncoder.encode(Twac.oRToken, Twac.ENCODING));
            }
            Long secondsEpoch = System.currentTimeMillis() / 1000;
            paramsMap.put(URLEncoder.encode("oauth_timestamp", Twac.ENCODING), URLEncoder.encode(secondsEpoch.toString(), Twac.ENCODING));
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                paramString.append(entry.getKey());
                paramString.append("=");
                paramString.append(entry.getValue());
                paramString.append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
            finParamString.append(URLEncoder.encode(paramString.toString(), Twac.ENCODING));
            byte[] ouath_sig_arr = Base64.getUrlEncoder().encode(mac.doFinal(finParamString.toString().getBytes()));
            paramsMap.put(URLEncoder.encode("oauth_signature"), URLEncoder.encode(new String(ouath_sig_arr), Twac.ENCODING));
        } catch (UnsupportedEncodingException ex) {
            handleException(ex);
            return false;
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
        HttpURLConnection connection = null;
        try {
            if (reqType == GET_ACCESS_TOKENS) {
                String full_url = postOauth2AccUrl + "?oauth_verifier=" + URLEncoder.encode(Twac.pin, Twac.ENCODING);
                url = new URL(full_url);
            } else if (reqType == GET_REQUEST_TOKENS) {
                url = new URL(postOauth2ReqUrl);
            } else {
                url = new URL(Twac.callUrl);
            }
            connection = (HttpURLConnection) url.openConnection();
            if (reqType != AUTHORIZE_REQUEST) {
                connection.setRequestMethod(Twac.HTTP_POST);
            } else {
                connection.setRequestMethod(Twac.httpMethod);
            }
            connection.setRequestProperty("Authorization", authHeader);
            connection.setRequestProperty("User-Agent", "YourApp");
            connection.setRequestProperty("Host", "api.twitter.com");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "*/*");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                int inputStream;
                char[] buf = new char[8096];
                StringBuilder builder = new StringBuilder();
                while ((inputStream = in.read(buf)) != -1) {
                    char[] end_buf = new char[inputStream];
                    for (int i = 0; i < end_buf.length; i++) {
                        end_buf[i] = buf[i];
                    }
                    builder.append(end_buf);
                }
                in.close();
                String respBody = builder.toString();
                if (reqType == GET_REQUEST_TOKENS) {
                    fillTokens(respBody, true);
                } else if (reqType == GET_ACCESS_TOKENS) {
                    fillTokens(respBody, false);
                } else {
                    printResponseToFile(respBody);
                }
            } else {
                System.out.println("Error response from authentication server: " + responseCode);
                return false;
            }
        } catch (MalformedURLException ex) {
            handleException(ex);
            return false;
        } catch (ProtocolException ex) {
            handleException(ex);
            return false;
        } catch (IOException ex) {
            handleException(ex);
            return false;
        }
        return true;
    }

    private static void fillTokens(String resp_body, boolean first_stage) {
        String ot = "oauth_token";
        String ots = "oauth_token_secret";
        String[] split = resp_body.split(Pattern.quote("="));
        for (int i = 0; i < split.length; i++) {
            if (split[i].contains(ot) && !split[i].contains(ots)) {
                int endIndex = split[i + 1].indexOf("&");
                if (endIndex < 0) {
                    endIndex = split[i + 1].length();
                }
                if (first_stage) {
                    Twac.oAToken = split[i + 1].substring(0, endIndex);
                } else {
                    Twac.oRToken = split[i + 1].substring(0, endIndex);
                }
            } else if (split[i].contains(ots)) {
                int endIndex = split[i + 1].indexOf("&");
                if (endIndex < 0) {
                    endIndex = split[i + 1].length();
                }
                if (first_stage) {
                    Twac.oATokenSec = split[i + 1].substring(0, endIndex);
                } else {
                    Twac.oRTokenSec = split[i + 1].substring(0, endIndex);
                }
            }
        }
    }

    private static void printResponseToFile(String respBody) throws IOException {
        File file = new File(outputFile);
        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        fw.write(respBody);
        fw.flush();
        fw.close();
    }
}
