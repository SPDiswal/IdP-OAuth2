// playlistr PLAYLIST MANAGER.

// Hosts HTML page.
// Wants to access protected resources.

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.util.Map;

public class Client
{
    private static final String TOKEN_ENDPOINT = "http://localhost:8080/token";

    private static final String PLAYLISTS_URI = "http://localhost:8082/playlists";
    private static final String PLAYLISTR_SECRET = "playlistrSecret";

    private HttpServer server;

    public Client()
    {
        try
        {
            server = HttpServer.create(new InetSocketAddress(8082), 0);

            setUpAuthentication();
            setUpRedirectionEndpoint();
            setUpPlaylists();

            server.setExecutor(null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void setUpAuthentication() throws IOException
    {
        server.createContext("/", request -> Utilities.html(request, "Assets/Playlistr.html"));
    }

    private void setUpRedirectionEndpoint() throws IOException
    {
        // Redirection endpoint.
        server.createContext("/authenticated", request ->
        {
            Map<String, String> params = Utilities.getQueryParameters(request.getRequestURI().getQuery());

            URL url = new URL(TOKEN_ENDPOINT + "?code=" + params.get("code") + "&client_id=" + PLAYLISTR_SECRET);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            String line;
            StringBuilder result = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
            {
                while ((line = reader.readLine()) != null)
                    result.append(line);
            }

            String accessTokenData = result.toString();
            String accessToken = accessTokenData.substring(accessTokenData.indexOf(":") + 2, accessTokenData.indexOf("\"", accessTokenData.indexOf(":") + 2));

            String redirectUri = PLAYLISTS_URI + "?accessToken=" + accessToken;
            request.getResponseHeaders().add("Location", redirectUri);
            request.sendResponseHeaders(302, 0);
            request.getResponseBody().close();
        });
    }

    private void setUpPlaylists() throws IOException
    {
        server.createContext("/playlists", request -> Utilities.html(request, "Assets/MyPlaylists.html"));
    }

    public void start()
    {
        server.start();
    }
}
