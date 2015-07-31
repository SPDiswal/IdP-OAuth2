// hipstr MUSIC LIBRARY.

// User's personal music uploaded as resources.

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class ResourceServer
{
    private HttpServer server;
    private Map<String, Set<String>> trackTitles = new HashMap<>();

    public ResourceServer()
    {
        initialiseTrackTitles();

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

    private void initialiseTrackTitles()
    {
        Set<String> titlesOfAlice = new HashSet<>();
        titlesOfAlice.add("Major Lazer - Powerful");
        titlesOfAlice.add("Zara Larsson - Lush Life");
        titlesOfAlice.add("Ed Sheeran - Photograph");
        trackTitles.put("Alice", titlesOfAlice);

        Set<String> titlesOfBob = new HashSet<>();
        titlesOfBob.add("Metallica - Enter Sandman");
        titlesOfBob.add("System Of A Down - B.Y.O.B.");
        trackTitles.put("Bob", titlesOfBob);
    }

    private void setUpGetMusic()
    {
        // STEP 16: Retrieve access token.
        server.createContext("/music", request ->
        {
            Map<String, String> params = Utilities.getQueryParameters(request.getRequestURI().getQuery());

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

    // TODO: Get username/email endpoint in addition to /music.
}
