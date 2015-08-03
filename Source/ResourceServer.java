// hipstr MUSIC LIBRARY.

// User's personal music uploaded as resources.

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;

public class ResourceServer
{
    private HttpServer server;
    private Map<String, Set<String>> trackTitles = new HashMap<>();
    private static final String TOKEN_VALIDATION = "http://localhost:8080/validate";

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
            if (request.getRequestHeaders().containsKey("Authorization"))
            {
                String accessToken = request.getRequestHeaders().getFirst("Authorization").substring(7);

                URL url = new URL(TOKEN_VALIDATION);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                addAccessTokenAsPostParam(accessToken, connection);

                String line;
                StringBuilder result = new StringBuilder();

                // STEP 17: Coordination
                // TODO: Contact OneID authorisation server and validate access token.
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
                {
                    while ((line = reader.readLine()) != null)
                        result.append(line);
                }

                // (STEP 18: Execute API call + STEP 19: Return info).
                String usernameData = result.toString();
                String username = usernameData.substring(usernameData.indexOf(":") + 2, usernameData.indexOf("\"", usernameData.indexOf(":") + 2));

                if (connection.getResponseCode() == 200)
                {
                    // TODO: get user from response
                    Set<String> tracks = trackTitles.get(username);
                    String list = String.join("\", \"", tracks);
                    String response = "[ \"" + list + "\" ]";

                    request.sendResponseHeaders(200, response.length());
                    OutputStream os = request.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
                else
                {
                    //TODO: invalid access token. Respond with 401 Unauthorized.
                    request.sendResponseHeaders(401, 0);
                    request.getResponseBody().close();
                }
            }
            else
            {
                // TODO: No access token found. Respond with 401 Unauthorized.
                request.sendResponseHeaders(401, 0);
                request.getResponseBody().close();
            }
        });
    }

    private void addAccessTokenAsPostParam(String accessToken, HttpURLConnection connection) throws IOException
    {
        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        String postParameters = "accessToken=" + accessToken;
        os.write(postParameters.getBytes());
        os.flush();
        os.close();
    }

    // TODO: Get username/email endpoint in addition to /music.
}
