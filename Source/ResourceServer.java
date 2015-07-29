// MUSIC LIBRARY.

// User's personal music uploaded as resources.

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class ResourceServer
{
    private HttpServer server;
    private Map<String, List<String>> trackTitles = new HashMap<>();

    public ResourceServer()
    {
        InitialiseTrackTitles();

        try
        {
            server = HttpServer.create(new InetSocketAddress(8081), 0);
            setUpGetMusic();
            server.setExecutor(null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void start()
    {
        server.start();
    }

    private void InitialiseTrackTitles()
    {
        List<String> titlesOfAlice = new ArrayList<>();
        titlesOfAlice.add("Major Lazer - Powerful");
        titlesOfAlice.add("Zara Larsson - Lush Life");
        titlesOfAlice.add("Ed Sheeran - Photograph");
        trackTitles.put("Alice", titlesOfAlice);

        List<String> titlesOfBob = new ArrayList<>();
        titlesOfBob.add("Metallica - Enter Sandman");
        titlesOfBob.add("System Of A Down - B.Y.O.B.");
        trackTitles.put("Bob", titlesOfBob);
    }

    private void setUpGetMusic()
    {
        // STEP 16: Retrieve access token.
        server.createContext("/music", request ->
        {
            Map<String, String> params = getQueryParameters(request.getRequestURI().getQuery());

            if (params.containsKey("accessToken"))
            {
                // STEP 17: Coordination
                // TODO: Contact OneID authorisation server and validate access token.
                //       If ok, return 200 OK with JSON array of track titles (STEP 18: Execute API call + STEP 19: Return info).
                //       Otherwise, return 401 Unauthorized.
            }
            else
            {
                // TODO: No access token found. Respond with 401 Unauthorized.
            }

            String response = "This is the response";
            request.sendResponseHeaders(200, response.length());
            OutputStream os = request.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
    }

    // Borrowed from: http://www.rgagnon.com/javadetails/java-get-url-parameters-using-jdk-http-server.html
    private Map<String, String> getQueryParameters(String query)
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
