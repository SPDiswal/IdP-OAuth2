// MUSIC LIBRARY.

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class ResourceServer
{
    private HttpServer server;

    public ResourceServer()
    {
        try
        {
            server = HttpServer.create(new InetSocketAddress(8081), 0);

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
