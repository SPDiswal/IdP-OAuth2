// IDENTITY PROVIDER.
// OneID.

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class AuthorisationServer
{
    private HttpServer server;

    public AuthorisationServer()
    {
        try
        {
            server = HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext("/test", t ->
            {
                String response = "This is the response";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });

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
}
