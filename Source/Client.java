// playlistr PLAYLIST MANAGER.

// Hosts HTML page.
// Wants to access protected resources.

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.util.Collection;
import java.util.Map;

public class Client
{
    private static final String TOKEN_ENDPOINT = "http://localhost:8080/token";
    private static final String RESOURCE_MUSIC = "http://localhost:8081/music";
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

            StringBuilder result = Utilities.getResponse(connection);

            String accessTokenData = result.toString();
            String accessToken = accessTokenData.substring(accessTokenData.indexOf(":") + 2, accessTokenData.indexOf("\"", accessTokenData.indexOf(":") + 2));

            request.getResponseHeaders().add("Location", PLAYLISTS_URI+"?access_token="+accessToken);
            request.sendResponseHeaders(302, 0);
            request.getResponseBody().close();
        });
    }

    private void setUpPlaylists()
    {
        server.createContext(
                "/playlists",
                request -> Utilities.html(
                        request,
                        "Assets/MyPlaylists.html",
                        this::findAndReplace
                ));
    }

    private String findAndReplace(String html, HttpExchange request) throws IOException {
        URL url = new URL(RESOURCE_MUSIC);

        Map<String, String> params = Utilities.getQueryParameters(request.getRequestURI().getQuery());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Bearer "+params.get("access_token"));
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        String result = Utilities.getResponse(connection).toString();
        //String result = "[\"Test 1\", \"Test 2\"]";

        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<String>>(){}.getType();
        Collection<String> tracks = gson.fromJson(result, collectionType);

        String tableBody = "";
        for (String track : tracks) {
            tableBody += "<tr><td>"+track+"</td></tr>";
        }


        return html.replaceAll("--playList--", tableBody);
    }

    public void start()
    {
        server.start();
    }
}
