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
}
