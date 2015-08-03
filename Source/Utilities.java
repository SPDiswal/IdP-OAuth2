import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.util.*;

public class Utilities
{
    private static SecureRandom random = new SecureRandom();

    // Borrowed from: http://www.rgagnon.com/javadetails/java-get-url-parameters-using-jdk-http-server.html
    public static Map<String, String> getQueryParameters(String query)
    {
        Map<String, String> result = new HashMap<>();

        for (String param : query.split("&"))
        {
            String pair[] = param.split("=");
            if (pair.length > 1) result.put(pair[0], pair[1]);
            else result.put(pair[0], "");
        }

        return result;
    }

    public static void html(HttpExchange request, String filename) throws IOException
    {
        html(request, filename, (html, r) -> html);
    }

    public static void html(HttpExchange request, String filename, HtmlModifier modifier) throws IOException
    {
        String fileData = readFile(filename);

        String html = modifier.modify(fileData, request);
        int htmlLength = html.length();

        try (OutputStream responseBody = request.getResponseBody())
        {
            request.sendResponseHeaders(200, htmlLength);
            responseBody.write(html.getBytes());
        }
    }

    public static String randomString()
    {
        return new BigInteger(130, random).toString(32);
    }

    private static String readFile(String file) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        }
    }

    public interface HtmlModifier
    {
        String modify(String html, HttpExchange request) throws IOException;
    }

    public static StringBuilder getResponse(HttpURLConnection request) throws IOException {
        String line;
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream())))
        {
            while ((line = reader.readLine()) != null)
                result.append(line);
        }
        return result;
    }
}
