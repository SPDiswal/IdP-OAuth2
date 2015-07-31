// playlistr PLAYLIST MANAGER.

// Hosts HTML page.
// Wants to access protected resources.

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.util.Map;

public class Client
{
    private static final String GET_ACCESS_TOKEN = "http://localhost:8080/token";
    private static final String PLAYLISTR_SECRET = "playlistrSecret";

    private HttpServer server;

    public Client()
    {
        try
        {
            server = HttpServer.create(new InetSocketAddress(8082), 0);

            setUpAuthentication();
            setUpAuthenticated();

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

    private void setUpAuthenticated() throws IOException
    {
        server.createContext("/authenticated", request ->
        {
            Map<String, String> params = Utilities.getQueryParameters(request.getRequestURI().getQuery());

            URL url = new URL(GET_ACCESS_TOKEN + "?authorisationCode=" + params.get("authorisationCode") + "&client_id=" + PLAYLISTR_SECRET);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            String line;
            StringBuilder result = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
            {
                while ((line = reader.readLine()) != null)
                    result.append(line);
            }

            String response = result.toString();
            request.sendResponseHeaders(200, response.length());
            OutputStream os = request.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
    }

    public void start()
    {
        server.start();
    }
}
