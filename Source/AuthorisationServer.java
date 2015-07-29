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

            setUpLogin();

            server.setExecutor(null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void setUpLogin()
    {
        server.createContext("/oauth2", request ->
        {


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
