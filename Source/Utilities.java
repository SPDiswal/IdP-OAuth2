import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.util.*;

public class Utilities
{
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
        File file = new File(filename);
        request.sendResponseHeaders(200, file.length());

        try (FileInputStream fileInputStream = new FileInputStream(file);
             OutputStream os = request.getResponseBody())
        {
            byte[] buffer = new byte[(int) file.length()];
            int count;

            while ((count = fileInputStream.read(buffer)) >= 0)
                os.write(buffer, 0, count);
        }
    }
}
