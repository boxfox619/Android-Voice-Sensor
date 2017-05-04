package com.enertalktalk.sttsapplication.voice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by boxfox on 2017-05-04.
 */

public class Recognizer {
    public enum Languages{
        AUTO_DETECT("auto"),
        ARABIC_JORDAN("ar-JO"),
        ARABIC_LEBANON("ar-LB"),
        ARABIC_QATAR("ar-QA"),
        ARABIC_UAE("ar-AE"),
        ARABIC_MOROCCO("ar-MA"),
        ARABIC_IRAQ("ar-IQ"),
        ARABIC_ALGERIA("ar-DZ"),
        ARABIC_BAHRAIN("ar-BH"),
        ARABIC_LYBIA("ar-LY"),
        ARABIC_OMAN("ar-OM"),
        ARABIC_SAUDI_ARABIA("ar-SA"),
        ARABIC_TUNISIA("ar-TN"),
        ARABIC_YEMEN("ar-YE"),
        BASQUE("eu"),
        CATALAN("ca"),
        CZECH("cs"),
        DUTCH("nl-NL"),
        ENGLISH_AUSTRALIA("en-AU"),
        ENGLISH_CANADA("en-CA"),
        ENGLISH_INDIA("en-IN"),
        ENGLISH_NEW_ZEALAND("en-NZ"),
        ENGLISH_SOUTH_AFRICA("en-ZA"),
        ENGLISH_UK("en-GB"),
        ENGLISH_US("en-US"),
        FINNISH("fi"),
        FRENCH("fr-FR"),
        GALICIAN("gl"),
        GERMAN("de-DE"),
        HEBREW("he"),
        HUNGARIAN("hu"),
        ICELANDIC("is"),
        ITALIAN("it-IT"),
        INDONESIAN("id"),
        JAPANESE("ja"),
        KOREAN("ko"),
        LATIN("la"),
        CHINESE_SIMPLIFIED("zh-CN"),
        CHINESE_TRANDITIONAL("zh-TW"),
        CHINESE_HONGKONG("zh-HK"),
        CHINESE_CANTONESE("zh-yue"),
        MALAYSIAN("ms-MY"),
        NORWEGIAN("no-NO"),
        POLISH("pl"),
        PIG_LATIN("xx-piglatin"),
        PORTUGUESE("pt-PT"),
        PORTUGUESE_BRASIL("pt-BR"),
        ROMANIAN("ro-RO"),
        RUSSIAN("ru"),
        SERBIAN("sr-SP"),
        SLOVAK("sk"),
        SPANISH_ARGENTINA("es-AR"),
        SPANISH_BOLIVIA("es-BO"),
        SPANISH_CHILE("es-CL"),
        SPANISH_COLOMBIA("es-CO"),
        SPANISH_COSTA_RICA("es-CR"),
        SPANISH_DOMINICAN_REPUBLIC("es-DO"),
        SPANISH_ECUADOR("es-EC"),
        SPANISH_EL_SALVADOR("es-SV"),
        SPANISH_GUATEMALA("es-GT"),
        SPANISH_HONDURAS("es-HN"),
        SPANISH_MEXICO("es-MX"),
        SPANISH_NICARAGUA("es-NI"),
        SPANISH_PANAMA("es-PA"),
        SPANISH_PARAGUAY("es-PY"),
        SPANISH_PERU("es-PE"),
        SPANISH_PUERTO_RICO("es-PR"),
        SPANISH_SPAIN("es-ES"),
        SPANISH_US("es-US"),
        SPANISH_URUGUAY("es-UY"),
        SPANISH_VENEZUELA("es-VE"),
        SWEDISH("sv-SE"),
        TURKISH("tr"),
        ZULU("zu");

        private final String languageCode;

        private Languages(final String languageCode){
            this.languageCode = languageCode;
        }

        public String toString(){
            return languageCode;
        }
    }
    private static final String GOOGLE_RECOGNIZER_URL = "http://www.google.com/speech-api/v2/recognize?client=chromium&output=json";
    private String apiKey;
    private String language;
    private boolean profanityFilter;


    public Recognizer(String apiKey){
        this(Languages.AUTO_DETECT, apiKey);
        this.apiKey = apiKey;
    }

    public Recognizer(Languages languages, String apiKey){
        this(languages, true, apiKey);
    }

    public Recognizer(Languages languages, boolean profanityFilter, String apiKey){
        this.profanityFilter = profanityFilter;
        this.language = languages.languageCode;
        this.apiKey = apiKey;
    }

    public GoogleResponse request(File flacFile) throws IOException {
        String [] response = rawRequest(flacFile, 1, 8000);
        GoogleResponse googleResponse = new GoogleResponse();
        try {
            parseResponse(response, googleResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googleResponse;
    }

    private void parseResponse(String[] rawResponse, GoogleResponse googleResponse) throws JSONException {
        for(String s : rawResponse) {
            JSONObject jsonResponse = new JSONObject(s);
            JSONArray jsonResultArray = jsonResponse.getJSONArray("result");
            for(int i = 0; i < jsonResultArray.length(); i++) {
                JSONObject jsonAlternativeObject = jsonResultArray.getJSONObject(i);
                JSONArray jsonAlternativeArray = jsonAlternativeObject.getJSONArray("alternative");
                double prevConfidence = 0;
                for(int j = 0; j < jsonAlternativeArray.length(); j++) {
                    JSONObject jsonTranscriptObject = jsonAlternativeArray.getJSONObject(j);
                    String transcript = jsonTranscriptObject.optString("transcript", "");
                    double confidence = jsonTranscriptObject.optDouble("confidence", 0.0);
                    if(confidence > prevConfidence) {
                        googleResponse.setResponse(transcript);
                        googleResponse.setConfidence(String.valueOf(confidence));
                        prevConfidence = confidence;
                    } else
                        googleResponse.getOtherPossibleResponses().add(transcript);
                }
            }
        }
    }

    private String[] rawRequest(File inputFile, int maxResults, int sampleRate) throws IOException{
        URL url;
        URLConnection urlConn;
        OutputStream outputStream;
        BufferedReader br;

        StringBuilder sb = new StringBuilder(GOOGLE_RECOGNIZER_URL);
        if( language != null ) {
            sb.append("&lang=");
            sb.append(language);
        }
        else{
            sb.append("&lang=auto");
        }
        if(apiKey != null) {
            sb.append("&key=");
            sb.append(apiKey);
        }

        if( !profanityFilter ) {
            sb.append("&pfilter=0");
        }
        sb.append("&maxresults=");
        sb.append(maxResults);

        url = new URL(sb.toString());
        urlConn = url.openConnection();
        urlConn.setDoOutput(true);
        urlConn.setUseCaches(false);
        urlConn.setRequestProperty("Content-Type", "audio/x-flac; rate=" + sampleRate);
        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.52 Safari/537.36");
        outputStream = urlConn.getOutputStream();

        FileInputStream fileInputStream = new FileInputStream(inputFile);
        byte[] buffer = new byte[256];
        while ((fileInputStream.read(buffer, 0, 256)) != -1) {
            outputStream.write(buffer, 0, 256);
        }
        fileInputStream.close();
        outputStream.close();
        br = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), Charset.forName("UTF-8")));
        List<String> completeResponse = new ArrayList<String>();
        String response = br.readLine();
        while(response != null) {
            completeResponse.add(response);
            response = br.readLine();
        }
        br.close();

        // System.out.println("Recognizer.rawRequest() -> completeResponse = " + completeResponse);
        return completeResponse.toArray(new String[completeResponse.size()]);
    }

}
