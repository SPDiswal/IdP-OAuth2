// playlistr PLAYLIST MANAGER.

// Hosts HTML page.
// Wants to access protected resources.

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class Client
{
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
            System.out.println("WORLD");

            String response = "This is the response";
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
